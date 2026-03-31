package com.ahmanpg.beitracker.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahmanpg.beitracker.viewmodel.PriceViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PriceTrackerScreen(
    viewModel: PriceViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "BeiTracker",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔍 Search Input
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search product (e.g. iPhone, TV)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (query.isNotBlank()) {
                    viewModel.trackProduct(query)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search & Track")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ⏳ Loading
        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        // ❌ Error
        uiState.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        // ✅ Current Result
        uiState.currentPrice?.let { product ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(product.title, style = MaterialTheme.typography.titleMedium)
                    product.price?.let {
                        Text("TZS $it", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 📈 History
        if (uiState.history.isNotEmpty()) {
            Text(
                text = "Price History",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(uiState.history) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(item.title)
                            Text("TZS ${item.price}")
                            Text(
                                text = item.timestamp?.let { ts ->
                                    SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
                                        .format(Date(ts))
                                } ?: ""
                            )
                        }
                    }
                }
            }
        }
    }
}
