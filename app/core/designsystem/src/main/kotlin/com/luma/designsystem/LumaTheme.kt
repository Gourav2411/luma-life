package com.luma.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object LumaColors {
    val Paper = Color(0xFFFFFDF5)
    val Ink = Color(0xFF10120F)
    val Lime = Color(0xFFDFFF4F)
    val LimeBright = Color(0xFFCFFF00)
    val Cobalt = Color(0xFF3457FF)
    val Coral = Color(0xFFFF5E4D)
    val Aqua = Color(0xFF41EAD4)
    val Violet = Color(0xFF8B5CF6)
    val Sun = Color(0xFFFFD84A)
    val Surface = Color(0xFFFFFFFF)
    val Muted = Color(0xFF62665F)
    val Border = Color(0xFFCFD1C9)
    val SoftInk = Color(0xFF252822)
    val DarkSurface = Color(0xFF1A1E19)
}

val LumaShapes = androidx.compose.material3.Shapes(
    extraSmall = RoundedCornerShape(9.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(34.dp),
)

private val LumaTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 50.sp,
        lineHeight = 48.sp,
        letterSpacing = (-2).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 42.sp,
        lineHeight = 42.sp,
        letterSpacing = (-1.5).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 34.sp,
        lineHeight = 35.sp,
        letterSpacing = (-0.8).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 25.sp,
        lineHeight = 29.sp,
        letterSpacing = (-0.25).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 25.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 21.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 23.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 14.sp,
        letterSpacing = 0.3.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 0.8.sp,
    ),
)

private val LightScheme = lightColorScheme(
    primary = LumaColors.Ink,
    onPrimary = Color.White,
    primaryContainer = LumaColors.Lime,
    onPrimaryContainer = LumaColors.Ink,
    secondary = LumaColors.Cobalt,
    onSecondary = Color.White,
    tertiary = LumaColors.Coral,
    background = LumaColors.Paper,
    onBackground = LumaColors.Ink,
    surface = LumaColors.Surface,
    onSurface = LumaColors.Ink,
    surfaceVariant = Color(0xFFF0EFE9),
    onSurfaceVariant = LumaColors.Muted,
    outline = Color(0xFFB8BBB2),
    outlineVariant = LumaColors.Border,
    error = Color(0xFFB42318),
)

private val DarkScheme = darkColorScheme(
    primary = LumaColors.Lime,
    onPrimary = LumaColors.Ink,
    primaryContainer = Color(0xFF3B4822),
    onPrimaryContainer = LumaColors.Lime,
    secondary = Color(0xFFB9C2FF),
    tertiary = Color(0xFFFFB4A9),
    background = Color(0xFF11140F),
    onBackground = Color(0xFFF2F4EF),
    surface = LumaColors.DarkSurface,
    onSurface = Color(0xFFF2F4EF),
    surfaceVariant = Color(0xFF2B342E),
    onSurfaceVariant = Color(0xFFC2CBC4),
    outline = Color(0xFF48534B),
)

@Composable
fun LumaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = LumaTypography,
        shapes = LumaShapes,
        content = content,
    )
}
