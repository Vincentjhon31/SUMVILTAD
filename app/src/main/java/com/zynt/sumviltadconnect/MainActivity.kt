package com.zynt.sumviltadconnect

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.google.firebase.messaging.FirebaseMessaging
import com.zynt.sumviltadconnect.data.network.ApiClient
import com.zynt.sumviltadconnect.ui.screens.*
import com.zynt.sumviltadconnect.ui.theme.SumviltadConnectTheme
import com.zynt.sumviltadconnect.ui.viewmodel.AuthViewModel
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Notification permission granted")
            // FCM SDK (and your app) can post notifications.
        } else {
            Log.d(TAG, "Notification permission denied")
            // TODO: Inform user that notifications are disabled
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize API client with context
        ApiClient.initialize(this)

        // Request notification permission for Android 13+
        askNotificationPermission()

        // Get FCM token - but don't send to server yet (will send after login)
        getFCMToken()

        // Get login status from splash screen intent
        val isLoggedInFromSplash = intent.getBooleanExtra("is_logged_in", false)

        // If user is already logged in, send FCM token to server
        if (isLoggedInFromSplash) {
            sendStoredFcmTokenToServer()
        }

        setContent {
            SumviltadConnectTheme {
                SumviltadConnectApp(initialLoginState = isLoggedInFromSplash)
            }
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
                Log.d(TAG, "Notification permission already granted")
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log the token
            Log.d(TAG, "FCM Token: $token")

            // Store token locally for later use
            saveFcmTokenLocally(token)
        }
    }

    private fun saveFcmTokenLocally(token: String) {
        val prefs = getSharedPreferences("sumviltad_prefs", MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()
        Log.d(TAG, "FCM token saved locally")
    }

    private fun sendStoredFcmTokenToServer() {
        val prefs = getSharedPreferences("sumviltad_prefs", MODE_PRIVATE)
        val token = prefs.getString("fcm_token", null)

        if (token != null) {
            sendTokenToServer(token)
        } else {
            Log.w(TAG, "No FCM token stored locally")
        }
    }

    private fun sendTokenToServer(token: String) {
        // Use a coroutine to send token to backend
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val authToken = getAuthToken()
                if (authToken != null) {
                    Log.d(TAG, "Sending FCM token to server...")
                    val response = ApiClient.apiService.storeFcmToken(
                        "Bearer $authToken",
                        mapOf("token" to token)
                    )
                    if (response.isSuccessful) {
                        Log.d(TAG, "✅ FCM token sent to server successfully")
                    } else {
                        Log.e(TAG, "❌ Failed to send FCM token to server: ${response.code()}")
                        Log.e(TAG, "Response: ${response.errorBody()?.string()}")
                    }
                } else {
                    Log.w(TAG, "⚠️ User not logged in, FCM token not sent to server")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error sending FCM token to server", e)
            }
        }
    }

    private fun getAuthToken(): String? {
        val prefs = getSharedPreferences("sumviltad_prefs", MODE_PRIVATE)
        return prefs.getString("auth_token", null)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SumviltadConnectApp(initialLoginState: Boolean = false) {
    val navController = rememberAnimatedNavController()
    val authViewModel: AuthViewModel = viewModel()

    // Check login status
    val context = androidx.compose.ui.platform.LocalContext.current

    // Set the initial auth state immediately to prevent flicker
    LaunchedEffect(initialLoginState) {
        if (initialLoginState) {
            authViewModel.setLoggedIn(true)
        }
    }

    LaunchedEffect(Unit) {
        authViewModel.checkLoginStatus(context)
    }

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState(initial = initialLoginState)

    // Use the initial login state for immediate routing to prevent showing register screen
    val startDestination = if (initialLoginState) "home" else "login"

    // Define transitions without unsupported initialOffset/targetOffset (removed overlap for compatibility)
    val enter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(220, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(180))
    }
    val exit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(220, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(150))
    }
    val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(200, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(160))
    }
    val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(200, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(140))
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        AnimatedNavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = enter,
            exitTransition = exit,
            popEnterTransition = popEnter,
            popExitTransition = popExit
        ) {
            composable("login") { LoginScreen(navController = navController, authViewModel = authViewModel) }
            composable("register") { RegisterScreen(navController = navController, authViewModel = authViewModel) }
            composable("home") { FarmerHomeScreen(rootNav = navController, authViewModel = authViewModel) }
            composable("main") { MainScreen(navController = navController, authViewModel = authViewModel) }
            composable("detection") { DiseaseDetectionScreen(navController = navController) }
            composable("history") { HistoryScreen(navController = navController) }
            composable("profile") { ProfileScreen(navController = navController, authViewModel = authViewModel) }
            composable("personal_information") { PersonalInformationScreen(navController = navController) }
        }
    }
}