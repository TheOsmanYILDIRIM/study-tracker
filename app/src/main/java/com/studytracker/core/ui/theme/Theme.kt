package com.studytracker.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ZomoPurpleLight,
    onPrimary = ZomoDarkBg,
    primaryContainer = ZomoDarkCard,
    onPrimaryContainer = ZomoPurpleLight,
    secondary = ZomoNeonMint,
    onSecondary = ZomoDarkBg,
    secondaryContainer = ZomoDarkSurface,
    tertiary = ZomoPink,
    background = ZomoDarkBg,
    surface = ZomoDarkSurface,
    surfaceVariant = ZomoDarkCard,
    onSurface = ZomoLavenderSurface,
    onSurfaceVariant = ZomoPurpleLight,
    outline = ZomoDarkBorder,
    error = ZomoPink
)

private val LightColorScheme = lightColorScheme(
    primary = ZomoPurplePrimary,
    onPrimary = ZomoLavenderSurface,
    primaryContainer = ZomoVioletContainer,
    onPrimaryContainer = ZomoPurpleDark,
    secondary = ZomoNeonMint,
    onSecondary = ZomoTextPrimary,
    secondaryContainer = ZomoLavenderCard,
    tertiary = ZomoAmber,
    background = ZomoLavenderBg,
    surface = ZomoLavenderSurface,
    surfaceVariant = ZomoLavenderCard,
    onSurface = ZomoTextPrimary,
    onSurfaceVariant = ZomoTextSecondary,
    outline = ZomoLavenderBorder,
    error = ZomoPink
)

@Composable
fun StudyTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}

