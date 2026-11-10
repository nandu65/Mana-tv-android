package com.m3uplayer.tv.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalUiPreferences = staticCompositionLocalOf { UiPreferences() }

@Composable
fun M3UPlayerTVTheme(
    uiPreferences: UiPreferences = UiPreferences(),
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = uiPreferences.accentColor.primary,
        secondary = uiPreferences.accentColor.glow,
        background = uiPreferences.theme.bgMain,
        surface = uiPreferences.theme.bgCard,
        onPrimary = TextPrimary,
        onBackground = TextPrimary,
        onSurface = TextPrimary
    )

    CompositionLocalProvider(LocalUiPreferences provides uiPreferences) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
