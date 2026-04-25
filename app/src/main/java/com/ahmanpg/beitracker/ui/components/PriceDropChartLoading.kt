package com.ahmanpg.beitracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ahmanpg.beitracker.ui.theme.BeiAccentGreen
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PriceDropChartLoading(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    color: Color = BeiAccentGreen
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PriceDropLoading")

    // Smooth progress (not linear → feels premium)
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Progress"
    )

    // Subtle glow pulse
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Glow"
    )

    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = this.size.width
            val height = this.size.height

            val strokeWidth = 3.dp.toPx()

            // Define points for a "downward trending chart"
            val points = listOf(
                Offset(0f, height * 0.2f),
                Offset(width * 0.25f, height * 0.4f),
                Offset(width * 0.5f, height * 0.35f),
                Offset(width * 0.75f, height * 0.7f),
                Offset(width, height * 0.9f)
            )

            val path = Path()
            path.moveTo(points[0].x, points[0].y)

            val currentProgress = progress * (points.size - 1)

            for (i in 1 until points.size) {
                if (i <= currentProgress) {
                    path.lineTo(points[i].x, points[i].y)
                } else if (i - 1 < currentProgress) {
                    val segmentProgress = currentProgress - (i - 1)
                    val x = points[i - 1].x + (points[i].x - points[i - 1].x) * segmentProgress
                    val y = points[i - 1].y + (points[i].y - points[i - 1].y) * segmentProgress
                    path.lineTo(x, y)
                }
            }

            // 🌈 Gradient stroke (glass feel)
            val gradient = Brush.linearGradient(
                colors = listOf(
                    color.copy(alpha = 0.2f),
                    color.copy(alpha = 0.6f),
                    color
                )
            )

            // 💡 Glow layer (blur illusion)
            drawPath(
                path = path,
                brush = gradient,
                style = Stroke(
                    width = strokeWidth * 2.5f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                ),
                alpha = glowAlpha * 0.4f
            )

            // ✨ Main sharp stroke
            drawPath(
                path = path,
                brush = gradient,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // 🎯 Arrow head
            if (progress > 0.1f) {
                val lastIdx = currentProgress.toInt().coerceIn(0, points.size - 2)
                val segmentProgress = currentProgress - lastIdx

                val currentX = points[lastIdx].x +
                        (points[lastIdx + 1].x - points[lastIdx].x) * segmentProgress

                val currentY = points[lastIdx].y +
                        (points[lastIdx + 1].y - points[lastIdx].y) * segmentProgress

                val angle = atan2(
                    (points[lastIdx + 1].y - points[lastIdx].y).toDouble(),
                    (points[lastIdx + 1].x - points[lastIdx].x).toDouble()
                ).toFloat()

                val arrowSize = 10.dp.toPx()

                val arrowPath = Path().apply {
                    moveTo(currentX, currentY)
                    lineTo(
                        currentX - arrowSize * cos(angle - Math.PI / 6).toFloat(),
                        currentY - arrowSize * sin(angle - Math.PI / 6).toFloat()
                    )
                    moveTo(currentX, currentY)
                    lineTo(
                        currentX - arrowSize * cos(angle + Math.PI / 6).toFloat(),
                        currentY - arrowSize * sin(angle + Math.PI / 6).toFloat()
                    )
                }

                drawPath(
                    path = arrowPath,
                    color = color,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    alpha = 0.9f
                )
            }
        }
    }
}
