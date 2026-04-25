package com.ahmanpg.beitracker.ui.screen.detail

import android.webkit.WebView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.ahmanpg.beitracker.ui.components.GlassButton
import com.ahmanpg.beitracker.ui.components.PriceDropChartLoading
import com.ahmanpg.beitracker.ui.theme.BeiAccentGreen
import com.ahmanpg.beitracker.viewmodel.PriceViewModel

@Composable
fun ProductNotFoundView(error: String?, viewModel: PriceViewModel, productUrl: String, navController: NavController) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(40.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🔍", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Text(error ?: "Product not found", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Black, fontSize = 22.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(12.dp))
            Text("The listing might have been removed or the server is temporarily unreachable.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(32.dp))
            GlassButton(
                onClick = { viewModel.loadProductDetails(productUrl) },
                containerColor = BeiAccentGreen,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Retry", fontWeight = FontWeight.Bold, color = Color.White)
            }
            TextButton(onClick = { navController.navigateUp() }) {
                Text("Go Back", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun DeepScraperView(productUrl: String) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PriceDropChartLoading(size = 48.dp)
            Spacer(modifier = Modifier.height(24.dp))
            Text("Bypassing security...", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    loadUrl(productUrl)
                }
            },
            onRelease = { webView ->
                webView.stopLoading()
                webView.destroy()
            },
            modifier = Modifier.size(1.dp)
        )
    }
}

@Composable
fun BuyNowBadge(status: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            status,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
