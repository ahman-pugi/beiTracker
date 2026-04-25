package com.ahmanpg.beitracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmanpg.beitracker.data.model.TrackedItem
import com.ahmanpg.beitracker.ui.theme.BeiAccentGreen
import com.ahmanpg.beitracker.util.LifecycleEngine
import com.ahmanpg.beitracker.util.LifecycleStage

@Composable
fun LifecycleVisualizer(item: TrackedItem) {
    val insight = remember(item) { LifecycleEngine.analyze(item) }
    val seasonalContext = remember { LifecycleEngine.getSeasonalContext() }
    
    Surface(
        color = Color.Transparent, // Made transparent
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    "MARKET LIFECYCLE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LifecycleProgressBar(insight.progress, insight.stage)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                insight.stage.name.replace("_", " "),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                insight.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                lineHeight = 20.sp
            )
            
            if (insight.nextExpectedDrop != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💡", fontSize = 14.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Next Drop: ${insight.nextExpectedDrop}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "SEASONAL CONTEXT",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold
            )
            Text(
                seasonalContext,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun LifecycleProgressBar(progress: Float, stage: LifecycleStage) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "lifecycleProgress"
    )
    
    val color = when (stage) {
        LifecycleStage.NEW_ENTRY -> Color(0xFF3B82F6)
        LifecycleStage.PEAK_POPULARITY -> Color(0xFF10B981)
        LifecycleStage.MATURE -> Color(0xFFF59E0B)
        LifecycleStage.END_OF_LIFE -> Color(0xFFEF4444)
        LifecycleStage.LEGACY -> Color(0xFF6B7280)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(color.copy(alpha = 0.7f), color)
                    )
                )
        )
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("ENTRY", fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
        Text("MATURE", fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
        Text("LEGACY", fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
    }
}
