package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PpiGreen,
    onPrimary = Color.White,
    primaryContainer = PpiGreenSoft,
    onPrimaryContainer = PpiGreenDark,
    secondary = PpiOrange,
    onSecondary = Color.White,
    secondaryContainer = PpiOrangeLight,
    onSecondaryContainer = PpiOrangeDark,
    tertiary = PpiGreenLight,
    onTertiary = Color.White,
    background = PpiGreenBackground,
    onBackground = PpiInk,
    surface = Color.White,
    onSurface = PpiInk,
    surfaceVariant = PpiGreenSoft,
    onSurfaceVariant = PpiInk,
    outline = PpiLine,
    error = PpiError,
    onError = Color.White,
    errorContainer = PpiErrorContainer,
    onErrorContainer = PpiError
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPpiGreen,
    onPrimary = Color.Black,
    primaryContainer = DarkPpiGreenContainer,
    onPrimaryContainer = DarkPpiText,
    secondary = DarkPpiOrange,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF5A3000),
    onSecondaryContainer = DarkPpiOrange,
    tertiary = DarkPpiGreen,
    onTertiary = Color.Black,
    background = DarkPpiBackground,
    onBackground = DarkPpiText,
    surface = DarkPpiSurface,
    onSurface = DarkPpiText,
    surfaceVariant = DarkPpiSurfaceVariant,
    onSurfaceVariant = DarkPpiText,
    outline = DarkPpiLine,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun SistemPpiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
