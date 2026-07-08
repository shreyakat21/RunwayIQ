package com.runwayiq.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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

// Palette values sourced from Ghostfolio's own theme (apps/client/src/styles.scss):
// primary teal rgb(54,207,204), warn red rgb(220,53,69), dark bg rgb(25,25,25),
// dark card rgb(66,66,66), light app-bar rgb(245,245,245).
private val LightPalette = RunwayPalette(
    purple = Color(0xFF0E9490),
    purpleLight = Color(0xFFE0F7F6),
    purpleDark = Color(0xFF0B615F),
    teal = Color(0xFF2E7D32),
    tealLight = Color(0xFFE8F5E9),
    tealDark = Color(0xFF1B5E20),
    coral = Color(0xFFB02A37),
    coralLight = Color(0xFFFDECEA),
    coralDark = Color(0xFF7A1F2B),
    amber = Color(0xFFB45309),
    amberLight = Color(0xFFFFFBEB),
    amberDark = Color(0xFF78350F),
    surface0 = Color(0xFFFAFAFA),
    surface1 = Color(0xFFF5F5F5),
    surface2 = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF212121),
    textSecondary = Color(0xFF666666),
    textMuted = Color(0xFF9E9E9E),
    borderDefault = Color(0xFFECECEC),
)

private val DarkPalette = RunwayPalette(
    purple = Color(0xFF36CFCC),
    purpleLight = Color(0xFF123B3A),
    purpleDark = Color(0xFF7FEAE7),
    teal = Color(0xFF66BB6A),
    tealLight = Color(0xFF1B3320),
    tealDark = Color(0xFFA5D6A7),
    coral = Color(0xFFDC3545),
    coralLight = Color(0xFF3D1418),
    coralDark = Color(0xFFF1948A),
    amber = Color(0xFFFBBF24),
    amberLight = Color(0xFF3F2A06),
    amberDark = Color(0xFFFDE68A),
    surface0 = Color(0xFF191919),
    surface1 = Color(0xFF212121),
    surface2 = Color(0xFF424242),
    textPrimary = Color(0xFFF2F2F2),
    textSecondary = Color(0xFFBCBCBC),
    textMuted = Color(0xFF909090),
    borderDefault = Color(0xFF515151),
)

val LocalRunwayPalette = staticCompositionLocalOf { LightPalette }

/** Numeric/metric font used for currency figures throughout the app. */
val NumericFontFamily: FontFamily = FontFamily.SansSerif

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
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.textPrimary,
                    letterSpacing = (-0.3).sp,
                ),
                headlineMedium = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.textPrimary,
                    letterSpacing = (-0.2).sp,
                ),
                headlineSmall = TextStyle(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.textPrimary,
                ),
                bodyLarge = TextStyle(fontSize = 15.sp, color = palette.textPrimary, lineHeight = 22.sp),
                bodyMedium = TextStyle(fontSize = 13.sp, color = palette.textSecondary, lineHeight = 20.sp),
                bodySmall = TextStyle(fontSize = 12.sp, color = palette.textMuted),
                labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = palette.textSecondary),
            ),
            content = content,
        )
    }
}
