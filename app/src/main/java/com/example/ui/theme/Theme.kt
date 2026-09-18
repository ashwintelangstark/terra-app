package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = TerracottaPrimary,
    onPrimary = Color.White,
    primaryContainer = TerracottaLight,
    onPrimaryContainer = InkPrimary,
    secondary = CobaltReserved,
    onSecondary = Color.White,
    background = PaperBackground,
    onBackground = InkPrimary,
    surface = PaperSurface,
    onSurface = InkPrimary,
    surfaceVariant = PaperSurfaceVariant,
    onSurfaceVariant = InkSecondary,
    outline = BorderSubtle,
    error = RedDestructive,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = TerracottaPrimary,
    onPrimary = Color.White,
    background = Color(0xFF1E1C1A),
    onBackground = Color(0xFFEDE9E3),
    surface = Color(0xFF282522),
    onSurface = Color(0xFFEDE9E3),
    surfaceVariant = Color(0xFF332F2B),
    onSurfaceVariant = Color(0xFFB5AFA8),
    outline = Color(0xFF45403B)
)

@Composable
fun TerraTheme(
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
