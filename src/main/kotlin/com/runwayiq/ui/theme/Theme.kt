package com.runwayiq.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.sp

data class RunwayPalette(
    val purple: Color,
    val purpleLight: Color,
    val purpleDark: Color,
    val teal: Color,
    val tealLight: Color,
    val tealDark: Color,
    val coral: Color,
    val coralLight: Color,
    val coralDark: Color,
    val amber: Color,
    val amberLight: Color,
    val amberDark: Color,
    val surface0: Color,
    val surface1: Color,
    val surface2: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val borderDefault: Color,
)

// Deep-navy fintech dashboard palette: bright cyan as the primary accent,
// emerald for positive/good indicators, rose for negative/alert indicators,
// amber for secondary warnings.
private val LightPalette = RunwayPalette(
    purple = Color(0xFF0891B2),
    purpleLight = Color(0xFFE0F7FA),
    purpleDark = Color(0xFF075E6B),
    teal = Color(0xFF059669),
    tealLight = Color(0xFFE7F8F0),
    tealDark = Color(0xFF04543B),
    coral = Color(0xFFE11D48),
    coralLight = Color(0xFFFCE8EC),
    coralDark = Color(0xFF9F1239),
    amber = Color(0xFFB45309),
    amberLight = Color(0xFFFFFBEB),
    amberDark = Color(0xFF78350F),
    surface0 = Color(0xFFF4F6FC),
    surface1 = Color(0xFFECEFF9),
    surface2 = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF1A1F36),
    textSecondary = Color(0xFF565C7D),
    textMuted = Color(0xFF8B90AC),
    borderDefault = Color(0xFFE2E5F3),
)

private val DarkPalette = RunwayPalette(
    purple = Color(0xFF22D3EE),
    purpleLight = Color(0xFF123244),
    purpleDark = Color(0xFF67E8F9),
    teal = Color(0xFF34D399),
    tealLight = Color(0xFF0F3327),
    tealDark = Color(0xFF6EE7B7),
    coral = Color(0xFFFB7185),
    coralLight = Color(0xFF3F1620),
    coralDark = Color(0xFFFCA5AF),
    amber = Color(0xFFFBBF24),
    amberLight = Color(0xFF3F2A06),
    amberDark = Color(0xFFFDE68A),
    surface0 = Color(0xFF12172E),
    surface1 = Color(0xFF171C38),
    surface2 = Color(0xFF1D2447),
    textPrimary = Color(0xFFF5F7FF),
    textSecondary = Color(0xFF9CA3C4),
    textMuted = Color(0xFF6B7299),
    borderDefault = Color(0xFF2B3260),
)

val LocalRunwayPalette = staticCompositionLocalOf { LightPalette }

/** App-wide rounded, friendly sans-serif (bundled from resources/font, variable-weight file). */
val AppFontFamily: FontFamily = FontFamily(
    Font("font/Quicksand.ttf", FontWeight.Normal),
    Font("font/Quicksand.ttf", FontWeight.Medium),
    Font("font/Quicksand.ttf", FontWeight.SemiBold),
    Font("font/Quicksand.ttf", FontWeight.Bold),
)

/** Numeric/metric font used for currency figures throughout the app. */
val NumericFontFamily: FontFamily = AppFontFamily

val Purple: Color
    @Composable get() = LocalRunwayPalette.current.purple
val PurpleLight: Color
    @Composable get() = LocalRunwayPalette.current.purpleLight
val PurpleDark: Color
    @Composable get() = LocalRunwayPalette.current.purpleDark
val Teal: Color
    @Composable get() = LocalRunwayPalette.current.teal
val TealLight: Color
    @Composable get() = LocalRunwayPalette.current.tealLight
val TealDark: Color
    @Composable get() = LocalRunwayPalette.current.tealDark
val Coral: Color
    @Composable get() = LocalRunwayPalette.current.coral
val CoralLight: Color
    @Composable get() = LocalRunwayPalette.current.coralLight
val CoralDark: Color
    @Composable get() = LocalRunwayPalette.current.coralDark
val Amber: Color
    @Composable get() = LocalRunwayPalette.current.amber
val AmberLight: Color
    @Composable get() = LocalRunwayPalette.current.amberLight
val AmberDark: Color
    @Composable get() = LocalRunwayPalette.current.amberDark
val Surface0: Color
    @Composable get() = LocalRunwayPalette.current.surface0
val Surface1: Color
    @Composable get() = LocalRunwayPalette.current.surface1
val Surface2: Color
    @Composable get() = LocalRunwayPalette.current.surface2
val TextPrimary: Color
    @Composable get() = LocalRunwayPalette.current.textPrimary
val TextSecondary: Color
    @Composable get() = LocalRunwayPalette.current.textSecondary
val TextMuted: Color
    @Composable get() = LocalRunwayPalette.current.textMuted
val BorderDefault: Color
    @Composable get() = LocalRunwayPalette.current.borderDefault

@Composable
fun RunwayIQTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    val palette = if (darkTheme) DarkPalette else LightPalette

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.purple,
            onPrimary = Color.White,
            primaryContainer = palette.purpleLight,
            onPrimaryContainer = palette.purpleDark,
            secondary = palette.teal,
            onSecondary = Color.White,
            secondaryContainer = palette.tealLight,
            onSecondaryContainer = palette.tealDark,
            error = palette.coral,
            errorContainer = palette.coralLight,
            onErrorContainer = palette.coralDark,
            background = palette.surface0,
            surface = palette.surface1,
            onBackground = palette.textPrimary,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.surface1,
            outline = palette.borderDefault,
        )
    } else {
        lightColorScheme(
            primary = palette.purple,
            onPrimary = Color.White,
            primaryContainer = palette.purpleLight,
            onPrimaryContainer = palette.purpleDark,
            secondary = palette.teal,
            onSecondary = Color.White,
            secondaryContainer = palette.tealLight,
            onSecondaryContainer = palette.tealDark,
            error = palette.coral,
            errorContainer = palette.coralLight,
            onErrorContainer = palette.coralDark,
            background = palette.surface0,
            surface = palette.surface1,
            onBackground = palette.textPrimary,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.surface1,
            outline = palette.borderDefault,
        )
    }

    CompositionLocalProvider(LocalRunwayPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography(
                headlineLarge = TextStyle(
                    fontFamily = AppFontFamily,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.textPrimary,
                    letterSpacing = (-0.3).sp,
                ),
                headlineMedium = TextStyle(
                    fontFamily = AppFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.textPrimary,
                    letterSpacing = (-0.2).sp,
                ),
                headlineSmall = TextStyle(
                    fontFamily = AppFontFamily,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.textPrimary,
                ),
                bodyLarge = TextStyle(fontFamily = AppFontFamily, fontSize = 15.sp, color = palette.textPrimary, lineHeight = 22.sp),
                bodyMedium = TextStyle(fontFamily = AppFontFamily, fontSize = 13.sp, color = palette.textSecondary, lineHeight = 20.sp),
                bodySmall = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, color = palette.textMuted),
                labelMedium = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = palette.textSecondary),
            ),
            content = content,
        )
    }
}
