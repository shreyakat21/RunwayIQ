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

private val LightPalette = RunwayPalette(
    purple = Color(0xFF4338CA),
    purpleLight = Color(0xFFEEF2FF),
    purpleDark = Color(0xFF312E81),
    teal = Color(0xFF047857),
    tealLight = Color(0xFFECFDF5),
    tealDark = Color(0xFF065F46),
    coral = Color(0xFFB91C1C),
    coralLight = Color(0xFFFEF2F2),
    coralDark = Color(0xFF7F1D1D),
    amber = Color(0xFFB45309),
    amberLight = Color(0xFFFFFBEB),
    amberDark = Color(0xFF78350F),
    surface0 = Color(0xFFF1F2F5),
    surface1 = Color(0xFFFAFAFC),
    surface2 = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    textMuted = Color(0xFF94A3B8),
    borderDefault = Color(0xFFE2E8F0),
)

private val DarkPalette = RunwayPalette(
    purple = Color(0xFF6366F1),
    purpleLight = Color(0xFF26244F),
    purpleDark = Color(0xFFC7D2FE),
    teal = Color(0xFF10B981),
    tealLight = Color(0xFF0B2E23),
    tealDark = Color(0xFF6EE7B7),
    coral = Color(0xFFF87171),
    coralLight = Color(0xFF3F1518),
    coralDark = Color(0xFFFCA5A5),
    amber = Color(0xFFFBBF24),
    amberLight = Color(0xFF3F2A06),
    amberDark = Color(0xFFFDE68A),
    surface0 = Color(0xFF0B0F1A),
    surface1 = Color(0xFF121826),
    surface2 = Color(0xFF171E2E),
    textPrimary = Color(0xFFF1F5F9),
    textSecondary = Color(0xFF94A3B8),
    textMuted = Color(0xFF64748B),
    borderDefault = Color(0xFF263042),
)

val LocalRunwayPalette = staticCompositionLocalOf { LightPalette }

/**
 * Numeric/ledger font used for currency and metric figures throughout the app.
 * Tabular monospace digits give financial figures a terminal/ledger feel and
 * keep amounts aligned in tables and lists.
 */
val NumericFontFamily: FontFamily = FontFamily.Monospace

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
