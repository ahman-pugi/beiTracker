package com.ahmanpg.beitracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.ahmanpg.beitracker.viewmodel.PriceViewModel

private val DarkColorScheme = darkColorScheme(
    primary = BeiAccentGreen,
    secondary = BeiAccentGreen,
    tertiary = BeiPriceDropRed,
    background = BeiNavyDark,
    surface = Color(0xFF1E293B),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = BeiAccentGreen,
    secondary = BeiAccentGreen,
    tertiary = BeiPriceDropRed,
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = BeiNavyDark,
    onSurface = BeiNavyDark
)

@Composable
fun BeiTrackerTheme(
    priceViewModel: PriceViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val uiState by priceViewModel.uiState.collectAsState()
    
    // themeMode -> 0: System, 1: Light, 2: Dark
    val darkMode = when (uiState.themeMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkMode) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
