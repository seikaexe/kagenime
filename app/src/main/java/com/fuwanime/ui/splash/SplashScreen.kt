package com.fuwanime.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.fuwanime.R
import com.fuwanime.ui.theme.KageColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val InkDeep = Color(0xFF111216)
private val Sky = Color(0xFF8FD3F2)
private val Lavender = Color(0xFFD4BBFF)

/** Replaces the old age-confirmation page: a short branded flash screen, then the app opens. */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val logo = remember { Animatable(0f) }
    val title = remember { Animatable(0f) }
    val halo = remember { Animatable(0f) }
    val progress = remember { Animatable(0f) }
    val exit = remember { Animatable(1f) }
    val done by rememberUpdatedState(onFinished)

    LaunchedEffect(Unit) {
        launch { logo.animateTo(1f, tween(650, easing = FastOutSlowInEasing)) }
        launch { title.animateTo(1f, tween(500, delayMillis = 160, easing = FastOutSlowInEasing)) }
        launch { halo.animateTo(1f, tween(1100, easing = LinearOutSlowInEasing)) }
        progress.animateTo(1f, tween(1000, delayMillis = 140, easing = FastOutSlowInEasing))
        delay(140)
        exit.animateTo(0f, tween(260, easing = FastOutSlowInEasing))
        done()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1B2030), KageColors.Background, InkDeep),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Soft blue aura behind the mark.
        Box(
            Modifier
                .size(320.dp)
                .alpha(halo.value)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            KageColors.Blue.copy(alpha = 0.20f),
                            Lavender.copy(alpha = 0.08f),
                            Color.Transparent,
                        ),
                    ),
                    CircleShape,
                ),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(exit.value)
                .scale(0.98f + 0.02f * exit.value),
        ) {
            Box(Modifier.size(204.dp), contentAlignment = Alignment.Center) {
                // Faint orbital rings keep the mark from looking pasted on.
                Box(Modifier.size(204.dp).alpha(halo.value * 0.7f).border(1.dp, Color.White.copy(alpha = 0.07f), CircleShape))
                Box(Modifier.size(158.dp).alpha(halo.value * 0.7f).border(1.dp, KageColors.Blue.copy(alpha = 0.14f), CircleShape))
                Box(
                    Modifier
                        .size(116.dp)
                        .scale(0.84f + 0.16f * logo.value)
                        .alpha(logo.value)
                        .clip(RoundedCornerShape(34.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF2B2C35), Color(0xFF1A1B21))))
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                listOf(Color.White.copy(alpha = 0.24f), KageColors.Blue.copy(alpha = 0.20f)),
                            ),
                            shape = RoundedCornerShape(34.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        Modifier
                            .size(88.dp)
                            .background(
                                Brush.radialGradient(
                                    listOf(Lavender.copy(alpha = 0.20f), Color.Transparent),
                                ),
                                CircleShape,
                            ),
                    )
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.size(96.dp),
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            val style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 38.sp,
                letterSpacing = (-0.035).em,
            )
            Row(
                modifier = Modifier
                    .alpha(title.value)
                    .offset(y = (12f * (1f - title.value)).dp),
            ) {
                Text("Kage", style = style, color = Color.White)
                Text("nime", style = style, color = KageColors.Blue)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.splash_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = KageColors.SubText,
                letterSpacing = 0.02.em,
                modifier = Modifier
                    .alpha(title.value * 0.9f)
                    .offset(y = (8f * (1f - title.value)).dp),
            )

            Spacer(Modifier.height(34.dp))
            Canvas(
                Modifier
                    .width(156.dp)
                    .height(4.dp)
                    .progressSemantics(progress.value),
            ) {
                val corner = CornerRadius(size.height / 2f)
                drawRoundRect(color = Color.White.copy(alpha = 0.08f), cornerRadius = corner)
                val filled = size.width * progress.value
                if (filled > 0f) {
                    drawRoundRect(
                        brush = Brush.horizontalGradient(listOf(KageColors.Blue, Sky)),
                        size = Size(filled, size.height),
                        cornerRadius = corner,
                    )
                }
            }
        }
    }
}
