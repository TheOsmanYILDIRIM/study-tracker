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
    primary = SapphireDark,
    secondary = PurpleDark,
    tertiary = EmeraldDark,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkCard,
    error = RoseDark
)

private val LightColorScheme = lightColorScheme(
    primary = SapphirePrimary,
    secondary = PurpleActive,
    tertiary = EmeraldSuccess,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightCard,
    error = RoseReject
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

    Material3Theme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
