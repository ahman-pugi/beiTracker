package com.ahmanpg.beitracker

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun ProductDetailScreen(productId: String, navController: NavController) {
    Text(text = "Product Detail Screen for $productId")
}