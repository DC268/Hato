package com.hato.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    primaryContainer = PrimaryGreenFixed,
    onPrimary = TextWhite,
    onPrimaryContainer = OnPrimaryFixed,
    secondary = SecondaryBlue,
    onSecondary = TextWhite,
    secondaryContainer = SecondaryBlueContainer,
    onSecondaryContainer = OnSecondaryContainer,
    error = ErrorRed,
    onError = TextWhite,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = TextWhite,
    onSurface = TextWhite,
    onSurfaceVariant = TextLightGray,
    outline = OutlineVariant,
    outlineVariant = OutlineVariant
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    primaryContainer = PrimaryGreenFixed,
    onPrimary = TextWhite,
    onPrimaryContainer = OnPrimaryFixed,
    secondary = SecondaryBlue,
    onSecondary = TextWhite,
    secondaryContainer = SecondaryBlueContainer,
    onSecondaryContainer = OnSecondaryContainer,
    error = ErrorRed,
    onError = TextWhite,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Color(0xFFF8FAF3),
    surface = Color(0xFFF8FAF3),
    surfaceVariant = Color(0xFFE2E3DD),
    onBackground = Color(0xFF191C18),
    onSurface = Color(0xFF191C18),
    onSurfaceVariant = Color(0xFF42493F),
    outline = Color(0xFF72796F),
    outlineVariant = Color(0xFFC2C8BD)
)

@Composable
fun HatoTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}