package com.ahmanpg.beitracker.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ahmanpg.beitracker.data.model.TrackedItem
import com.ahmanpg.beitracker.viewmodel.PriceViewModel

@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: PriceViewModel = viewModel(),
    onProductClick: (TrackedItem) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }

    Column(Modifier.padding(16.dp)) {

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search product") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                if (query.isNotBlank()) {
                    viewModel.searchProducts(query)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search")
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(vertical = 16.dp))
        }

        LazyColumn {
            items(uiState.searchResults) { product ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    onClick = { onProductClick(product) }
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(product.name, style = MaterialTheme.typography.titleMedium)
                        Text(product.formattedCurrentPrice, color = MaterialTheme.colorScheme.primary)
                        Text(product.source, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
