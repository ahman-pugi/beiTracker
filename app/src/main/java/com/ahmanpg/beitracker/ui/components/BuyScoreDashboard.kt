package com.ahmanpg.beitracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmanpg.beitracker.data.model.TrackedItem
import com.ahmanpg.beitracker.ui.theme.BeiAccentGreen
import com.ahmanpg.beitracker.ui.theme.BeiPriceDropRed
import com.ahmanpg.beitracker.util.BuyScoreEngine
import com.ahmanpg.beitracker.util.ScoreFactor

private const val STATE_INSUFFICIENT_DATA = "INSUFFICIENT_DATA"

@Composable
fun BuyScoreDashboard(item: TrackedItem) {
    val result = remember(item) { BuyScoreEngine.calculateScore(item) }
    val isInsufficientData = result.confidenceScore < 40
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Surface(
        color = Color.Transparent, // Made transparent
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "BUY SCORE",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                        HelpIcon(
                            title = androidx.compose.ui.res.stringResource(com.ahmanpg.beitracker.R.string.help_buy_score_title),
                            description = androidx.compose.ui.res.stringResource(com.ahmanpg.beitracker.R.string.help_buy_score_desc),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    RecommendationBadge(
                        if (isInsufficientData) STATE_INSUFFICIENT_DATA else result.recommendation
                    )
                }
                
                ScoreCircle(
                    score = if (isInsufficientData) null else result.score
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "WHY THIS SCORE?",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isInsufficientData) {
                Text(
                    "Collecting more data for deeper insights...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.alpha(pulseAlpha)
                )
            } else {
                result.factors.forEach { factor ->
                    FactorRow(factor)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                if (result.factors.isEmpty()) {
                    Text(
                        "Analyzing market trends...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }

                if (result.score < 50) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Note: Higher prices often reflect 'Brand New' condition, low mileage, or special edition features. Evaluate the item details carefully.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontStyle = FontStyle.Italic,
                        lineHeight = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = { result.confidenceScore / 100f },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = if (isInsufficientData) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
            )
            Text(
                "Confidence: ${result.confidenceScore}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun ScoreCircle(score: Int?) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val animatedScore by animateFloatAsState(
        targetValue = (score ?: 0).toFloat(),
        animationSpec = tween(durationMillis = 1000),
        label = "score"
    )
    
    val color = when {
        score == null -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
        score >= 75 -> BeiAccentGreen
        score >= 45 -> Color(0xFFF59E0B)
        else -> BeiPriceDropRed
    }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
        CircularProgressIndicator(
            progress = { if (score == null) 0.2f else animatedScore / 100f },
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (score == null) Modifier.graphicsLayer { rotationZ = rotation } else Modifier
                ),
            color = color,
            strokeWidth = 8.dp,
            trackColor = color.copy(alpha = 0.1f),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = score?.toString() ?: "--",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = if (score == null) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f) else color
            )
            if (score != null) {
                Text(
                    text = "/100",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = color.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun RecommendationBadge(recommendation: String) {
    val (text, color) = when (recommendation) {
        "BUY" -> "BUY NOW" to BeiAccentGreen
        "WAIT" -> "WAITING" to Color(0xFFF59E0B)
        STATE_INSUFFICIENT_DATA -> "Collecting data..." to MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
        else -> "PREMIUM" to Color(0xFFF59E0B)
    }
    
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text,
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun FactorRow(factor: ScoreFactor) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (factor.isPositive) Icons.Default.CheckCircle else Icons.Default.Error,
            contentDescription = null,
            tint = if (factor.isPositive) BeiAccentGreen else BeiPriceDropRed,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            factor.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            "${if (factor.impact > 0) "+" else ""}${factor.impact}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (factor.isPositive) BeiAccentGreen else BeiPriceDropRed
        )
    }
}
