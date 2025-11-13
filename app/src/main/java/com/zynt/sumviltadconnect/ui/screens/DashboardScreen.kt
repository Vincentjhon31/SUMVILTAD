package com.zynt.sumviltadconnect.ui.screens

import android.graphics.Color as AndroidColor
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.zynt.sumviltadconnect.ui.theme.AppDimensions
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.Entry
import com.zynt.sumviltadconnect.R
import com.zynt.sumviltadconnect.data.model.DashboardSummary
import com.zynt.sumviltadconnect.ui.components.FullScreenSkeleton
import com.zynt.sumviltadconnect.ui.viewmodel.DashboardUiState
import com.zynt.sumviltadconnect.ui.viewmodel.DashboardViewModel
import com.zynt.sumviltadconnect.utils.TokenManager
import androidx.compose.ui.viewinterop.AndroidView
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
                    onNavigate = { route: String -> navController.navigate(route) }
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
            .padding(horizontal = AppDimensions.paddingMedium()),
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
                    title = "Total Analyses",
                    value = data.totalAnalyses,
                    icon = Icons.Default.Analytics,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f),
                    onClick = {},
                    animationDelay = 0
                )
                EnhancedStatCard(
                    title = "Expert Reviewed",
                    value = data.expertReviewed,
                    icon = Icons.Default.Verified,
                    color = Color(0xFF2196F3),
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
                    modifier = Modifier.weight(1f),
                    onClick = {},
                    animationDelay = 200
                )
                EnhancedStatCard(
                    title = "Task Progress",
                    value = data.taskProgress,
                    icon = Icons.Default.AssignmentTurnedIn,
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f),
                    onClick = {},
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

@Composable
private fun EnhancedStatCard(
    title: String,
    value: Int,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    animationDelay: Int = 0
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
        Column(modifier = Modifier.padding(AppDimensions.paddingMedium())) {
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
                    text = value.toString(),
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

    // Get theme-aware colors that update with theme changes
    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val textColor = onSurfaceColor.toArgb()
    val axisColor = if (isDarkTheme) AndroidColor.LTGRAY else AndroidColor.DKGRAY
    val gridColor = if (isDarkTheme) AndroidColor.rgb(80, 80, 80) else AndroidColor.LTGRAY

    val entries = remember(diseaseDistribution) {
        diseaseDistribution.entries.mapIndexed { idx: Int, entry: Map.Entry<String, Int> ->
            BarEntry(idx.toFloat(), entry.value.toFloat())
        }
    }
    val labels = remember(diseaseDistribution) {
        diseaseDistribution.keys.toList()
    }

    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                val dataSet = BarDataSet(entries, "Disease Distribution")
                dataSet.color = AndroidColor.rgb(76, 175, 80)
                dataSet.valueTextColor = textColor
                dataSet.valueTextSize = 11f
                val barData = BarData(dataSet)
                this.data = barData
                this.setFitBars(true)
                this.setDrawValueAboveBar(true)
                this.axisLeft.axisMinimum = 0f
                this.axisLeft.setTextColor(axisColor)
                this.axisLeft.setGridColor(gridColor)
                this.axisRight.isEnabled = false
                this.xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels)
                this.xAxis.granularity = 1f
                this.xAxis.setDrawGridLines(false)
                this.xAxis.labelRotationAngle = -45f
                this.xAxis.textSize = 10f
                this.xAxis.setTextColor(axisColor)
                this.description = Description().apply { text = "" }
                this.legend.isEnabled = false

                // Set background transparent
                this.setBackgroundColor(AndroidColor.TRANSPARENT)

                // Animate the chart
                this.animateY(800, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)

                this.invalidate()
            }
        },
        update = { chart ->
            // Update colors when theme changes
            val dataSet = chart.data?.getDataSetByIndex(0) as? BarDataSet
            dataSet?.valueTextColor = textColor
            chart.xAxis.setTextColor(axisColor)
            chart.axisLeft.setTextColor(axisColor)
            chart.axisLeft.setGridColor(gridColor)
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(AppDimensions.detectionImageHeight())
    )
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

    // Get theme-aware colors that update with theme changes
    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val textColor = onSurfaceColor.toArgb()
    val axisColor = if (isDarkTheme) AndroidColor.LTGRAY else AndroidColor.DKGRAY
    val gridColor = if (isDarkTheme) AndroidColor.rgb(80, 80, 80) else AndroidColor.LTGRAY
    val legendColor = if (isDarkTheme) AndroidColor.LTGRAY else AndroidColor.DKGRAY

    // Generate month labels if not provided (fallback)
    val monthLabels = remember(monthlyTrend.size) {
        List(monthlyTrend.size) { index ->
            val calendar = java.util.Calendar.getInstance()
            calendar.add(java.util.Calendar.MONTH, -(monthlyTrend.size - 1 - index))
            val monthName = java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault())
                .format(calendar.time)
            val year = calendar.get(java.util.Calendar.YEAR)
            "$monthName $year"
        }
    }

    val entries = remember(monthlyTrend) {
        monthlyTrend.mapIndexed { index: Int, value: Int ->
            Entry(index.toFloat(), value.toFloat())
        }
    }

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                val dataSet = LineDataSet(entries, "Monthly Analyses")

                // Styling
                dataSet.color = AndroidColor.rgb(33, 150, 243)
                dataSet.valueTextColor = textColor
                dataSet.setCircleColor(AndroidColor.rgb(33, 150, 243))
                dataSet.circleRadius = 5f
                dataSet.lineWidth = 3f
                dataSet.setDrawFilled(true)
                dataSet.fillColor = AndroidColor.rgb(33, 150, 243)
                dataSet.fillAlpha = 50
                dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
                dataSet.cubicIntensity = 0.2f
                dataSet.setDrawValues(true)
                dataSet.valueTextSize = 10f

                // Enable highlighting
                dataSet.setDrawHighlightIndicators(true)
                dataSet.highlightLineWidth = 2f
                dataSet.highLightColor = AndroidColor.rgb(255, 152, 0)

                val lineData = LineData(dataSet)
                this.data = lineData

                // X-Axis configuration with month labels
                this.xAxis.apply {
                    position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                    valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(monthLabels)
                    granularity = 1f
                    setDrawGridLines(false)
                    labelRotationAngle = -45f
                    textSize = 10f
                    setTextColor(axisColor)
                    setAvoidFirstLastClipping(true)
                }

                // Y-Axis configuration
                this.axisLeft.apply {
                    axisMinimum = 0f
                    granularity = 1f
                    textSize = 10f
                    setTextColor(axisColor)
                    setDrawGridLines(true)
                    setGridColor(gridColor)
                    gridLineWidth = 0.5f
                }

                this.axisRight.isEnabled = false

                // Description
                this.description = Description().apply {
                    text = ""
                    textSize = 9f
                    setTextColor(legendColor)
                }

                // Legend
                this.legend.apply {
                    isEnabled = true
                    textSize = 11f
                    setTextColor(legendColor)
                    verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
                    horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT
                }

                // Set background transparent
                this.setBackgroundColor(AndroidColor.TRANSPARENT)

                // Enable touch interactions
                this.setTouchEnabled(true)
                this.isDragEnabled = true
                this.setScaleEnabled(false)
                this.setPinchZoom(false)
                this.isDoubleTapToZoomEnabled = false

                // Enable value highlighting on touch
                this.isHighlightPerTapEnabled = true
                this.isHighlightPerDragEnabled = false

                // Animate the chart
                this.animateX(1000, com.github.mikephil.charting.animation.Easing.EaseInOutCubic)

                this.invalidate()
            }
        },
        update = { chart ->
            // Update colors when theme changes
            val dataSet = chart.data?.getDataSetByIndex(0) as? LineDataSet
            dataSet?.valueTextColor = textColor
            chart.xAxis.setTextColor(axisColor)
            chart.axisLeft.setTextColor(axisColor)
            chart.axisLeft.setGridColor(gridColor)
            chart.legend.setTextColor(legendColor)
            chart.description?.setTextColor(legendColor)
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(AppDimensions.detectionImageHeight())
    )
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
