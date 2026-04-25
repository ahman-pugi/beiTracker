package com.ahmanpg.beitracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmanpg.beitracker.data.local.entity.PriceAlertEntity
import com.ahmanpg.beitracker.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun FiredAlertCard(
    alert: PriceAlertEntity,
    numberFormat: NumberFormat,
    dateFormat: SimpleDateFormat,
    onClick: (() -> Unit)? = null
) {
    val modifier = Modifier
        .fillMaxWidth()
        .let {
            if (onClick != null) it.clickable { onClick() } else it
        }
    
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    
    Surface(
        color = onBackgroundColor.copy(alpha = 0.05f),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, onBackgroundColor.copy(alpha = 0.1f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon Badge
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = if (!alert.isRead) BeiAccentGreen.copy(alpha = 0.15f) else onBackgroundColor.copy(alpha = 0.05f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, 
                    if (!alert.isRead) BeiAccentGreen.copy(alpha = 0.3f) else onBackgroundColor.copy(alpha = 0.1f)
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (alert.newPrice < alert.oldPrice) 
                            Icons.AutoMirrored.Filled.TrendingDown else Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = if (!alert.isRead) BeiAccentGreen else onBackgroundColor.copy(alpha = 0.4f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isDrop = alert.newPrice < alert.oldPrice
                    Text(
                        text = if (isDrop) "PRICE DROP DETECTED" else "PRICE UPDATE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isDrop) BeiAccentGreen else onBackgroundColor.copy(alpha = 0.5f),
                        letterSpacing = 1.2.sp
                    )
                    
                    if (!alert.isRead) {
                        Surface(
                            color = BeiAccentGreen,
                            shape = CircleShape,
                            modifier = Modifier.size(8.dp)
                        ) {}
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    alert.productTitle,
                    fontWeight = FontWeight.Bold,
                    color = onBackgroundColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 16.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${numberFormat.format(alert.newPrice)}/=",
                        color = if (alert.newPrice < alert.oldPrice) BeiAccentGreen else onBackgroundColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        "${numberFormat.format(alert.oldPrice)}/=",
                        style = TextStyle(textDecoration = TextDecoration.LineThrough),
                        color = onBackgroundColor.copy(alpha = 0.3f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                val drop = if (alert.oldPrice > 0) ((alert.oldPrice - alert.newPrice) / alert.oldPrice * 100).toInt() else 0
                if (drop > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = BeiAccentGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BeiAccentGreen.copy(alpha = 0.2f))
                    ) {
                        Text(
                            "  Save ${drop}%  ",
                            color = BeiAccentGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    dateFormat.format(Date(alert.createdAt)),
                    color = onBackgroundColor.copy(alpha = 0.3f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
