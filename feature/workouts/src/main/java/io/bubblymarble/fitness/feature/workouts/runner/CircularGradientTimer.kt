package io.bubblymarble.fitness.feature.workouts.runner

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.bubblymarble.fitness.core.designsystem.theme.MonoNumericStyle

/**
 * Circular countdown ring for rest periods. The ring sweeps clockwise from 12-o'clock,
 * coloured by a sweep gradient (sky → teal → emerald). Progress animates between
 * 1-second ticks so the motion is smooth, not steppy.
 */
@Composable
fun CircularGradientTimer(
    secondsLeft: Int,
    totalSeconds: Int,
    modifier: Modifier = Modifier,
    diameter: androidx.compose.ui.unit.Dp = 200.dp,
    stroke: androidx.compose.ui.unit.Dp = 14.dp,
) {
    val target = if (totalSeconds <= 0) 0f else secondsLeft.coerceAtLeast(0).toFloat() / totalSeconds
    val progress = remember { Animatable(target) }
    LaunchedEffect(secondsLeft, totalSeconds) {
        progress.animateTo(target, animationSpec = tween(durationMillis = 950))
    }

    val track = MaterialTheme.colorScheme.surfaceContainerHigh
    val gradient = Brush.sweepGradient(
        listOf(
            Color(0xFF38BDF8), // sky-400
            Color(0xFF2DD4BF), // teal-400
            Color(0xFF34D399), // emerald-400
            Color(0xFF38BDF8), // close the loop
        ),
    )

    Box(modifier = modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(diameter)) {
            val strokePx = stroke.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(size.width - strokePx, size.height - strokePx)
            val topLeft = Offset(inset, inset)
            // Track
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
            // Progress, clockwise from 12-o'clock
            val sweep = 360f * progress.value
            drawArc(
                brush = gradient,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
        }
        Text(
            text = "${secondsLeft.coerceAtLeast(0)}s",
            style = MaterialTheme.typography.displaySmall.merge(MonoNumericStyle),
            fontWeight = FontWeight.Medium,
        )
    }
}
