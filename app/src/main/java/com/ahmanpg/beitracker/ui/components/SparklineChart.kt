package com.ahmanpg.beitracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.PathEffect
import com.ahmanpg.beitracker.ui.theme.BeiAccentGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PricePoint(
    val timestamp: Long,
    val price: Double
)

@Composable
fun SparklineChart(
    dataPoints: List<PricePoint>,
    color: Color,
    modifier: Modifier = Modifier,
    showMarkers: Boolean = true,
    minYValue: Double? = null,
    maxYValue: Double? = null,
    minXValue: Long? = null,
    maxXValue: Long? = null,
    highlightPrice: Double? = null
) {
    if (dataPoints.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        fontSize = 10.sp,
        color = Color.Gray.copy(alpha = 0.6f),
        fontWeight = FontWeight.Medium
    )

    val minTimestamp = minXValue ?: dataPoints.minOf { it.timestamp }
    val maxTimestamp = maxXValue ?: dataPoints.maxOf { it.timestamp }
    val timeRange = (maxTimestamp - minTimestamp).coerceAtLeast(1L)
    
    val minPriceValue = dataPoints.minOf { it.price }
    val maxPriceValue = dataPoints.maxOf { it.price }
    
    val effectiveMinY = minYValue ?: (minPriceValue * 0.9)
    val effectiveMaxY = maxYValue ?: (maxPriceValue * 1.1)
    val priceRange = (effectiveMaxY - effectiveMinY).coerceAtLeast(1.0)

    val gridLineColor = Color.LightGray.copy(alpha = 0.2f)
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    val dayFormat = SimpleDateFormat("MMM d", Locale.US)

    // Apple Glass Green Colors
    val appleGreenMain = Color(0xFF34C759)

    Canvas(modifier = modifier.fillMaxSize()) {
        if (size.width <= 0f || size.height <= 0f) return@Canvas

        val width = size.width
        val height = size.height
        
        val leftPadding = 50.dp.toPx()
        val bottomPadding = 30.dp.toPx()
        val rightPadding = 50.dp.toPx() // Reduced for more chart width
        val topPadding = 20.dp.toPx()
        
        val usableHeight = height - topPadding - bottomPadding
        val usableWidth = width - leftPadding - rightPadding

        // Calculate points
        val points = dataPoints.map { point ->
            val xRatio = (point.timestamp - minTimestamp).toFloat() / timeRange
            val x = leftPadding + (xRatio * usableWidth)
            
            val yRatio = ((point.price - effectiveMinY) / priceRange).toFloat()
            val y = height - bottomPadding - (yRatio * usableHeight)
            Offset(x, y)
        }

        // 1. Draw Grid and Labels
        val horizontalLines = 5
        for (i in 0 until horizontalLines) {
            val yRatio = i.toFloat() / (horizontalLines - 1)
            val y = height - bottomPadding - (yRatio * usableHeight)
            val priceLabel = effectiveMinY + (yRatio * priceRange)
            
            // Draw small ticks instead of full lines
            drawLine(
                color = gridLineColor,
                start = Offset(leftPadding, y),
                end = Offset(leftPadding + 4.dp.toPx(), y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = gridLineColor,
                start = Offset(leftPadding + usableWidth - 4.dp.toPx(), y),
                end = Offset(leftPadding + usableWidth, y),
                strokeWidth = 1.dp.toPx()
            )
            
            val textLayout = textMeasurer.measure(
                text = formatCompact(priceLabel),
                style = labelStyle
            )
            drawText(
                textLayout,
                topLeft = Offset(leftPadding - textLayout.size.width - 8.dp.toPx(), y - textLayout.size.height / 2)
            )
        }

        // Vertical markers (dates)
        val oneDayMillis = 24 * 60 * 60 * 1000L
        val daysInRange = timeRange / oneDayMillis
        
        // For a 7D view, we want 8 markers to create 7 daily "frames" 
        // (start of day 1, start of day 2, ..., start of day 7, end of day 7)
        val dateMarkers = when {
            daysInRange in 6L..8L -> 8
            daysInRange in 1L..5L -> (daysInRange + 1).toInt()
            daysInRange <= 30 -> 5
            else -> 4
        }

        for (i in 0 until dateMarkers) {
            val xRatio = i.toFloat() / (dateMarkers - 1)
            val x = leftPadding + (xRatio * usableWidth)
            val timestamp = minTimestamp + (xRatio * timeRange).toLong()
            
            // Draw small ticks instead of full lines
            drawLine(
                color = gridLineColor,
                start = Offset(x, height - bottomPadding),
                end = Offset(x, height - bottomPadding - 4.dp.toPx()),
                strokeWidth = 1.dp.toPx()
            )
            
            val dateLabel = dayFormat.format(Date(timestamp))
            val textLayout = textMeasurer.measure(
                text = dateLabel,
                style = labelStyle
            )
            
            // Rotate labels if we have many markers (7D view)
            if (dateMarkers > 5) {
                rotate(45f, Offset(x, height - bottomPadding + 4.dp.toPx())) {
                    drawText(
                        textLayout,
                        topLeft = Offset(x, height - bottomPadding + 4.dp.toPx())
                    )
                }
            } else {
                drawText(
                    textLayout,
                    topLeft = Offset(x - textLayout.size.width / 2, height - bottomPadding + 4.dp.toPx())
                )
            }
        }

        // 2. Draw Current Price Dotted Line & Pointer
        val lastPoint = points.last()
        drawLine(
            color = Color.Gray.copy(alpha = 0.5f),
            start = Offset(leftPadding, lastPoint.y),
            end = Offset(leftPadding + usableWidth, lastPoint.y),
            strokeWidth = 1.dp.toPx(),
            pathEffect = dashEffect
        )
        
        // Stylish Pointer (Triangle) at the end of the chart
        val pointerPath = Path().apply {
            moveTo(leftPadding + usableWidth, lastPoint.y)
            lineTo(leftPadding + usableWidth + 6.dp.toPx(), lastPoint.y - 4.dp.toPx())
            lineTo(leftPadding + usableWidth + 6.dp.toPx(), lastPoint.y + 4.dp.toPx())
            close()
        }
        drawPath(pointerPath, color = appleGreenMain)
        
        val latestValueText = formatCompact(dataPoints.last().price)
        val latestLabelLayout = textMeasurer.measure(
            text = latestValueText,
            style = labelStyle.copy(color = Color.Black, fontWeight = FontWeight.Black, fontSize = 10.sp)
        )
        drawText(
            latestLabelLayout,
            topLeft = Offset(leftPadding + usableWidth + 10.dp.toPx(), lastPoint.y - latestLabelLayout.size.height / 2)
        )

        drawCircle(color = appleGreenMain, radius = 4.dp.toPx(), center = lastPoint)
        drawCircle(color = Color.White, radius = 2.dp.toPx(), center = lastPoint)

        // 3. Draw Sparkline with Bezier curves
        if (points.size >= 2) {
            val strokePath = Path().apply {
                moveTo(points[0].x, points[0].y)
                for (i in 0 until points.size - 1) {
                    val p1 = points[i]
                    val p2 = points[i + 1]
                    val controlPoint1 = Offset(p1.x + (p2.x - p1.x) / 2f, p1.y)
                    val controlPoint2 = Offset(p1.x + (p2.x - p1.x) / 2f, p2.y)
                    cubicTo(
                        controlPoint1.x, controlPoint1.y,
                        controlPoint2.x, controlPoint2.y,
                        p2.x, p2.y
                    )
                }
            }

            val fillPath = Path().apply {
                addPath(strokePath)
                lineTo(points.last().x, height - bottomPadding)
                lineTo(points.first().x, height - bottomPadding)
                close()
            }

            // Stylish Apple Glass Green Gradient
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        appleGreenMain.copy(alpha = 0.3f),
                        appleGreenMain.copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    startY = points.minOf { it.y },
                    endY = height - bottomPadding
                ),
                style = Fill
            )

            drawPath(
                path = strokePath,
                color = appleGreenMain,
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
        
        // Vertical indicator at latest
        drawLine(
            color = appleGreenMain.copy(alpha = 0.4f),
            start = Offset(lastPoint.x, topPadding),
            end = Offset(lastPoint.x, height - bottomPadding),
            strokeWidth = 1.dp.toPx()
        )
    }
}

@Composable
fun SparklineChart(
    prices: List<Double>,
    color: Color,
    modifier: Modifier = Modifier,
    showMarkers: Boolean = true,
    maxValue: Double? = null,
    highlightPrice: Double? = null
) {
    val dataPoints = remember(prices) {
        val now = System.currentTimeMillis()
        val dayMillis = 24 * 60 * 60 * 1000L
        prices.mapIndexed { index, price ->
            PricePoint(
                timestamp = now - (prices.size - 1 - index) * dayMillis,
                price = price
            )
        }
    }
    
    SparklineChart(
        dataPoints = dataPoints,
        color = color,
        modifier = modifier,
        showMarkers = showMarkers,
        maxYValue = maxValue,
        highlightPrice = highlightPrice
    )
}

private fun formatCompact(price: Double): String {
    return when {
        price >= 1_000_000 -> {
            val formatted = String.format(Locale.US, "%.1f", price / 1_000_000)
            "Tsh " + formatted.trimEnd('0').trimEnd('.') + "m"
        }
        price >= 1_000 -> {
            val formatted = String.format(Locale.US, "%.0f", price / 1_000)
            "Tsh " + formatted + "k"
        }
        else -> "Tsh " + price.toInt().toString()
    }
}
