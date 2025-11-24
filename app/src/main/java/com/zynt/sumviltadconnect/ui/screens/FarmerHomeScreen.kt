@file:OptIn(ExperimentalAnimationApi::class)

package com.zynt.sumviltadconnect.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.zynt.sumviltadconnect.R
import com.zynt.sumviltadconnect.ui.viewmodel.AuthViewModel
import com.zynt.sumviltadconnect.ui.viewmodel.CropHealthViewModel
import kotlinx.coroutines.launch
import androidx.navigation.NavBackStackEntry
import androidx.core.content.ContextCompat
import com.zynt.sumviltadconnect.ui.theme.AppDimensions
import com.zynt.sumviltadconnect.utils.UpdateChecker
import com.zynt.sumviltadconnect.ui.components.UpdateAvailableDialog

// Helper function to safely load app icon
@Composable
private fun AppIcon(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val appIcon = remember {
        try {
            // Try to load the launcher icon as a drawable
            ContextCompat.getDrawable(context, R.mipmap.ic_launcher_round)
                ?.let { drawable ->
                    val bitmap = android.graphics.Bitmap.createBitmap(
                        drawable.intrinsicWidth,
                        drawable.intrinsicHeight,
                        android.graphics.Bitmap.Config.ARGB_8888
                    )
                    val canvas = android.graphics.Canvas(bitmap)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                    bitmap.asImageBitmap()
                }
        } catch (e: Exception) {
            null
        }
    }

    if (appIcon != null) {
        Image(
            bitmap = appIcon,
            contentDescription = "App Logo",
            modifier = modifier
        )
    } else {
        // Fallback to Material Icon
        Icon(
            Icons.Default.Agriculture,
            contentDescription = "App Logo",
            tint = Color.White,
            modifier = modifier
        )
    }
}

private sealed class HomeItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard: HomeItem("dashboard","Dashboard", Icons.Default.Home)
    object CropHealth: HomeItem("cropHealth","Crop Health", Icons.Default.HealthAndSafety)
    object Events: HomeItem("events","Events", Icons.Default.Event)
    object Tasks: HomeItem("tasks","Tasks", Icons.Default.Assignment)
    object Irrigation: HomeItem("irrigation","Irrigation", Icons.Default.WaterDrop)
    object Notifications: HomeItem("notifications","Notifications", Icons.Default.Notifications)
    // Removed Settings - now in Profile screen
    object Profile: HomeItem("profileHome","Profile", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerHomeScreen(rootNav: NavController, authViewModel: AuthViewModel) {
    val nav = rememberAnimatedNavController() // switched to animated controller
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val context = LocalContext.current
    
    // Create a shared ViewModel for CropHealth screens
    val cropHealthViewModel: CropHealthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    
    // Update checker
    var showUpdateDialog by remember { mutableStateOf(false) }
    var availableUpdate by remember { mutableStateOf<com.zynt.sumviltadconnect.utils.AppVersion?>(null) }
    
    // Check for updates on app launch
    LaunchedEffect(Unit) {
        val update = UpdateChecker.checkForUpdates()
        if (update?.isUpdateAvailable == true) {
            availableUpdate = update
            showUpdateDialog = true
        }
    }

    val drawerItems = listOf(
        HomeItem.Dashboard,
        HomeItem.CropHealth,
        HomeItem.Irrigation,
        HomeItem.Events,
        HomeItem.Tasks,
        HomeItem.Notifications,
        // Removed Settings from drawer
        HomeItem.Profile
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            EnhancedDrawerContent(
                items = drawerItems,
                onSelect = { item ->
                    scope.launch { drawerState.close() }
                    when(item) {
                        HomeItem.Profile -> rootNav.navigate("profile")
                        else -> nav.navigate(item.route) { launchSingleTop = true }
                    }
                },
                onLogout = {
                    scope.launch { drawerState.close() }
                    authViewModel.logout(context) {
                        rootNav.navigate("login") { popUpTo(0) { inclusive = true } }
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                EnhancedTopAppBar(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onProfileClick = { rootNav.navigate("profile") }
                )
            },
            floatingActionButton = {
                EnhancedFAB(onClick = { rootNav.navigate("detection") })
            }
        ) { paddingValues ->
            val enter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(200, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(150))
            }
            val exit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(200, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(140))
            }
            val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(190, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(140))
            }
            val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(190, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(130))
            }
            AnimatedNavHost(
                navController = nav,
                startDestination = "dashboard",
                modifier = Modifier.padding(paddingValues),
                enterTransition = enter,
                exitTransition = exit,
                popEnterTransition = popEnter,
                popExitTransition = popExit
            ) {
                composable("dashboard") { DashboardScreen(rootNav) }
                composable("cropHealth") { 
                    CropHealthScreen(
                        navController = nav, 
                        vm = cropHealthViewModel,
                        onNewDetection = { rootNav.navigate("detection") }
                    ) 
                }
                
                // Crop Health Detail Screen
                composable(
                    route = "crop_health_detail/{recordId}",
                    arguments = listOf(navArgument("recordId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val recordId = backStackEntry.arguments?.getInt("recordId") ?: 0
                    val record = cropHealthViewModel.getRecordById(recordId)
                    
                    record?.let {
                        CropHealthDetailScreen(navController = nav, record = it, viewModel = cropHealthViewModel)
                    } ?: run {
                        // Handle case where record is not found
                        LaunchedEffect(Unit) {
                            nav.popBackStack()
                        }
                    }
                }
                
                // Crop Health Comments Screen
                composable(
                    route = "crop_health_comments/{recordId}",
                    arguments = listOf(navArgument("recordId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val recordId = backStackEntry.arguments?.getInt("recordId") ?: 0
                    // TODO: Create CropHealthCommentsScreen or use existing comments dialog
                    // For now, navigating back
                    LaunchedEffect(Unit) {
                        nav.popBackStack()
                    }
                }
                
                composable("irrigation") { IrrigationScheduleScreen() }
                composable("tasks") { TasksScreen() }
                composable("events") { EventsScreen(nav) }
                composable(
                    "eventDetails/{eventId}",
                    arguments = listOf(androidx.navigation.navArgument("eventId") { type = androidx.navigation.NavType.IntType })
                ) { backStack ->
                    val id = backStack.arguments?.getInt("eventId") ?: 0
                    EventDetailsScreen(eventId = id, onBack = { nav.popBackStack() })
                }
                composable("notifications") { NotificationsScreen() }
                composable("settings") { SettingsScreen() }
            }
        }
    }
}

@Composable
private fun EnhancedDrawerContent(
    items: List<HomeItem>,
    onSelect: (HomeItem) -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(AppDimensions.drawerWidth())
    ) {
        // Header with gradient - Using custom app icon
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppDimensions.drawerHeaderHeight())
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                )
                .padding(AppDimensions.paddingLarge()),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(AppDimensions.drawerLogoSize())
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            CircleShape
                        )
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Using Image instead of Icon to support adaptive icons
                    AppIcon(
                        modifier = Modifier
                            .size(AppDimensions.logoSizeSmall())
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))

                Text(
                    text = "SumviltadConnect",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Smart Rice Farming",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))

        // Navigation items
        items.forEach { item ->
            NavigationDrawerItem(
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = null,
                        modifier = Modifier.size(AppDimensions.iconSizeMedium())
                    )
                },
                label = {
                    Text(
                        item.label,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                selected = false,
                onClick = { onSelect(item) },
                modifier = Modifier.padding(horizontal = AppDimensions.paddingSmall(), vertical = AppDimensions.paddingExtraSmall()),
                shape = RoundedCornerShape(AppDimensions.cornerRadiusSmall())
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Logout button
        NavigationDrawerItem(
            icon = {
                Icon(
                    Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            label = {
                Text(
                    "Logout",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            },
            selected = false,
            onClick = onLogout,
            modifier = Modifier.padding(horizontal = AppDimensions.paddingSmall(), vertical = AppDimensions.paddingSmall()),
            shape = RoundedCornerShape(AppDimensions.cornerRadiusSmall())
        )

        Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedTopAppBar(
    onMenuClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 2.dp,
        tonalElevation = 0.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppDimensions.topAppBarHeight())
                .padding(horizontal = AppDimensions.paddingSmall(), vertical = AppDimensions.paddingSmall()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side: Menu button
            Box(
                modifier = Modifier
                    .size(AppDimensions.buttonHeight())
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(AppDimensions.buttonHeight())
                ) {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(AppDimensions.iconSizeMedium())
                    )
                }
            }

            // Center: Logo and Title
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(AppDimensions.logoSizeSmall())
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    AppIcon(
                        modifier = Modifier
                            .size(AppDimensions.iconSizeLarge())
                            .clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(AppDimensions.paddingSmall()))
                Text(
                    "SumviltadConnect",
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 0.5.sp
                )
            }

            // Right side: Profile button
            Box(
                modifier = Modifier
                    .size(AppDimensions.buttonHeight())
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.size(AppDimensions.buttonHeight())
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(AppDimensions.iconSizeMedium())
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedFAB(onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    FloatingActionButton(
        onClick = {
            isPressed = true
            onClick()
            isPressed = false
        },
        modifier = Modifier
            .scale(scale)
            .size(AppDimensions.fabSize()),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.BugReport,
                contentDescription = "Detect Disease",
                modifier = Modifier.size(AppDimensions.iconSizeMedium())
            )
            Text(
                "Detect",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
    
    // Show update dialog if available
    if (showUpdateDialog && availableUpdate != null) {
        UpdateAvailableDialog(
            appVersion = availableUpdate!!,
            onDismiss = { showUpdateDialog = false }
        )
    }
}


