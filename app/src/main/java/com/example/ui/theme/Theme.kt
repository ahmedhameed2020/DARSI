package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Darsi Design System - Dedicated Premium Light Mode Theme
private val DarsiLightColorScheme = lightColorScheme(
    primary = DarsiRoyalBlue,
    onPrimary = Color.White,
    primaryContainer = DarsiRoyalBlueSubtle,
    onPrimaryContainer = DarsiRoyalBlueDark,
    secondary = DarsiNavy,
    onSecondary = Color.White,
    secondaryContainer = DarsiSurfaceSecondary,
    onSecondaryContainer = DarsiNavyDark,
    tertiary = DarsiAmberDark,
    onTertiary = Color.White,
    tertiaryContainer = DarsiAmberBg,
    onTertiaryContainer = DarsiAmberDark,
    background = DarsiBackgroundWarm,
    onBackground = DarsiNavyDark,
    surface = DarsiSurfaceWhite,
    onSurface = DarsiNavyDark,
    surfaceVariant = DarsiSurfaceSecondary,
    onSurfaceVariant = DarsiNavyMuted,
    outline = DarsiBorder,
    outlineVariant = DarsiBorderSubtle,
    error = DarsiCoralRedDark,
    errorContainer = DarsiCoralRedBg,
    onError = Color.White,
    onErrorContainer = DarsiCoralRedDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Light Mode is the default & primary visual theme
    content: @Composable () -> Unit
) {
    // Explicit Light Theme styling: warm off-white background, pure white cards, dark navy text
    val colorScheme = DarsiLightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
