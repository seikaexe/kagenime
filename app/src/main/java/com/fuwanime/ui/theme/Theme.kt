package com.fuwanime.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.fuwanime.R
import com.fuwanime.data.local.AppSettings
import com.fuwanime.data.local.ThemeMode
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme

/** Kagenime accent blue (toggles, badges, selected dots). */
internal val BlueSeed = Color(0xFF2196F3)

/** Colours sampled from the Kagenime design: near-black grey canvas, slightly lighter cards, blue accent. */
internal object KageColors {
    val Background = Color(0xFF161719)
    val NavBar = Color(0xFF17181A)
    val Card = Color(0xFF222327)
    val CardHigh = Color(0xFF2D2E33)
    val Chip = Color(0xFF27282C)
    val Outline = Color(0xFF45464A)
    val Blue = Color(0xFF2196F3)
    val BlueDeep = Color(0xFF24466B)
    val BlueBorder = Color(0xFF204770)
    val Yellow = Color(0xFFFFB300)
    val Red = Color(0xFFF44236)
    val NavPill = Color(0xFF494458)
    val SubText = Color(0xFF9A9AA0)
}

internal val DarkScheme = dynamicColorScheme(seedColor = BlueSeed, isDark = true, style = PaletteStyle.TonalSpot).toKage()
internal val LightScheme = dynamicColorScheme(seedColor = BlueSeed, isDark = false, style = PaletteStyle.TonalSpot)

/** Neutral charcoal surfaces layered under the generated blue roles, matching the Kagenime design 1:1. */
private fun ColorScheme.toKage() = copy(
    primary = KageColors.Blue,
    onPrimary = Color.White,
    primaryContainer = KageColors.BlueDeep,
    onPrimaryContainer = Color.White,
    secondaryContainer = KageColors.NavPill,
    onSecondaryContainer = Color.White,
    background = KageColors.Background,
    surface = KageColors.Background,
    surfaceDim = KageColors.Background,
    surfaceBright = KageColors.CardHigh,
    surfaceContainerLowest = KageColors.Background,
    surfaceContainerLow = KageColors.NavBar,
    surfaceContainer = KageColors.Card,
    surfaceContainerHigh = KageColors.CardHigh,
    surfaceContainerHighest = Color(0xFF35363B),
    surfaceVariant = KageColors.Card,
    onSurfaceVariant = KageColors.SubText,
    outline = KageColors.Outline,
    outlineVariant = KageColors.Outline,
    error = KageColors.Red,
)

private fun ColorScheme.toAmoled() = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceDim = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF0A0A0B),
    surfaceContainer = Color(0xFF141416),
    surfaceContainerHigh = Color(0xFF1C1D20),
    surfaceContainerHighest = Color(0xFF26272B),
)

val PlusJakarta = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold),
)

private val AppTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = PlusJakarta),
        displayMedium = displayMedium.copy(fontFamily = PlusJakarta),
        displaySmall = displaySmall.copy(fontFamily = PlusJakarta, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.03).em),
        headlineLarge = headlineLarge.copy(fontFamily = PlusJakarta, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.02).em),
        headlineMedium = headlineMedium.copy(fontFamily = PlusJakarta, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.02).em),
        headlineSmall = headlineSmall.copy(fontFamily = PlusJakarta, fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em),
        titleLarge = titleLarge.copy(fontFamily = PlusJakarta, fontWeight = FontWeight.Bold),
        titleMedium = titleMedium.copy(fontFamily = PlusJakarta, fontWeight = FontWeight.SemiBold),
        titleSmall = titleSmall.copy(fontFamily = PlusJakarta, fontWeight = FontWeight.SemiBold),
        bodyLarge = bodyLarge.copy(fontFamily = PlusJakarta),
        bodyMedium = bodyMedium.copy(fontFamily = PlusJakarta),
        bodySmall = bodySmall.copy(fontFamily = PlusJakarta),
        labelLarge = labelLarge.copy(fontFamily = PlusJakarta, fontWeight = FontWeight.SemiBold),
        labelMedium = labelMedium.copy(fontFamily = PlusJakarta),
        labelSmall = TextStyle(fontFamily = PlusJakarta, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp),
    )
}

/** Expressive shape scale: fully rounded buttons/chips come from component defaults; cards are generous. */
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/** Whether the app is currently in dark mode, so content-seeded subtrees (anime color) can match. */
val LocalFuwaDark = compositionLocalOf { true }

@Composable
fun FuwaTheme(settings: AppSettings = AppSettings(), content: @Composable () -> Unit) {
    val dark = when (settings.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    val context = LocalContext.current
    val base = when {
        settings.dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> DarkScheme
        else -> LightScheme
    }
    val scheme = if (dark && settings.amoledBlack) base.toAmoled() else base

    CompositionLocalProvider(LocalFuwaDark provides dark) {
        MaterialTheme(colorScheme = scheme, typography = AppTypography, shapes = AppShapes, content = content)
    }
}
