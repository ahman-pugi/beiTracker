package com.ahmanpg.beitracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmanpg.beitracker.ui.theme.BeiAccentGreen

@Composable
fun AppLogo(
    size: Dp = 44.dp,
    cornerRadius: Dp = 12.dp,
    fontSize: Int = 24
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logoPulse")
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier
            .size(size)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = BeiAccentGreen.copy(alpha = glowAlpha),
                spotColor = BeiAccentGreen
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    0.0f to Color(0xFF4ADE80), // Light green
                    0.5f to BeiAccentGreen,    // Brand green
                    1.0f to Color(0xFF065F46)  // Dark emerald
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.6f), Color.Transparent, Color.Black.copy(alpha = 0.2f))
                ),
                shape = RoundedCornerShape(cornerRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Pattern Background (Subtle Grid)
        Canvas(modifier = Modifier.fillMaxSize().graphicsLayer(alpha = 0.1f)) {
            val step = 8.dp.toPx()
            for (i in 0..(size.toPx() / step).toInt()) {
                drawLine(Color.White, Offset(i * step, 0f), Offset(i * step, size.toPx()), 1f)
                drawLine(Color.White, Offset(0f, i * step), Offset(size.toPx(), i * step), 1f)
            }
        }

        // Gloss / Shine
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    onDrawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                                endY = size.toPx() * 0.45f
                            ),
                            blendMode = BlendMode.Overlay
                        )
                    }
                }
        )

        // The 'B' with Layered Typography
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "B",
                color = Color.Black.copy(alpha = 0.25f),
                fontWeight = FontWeight.Black,
                fontSize = fontSize.sp,
                modifier = Modifier.offset(x = 2.dp, y = 2.dp)
            )
            Text(
                text = "B",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = fontSize.sp,
                modifier = Modifier.graphicsLayer(alpha = 0.99f)
            )
        }

        // Moving Trend Icon
        val trendOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -3f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = EaseInOutBack),
                repeatMode = RepeatMode.Reverse
            ),
            label = "trendOffset"
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.TrendingDown,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier
                .size(size / 2.5f)
                .align(Alignment.BottomEnd)
                .padding(bottom = 2.dp, end = 2.dp)
                .offset(y = trendOffset.dp)
        )
    }
}

@Composable
fun AppLogoText(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "bei",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = BeiAccentGreen,
                letterSpacing = (-0.5).sp
            )
            Text(
                "Tracker",
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = textColor,
                letterSpacing = (-0.5).sp
            )
        }
        
        // Premium looking badge
        Surface(
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.padding(top = 2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)) {
                Text(
                    "PRECISION MARKET INTELLIGENCE",
                    fontSize = 9.sp,
                    color = textColor.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun LogoPreview() {
    Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        AppLogo(size = 80.dp, fontSize = 48)
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppLogo(size = 48.dp, fontSize = 28)
            Spacer(Modifier.width(16.dp))
            AppLogoText()
        }
    }
}
