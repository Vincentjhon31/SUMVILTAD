package com.zynt.sumviltadconnect.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Responsive dimension system for multi-device support
 * Adapts UI components based on screen size and density
 */

// Screen size categories based on Material Design guidelines
enum class WindowSize {
    COMPACT,    // Small phones: width < 600dp (e.g., 4-5" phones)
    MEDIUM,     // Standard phones: 600dp ≤ width < 840dp (e.g., 5-6.5" phones)
    EXPANDED    // Large phones/tablets: width ≥ 840dp (e.g., 6.5"+ phones, tablets)
}

// Get current window size category
@Composable
fun rememberWindowSize(): WindowSize {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    
    return when {
        screenWidth < 600 -> WindowSize.COMPACT
        screenWidth < 840 -> WindowSize.MEDIUM
        else -> WindowSize.EXPANDED
    }
}

// Responsive dimensions object
object AppDimensions {
    
    // Spacing scale
    @Composable
    fun paddingExtraSmall(): Dp = when (rememberWindowSize()) {
        WindowSize.COMPACT -> 4.dp
        WindowSize.MEDIUM -> 6.dp
        WindowSize.EXPANDED -> 8.dp
    }
    
    @Composable
    fun paddingSmall(): Dp = when (rememberWindowSize()) {
        WindowSize.COMPACT -> 8.dp
        WindowSize.MEDIUM -> 12.dp
        WindowSize.EXPANDED -> 16.dp
    }
    
    @Composable
    fun paddingMedium(): Dp = when (rememberWindowSize()) {
        WindowSize.COMPACT -> 16.dp
        WindowSize.MEDIUM -> 20.dp
        WindowSize.EXPANDED -> 24.dp
    }
    
    @Composable
    fun paddingLarge(): Dp = when (rememberWindowSize()) {
        WindowSize.COMPACT -> 24.dp
        WindowSize.MEDIUM -> 28.dp
        WindowSize.EXPANDED -> 32.dp
    }
    
    @Composable
    fun paddingExtraLarge(): Dp = when (rememberWindowSize()) {
        WindowSize.COMPACT -> 32.dp
        WindowSize.MEDIUM -> 40.dp
        WindowSize.EXPANDED -> 48.dp
    }
    
    // Component sizes
    @Composable
    fun iconSizeSmall(): Dp = when (rememberWindowSize()) {
        WindowSize.COMPACT -> 18.dp
        WindowSize.MEDIUM -> 20.dp
        WindowSize.EXPANDED -> 24.dp
    }
    
    @Composable
    fun iconSizeMedium(): Dp = when (rememberWindowSize()) {
        WindowSize.COMPACT -> 24.dp
        WindowSize.MEDIUM -> 28.dp
        WindowSize.EXPANDED -> 32.dp
    }
    
    @Composable
    fun iconSizeLarge(): Dp = when (rememberWindowSize()) {
        WindowSize.COMPACT -> 32.dp
        WindowSize.MEDIUM -> 40.dp
        WindowSize.EXPANDED -> 48.dp
    }
    
    // Button dimensions
    @Composable
    fun buttonHeight(): Dp = when (rememberWindowSize()) {
        WindowSize.COMPACT -> 48.dp  // Minimum touch target
        WindowSize.MEDIUM -> 52.dp
        WindowSize.EXPANDED -> 56.dp
    }
    
    @Composable
    fun fabSize(): Dp = when (rememberWindowSize()) {
        WindowSize.COMPACT -> 56.dp  // Standard FAB
        WindowSize.MEDIUM -> 64.dp
        WindowSize.EXPANDED -> 72.dp
    }
    
    // Navigation components
    @Composable
    fun topAppBarHeight(): Dp = when (rememberWindowSize()) {
        WindowSize.COMPACT -> 56.dp
        WindowSize.MEDIUM -> 64.dp
        WindowSize.EXPANDED -> 72.dp
    }
    
    @Composable
    fun bottomNavHeight(): Dp = when (rememberWindowSize()) {
        WindowSize.COMPACT -> 56.dp
        WindowSize.MEDIUM -> 64.dp
        WindowSize.EXPANDED -> 72.dp
    }
    
    @Composable
    fun drawerWidth(): Dp {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        val maxWidth = 360.dp
        
        return when (rememberWindowSize()) {
            WindowSize.COMPACT -> minOf(screenWidth * 0.85f, maxWidth)
            WindowSize.MEDIUM -> minOf(screenWidth * 0.80f, maxWidth)
            WindowSize.EXPANDED -> 360.dp
        }
    }
    
    // Card and container dimensions
    @Composable
    fun cardElevation(): Dp = when (rememberWindowSize()) {
        WindowSize.COMPACT -> 2.dp
        WindowSize.MEDIUM -> 4.dp
        WindowSize.EXPANDED -> 6.dp
    }
    
    @Composable
    fun cornerRadiusSmall(): Dp = when (rememberWindowSize()) {
        WindowSize.COMPACT -> 8.dp
        WindowSize.MEDIUM -> 12.dp
        WindowSize.EXPANDED -> 16.dp
    }
    
    @Composable
    fun cornerRadiusMedium(): Dp = when (rememberWindowSize()) {
        WindowSize.COMPACT -> 12.dp
        WindowSize.MEDIUM -> 16.dp
        WindowSize.EXPANDED -> 20.dp
    }
    
    @Composable
    fun cornerRadiusLarge(): Dp = when (rememberWindowSize()) {
        WindowSize.COMPACT -> 16.dp
        WindowSize.MEDIUM -> 20.dp
        WindowSize.EXPANDED -> 24.dp
    }
    
    // Logo and image sizes
    @Composable
    fun logoSizeSmall(): Dp = when (rememberWindowSize()) {
        WindowSize.COMPACT -> 32.dp
        WindowSize.MEDIUM -> 40.dp
        WindowSize.EXPANDED -> 48.dp
    }
    
    @Composable
    fun logoSizeMedium(): Dp = when (rememberWindowSize()) {
        WindowSize.COMPACT -> 50.dp
        WindowSize.MEDIUM -> 60.dp
        WindowSize.EXPANDED -> 70.dp
    }
    
    @Composable
    fun logoSizeLarge(): Dp = when (rememberWindowSize()) {
        WindowSize.COMPACT -> 90.dp
        WindowSize.MEDIUM -> 110.dp
        WindowSize.EXPANDED -> 130.dp
    }
    
    // Splash screen specific
    @Composable
    fun splashLogoSize(): Dp = when (rememberWindowSize()) {
        WindowSize.COMPACT -> 90.dp
        WindowSize.MEDIUM -> 110.dp
        WindowSize.EXPANDED -> 130.dp
    }
    
    @Composable
    fun splashProgressBarWidth(): Float = when (rememberWindowSize()) {
        WindowSize.COMPACT -> 0.75f
        WindowSize.MEDIUM -> 0.70f
        WindowSize.EXPANDED -> 0.65f
    }
    
    // Drawer header
    @Composable
    fun drawerHeaderHeight(): Dp = when (rememberWindowSize()) {
        WindowSize.COMPACT -> 180.dp
        WindowSize.MEDIUM -> 200.dp
        WindowSize.EXPANDED -> 220.dp
    }
    
    @Composable
    fun drawerLogoSize(): Dp = when (rememberWindowSize()) {
        WindowSize.COMPACT -> 50.dp
        WindowSize.MEDIUM -> 60.dp
        WindowSize.EXPANDED -> 70.dp
    }
    
    // Detection screen
    @Composable
    fun detectionImageHeight(): Dp = when (rememberWindowSize()) {
        WindowSize.COMPACT -> 250.dp
        WindowSize.MEDIUM -> 300.dp
        WindowSize.EXPANDED -> 350.dp
    }
    
    // Minimum touch target size (WCAG 2.1 compliant)
    val minTouchTarget = 48.dp
}

// Extension functions for convenience
@Composable
fun isCompactScreen(): Boolean = rememberWindowSize() == WindowSize.COMPACT

@Composable
fun isMediumScreen(): Boolean = rememberWindowSize() == WindowSize.MEDIUM

@Composable
fun isExpandedScreen(): Boolean = rememberWindowSize() == WindowSize.EXPANDED
