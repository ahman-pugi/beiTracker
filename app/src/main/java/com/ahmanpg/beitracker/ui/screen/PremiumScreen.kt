package com.ahmanpg.beitracker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmanpg.beitracker.ui.theme.BeiAccentGreen

@Composable
fun PremiumScreen() {
    val premiumFeatures = listOf(
        "Unlimited Market Watching",
        "Priority Sync Frequency",
        "Deep Market Historical Insights",
        "Multi-channel Alerts (WhatsApp/SMS)",
        "Zero Advertisements",
        "Early Access to AI Features"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(40.dp))
                
                Surface(
                    color = BeiAccentGreen.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(120.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BeiAccentGreen.copy(alpha = 0.3f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = BeiAccentGreen
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Unlock BeiTracker Pro",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-0.5).sp
                )
                
                Text(
                    text = "The ultimate edge in price intelligence",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Surface(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(28.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        premiumFeatures.forEach { feature ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = BeiAccentGreen.copy(alpha = 0.2f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = BeiAccentGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = feature,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                val backgroundOverlayColor = MaterialTheme.colorScheme.background
                Surface(
                    color = MaterialTheme.colorScheme.onBackground,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawWithCache {
                                onDrawWithContent {
                                    drawContent()
                                    drawRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(backgroundOverlayColor.copy(alpha = 0.05f), Color.Transparent)
                                        ),
                                        blendMode = BlendMode.Overlay
                                    )
                                }
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "PRO MEMBERSHIP",
                                color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp,
                                fontSize = 11.sp
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    "TZS 5,000",
                                    color = MaterialTheme.colorScheme.background,
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-1).sp
                                )
                                Text(
                                    "/mo",
                                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.4f),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Button(
                                onClick = { /* Handle subscription */ },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.background,
                                    contentColor = MaterialTheme.colorScheme.onBackground
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                            ) {
                                Text("Subscribe Now", fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                "Cancel anytime. No commitment.",
                                color = MaterialTheme.colorScheme.background.copy(alpha = 0.4f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                TextButton(
                    onClick = { /* Handle annual plan */ },
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text(
                        "SWITCH TO ANNUAL & SAVE 25%", 
                        color = BeiAccentGreen, 
                        fontWeight = FontWeight.Black, 
                        letterSpacing = 1.sp,
                        fontSize = 11.sp
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
