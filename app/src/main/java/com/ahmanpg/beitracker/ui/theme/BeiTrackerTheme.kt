package com.ahmanpg.beitracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// ── Custom brand colors (Tanzania-inspired: green for growth, blue for trust) ──
private val PrimaryGreen      = Color(0xFF2E7D32)
private val PrimaryGreenLight = Color(0xFF4CAF50)
private val OnPrimary         = Color.White

private val SecondaryBlue      = Color(0xFF1976D2)
private val SecondaryBlueLight = Color(0xFF2196F3)

private val ErrorRed = Color(0xFFD32F2F)

// ── Light color scheme ──
private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryGreenLight.copy(alpha = 0.12f),
    onPrimaryContainer = PrimaryGreen,

    secondary = SecondaryBlue,
    onSecondary = Color.White,
    secondaryContainer = SecondaryBlueLight.copy(alpha = 0.12f),
    onSecondaryContainer = SecondaryBlue,

    tertiary = Color(0xFF388E3C),
    background = Color(0xFFFAFAFA),
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF424242),

    error = ErrorRed,
    onError = Color.White,

    outline = Color(0xFF757575),
    scrim = Color.Black.copy(alpha = 0.32f)
)

// ── Dark color scheme ──
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreenLight,
    onPrimary = Color.Black,
    primaryContainer = PrimaryGreen.copy(alpha = 0.24f),
    onPrimaryContainer = PrimaryGreenLight,

    secondary = SecondaryBlueLight,
    onSecondary = Color.Black,
    secondaryContainer = SecondaryBlue.copy(alpha = 0.24f),
    onSecondaryContainer = SecondaryBlueLight,

    tertiary = Color(0xFF66BB6A),
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF424242),
    onSurfaceVariant = Color(0xFFBDBDBD),

    error = Color(0xFFEF5350),
    onError = Color.Black,

    outline = Color(0xFF757575),
    scrim = Color.Black.copy(alpha = 0.60f)
)

// ── Define Typography (required – defaults or custom) ──
val BeiTypography = androidx.compose.material3.Typography(
    // You can customize any of these; here are a few examples
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // Add more styles as needed (bodyMedium, labelSmall, etc.)
    // Full list: https://developer.android.com/reference/kotlin/androidx/compose/material3/Typography
)

// ── Define Shapes (optional – use defaults or customize) ──
val BeiShapes = androidx.compose.material3.Shapes(
    // Example: more rounded corners for a modern feel
    // small = RoundedCornerShape(8.dp),
    // medium = RoundedCornerShape(12.dp),
    // large = RoundedCornerShape(16.dp)
    // Or just use the default: androidx.compose.material3.Shapes()
)

@RequiresApi(Build.VERSION_CODES.CUPCAKE)
@Composable
fun BeiTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color (Material You) – only on Android 12+; safe to keep false for TZ compatibility
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.primary.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()

            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BeiTypography,   // ← now resolved
        shapes = BeiShapes,           // ← now resolved (or use androidx.compose.material3.Shapes() if you remove BeiShapes)
        content = content
    )
}