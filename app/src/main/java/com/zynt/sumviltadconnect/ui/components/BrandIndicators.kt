package com.zynt.sumviltadconnect.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Custom brand-aligned progress indicator.
 * Duration kept tight for responsiveness (<250ms per rotation segment) while still smooth.
 */
@Composable
fun BrandProgressIndicator(
    size: Dp = 48.dp,
    stroke: Dp = 5.dp
) {
    val infinite = rememberInfiniteTransition(label = "brandProgress")
    val sweep by infinite.animateFloat(
        initialValue = 40f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ), label = "sweep"
    )
    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )

    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val gradient = listOf(primary, tertiary, primary.copy(alpha = 0.4f))

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background track
            drawArc(
                color = primary.copy(alpha = 0.12f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke.toPx(), cap = StrokeCap.Round)
            )
            // Animated arc (manual transform)
            withTransform({ rotate(degrees = rotation) }) {
                drawArc(
                    brush = Brush.sweepGradient(gradient),
                    startAngle = 0f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = stroke.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}

// Lightweight animated pulse used for skeleton elements
@Composable
private fun pulseAlpha(): Float {
    val infinite = rememberInfiniteTransition(label = "skeletonPulse")
    val alpha by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )
    return alpha
}

/** Skeleton place-holder for list items */
@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier,
    lines: Int = 3,
    includeAvatar: Boolean = true
) {
    val alpha = pulseAlpha()
    val baseColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f * alpha)
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (includeAvatar) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(baseColor)
                )
                Spacer(Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                repeat(lines) { index ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (index == lines - 1) 0.6f else 1f)
                            .height(14.dp)
                            .clip(CircleShape)
                            .background(baseColor)
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

/** Full page skeleton with brand gradient background */
@Composable
fun FullScreenSkeleton(listItems: Int = 5) {
    val bg = Brush.verticalGradient(
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            MaterialTheme.colorScheme.surface
        )
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(16.dp)
    ) {
        repeat(listItems) {
            SkeletonCard(modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
        }
    }
}
