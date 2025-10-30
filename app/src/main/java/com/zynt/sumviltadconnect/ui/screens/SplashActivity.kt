package com.zynt.sumviltadconnect.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import androidx.core.content.ContextCompat
import com.zynt.sumviltadconnect.MainActivity
import com.zynt.sumviltadconnect.R
import com.zynt.sumviltadconnect.ui.theme.SumviltadConnectTheme
import kotlinx.coroutines.delay

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
            modifier = modifier.clip(CircleShape)
        )
    } else {
        // Fallback to drawable logo
        Image(
            painter = painterResource(id = R.drawable.ic_app_logo),
            contentDescription = "App Logo",
            modifier = modifier
        )
    }
}

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SumviltadConnectTheme {
                SplashScreen {
                    // Check if user is logged in using TokenManager
                    val isLoggedIn = com.zynt.sumviltadconnect.utils.TokenManager.isLoggedIn(this)

                    // Pass login state to MainActivity
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("is_logged_in", isLoggedIn)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    var loadingProgress by remember { mutableStateOf(0f) }

    // Logo bounce animation
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )

    // Logo fade animation
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "logoAlpha"
    )

    // Rotating animation for loading indicator
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Text pulsing animation
    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "textPulse"
    )

    LaunchedEffect(Unit) {
        startAnimation = true

        // Simulate loading progress - faster loading (1.5 seconds instead of 2.5)
        for (i in 0..100) {
            loadingProgress = i / 100f
            delay(15) // Reduced from 25ms to 15ms = 1.5 seconds total
        }

        delay(200) // Reduced from 500ms to 200ms extra delay
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // App Logo with animations using the actual launcher icon
            AppIcon(
                modifier = Modifier
                    .size(110.dp)
                    .scale(scale)
                    .alpha(alpha)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // App Name
            Text(
                text = "Sumviltad Connect",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.alpha(alpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Agricultural Intelligence Platform",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(alpha * textAlpha)
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Loading Progress Bar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .alpha(alpha)
            ) {
                // Progress bar
                LinearProgressIndicator(
                    progress = { loadingProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Loading text
                Text(
                    text = when {
                        loadingProgress < 0.33f -> "Initializing..."
                        loadingProgress < 0.66f -> "Loading resources..."
                        loadingProgress < 1f -> "Almost ready..."
                        else -> "Welcome!"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.alpha(textAlpha)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Percentage
                Text(
                    text = "${(loadingProgress * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Animated loading dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.alpha(alpha)
            ) {
                repeat(3) { index ->
                    LoadingDot(
                        delay = index * 200,
                        alpha = textAlpha
                    )
                }
            }
        }

        // Bottom branding
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(alpha * 0.8f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Powered by AI",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "v1.0.0",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun LoadingDot(delay: Int, alpha: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "dotAnimation")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = delay, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotScale"
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .scale(scale)
            .alpha(alpha)
            .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
    )
}
