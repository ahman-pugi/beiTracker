package com.ahmanpg.beitracker.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmanpg.beitracker.ui.components.AppLogo
import com.ahmanpg.beitracker.ui.theme.BeiTrackerTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    var start by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (start) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 80f
        ),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (start) 1f else 0f,
        animationSpec = tween(900),
        label = "alpha"
    )

    LaunchedEffect(Unit) {
        start = true
        delay(2200)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0B0F1A),
                        Color(0xFF05070D)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Floating glow
        Box(
            modifier = Modifier
                .size(400.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x334ADE80), // Subtle green glow
                            Color.Transparent
                        )
                    )
                )
        )

        // Glass Card
        Box(
            modifier = Modifier
                .scale(scale)
                .alpha(alpha)
                .size(200.dp)
                .background(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(32.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AppLogo(size = 90.dp, cornerRadius = 24.dp, fontSize = 48)

                Spacer(Modifier.height(16.dp))

                Text(
                    "BeiTracker",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
            }
        }

        Text(
            "PRECISION MARKET INTELLIGENCE",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .alpha(alpha * 0.5f),
            fontSize = 10.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    BeiTrackerTheme {
        SplashScreen(onTimeout = {})
    }
}
