package com.zynt.sumviltadconnect.ui.screens

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.zynt.sumviltadconnect.R
import com.zynt.sumviltadconnect.data.model.DashboardSummary
import com.zynt.sumviltadconnect.ui.components.FullScreenSkeleton
import com.zynt.sumviltadconnect.ui.theme.AppDimensions
import com.zynt.sumviltadconnect.ui.viewmodel.DashboardUiState
import com.zynt.sumviltadconnect.ui.viewmodel.DashboardViewModel
import com.zynt.sumviltadconnect.utils.TokenManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
            modifier = Modifier.size(AppDimensions.fabSize()).clip(CircleShape)
        )
    } else {
        // Fallback to Material Icon
        Icon(
            Icons.Default.Dashboard,
            contentDescription = "App Logo",
            tint = Color.White,
            modifier = modifier
        )
    }
}

@Composable
fun DashboardScreen(
    navController: NavController,
    vm: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val userName = TokenManager.getUserName(context) ?: "Farmer"
    val state by vm.state.collectAsState()

    // API debugging
    LaunchedEffect(Unit) {
        com.zynt.sumviltadconnect.utils.ApiDebugHelper.logApiConfiguration(context)
        com.zynt.sumviltadconnect.utils.ApiDebugHelper.testApiConnectivity(context)
    }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .verticalScroll(rememberScrollState())
    ) {
        EnhancedHeader(userName = userName)
        Spacer(modifier = Modifier.height(AppDimensions.paddingLarge()))

        when (state) {
            is DashboardUiState.Loading -> DashboardSkeleton()
            is DashboardUiState.Error -> {
                val errorState = state as DashboardUiState.Error
                EnhancedErrorState(
                    message = errorState.message,
                    onRetry = { vm.refresh() }
                )
            }
            is DashboardUiState.Success -> {
                val successState = state as DashboardUiState.Success
                EnhancedDashboardContent(
                    data = successState.data,
                    isVisible = isVisible,
                    onNavigate = { route: String -> 
                        try {
                            navController.navigate(route)
                        } catch (e: Exception) {
                            Log.e("DashboardScreen", "Navigation error to $route", e)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(AppDimensions.fabSize()))
    }
}

@Composable
private fun EnhancedHeader(userName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimensions.paddingMedium(), vertical = AppDimensions.paddingSmall()),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimensions.paddingSmall()),
        shape = RoundedCornerShape(AppDimensions.paddingLarge())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
                        )
                    )
                )
                .padding(AppDimensions.paddingLarge())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Good day,", fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))
                    Text(
                        text = userName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Monitor your farm's health",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(AppDimensions.buttonHeight())
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Using AppIcon helper that safely loads the icon
                    AppIcon(
                        modifier = Modifier.size(AppDimensions.iconSizeLarge())
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardSkeleton() {
    Box(Modifier.fillMaxSize()) {
        FullScreenSkeleton(listItems = 6)
        Column(
            Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(AppDimensions.paddingMedium()))
            Text("Loading dashboard...", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun EnhancedErrorState(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppDimensions.paddingMedium()),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimensions.cardElevation()),
        shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium())
    ) {
        Column(
            modifier = Modifier.padding(AppDimensions.paddingLarge()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(AppDimensions.iconSizeLarge())
            )
            Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
            Text(
                text = "Error loading dashboard",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))
            Text(
                text = message,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EnhancedDashboardContent(
    data: DashboardSummary,
    isVisible: Boolean,
    onNavigate: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = AppDimensions.paddingMedium())) {
        Text(
            text = "Farm Overview",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = AppDimensions.paddingMedium())
        )

        Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))

        // First Row - Staggered Animation
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = tween(300, easing = FastOutSlowInEasing, delayMillis = 50)
            ) + fadeIn(animationSpec = tween(300, delayMillis = 50))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.paddingMedium())
            ) {
                EnhancedStatCard(
                    title = "Total Analysis",
                    value = data.totalAnalyses,
                    icon = Icons.Default.Analytics,
                    color = Color(0xFF4CAF50),
                    supportingText = "Reports logged",
                    modifier = Modifier.weight(1f),
                    onClick = {},
                    animationDelay = 0
                )
                EnhancedStatCard(
                    title = "Expert Reviewed",
                    value = data.expertReviewed,
                    icon = Icons.Default.Verified,
                    color = Color(0xFF2196F3),
                    supportingText = "Validated cases",
                    modifier = Modifier.weight(1f),
                    onClick = {},
                    animationDelay = 100
                )
            }
        }

        Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))

        // Second Row - Staggered Animation
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = tween(300, easing = FastOutSlowInEasing, delayMillis = 150)
            ) + fadeIn(animationSpec = tween(300, delayMillis = 150))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.paddingMedium())
            ) {
                EnhancedStatCard(
                    title = "Pending Review",
                    value = data.pendingReview,
                    icon = Icons.Default.Pending,
                    color = Color(0xFFFF9800),
                    supportingText = "Awaiting expert check",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("cropHealth") },
                    animationDelay = 200
                )
                EnhancedStatCard(
                    title = "Task Progress",
                    value = data.taskProgress,
                    icon = Icons.Default.AssignmentTurnedIn,
                    color = Color(0xFF9C27B0),
                    supportingText = "Field plan status",
                    valueSuffix = "%",
                    progressFraction = data.taskProgress.coerceIn(0, 100) / 100f,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("tasks") },
                    animationDelay = 300
                )
            }
        }

        Spacer(modifier = Modifier.height(AppDimensions.paddingLarge()))

        // Disease Distribution Chart with Animation
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = tween(300, easing = FastOutSlowInEasing, delayMillis = 250)
            ) + fadeIn(animationSpec = tween(300, delayMillis = 250))
        ) {
            Column {
                Text(
                    text = "Disease Distribution",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = AppDimensions.paddingMedium())
                )
                EnhancedChartCard {
                    DiseaseDistributionBarChartVico(data.diseaseDistribution)
                }
            }
        }

        Spacer(modifier = Modifier.height(AppDimensions.paddingLarge()))

        // Monthly Trend Chart with Animation
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = tween(300, easing = FastOutSlowInEasing, delayMillis = 350)
            ) + fadeIn(animationSpec = tween(300, delayMillis = 350))
        ) {
            Column {
                Text(
                    text = "Monthly Trend",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = AppDimensions.paddingMedium())
                )
                EnhancedChartCard {
                    MonthlyTrendLineChartVico(data.monthlyTrend)
                }
            }
        }

        Spacer(modifier = Modifier.height(AppDimensions.paddingLarge()))
    }
}

private data class QuickStat(
    val icon: ImageVector,
    val value: Int,
    val label: String,
    val accent: Color,
    val route: String
)

@Composable
private fun OverviewQuickStatsRow(
    pendingTasks: Int,
    upcomingEvents: Int,
    onNavigate: (String) -> Unit
) {
    val quickStats = listOf(
        QuickStat(Icons.Default.EventAvailable, upcomingEvents, "Upcoming events", Color(0xFF2E7D32), "events"),
        QuickStat(Icons.Default.TaskAlt, pendingTasks, "Pending tasks", Color(0xFFEF6C00), "tasks")
    )

    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(AppDimensions.paddingSmall())
    ) {
        quickStats.forEach { stat ->
            OverviewQuickStatChip(
                icon = stat.icon,
                value = stat.value,
                label = stat.label,
                accent = stat.accent,
                onClick = { onNavigate(stat.route) }
            )
        }
    }
}

@Composable
private fun OverviewQuickStatChip(
    icon: ImageVector,
    value: Int,
    label: String,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        border = BorderStroke(1.dp, accent.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(32.dp),
        color = accent.copy(alpha = 0.08f),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = AppDimensions.paddingMedium(), vertical = AppDimensions.paddingSmall()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimensions.paddingSmall())
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(AppDimensions.iconSizeMedium())
            )
            Column {
                Text(
                    text = value.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EnhancedStatCard(
    title: String,
    value: Int,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    animationDelay: Int = 0,
    supportingText: String? = null,
    valueSuffix: String? = null,
    progressFraction: Float? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelay.toLong())
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "alpha"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .graphicsLayer { this.alpha = alpha }
            .clickable {
                isPressed = true
                onClick()
                isPressed = false
            },
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimensions.cardElevation()),
        shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppDimensions.cornerRadiusMedium()))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            color.copy(alpha = 0.18f),
                            color.copy(alpha = 0.04f)
                        )
                    )
                )
                .padding(AppDimensions.paddingMedium())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(AppDimensions.iconSizeLarge())
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    color.copy(alpha = 0.2f),
                                    color.copy(alpha = 0.05f)
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(AppDimensions.iconSizeMedium())
                    )
                }

                Text(
                    text = valueSuffix?.let { "$value$it" } ?: value.toString(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))

            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            supportingText?.let {
                Spacer(modifier = Modifier.height(AppDimensions.paddingExtraSmall()))
                Text(
                    text = it,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }

            progressFraction?.let { fraction ->
                Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
                LinearProgressIndicator(
                    progress = fraction.coerceIn(0f, 1f),
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    color = color,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun EnhancedChartCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimensions.cardElevation()),
        shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimensions.paddingMedium())
        ) {
            content()
        }
    }
}

@Composable
private fun DiseaseDistributionBarChartVico(diseaseDistribution: Map<String, Int>) {
    if (diseaseDistribution.isEmpty()) {
        EmptyChartState(
            icon = Icons.Default.BarChart,
            message = "No disease data available yet",
            description = "Disease distribution will appear here once you start analyzing crops"
        )
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface

    var animationPlayed by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "barHeight"
    )
    LaunchedEffect(Unit) { animationPlayed = true }

    val maxValue = remember(diseaseDistribution) { diseaseDistribution.values.maxOrNull() ?: 0 }
    val labels = remember(diseaseDistribution) { diseaseDistribution.keys.toList() }
    val values = remember(diseaseDistribution) { diseaseDistribution.values.toList() }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppDimensions.detectionImageHeight())
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        val barSpacing = size.width / (labels.size * 2f)
        val barWidth = barSpacing * 0.85f
        val chartBottom = size.height - 36.dp.toPx()
        val maxBarHeight = chartBottom - 20.dp.toPx()

        labels.forEachIndexed { index, label ->
            val value = values[index]
            val normalizedHeight = if (maxValue > 0) value / maxValue.toFloat() else 0f
            val barHeight = maxBarHeight * normalizedHeight * progress

            val x = (index * (barWidth + barSpacing)) + barSpacing / 2
            val topY = chartBottom - barHeight

            // Draw subtle background track
            drawRoundRect(
                color = primaryColor.copy(alpha = 0.08f),
                topLeft = Offset(x, chartBottom - maxBarHeight),
                size = Size(barWidth, maxBarHeight),
                cornerRadius = CornerRadius(10.dp.toPx())
            )

            // Draw animated bar
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor, primaryColor.copy(alpha = 0.55f)),
                    startY = topY,
                    endY = chartBottom
                ),
                topLeft = Offset(x, topY),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(10.dp.toPx())
            )

            // Value label
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    value.toString(),
                    x + barWidth / 2,
                    topY - 6.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = onSurface.toArgb()
                        textSize = 12.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                        isFakeBoldText = true
                    }
                )
            }

            // Rotated x-axis label for readability
            drawContext.canvas.nativeCanvas.apply {
                save()
                rotate(-35f, x + barWidth / 2, size.height - 8.dp.toPx())
                drawText(
                    label,
                    x + barWidth / 2,
                    size.height - 8.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = onSurface.copy(alpha = 0.75f).toArgb()
                        textSize = 11.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                )
                restore()
            }
        }
    }
}

@Composable
private fun MonthlyTrendLineChartVico(monthlyTrend: List<Int>) {
    if (monthlyTrend.isEmpty()) {
        EmptyChartState(
            icon = Icons.Default.ShowChart,
            message = "No trend data available yet",
            description = "Monthly analysis trends will be displayed here"
        )
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val outline = MaterialTheme.colorScheme.outlineVariant

    var animationPlayed by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "lineProgress"
    )
    LaunchedEffect(Unit) { animationPlayed = true }

    val labels = remember(monthlyTrend.size) {
        List(monthlyTrend.size) { index ->
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, -(monthlyTrend.size - 1 - index))
            SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)
        }
    }

    val maxValue = remember(monthlyTrend) { (monthlyTrend.maxOrNull() ?: 0).coerceAtLeast(1) }
    val minValue = remember(monthlyTrend) { monthlyTrend.minOrNull() ?: 0 }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppDimensions.detectionImageHeight())
            .padding(horizontal = 12.dp, vertical = 16.dp)
    ) {
        val width = size.width
        val height = size.height - 32.dp.toPx()
        val chartBottom = height
        val chartTop = 16.dp.toPx()
        val chartHeight = chartBottom - chartTop
        val stepX = width / (monthlyTrend.size - 1).coerceAtLeast(1)

        val points = monthlyTrend.mapIndexed { index, value ->
            val normalized = if (maxValue == minValue) 0.5f else (value - minValue) / (maxValue - minValue).toFloat()
            val y = chartBottom - (chartHeight * normalized)
            Offset(index * stepX, y)
        }

        if (points.isNotEmpty()) {
            val linePath = Path().apply { moveTo(points.first().x, points.first().y) }
            val fillPath = Path().apply {
                moveTo(points.first().x, chartBottom)
                lineTo(points.first().x, points.first().y)
            }

            for (i in 0 until points.lastIndex) {
                val current = points[i]
                val next = points[i + 1]
                val controlX = (current.x + next.x) / 2

                linePath.cubicTo(controlX, current.y, controlX, next.y, next.x, next.y)
                fillPath.cubicTo(controlX, current.y, controlX, next.y, next.x, next.y)
            }

            fillPath.lineTo(points.last().x, chartBottom)
            fillPath.close()

            // horizontal grid lines
            val gridLines = 4
            repeat(gridLines + 1) { idx ->
                val y = chartTop + (chartHeight / gridLines) * idx
                drawLine(
                    color = outline.copy(alpha = 0.25f),
                    start = Offset(0f, y),
                    end = Offset(width * progress, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Fill under curve
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.35f), Color.Transparent),
                    startY = chartTop,
                    endY = chartBottom
                ),
                alpha = progress
            )

            // Curve
            drawPath(
                path = linePath,
                color = primaryColor,
                style = Stroke(width = 3.dp.toPx()),
                alpha = progress
            )

            points.forEachIndexed { index, point ->
                if (index <= (points.lastIndex * progress).toInt()) {
                    drawCircle(color = primaryColor.copy(alpha = 0.2f), radius = 8.dp.toPx(), center = point)
                    drawCircle(color = primaryColor, radius = 5.dp.toPx(), center = point)
                    drawCircle(color = Color.White, radius = 2.dp.toPx(), center = point)

                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            monthlyTrend[index].toString(),
                            point.x,
                            point.y - 10.dp.toPx(),
                            android.graphics.Paint().apply {
                                color = onSurface.toArgb()
                                textSize = 11.sp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = true
                                isAntiAlias = true
                            }
                        )

                        drawText(
                            labels[index],
                            point.x,
                            size.height,
                            android.graphics.Paint().apply {
                                color = onSurface.copy(alpha = 0.75f).toArgb()
                                textSize = 11.sp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyChartState(
    icon: ImageVector,
    message: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppDimensions.detectionImageHeight())
            .padding(AppDimensions.paddingLarge()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(AppDimensions.buttonHeight())
                .background(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(AppDimensions.iconSizeMedium())
            )
        }
        Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
        Text(
            text = message,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(AppDimensions.paddingExtraSmall()))
        Text(
            text = description,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
