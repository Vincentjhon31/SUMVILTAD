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
    primary = GrayGreen80,
    secondary = GrayGreenLight80,
    tertiary = GrayGreenAccent80,
    background = Color(0xFF1A1C1A),
    surface = Color(0xFF1F1F1F),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFE8E8E8),
    onSurface = Color(0xFFE8E8E8),
    primaryContainer = GrayGreenDark40,
    onPrimaryContainer = GrayGreen80,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFD0D0D0)
)

private val LightColorScheme = lightColorScheme(
    primary = GrayGreen40,
    secondary = GrayGreenDark40,
    tertiary = GrayGreenAccent40,
    background = SoftWhite,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    primaryContainer = LightGreen,
    onPrimaryContainer = DarkGreen,
    surfaceVariant = LightGreen,
    onSurfaceVariant = DarkGreen
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