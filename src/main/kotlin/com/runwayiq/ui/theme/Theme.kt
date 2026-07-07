package com.runwayiq.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Purple = Color(0xFF7F77DD)
val PurpleLight = Color(0xFFEEEDFE)
val PurpleDark = Color(0xFF3C3489)

val Teal = Color(0xFF1D9E75)
val TealLight = Color(0xFFE1F5EE)
val TealDark = Color(0xFF085041)

val Coral = Color(0xFFD85A30)
val CoralLight = Color(0xFFFAECE7)
val CoralDark = Color(0xFF712B13)

val AmberLight = Color(0xFFFAEEDA)
val AmberDark = Color(0xFF633806)
val Amber = Color(0xFFBA7517)

val Surface0 = Color(0xFFF5F4F1)
val Surface1 = Color(0xFFFAF9F7)
val Surface2 = Color(0xFFFFFFFF)

val TextPrimary = Color(0xFF1A1A18)
val TextSecondary = Color(0xFF5F5E5A)
val TextMuted = Color(0xFF888780)

val BorderDefault = Color(0xFFD3D1C7)

private val LightColors = lightColorScheme(
    primary = Purple,
    onPrimary = Color.White,
    primaryContainer = PurpleLight,
    onPrimaryContainer = PurpleDark,
    secondary = Teal,
    onSecondary = Color.White,
    secondaryContainer = TealLight,
    onSecondaryContainer = TealDark,
    error = Coral,
    errorContainer = CoralLight,
    onErrorContainer = CoralDark,
    background = Surface0,
    surface = Surface1,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = Surface1,
    outline = BorderDefault
)

@Composable
fun RunwayIQTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography(
            headlineLarge = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Medium, color = TextPrimary),
            headlineMedium = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium, color = TextPrimary),
            headlineSmall = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Medium, color = TextPrimary),
            bodyLarge = TextStyle(fontSize = 15.sp, color = TextPrimary, lineHeight = 22.sp),
            bodyMedium = TextStyle(fontSize = 13.sp, color = TextSecondary, lineHeight = 20.sp),
            bodySmall = TextStyle(fontSize = 12.sp, color = TextMuted),
            labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextSecondary),
        ),
        content = content
    )
}
