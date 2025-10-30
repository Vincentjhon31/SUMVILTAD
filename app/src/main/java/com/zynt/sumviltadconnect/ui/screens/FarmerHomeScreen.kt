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
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.zynt.sumviltadconnect.R
import com.zynt.sumviltadconnect.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import androidx.navigation.NavBackStackEntry
import androidx.core.content.ContextCompat

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

    val bottomItems = listOf(
        HomeItem.Dashboard,
        HomeItem.CropHealth,
        HomeItem.Irrigation,
        HomeItem.Tasks,
        HomeItem.Events
    )

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
            },
            bottomBar = {
                EnhancedBottomNavigation(
                    items = bottomItems,
                    navController = nav
                )
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
                composable("cropHealth") { CropHealthScreen(rootNav) }
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
        modifier = Modifier.width(280.dp)
    ) {
        // Header with gradient - Using custom app icon
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            CircleShape
                        )
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Using Image instead of Icon to support adaptive icons
                    AppIcon(
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation items
        items.forEach { item ->
            NavigationDrawerItem(
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
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
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
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
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
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
                .height(64.dp)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side: Menu button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
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
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    AppIcon(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
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
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
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
            .size(64.dp),
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
                modifier = Modifier.size(28.dp)
            )
            Text(
                "Detect",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EnhancedBottomNavigation(
    items: List<HomeItem>,
    navController: NavController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true

            NavigationBarItem(
                icon = {
                    AnimatedContent(
                        targetState = isSelected,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(180)) with fadeOut(animationSpec = tween(180))
                        }, label = "bottomNavIcon"
                    ) { selected ->
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    item.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            Icon(
                                item.icon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                label = {
                    Text(
                        item.label.split(" ").first(), // Show only first word for space
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
