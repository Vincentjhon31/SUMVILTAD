package com.zynt.sumviltadconnect.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.zynt.sumviltadconnect.data.local.SettingsStore

private val DarkColorScheme = darkColorScheme(
    primary = ModernGreenPrimary80,
    secondary = ModernGreenSecondary80,
    tertiary = ModernGreenTertiary80,
    background = ModernGreenBackgroundDark,
    surface = ModernGreenSurfaceDark,
    onPrimary = ModernGreenBackgroundDark,
    onSecondary = ModernGreenBackgroundDark,
    onTertiary = ModernGreenBackgroundDark,
    onBackground = OnGreenBackgroundDark,
    onSurface = OnGreenSurfaceDark,
    primaryContainer = ModernGreenContainerDark,
    onPrimaryContainer = ModernGreenOnContainerDark,
    surfaceVariant = ModernGreenContainerDark,
    onSurfaceVariant = ModernGreenOnContainerDark
)

private val LightColorScheme = lightColorScheme(
    primary = ModernGreenPrimary,
    secondary = ModernGreenSecondary,
    tertiary = ModernGreenTertiary,
    background = ModernGreenBackgroundLight,
    surface = ModernGreenSurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = OnGreenBackgroundLight,
    onSurface = OnGreenSurfaceLight,
    primaryContainer = ModernGreenContainerLight,
    onPrimaryContainer = ModernGreenOnContainerLight,
    surfaceVariant = ModernGreenContainerLight,
    onSurfaceVariant = ModernGreenOnContainerLight
)

@Composable
fun SumviltadConnectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Changed to false to always use Gray Green theme
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val settingsStore = SettingsStore(context)
    val themeSetting by settingsStore.theme.collectAsState(initial = "light")

    // Determine which theme to use based on user preference
    val useDarkTheme = when (themeSetting) {
        "dark" -> true
        "light" -> false
        "system" -> darkTheme
        else -> false // Default to light theme (Gray Green)
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        useDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}