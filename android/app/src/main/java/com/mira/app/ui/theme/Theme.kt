package com.mira.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MiraColorScheme = lightColorScheme(
    primary = Terracotta,
    onPrimary = Cream,
    secondary = Rose,
    onSecondary = Cream,
    tertiary = Teal,
    background = Cream,
    onBackground = TextPrimary,
    surface = Cream,
    onSurface = TextPrimary,
    error = Rose
)

@Composable
fun MIRATheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Terracotta.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = MiraColorScheme,
        typography = Typography,
        content = content
    )
}