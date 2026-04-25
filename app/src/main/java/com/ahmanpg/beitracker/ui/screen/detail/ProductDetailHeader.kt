package com.ahmanpg.beitracker.ui.screen.detail

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ahmanpg.beitracker.data.model.TrackedItem
import java.net.URLEncoder
import java.text.NumberFormat

@Composable
fun DetailHeader(
    item: TrackedItem,
    navController: NavController,
    context: android.content.Context,
    numberFormat: NumberFormat
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier
                .size(44.dp)
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f), CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            item.name,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
                .clip(MaterialTheme.shapes.small)
                .clickable {
                    val encodedUrl = URLEncoder.encode(item.url, "UTF-8")
                    navController.navigate("jiji?url=$encodedUrl")
                },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 22.sp
        )
        IconButton(
            onClick = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "BeiTracker Deal: ${item.name} for TZS ${numberFormat.format(item.currentPrice)}\nLink: ${item.url}"
                    )
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, null))
            },
            modifier = Modifier
                .size(44.dp)
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f), CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(
                Icons.Default.Share,
                contentDescription = "Share",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
