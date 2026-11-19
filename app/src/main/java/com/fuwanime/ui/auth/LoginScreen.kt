package com.fuwanime.ui.auth

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fuwanime.R
import com.fuwanime.data.auth.GoogleAuth
import com.fuwanime.ui.legal.LegalText
import com.fuwanime.ui.legal.legalResources
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Second step of onboarding: the age gate has been accepted, now the user signs in with Google.
 * A guest path is offered when no OAuth client is configured, so a source build never becomes a dead end.
 */
@Composable
fun LoginScreen(auth: GoogleAuth, onSignedIn: () -> Unit, onGuest: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var working by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var showPrivacy by remember { mutableStateOf(false) }
    var browserFallback by remember { mutableStateOf(false) }

    val redirect by auth.redirects.collectAsStateWithLifecycle()
    LaunchedEffect(redirect) {
        val uri = redirect ?: return@LaunchedEffect
        auth.consumeRedirect()
        when (val result = auth.complete(uri)) {
            is GoogleAuth.Result.SignedIn -> {
                working = false
                onSignedIn()
            }
            GoogleAuth.Result.Cancelled -> {
                working = false
                message = context.getString(R.string.login_cancelled)
            }
            is GoogleAuth.Result.Failed -> {
                working = false
                message = context.getString(R.string.login_failed)
            }
        }
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "K",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            Spacer(Modifier.height(22.dp))
            Text(
                stringResource(R.string.login_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        stringResource(R.string.mine_age_badge),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.login_age_note),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(30.dp))

            Button(
                onClick = {
                    message = null
                    browserFallback = false
                    if (!auth.isConfigured) {
                        message = context.getString(R.string.login_guest_hint)
                    } else {
                        working = true
                        scope.launch {
                            when (val result = auth.signInWithAccountPicker(context)) {
                                is GoogleAuth.Result.SignedIn -> {
                                    working = false
                                    onSignedIn()
                                }
                                GoogleAuth.Result.Cancelled -> {
                                    working = false
                                    message = context.getString(R.string.login_cancelled)
                                }
                                is GoogleAuth.Result.Failed -> {
                                    working = false
                                    message = context.getString(R.string.login_failed)
                                    browserFallback = true
                                }
                            }
                        }
                    }
                },
                enabled = !working,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1F1F1F),
                    disabledContainerColor = Color.White.copy(alpha = 0.6f),
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                if (working) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(stringResource(R.string.login_signing_in), fontWeight = FontWeight.Bold)
                } else {
                    Text("G", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4285F4))
                    Spacer(Modifier.width(10.dp))
                    Text(stringResource(R.string.login_google), fontWeight = FontWeight.Bold)
                }
            }

            message?.let {
                Spacer(Modifier.height(12.dp))
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }

            if (browserFallback && !working) {
                TextButton(onClick = {
                    message = null
                    val intent = auth.authorizationIntent()
                    if (intent == null) {
                        message = context.getString(R.string.login_guest_hint)
                    } else {
                        context.startActivity(intent)
                    }
                }) {
                    Text(stringResource(R.string.login_browser))
                }
            }

            if (!auth.isConfigured) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            stringResource(R.string.login_config_title),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            stringResource(R.string.login_config_body),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Google login is required once it is configured. The guest path only exists so a
            // source build without a client ID (and therefore without a working login) is usable.
            if (!auth.isConfigured) {
                Spacer(Modifier.height(18.dp))
                TextButton(onClick = onGuest) {
                    Text(stringResource(R.string.login_guest))
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Lock,
                    null,
                    Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    stringResource(R.string.login_terms),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            TextButton(onClick = { showPrivacy = true }) {
                Text(stringResource(R.string.login_privacy))
            }
        }
    }

    if (showPrivacy) {
        val res = legalResources()
        AlertDialog(
            onDismissRequest = { showPrivacy = false },
            confirmButton = {
                TextButton(onClick = { showPrivacy = false }) { Text(stringResource(R.string.mine_close)) }
            },
            title = { Text(stringResource(R.string.mine_privacy_policy)) },
            text = {
                LazyColumn(contentPadding = PaddingValues(vertical = 4.dp)) {
                    items(LegalText.privacy(res).split("\n\n")) { paragraph ->
                        Text(
                            paragraph,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                }
            },
        )
    }
}
