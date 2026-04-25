package com.ahmanpg.beitracker.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmanpg.beitracker.ui.theme.BeiAccentGreen
import com.ahmanpg.beitracker.ui.theme.BeiNavyDark

@Composable
fun LaunchesScreen() {
    Column(
        modifier = Modifier.fillMaxSize().background(BeiNavyDark).padding(32.dp), 
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = Color.White.copy(alpha = 0.05f),
            shape = CircleShape,
            modifier = Modifier.size(120.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.RocketLaunch, 
                    contentDescription = null, 
                    modifier = Modifier.size(56.dp), 
                    tint = BeiAccentGreen
                )
            }
        }
        Spacer(Modifier.height(32.dp))
        Text(
            "Upcoming Launches", 
            style = MaterialTheme.typography.headlineMedium, 
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = (-0.5).sp
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Be the first to know about new gadget releases in Tanzania. We track global trends and local availability.", 
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.White.copy(alpha = 0.5f),
            lineHeight = 24.sp
        )
    }
}

@Composable
fun NewsScreen() {
    Column(modifier = Modifier.fillMaxSize().background(BeiNavyDark)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(), 
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text(
                    "Market Insights", 
                    style = MaterialTheme.typography.headlineMedium, 
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    "Price movement analysis and predictions", 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
            }
            items(10) { index ->
                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            "WEEKLY REPORT #$index", 
                            style = MaterialTheme.typography.labelMedium, 
                            fontWeight = FontWeight.Black,
                            color = BeiAccentGreen,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Kariakoo Market price shift", 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "We are observing a significant drop in high-end smartphone prices. Jiji listings for iPhone 15 series show a 4.2% decrease...", 
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.6f),
                            lineHeight = 20.sp
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "2 HOURS AGO", 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Black, 
                            color = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ForumsScreen() {
    Column(
        modifier = Modifier.fillMaxSize().background(BeiNavyDark).padding(32.dp), 
        horizontalAlignment = Alignment.CenterHorizontally, 
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = Color.White.copy(alpha = 0.05f),
            shape = CircleShape,
            modifier = Modifier.size(120.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(56.dp), tint = Color.White)
            }
        }
        Spacer(Modifier.height(32.dp))
        Text(
            "Community Hub", 
            style = MaterialTheme.typography.headlineMedium, 
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = (-0.5).sp
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Connect with smart shoppers across Tanzania. Share deals, report fake listings, and get seller recommendations.", 
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.White.copy(alpha = 0.5f),
            lineHeight = 24.sp
        )
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BeiAccentGreen)
        ) {
            Text("Enter Community", fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
    }
}

@Composable
fun HelpSupportScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BeiNavyDark)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
            Spacer(Modifier.width(16.dp))
            Text(
                "Help & Support",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SupportActionCard(
                    title = "Contact Support",
                    subtitle = "Chat with our team directly",
                    icon = Icons.Default.Chat,
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@beitracker.tz")
                            putExtra(Intent.EXTRA_SUBJECT, "App Support Request")
                        }
                        context.startActivity(intent)
                    }
                )
            }
            item {
                SupportActionCard(
                    title = "FAQs",
                    subtitle = "Frequently asked questions",
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    onClick = { /* Navigate to FAQ */ }
                )
            }
            item {
                SupportActionCard(
                    title = "Tutorials",
                    subtitle = "Learn how to use BeiTracker",
                    icon = Icons.Default.PlayCircle,
                    onClick = { /* Navigate to Tutorials */ }
                )
            }
            item {
                SupportActionCard(
                    title = "Report an Issue",
                    subtitle = "Tell us what went wrong",
                    icon = Icons.Default.BugReport,
                    onClick = { /* Handle Report */ }
                )
            }
        }
    }
}

@Composable
private fun SupportActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = BeiAccentGreen.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = BeiAccentGreen, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Text(subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.3f))
        }
    }
}

@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier.fillMaxSize().background(BeiNavyDark).padding(24.dp), 
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        Surface(
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.size(100.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Info, 
                    contentDescription = null, 
                    modifier = Modifier.size(48.dp),
                    tint = BeiAccentGreen
                )
            }
        }
        Spacer(Modifier.height(32.dp))
        Text(
            "BeiTracker", 
            style = MaterialTheme.typography.headlineMedium, 
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = (-0.5).sp
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Tanzania's #1 price intelligence tool. We provide real-time market data to help you save more on every purchase.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.White.copy(alpha = 0.6f),
            lineHeight = 24.sp
        )
        Spacer(Modifier.height(40.dp))
        
        Surface(
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(28.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AboutBenefitItem("Real-time price monitoring")
                AboutBenefitItem("Historical price analytics")
                AboutBenefitItem("Multi-source deal aggregation")
                AboutBenefitItem("Instant drop notifications")
            }
        }
        
        Spacer(Modifier.weight(1f))
        Text("VERSION 1.2.0-STABLE", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.3f), fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Text("© 2025 AHMAN LABS.", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.3f), fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun AboutBenefitItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Check, null, tint = BeiAccentGreen, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}
