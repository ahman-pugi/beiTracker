package com.ahmanpg.beitracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmanpg.beitracker.data.model.TrackedItem
import com.ahmanpg.beitracker.ui.theme.*
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceAlertSettings(
    item: TrackedItem,
    numberFormat: NumberFormat,
    onBack: () -> Unit,
    onConfirm: (TrackedItem) -> Unit
) {
    var targetPrice by remember { mutableDoubleStateOf(item.targetPrice ?: (item.currentPrice * 0.95)) }
    var alertCondition by remember { mutableStateOf(item.alertCondition) }
    
    var notifyPush by remember { mutableStateOf(item.notifyPush) }
    var notifySms by remember { mutableStateOf(item.notifySms) }
    var notifyEmail by remember { mutableStateOf(item.notifyEmail) }
    var notifyWhatsapp by remember { mutableStateOf(item.notifyWhatsapp) }

    val minPrice = item.currentPrice * 0.5
    val maxPrice = item.currentPrice * 1.5
    val scrollState = rememberScrollState()
    
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val backgroundColor = MaterialTheme.colorScheme.background

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .background(onBackgroundColor.copy(alpha = 0.08f), CircleShape)
                    .border(1.dp, onBackgroundColor.copy(alpha = 0.1f), CircleShape)
                    .size(44.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = onBackgroundColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                "Alert Settings",
                color = onBackgroundColor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                item.name,
                color = onBackgroundColor.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // 1. Target Price Section
            GlassSection(title = "TARGET PRICE") {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            "${numberFormat.format(targetPrice)}/=",
                            color = onBackgroundColor,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp
                        )
                        Text(
                            "Market: ${numberFormat.format(item.currentPrice / 1000)}K",
                            color = onBackgroundColor.copy(alpha = 0.4f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    Slider(
                        value = targetPrice.toFloat(),
                        onValueChange = { targetPrice = it.toDouble() },
                        valueRange = minPrice.toFloat()..maxPrice.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = onBackgroundColor,
                            activeTrackColor = BeiAccentGreen,
                            inactiveTrackColor = onBackgroundColor.copy(alpha = 0.1f)
                        )
                    )
                    
                    val diff = item.currentPrice - targetPrice
                    val percent = if (item.currentPrice > 0) (diff / item.currentPrice) * 100 else 0.0
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        color = if (diff > 0) BeiAccentGreen.copy(alpha = 0.1f) else onBackgroundColor.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (diff > 0) BeiAccentGreen.copy(alpha = 0.2f) else onBackgroundColor.copy(alpha = 0.1f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (diff > 0) Icons.Default.CheckCircle else Icons.Default.Notifications, 
                                contentDescription = null, 
                                tint = if (diff > 0) BeiAccentGreen else onBackgroundColor.copy(alpha = 0.4f), 
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (diff > 0) "Alert at ${numberFormat.format(diff)}/= (-${percent.toInt()}%) drop"
                                       else "Alert when price increases to target",
                                color = if (diff > 0) BeiAccentGreen else onBackgroundColor.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Condition Section
            GlassSection(title = "CONDITION") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassChip("Below", alertCondition == "BELOW", Modifier.weight(1f)) { alertCondition = "BELOW" }
                    GlassChip("Above", alertCondition == "ABOVE", Modifier.weight(1f)) { alertCondition = "ABOVE" }
                    GlassChip("Any", alertCondition == "ANY", Modifier.weight(1f)) { alertCondition = "ANY" }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Channels Section
            GlassSection(title = "NOTIFICATION CHANNELS") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    GlassToggle("Push Notification", notifyPush) { notifyPush = it }
                    GlassToggle("SMS Alert", notifySms) { notifySms = it }
                    GlassToggle("WhatsApp", notifyWhatsapp) { notifyWhatsapp = it }
                    GlassToggle("Email", notifyEmail) { notifyEmail = it }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }

        // Bottom Bar
        Surface(
            color = onBackgroundColor.copy(alpha = 0.05f),
            modifier = Modifier.fillMaxWidth().drawWithCache {
                onDrawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(onBackgroundColor.copy(alpha = 0.1f), Color.Transparent)
                        ),
                        blendMode = BlendMode.Overlay
                    )
                }
            }
        ) {
            GlassButton(
                onClick = { 
                    onConfirm(item.copy(
                        targetPrice = targetPrice,
                        alertCondition = alertCondition,
                        notifyPush = notifyPush,
                        notifySms = notifySms,
                        notifyEmail = notifyEmail,
                        notifyWhatsapp = notifyWhatsapp,
                        isAlertEnabled = true
                    ))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(56.dp),
                containerColor = BeiAccentGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save Alert Configuration", fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun GlassSection(title: String, content: @Composable () -> Unit) {
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    Column {
        Text(
            title,
            color = onBackgroundColor.copy(alpha = 0.4f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
        Surface(
            color = onBackgroundColor.copy(alpha = 0.05f),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, onBackgroundColor.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(Modifier.padding(20.dp)) {
                content()
            }
        }
    }
}

@Composable
fun GlassChip(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val backgroundColor = MaterialTheme.colorScheme.background
    
    Surface(
        onClick = onClick,
        color = if (selected) onBackgroundColor else onBackgroundColor.copy(alpha = 0.05f),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) onBackgroundColor else onBackgroundColor.copy(alpha = 0.1f)),
        modifier = modifier.height(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text,
                color = if (selected) backgroundColor else onBackgroundColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun GlassToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = onBackgroundColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.surface,
                checkedTrackColor = BeiAccentGreen,
                uncheckedThumbColor = onBackgroundColor.copy(alpha = 0.4f),
                uncheckedTrackColor = onBackgroundColor.copy(alpha = 0.1f),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}
