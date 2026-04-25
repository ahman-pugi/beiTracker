package com.ahmanpg.beitracker.ui.components

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import java.text.NumberFormat
import java.util.Locale

@Composable
fun StoreComparisonCard(
    currentItem: TrackedItem,
    similarItems: List<TrackedItem>,
    onNavigateToProduct: (String) -> Unit = {},
    onBuyNow: (String) -> Unit = {}
) {
    if (similarItems.isEmpty()) return

    val numberFormat = NumberFormat.getNumberInstance(Locale.US)
    
    // Filter similar items by manufacture year to ensure appropriate comparison
    val filteredSimilarItems = if (currentItem.manufactureYear != null) {
        similarItems.filter { it.manufactureYear == currentItem.manufactureYear }
    } else {
        similarItems
    }

    if (filteredSimilarItems.isEmpty()) return

    val bestAlternative = filteredSimilarItems.minByOrNull { it.currentPrice }
    val lowestPrice = bestAlternative?.currentPrice ?: currentItem.currentPrice
    val savings = currentItem.currentPrice - lowestPrice
    
    Surface(
        color = Color.Transparent, // Made transparent
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Store, null, tint = BeiAccentGreen, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    "STORE COMPARISON",
                    style = MaterialTheme.typography.labelMedium,
                    color = BeiAccentGreen,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
                
                currentItem.manufactureYear?.let { year ->
                    Spacer(Modifier.weight(1f))
                    Surface(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            year.toString() + "model",
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Current Store
                val isCurrentLowest = currentPriceIsLowest(currentItem.currentPrice, lowestPrice)
                if (!isCurrentLowest) {
                    StorePriceRow(
                        storeName = currentItem.source,
                        price = currentItem.currentPrice,
                        isLowest = false,
                        numberFormat = numberFormat,
                        onClick = { onBuyNow(currentItem.url) }
                    )
                }
                
                // Best Alternative Store
                if (!isCurrentLowest && bestAlternative != null) {
                    StorePriceRow(
                        storeName = bestAlternative.sellerName ?: "Alternative Seller",
                        price = lowestPrice,
                        isLowest = true,
                        numberFormat = numberFormat,
                        onClick = { onNavigateToProduct(bestAlternative.url) }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Surface(
                        color = BeiAccentGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Save ${numberFormat.format(savings)}/= elsewhere!",
                                color = BeiAccentGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            
                            TextButton(
                                onClick = { onNavigateToProduct(bestAlternative.url) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("VIEW DEAL", color = BeiAccentGreen, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = BeiAccentGreen, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        onClick = { onBuyNow(currentItem.url) },
                        color = BeiAccentGreen,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.25f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "BEST PRICE SECURED",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = Color(0xFFFEF3C7),
                                            shape = RoundedCornerShape(4.dp),
                                            border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.3f))
                                        ) {
                                            Text(
                                                "Jiji",
                                                color = Color(0xFFD97706),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Buy instantly",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                                
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun currentPriceIsLowest(current: Double, lowest: Double): Boolean {
    return current <= lowest + 1.0 // Tiny margin for floating point
}

@Composable
fun StorePriceRow(
    storeName: String,
    price: Double,
    isLowest: Boolean,
    numberFormat: NumberFormat,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick) else Modifier)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                storeName.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
            Text(
                "${numberFormat.format(price)}/=",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = if (isLowest) BeiAccentGreen else MaterialTheme.colorScheme.onBackground
            )
        }
        
        if (isLowest) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onClick != null) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = BeiAccentGreen, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                }
                Surface(
                    color = BeiAccentGreen,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "BEST",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        } else if (onClick != null) {
             Icon(
                Icons.AutoMirrored.Filled.ArrowForward, 
                null, 
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
