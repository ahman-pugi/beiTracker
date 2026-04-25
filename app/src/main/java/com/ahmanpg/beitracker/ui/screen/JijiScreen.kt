package com.ahmanpg.beitracker.ui.screen

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.ahmanpg.beitracker.ui.theme.BeiAccentGreen
import com.ahmanpg.beitracker.ui.theme.BeiNavyDark

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun JijiScreen(initialUrl: String? = null) {
    var isLoading by remember { mutableStateOf(true) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    val startUrl = initialUrl ?: "https://jiji.co.tz"
    
    // Track if we've already loaded the initial URL to prevent reloads on recomposition
    var hasLoadedInitial by remember { mutableStateOf(false) }

    LaunchedEffect(initialUrl) {
        if (initialUrl != null && hasLoadedInitial) {
            webView?.loadUrl(initialUrl)
        }
    }
    
    Column(modifier = Modifier.fillMaxSize().background(BeiNavyDark)) {
        // Glassy Top Bar
        Surface(
            color = Color.White.copy(alpha = 0.05f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = BeiAccentGreen.copy(alpha = 0.1f),
                        shape = CircleShape,
                        modifier = Modifier.size(32.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BeiAccentGreen.copy(alpha = 0.2f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Security, null, tint = BeiAccentGreen, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Jiji.co.tz", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Text("SECURE BROWSING ACTIVE", color = BeiAccentGreen, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                }
                
                IconButton(
                    onClick = { webView?.reload() },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(Icons.Default.Refresh, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isLoading = true
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                            }
                        }
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; SM-G973F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36"
                        loadUrl(startUrl)
                        hasLoadedInitial = true
                        webView = this
                    }
                },
                update = { view ->
                    // Optional: handle updates if needed, though LaunchedEffect handles URL changes
                },
                onRelease = { view ->
                    view.stopLoading()
                    view.destroy()
                    webView = null
                }
            )
            
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(2.dp).align(Alignment.TopCenter),
                    color = BeiAccentGreen,
                    trackColor = Color.Transparent
                )
            }
        }
    }
}
