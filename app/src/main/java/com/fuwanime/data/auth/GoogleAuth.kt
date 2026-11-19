package com.fuwanime.data.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.fuwanime.BuildConfig
import com.fuwanime.data.local.AccountProfile
import com.fuwanime.data.local.SettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import android.util.Base64
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/** Google OAuth 2.0 with PKCE; no client secret and no long-lived tokens are stored. */
class GoogleAuth(
    private val context: Context,
    private val client: OkHttpClient,
    private val settingsStore: SettingsStore,
) {
    /** Redirect URIs arriving from the browser. MainActivity forwards every matching intent here. */
    private val _redirects = MutableStateFlow<Uri?>(null)
    val redirects: StateFlow<Uri?> = _redirects.asStateFlow()

    fun consumeRedirect() {
        _redirects.value = null
    }

    val clientId: String = BuildConfig.GOOGLE_OAUTH_CLIENT_ID.trim()
    val isConfigured: Boolean get() = clientId.isNotBlank()

    /**
     * Google only accepts an https redirect or the reversed client ID for installed apps.
     * `kagenime://oauth2redirect` is rejected with HTTP 400 invalid_request.
     */
    private val redirectScheme: String =
        if (clientId.endsWith(CLIENT_SUFFIX)) {
            "com.googleusercontent.apps.${clientId.removeSuffix(CLIENT_SUFFIX)}"
        } else {
            REDIRECT_SCHEME
        }
    private val redirectUri: String =
        if (redirectScheme == REDIRECT_SCHEME) "$REDIRECT_SCHEME://$REDIRECT_HOST" else "$redirectScheme:/oauth2redirect"

    private val prefs = context.getSharedPreferences("google_oauth", Context.MODE_PRIVATE)

    fun isRedirect(uri: Uri?): Boolean =
        uri != null && uri.scheme == redirectScheme &&
            (uri.host == REDIRECT_HOST || uri.path == "/oauth2redirect")

    fun deliverRedirect(uri: Uri?) {
        if (isRedirect(uri)) _redirects.value = uri
    }

    /**
     * Builds the consent screen intent. The verifier and state stay in private storage so the
     * flow still completes after Android kills the activity while the browser is open.
     */
    fun authorizationIntent(): Intent? {
        if (!isConfigured) return null
        val verifier = randomToken(64)
        val state = randomToken(24)
        val challenge = base64Url(MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray()))
        prefs.edit().putString(KEY_VERIFIER, verifier).putString(KEY_STATE, state).apply()

        val url = Uri.parse(AUTHORIZE_ENDPOINT).buildUpon()
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("redirect_uri", redirectUri)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("scope", "openid email profile")
            .appendQueryParameter("code_challenge", challenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("state", state)
            .appendQueryParameter("prompt", "select_account")
            .build()
        return Intent(Intent.ACTION_VIEW, url).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    sealed interface Result {
        data class SignedIn(val profile: AccountProfile) : Result
        data object Cancelled : Result
        data class Failed(val message: String) : Result
    }

    /**
     * Native Google account picker (Credential Manager), shown inside Kagenime instead of a
     * browser. The Cloud project needs a Web OAuth client (passed as server client ID) plus an
     * Android OAuth client for package `com.kagenime.app` and the release certificate.
     */
    suspend fun signInWithAccountPicker(activity: Context): Result {
        if (!isConfigured) return Result.Failed("not_configured")
        return try {
            val manager = CredentialManager.create(activity)
            val option = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(false)
                .build()
            val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
            val credential = manager.getCredential(activity, request).credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val google = GoogleIdTokenCredential.createFrom(credential.data)
                val profile = AccountProfile(
                    name = google.displayName?.takeIf { it.isNotBlank() } ?: google.id.substringBefore('@'),
                    email = google.id,
                    photoUrl = google.profilePictureUri?.toString().orEmpty(),
                )
                settingsStore.saveGoogleAccount(profile.name, profile.email, profile.photoUrl)
                Result.SignedIn(profile)
            } else {
                Result.Failed("unexpected_credential")
            }
        } catch (e: GetCredentialCancellationException) {
            Result.Cancelled
        } catch (e: Exception) {
            Result.Failed(e.message ?: e.javaClass.simpleName)
        }
    }

    /** Exchanges the authorization code and stores the Google profile locally. */
    suspend fun complete(uri: Uri): Result = withContext(Dispatchers.IO) {
        val error = uri.getQueryParameter("error")
        if (error != null) return@withContext if (error == "access_denied") Result.Cancelled else Result.Failed(error)
        val code = uri.getQueryParameter("code") ?: return@withContext Result.Failed("missing_code")
        val returnedState = uri.getQueryParameter("state")
        val expectedState = prefs.getString(KEY_STATE, null)
        val verifier = prefs.getString(KEY_VERIFIER, null)
        if (returnedState.isNullOrBlank() || expectedState == null || returnedState != expectedState || verifier.isNullOrBlank()) {
            return@withContext Result.Failed("state_mismatch")
        }
        prefs.edit().remove(KEY_STATE).remove(KEY_VERIFIER).apply()

        runCatching {
            val tokenRequest = Request.Builder()
                .url(TOKEN_ENDPOINT)
                .post(
                    FormBody.Builder()
                        .add("client_id", clientId)
                        .add("code", code)
                        .add("code_verifier", verifier)
                        .add("grant_type", "authorization_code")
                        .add("redirect_uri", redirectUri)
                        .build(),
                )
                .build()
            client.newCall(tokenRequest).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) error("token_http_${response.code}")
                val accessToken = JSONObject(body).optString("access_token")
                if (accessToken.isBlank()) error("missing_access_token")
                accessToken
            }
        }.mapCatching { accessToken ->
            val userRequest = Request.Builder()
                .url(USERINFO_ENDPOINT)
                .header("Authorization", "Bearer $accessToken")
                .build()
            client.newCall(userRequest).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) error("userinfo_http_${response.code}")
                val json = JSONObject(body)
                AccountProfile(
                    name = json.optString("name").ifBlank { json.optString("email").substringBefore('@') },
                    email = json.optString("email"),
                    photoUrl = json.optString("picture"),
                )
            }
        }.fold(
            onSuccess = { profile ->
                settingsStore.saveGoogleAccount(profile.name, profile.email, profile.photoUrl)
                Result.SignedIn(profile)
            },
            onFailure = { e -> Result.Failed(e.message ?: "oauth_failed") },
        )
    }

    private fun randomToken(bytes: Int): String {
        val data = ByteArray(bytes)
        SecureRandom().nextBytes(data)
        return base64Url(data)
    }

    private fun base64Url(data: ByteArray): String =
        Base64.encodeToString(data, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)

    companion object {
        const val REDIRECT_SCHEME = "kagenime"
        const val REDIRECT_HOST = "oauth2redirect"
        private const val CLIENT_SUFFIX = ".apps.googleusercontent.com"
        private const val AUTHORIZE_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth"
        private const val TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token"
        private const val USERINFO_ENDPOINT = "https://openidconnect.googleapis.com/v1/userinfo"
        private const val KEY_VERIFIER = "pkce_verifier"
        private const val KEY_STATE = "oauth_state"
    }
}
