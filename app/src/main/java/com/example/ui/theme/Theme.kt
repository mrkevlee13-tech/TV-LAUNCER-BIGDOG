package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val B2DarkColorScheme = darkColorScheme(
    primary = B2Accent,
    onPrimary = B2AccentContent,
    secondary = B2RaisedSurface,
    onSecondary = B2MainText,
    tertiary = B2Accent,
    background = B2Background,
    onBackground = B2MainText,
    surface = B2TileSurface,
    onSurface = B2MainText,
    surfaceVariant = B2RaisedSurface,
    onSurfaceVariant = B2QuietText,
    outline = B2NotConnectedDot
)

@Composable
fun BigDogLauncherTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = B2DarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    BigDogLauncherTheme(content = content)
}
