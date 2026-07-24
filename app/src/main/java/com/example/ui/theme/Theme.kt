package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val FinanzaDarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = EmeraldOnPrimary,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = EmeraldOnContainer,
    secondary = SapphireSecondary,
    onSecondary = TextPrimary,
    secondaryContainer = SapphireContainer,
    onSecondaryContainer = SapphireOnContainer,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkOutline,
    error = ExpenseRed
)

@Composable
fun FinanzaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FinanzaDarkColorScheme,
        typography = Typography,
        content = content
    )
}

