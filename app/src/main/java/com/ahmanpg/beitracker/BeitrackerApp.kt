package com.ahmanpg.beitracker

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ahmanpg.beitracker.ui.screen.*
import com.ahmanpg.beitracker.ui.components.*
import com.ahmanpg.beitracker.ui.theme.*
import com.ahmanpg.beitracker.viewmodel.AuthViewModel
import com.ahmanpg.beitracker.viewmodel.CategoryDeals
import com.ahmanpg.beitracker.viewmodel.HomeUiState
import com.ahmanpg.beitracker.viewmodel.PriceViewModel
import kotlinx.coroutines.launch

sealed class Screen(
    val route: String,
    val titleResId: Int,
    val icon: ImageVector
) {
    object Home : Screen("home", R.string.home, Icons.Default.GridView)
    object Search : Screen("search", R.string.search, Icons.Default.Search)
    object Jiji : Screen("jiji", R.string.jiji, Icons.Default.ShoppingBag)
    object Triggers : Screen("triggers", R.string.triggers, Icons.Default.Notifications)
    object Profile : Screen("profile", R.string.profile, Icons.Outlined.PersonOutline)
    
    object Auth : Screen("auth", R.string.login, Icons.Default.Lock)
    object ProductDetail : Screen("product_detail?url={url}", R.string.product_detail, Icons.Default.Info)
    object Help : Screen("help", R.string.help, Icons.Default.QuestionAnswer)
    object MarketTrends : Screen("market_trends", R.string.menu_popular_products, Icons.Default.TrendingUp)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeiTrackerApp(
    priceViewModel: PriceViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by priceViewModel.uiState.collectAsState()
    val user by authViewModel.user.collectAsState()
    val unreadCount = uiState.alerts.count { !it.isRead }
    
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen(onTimeout = { showSplash = false })
        return
    }

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Search,
        Screen.Jiji,
        Screen.Triggers,
        Screen.Profile
    )

    Box(modifier = Modifier.fillMaxSize()) {
        if (user == null) {
            AuthScreen(onAuthSuccess = {})
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    CategoryDrawerContent(
                        userName = user?.displayName ?: uiState.userName,
                        onClose = { scope.launch { drawerState.close() } },
                        onNavigate = { route ->
                            scope.launch { drawerState.close() }
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            ) {
                Scaffold(
                    bottomBar = {
                        // Persistent bottom bar for all authenticated screens
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            tonalElevation = 8.dp,
                            modifier = Modifier.drawWithCache {
                                onDrawWithContent {
                                    drawContent()
                                    drawRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color.White.copy(alpha = 0.05f), Color.Transparent)
                                        ),
                                        blendMode = BlendMode.Overlay
                                    )
                                }
                            }
                        ) {
                            NavigationBar(
                                containerColor = Color.Transparent,
                                tonalElevation = 0.dp,
                                modifier = Modifier.height(80.dp)
                            ) {
                                bottomNavItems.forEach { screen ->
                                    val selected = currentDestination?.hierarchy?.any { 
                                        val route = it.route ?: return@any false
                                        // Highlight tab if the route matches or if we're on a sub-screen related to this tab
                                        route == screen.route || (screen == Screen.Jiji && route.startsWith("jiji"))
                                    } == true
                                    val title = stringResource(screen.titleResId)
                                    
                                    NavigationBarItem(
                                        icon = { 
                                            if (screen == Screen.Jiji) {
                                                Surface(
                                                    color = if (selected) BeiAccentGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.size(28.dp),
                                                    border = if (!selected) BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)) else null
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text("J", color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Black, fontSize = 15.sp)
                                                    }
                                                }
                                            } else {
                                                BadgedBox(
                                                    badge = {
                                                        if (screen == Screen.Triggers && unreadCount > 0) {
                                                            Badge(containerColor = BeiPriceDropRed, contentColor = Color.White) {
                                                                Text(unreadCount.toString())
                                                            }
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        screen.icon, 
                                                        contentDescription = title,
                                                        modifier = Modifier.size(26.dp)
                                                    )
                                                }
                                            }
                                        },
                                        label = { 
                                            Text(
                                                title, 
                                                fontSize = 10.sp, 
                                                fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            ) 
                                        },
                                        selected = selected,
                                        onClick = {
                                            // Handle global transition from any screen to a top-level tab
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        )
                                    )
                                }
                            }
                        }
                    },
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Home.route,
                            // High-performance smooth transitions for tab-like feel
                            enterTransition = { 
                                fadeIn(animationSpec = tween(400, easing = FastOutSlowInEasing)) + 
                                scaleIn(initialScale = 0.98f, animationSpec = tween(400, easing = FastOutSlowInEasing))
                            },
                            exitTransition = { 
                                fadeOut(animationSpec = tween(300)) +
                                scaleOut(targetScale = 1.02f, animationSpec = tween(300))
                            },
                            popEnterTransition = { 
                                fadeIn(animationSpec = tween(350)) +
                                scaleIn(initialScale = 1.02f, animationSpec = tween(350))
                            },
                            popExitTransition = { 
                                fadeOut(animationSpec = tween(350)) +
                                scaleOut(targetScale = 0.98f, animationSpec = tween(350))
                            }
                        ) {
                            composable(Screen.Home.route) { 
                                val homeUiState = remember(uiState) {
                                    HomeUiState(
                                        userName = user?.displayName ?: uiState.userName,
                                        watchlistSummary = uiState.watchlistSummary,
                                        // Filter to only show products that have a real calculated price drop
                                        featuredPriceDrops = uiState.topPriceDrops,
                                        watchlist = uiState.trackedProducts,
                                        categories = uiState.categories.map { CategoryDeals(it.key, it.value) },
                                        alerts = uiState.alerts
                                    )
                                }
                                HomeScreen(
                                    navController = navController,
                                    uiState = homeUiState,
                                    isLoading = uiState.isLoading,
                                    onRefresh = { priceViewModel.refreshAllPrices() },
                                    onDeleteWatchlistItem = { priceViewModel.untrackProduct(it.url) },
                                    onMarkAllAlertsRead = { priceViewModel.markAllAlertsRead() },
                                    onAlertClick = { url ->
                                        priceViewModel.markAlertsRead(url)
                                        navController.navigate("product_detail?url=${Uri.encode(url)}")
                                    },
                                    onOpenDrawer = { scope.launch { drawerState.open() } }
                                ) 
                            }
                            composable(Screen.Search.route) { SearchScreen(navController = navController) }
                            composable(
                                route = "jiji?url={url}",
                                arguments = listOf(navArgument("url") { type = NavType.StringType; nullable = true; defaultValue = null })
                            ) { backStackEntry ->
                                val url = backStackEntry.arguments?.getString("url")
                                JijiScreen(initialUrl = url)
                            }
                            composable(Screen.Jiji.route) { JijiScreen() }
                            
                            composable(Screen.Triggers.route) {
                                TriggersScreen(
                                    onBack = { navController.navigateUp() },
                                    onAlertClick = { url ->
                                        priceViewModel.markAlertsRead(url)
                                        navController.navigate("product_detail?url=${Uri.encode(url)}")
                                    }
                                ) 
                            }
                            composable(Screen.Profile.route) { 
                                SettingsScreen(
                                    priceViewModel = priceViewModel,
                                    onBack = { navController.navigateUp() }
                                )
                            }
                            
                            composable(
                                route = Screen.ProductDetail.route,
                                arguments = listOf(
                                    navArgument("url") { 
                                        type = NavType.StringType
                                        defaultValue = ""
                                    }
                                )
                            ) { backStackEntry ->
                                val url = backStackEntry.arguments?.getString("url") ?: ""
                                ProductDetailScreen(productUrl = url, navController = navController)
                            }
                            
                            composable("category/{name}") { backStackEntry ->
                                val name = backStackEntry.arguments?.getString("name") ?: ""
                                CategoryScreen(categoryName = name, navController = navController)
                            }

                            composable("popular_products") { BestProductsScreen(type = BestProductsType.POPULAR, navController = navController) }
                            
                            composable(Screen.MarketTrends.route) { MarketTrendsScreen(navController = navController) }
                            
                            composable(Screen.Help.route) {
                                HelpSupportScreen(onBack = { navController.navigateUp() })
                            }
                        }
                    }
                }
            }
        }
    }
}
