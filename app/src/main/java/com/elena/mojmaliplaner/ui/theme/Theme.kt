package com.elena.mojmaliplaner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PastelBlue,
    onPrimary = BlackCharcoal,
    primaryContainer = PastelPurple,
    onPrimaryContainer = BlackCharcoal,
    secondary = SoftGold,
    onSecondary = BlackCharcoal,
    secondaryContainer = SoftGray.copy(alpha = 0.2f),
    onSecondaryContainer = BlackCharcoal,
    tertiary = PastelPurple,
    onTertiary = BlackCharcoal,
    background = WarmPastelBg,
    onBackground = BlackCharcoal,
    surface = SurfaceLight,
    onSurface = BlackCharcoal,
    surfaceVariant = WarmPastelBg,
    onSurfaceVariant = BlackCharcoal,
    outline = SoftGray
)

private val DarkColorScheme = darkColorScheme(
    primary = SoftGold,
    onPrimary = DarkBg,
    primaryContainer = DarkCardBase,
    onPrimaryContainer = SoftGold,
    secondary = PastelBlue,
    onSecondary = DarkBg,
    secondaryContainer = DarkCardBase,
    onSecondaryContainer = PastelBlue,
    tertiary = PastelPurple,
    onTertiary = DarkBg,
    background = DarkBg,
    onBackground = BrightCreamText,
    surface = DarkCardBase,
    onSurface = BrightCreamText,
    surfaceVariant = DarkCardBase,
    onSurfaceVariant = SoftSilverGray,
    outline = SoftGold
)

@Composable
fun MojMaliPlanerTheme(
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
