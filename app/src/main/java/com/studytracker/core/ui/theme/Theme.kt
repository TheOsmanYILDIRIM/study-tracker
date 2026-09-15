package com.studytracker.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CyberVioletColorScheme = darkColorScheme(
    primary = ZomoPurplePrimary,
    onPrimary = ZomoDarkCanvas,
    primaryContainer = ZomoDarkCard,
    onPrimaryContainer = ZomoPurpleLight,
    secondary = ZomoNeonMint,
    onSecondary = ZomoNeonMintText,
    secondaryContainer = ZomoDarkCardElevated,
    onSecondaryContainer = ZomoNeonMint,
    tertiary = ZomoPink,
    background = ZomoDarkCanvas,
    surface = ZomoDarkSurface,
    surfaceVariant = ZomoDarkCard,
    onBackground = ZomoTextPrimary,
    onSurface = ZomoTextPrimary,
    onSurfaceVariant = ZomoTextSecondary,
    outline = ZomoDarkBorder,
    outlineVariant = ZomoGlassBorder,
    error = ZomoPink
)

@Composable
fun StudyTrackerTheme(
    darkTheme: Boolean = true, // Futuristic Cyber-Violet Dark is default
    content: @Composable () -> Unit
) {
    val colorScheme = CyberVioletColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
