package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlueLight,
    onPrimary = TextWhite,
    primaryContainer = ElectricBlueContainer,
    onPrimaryContainer = ElectricBlueOnContainer,
    secondary = AccentCyan,
    onSecondary = NavyDarkest,
    secondaryContainer = NavySurfaceVariant,
    onSecondaryContainer = TextOffWhite,
    tertiary = IncomeGreen,
    onTertiary = NavyDarkest,
    background = NavyDarkest,
    onBackground = TextOffWhite,
    surface = NavyDark,
    onSurface = TextOffWhite,
    surfaceVariant = NavySurface,
    onSurfaceVariant = TextMuted,
    outline = NavyOutline,
    outlineVariant = NavyOutlineVariant,
    error = ExpenseRed,
    onError = TextWhite,
    errorContainer = ExpenseRedBg,
    onErrorContainer = ExpenseRed
)

private val LightColorScheme = lightColorScheme(
    primary = SlateLightPrimary,
    onPrimary = TextWhite,
    primaryContainer = SlateLightPrimaryContainer,
    onPrimaryContainer = SlateLightPrimary,
    secondary = ElectricBlue,
    onSecondary = TextWhite,
    secondaryContainer = SlateLightSurfaceVariant,
    onSecondaryContainer = SlateLightTextPrimary,
    tertiary = SuccessGreen,
    onTertiary = TextWhite,
    background = SlateLightBackground,
    onBackground = SlateLightTextPrimary,
    surface = SlateLightSurface,
    onSurface = SlateLightTextPrimary,
    surfaceVariant = SlateLightSurfaceVariant,
    onSurfaceVariant = SlateLightTextSecondary,
    outline = SlateLightOutline,
    error = ExpenseRed,
    onError = TextWhite
)

@Composable
fun NidhiTheme(
    darkTheme: Boolean = true, // Dark-first by default as instructed
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
