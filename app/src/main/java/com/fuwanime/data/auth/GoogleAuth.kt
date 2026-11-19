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
import java.util.UUID
import android.util.Base64

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

    private val prefs = context.getSharedPreferences("google_oauth", Context.MODE_PRIVATE)

    fun isRedirect(uri: Uri?): Boolean =
        uri != null && uri.scheme == REDIRECT_SCHEME && uri.host == REDIRECT_HOST

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
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
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
                        .add("redirect_uri", REDIRECT_URI)
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
        const val REDIRECT_URI = "$REDIRECT_SCHEME://$REDIRECT_HOST"
        private const val AUTHORIZE_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth"
        private const val TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token"
        private const val USERINFO_ENDPOINT = "https://openidconnect.googleapis.com/v1/userinfo"
        private const val KEY_VERIFIER = "pkce_verifier"
        private const val KEY_STATE = "oauth_state"
    }
}
