package com.ahmanpg.beitracker.ui.screen.detail

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ahmanpg.beitracker.data.model.TrackedItem
import com.ahmanpg.beitracker.ui.components.GlassButton
import com.ahmanpg.beitracker.ui.theme.BeiAccentGreen
import com.ahmanpg.beitracker.viewmodel.PriceViewModel

@Composable
fun DetailBottomBar(item: TrackedItem, viewModel: PriceViewModel, navController: NavController) {
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GlassButton(
                onClick = { 
                    val encodedUrl = Uri.encode(item.url)
                    navController.navigate("jiji?url=$encodedUrl")
                },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.1f)
            ) {
                Text("View on ${item.source}", fontWeight = FontWeight.Bold, color = onBackgroundColor)
            }
            
            GlassButton(
                onClick = { viewModel.showAlertSettings(item) },
                containerColor = BeiAccentGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1.2f).height(56.dp)
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (item.isTracked) "Alert Settings" else "Track Price",
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}
