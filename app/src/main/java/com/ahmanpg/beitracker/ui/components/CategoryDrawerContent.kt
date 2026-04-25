package com.ahmanpg.beitracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmanpg.beitracker.ui.theme.BeiAccentGreen

@Composable
fun CategoryDrawerContent(
    userName: String,
    onClose: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp),
        modifier = Modifier.width(320.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Surface(
                    color = BeiAccentGreen,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = userName.firstOrNull()?.toString()?.uppercase() ?: "G",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Habari,",
                        color = onSurfaceColor.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = userName,
                        color = onSurfaceColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Default.Close, 
                        contentDescription = "Close", 
                        tint = onSurfaceColor
                    )
                }
            }

            Text(
                text = "DISCOVER",
                color = onSurfaceColor.copy(alpha = 0.3f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            DrawerItem(
                icon = Icons.Default.TrendingUp,
                label = "Market Trends",
                onClick = { onNavigate("market_trends") }
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "BROWSE CATEGORIES",
                color = onSurfaceColor.copy(alpha = 0.3f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val categories = listOf(
                DrawerCategory("Smartphones", "smartphones", Icons.Default.Smartphone),
                DrawerCategory("Laptops", "laptops", Icons.Default.Laptop),
                DrawerCategory("Televisions", "tvs", Icons.Default.Tv),
                DrawerCategory("Smartwatches", "watches", Icons.Default.Watch),
                DrawerCategory("Audio & Sound", "audio", Icons.Default.Headphones),
                DrawerCategory("Cameras", "cameras", Icons.Default.CameraAlt),
                DrawerCategory("Vehicles", "vehicles", Icons.Default.DirectionsCar)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    DrawerItem(
                        icon = category.icon,
                        label = category.title,
                        onClick = { onNavigate("category/${category.slug}") }
                    )
                }
            }

            HorizontalDivider(
                color = onSurfaceColor.copy(alpha = 0.1f), 
                modifier = Modifier.padding(vertical = 24.dp)
            )

            DrawerItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                onClick = { onNavigate("profile") }
            )
            
            Spacer(Modifier.height(8.dp))
            
            DrawerItem(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                label = "Help & Support",
                onClick = { onNavigate("help") }
            )
        }
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = onSurfaceColor.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = label,
                color = onSurfaceColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private data class DrawerCategory(
    val title: String,
    val slug: String,
    val icon: ImageVector
)
