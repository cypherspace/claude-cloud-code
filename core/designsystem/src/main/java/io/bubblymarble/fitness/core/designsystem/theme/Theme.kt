package io.bubblymarble.fitness.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------------
// Color tokens — verbatim from colors_and_type.css.
// Dark is the canonical scheme; light mirrors the same intent.
// ---------------------------------------------------------------------------

private val DarkColors = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF002B3D),
    secondary = Color(0xFF2DD4BF),
    onSecondary = Color(0xFF003733),
    background = Color(0xFF0B1220),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFF94A3B8),
    surfaceContainer = Color(0xFF131C2E),
    surfaceContainerHigh = Color(0xFF182238),
    surfaceContainerHighest = Color(0xFF1D2942),
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1F2937),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF0EA5E9),
    onPrimary = Color.White,
    secondary = Color(0xFF14B8A6),
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0B1220),
    surface = Color.White,
    onSurface = Color(0xFF0B1220),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color(0xFFF1F5F9),
    surfaceContainerHighest = Color(0xFFE2E8F0),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
)

// ---------------------------------------------------------------------------
// Feature accents — small marks only, never full backgrounds.
// ---------------------------------------------------------------------------

data class FeatureAccents(
    val plans: Color,
    val workouts: Color,
    val meals: Color,
    val body: Color,
    val stats: Color,
    val settings: Color,
)

private val DarkAccents = FeatureAccents(
    plans = Color(0xFFA78BFA),
    workouts = Color(0xFF38BDF8),
    meals = Color(0xFF34D399),
    body = Color(0xFFFBBF24),
    stats = Color(0xFFF472B6),
    settings = Color(0xFF94A3B8),
)

private val LightAccents = FeatureAccents(
    plans = Color(0xFF7C3AED),
    workouts = Color(0xFF0EA5E9),
    meals = Color(0xFF059669),
    body = Color(0xFFD97706),
    stats = Color(0xFFDB2777),
    settings = Color(0xFF64748B),
)

val LocalFeatureAccents = staticCompositionLocalOf { DarkAccents }

// ---------------------------------------------------------------------------
// Typography — Material 3 scale tightened to the spec. Inter would land here
// as a Google-Fonts FontFamily; for v1 we use the system default which is
// Roboto on Android, visually close to Inter at body sizes.
// ---------------------------------------------------------------------------

private val Typo = Typography().run {
    val sans = FontFamily.Default
    copy(
        headlineLarge = headlineLarge.copy(fontFamily = sans, fontSize = 32.sp, lineHeight = 40.sp),
        headlineMedium = headlineMedium.copy(fontFamily = sans, fontSize = 28.sp, lineHeight = 36.sp),
        headlineSmall = headlineSmall.copy(fontFamily = sans, fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Medium),
        titleLarge = titleLarge.copy(fontFamily = sans, fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium),
        titleMedium = titleMedium.copy(fontFamily = sans, fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),
        titleSmall = titleSmall.copy(fontFamily = sans, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
        bodyLarge = bodyLarge.copy(fontFamily = sans, fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium = bodyMedium.copy(fontFamily = sans, fontSize = 14.sp, lineHeight = 20.sp),
        bodySmall = bodySmall.copy(fontFamily = sans, fontSize = 12.sp, lineHeight = 16.sp),
        labelLarge = labelLarge.copy(fontFamily = sans, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
        labelMedium = labelMedium.copy(fontFamily = sans, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
        labelSmall = labelSmall.copy(fontFamily = sans, fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    )
}

/** Tabular-figures variant for numeric values in stat cards, timers, set logs. */
val MonoNumericStyle: TextStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Medium,
)

@Composable
fun OldFitTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val scheme = if (darkTheme) DarkColors else LightColors
    val accents = if (darkTheme) DarkAccents else LightAccents
    CompositionLocalProvider(LocalFeatureAccents provides accents) {
        MaterialTheme(
            colorScheme = scheme,
            typography = Typo,
            content = content,
        )
    }
}

// Backwards-compatible alias kept temporarily so MainActivity keeps building.
@Composable
fun BubblymarbleTheme(content: @Composable () -> Unit) = OldFitTheme(content = content)
