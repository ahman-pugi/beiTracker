package com.ahmanpg.beitracker.ui.screen

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ahmanpg.beitracker.R
import com.ahmanpg.beitracker.ui.components.GlassButton
import com.ahmanpg.beitracker.ui.components.HelpIcon
import com.ahmanpg.beitracker.ui.theme.*
import com.ahmanpg.beitracker.viewmodel.AuthViewModel
import com.ahmanpg.beitracker.viewmodel.PriceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    priceViewModel: PriceViewModel,
    authViewModel: AuthViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by priceViewModel.uiState.collectAsState()
    val user by authViewModel.user.collectAsState()
    
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    
    var showProfileEdit by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }
    var showThemeSelector by remember { mutableStateOf(false) }
    var showLanguageSelector by remember { mutableStateOf(false) }
    var showRegionSelector by remember { mutableStateOf(false) }
    
    var tempName by remember(user?.displayName, uiState.userName) { mutableStateOf(user?.displayName ?: uiState.userName) }
    var tempBio by remember(uiState.userBio) { mutableStateOf(uiState.userBio) }

    val joinDateStr = remember(uiState.joinDate) {
        val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        sdf.format(Date(uiState.joinDate))
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Decorative background
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(BeiAccentGreen.copy(alpha = 0.05f), Color.Transparent),
                    radius = size.width * 1.2f
                ),
                center = Offset(size.width * 0.1f, size.height * 0.9f)
            )
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.settings),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f), CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(20.dp))
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // PROFILE SECTION
                item {
                    GlassSettingsCard {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(64.dp)) {
                                    if (uiState.profileImageUri != null) {
                                        AsyncImage(
                                            model = uiState.profileImageUri,
                                            contentDescription = "Profile Image",
                                            modifier = Modifier.fillMaxSize().clip(CircleShape).border(1.dp, BeiAccentGreen.copy(alpha = 0.2f), CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Surface(
                                            modifier = Modifier.fillMaxSize(),
                                            shape = CircleShape,
                                            color = BeiAccentGreen.copy(alpha = 0.1f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, BeiAccentGreen.copy(alpha = 0.2f))
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    (user?.displayName ?: uiState.userName).take(1).uppercase(),
                                                    color = BeiAccentGreen,
                                                    fontSize = 26.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }
                                    }
                                    
                                    Box(
                                        modifier = Modifier.align(Alignment.BottomEnd).size(20.dp).background(MaterialTheme.colorScheme.surface, CircleShape).padding(2.dp).background(BeiAccentGreen, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Verified, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(user?.displayName ?: uiState.userName, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
                                        Spacer(Modifier.width(8.dp))
                                        Surface(
                                            color = BeiAccentGreen.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                uiState.accountType.uppercase(),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black,
                                                color = BeiAccentGreen
                                            )
                                        }
                                    }
                                    Text(user?.email ?: uiState.userEmail, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                                }
                                
                                IconButton(
                                    onClick = { showProfileEdit = true },
                                    modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(16.dp))
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                uiState.userBio,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Stats Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProfileStatItem(label = "TRACKED", count = uiState.trackedProducts.size.toString())
                                Box(modifier = Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)))
                                ProfileStatItem(label = "SAVED", count = "TZS ${String.format("%,.0f", uiState.totalSavings)}")
                                Box(modifier = Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)))
                                ProfileStatItem(label = "SINCE", count = joinDateStr)
                            }
                        }
                    }
                }

                // APPEARANCE & LOCALIZATION
                item {
                    SettingsSectionTitle("APPEARANCE & LOCAL")
                    GlassSettingsCard {
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            val themeLabel = when(uiState.themeMode) {
                                1 -> "Light Mode"
                                2 -> "Dark Mode"
                                else -> "System Default"
                            }
                            SettingsActionItem(
                                icon = Icons.Default.Palette,
                                title = "Theme",
                                subtitle = themeLabel,
                                onClick = { showThemeSelector = true }
                            )

                            SettingsActionItem(
                                icon = Icons.Default.Language,
                                title = "Language",
                                subtitle = if (uiState.preferredLanguage == "sw") "Kiswahili" else "English",
                                onClick = { showLanguageSelector = true }
                            )

                            SettingsActionItem(
                                icon = Icons.Default.Public,
                                title = "Region",
                                subtitle = uiState.preferredRegion,
                                onClick = { showRegionSelector = true }
                            )
                        }
                    }
                }

                // SMART ALERTS ENGINE
                item {
                    SettingsSectionTitle("SMART ALERTS")
                    GlassSettingsCard {
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            NotificationToggleItem(
                                title = "Push Notifications",
                                subtitle = "Real-time price volatility pings",
                                checked = uiState.notificationsEnabled,
                                onCheckedChange = { priceViewModel.updateNotificationsEnabled(it) }
                            )
                            
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)))
                            
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.FilterList, null, tint = BeiAccentGreen, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Alert Threshold", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                                    HelpIcon(
                                        title = stringResource(R.string.help_alert_threshold_title),
                                        description = stringResource(R.string.help_alert_threshold_desc),
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                    Spacer(Modifier.weight(1f))
                                    Text(if (uiState.alertThresholdPercent == 0) "Any Drop" else "> ${uiState.alertThresholdPercent}%", color = BeiAccentGreen, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                }
                                Slider(
                                    value = uiState.alertThresholdPercent.toFloat(),
                                    onValueChange = { priceViewModel.updateAlertThreshold(it.toInt()) },
                                    valueRange = 0f..50f,
                                    steps = 9,
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.onBackground,
                                        activeTrackColor = BeiAccentGreen,
                                        inactiveTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                                    )
                                )
                            }
                        }
                    }
                }

                // SYNC ENGINE
                item {
                    SettingsSectionTitle("SYNC ENGINE")
                    GlassSettingsCard {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Sync, null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Update every ${uiState.checkIntervalHours} hours", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp)
                                        HelpIcon(
                                            title = stringResource(R.string.help_sync_engine_title),
                                            description = stringResource(R.string.help_sync_engine_desc),
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }
                                    val (mode, color) = when {
                                        uiState.checkIntervalHours <= 3 -> "High Performance" to BeiPriceDropRed
                                        uiState.checkIntervalHours <= 12 -> "Balanced" to BeiAccentGreen
                                        else -> "Energy Saver" to MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                                    }
                                    Text(mode, fontSize = 11.sp, color = color, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            Slider(
                                value = uiState.checkIntervalHours.toFloat(),
                                onValueChange = { priceViewModel.updateCheckInterval(it.toInt()) },
                                valueRange = 1f..24f,
                                steps = 23,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.onBackground,
                                    activeTrackColor = BeiAccentGreen,
                                    inactiveTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                                )
                            )
                        }
                    }
                }

                // DATA MANAGEMENT
                item {
                    SettingsSectionTitle("DATA")
                    GlassSettingsCard {
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            SettingsActionItem(
                                icon = Icons.Default.CloudSync,
                                title = "Force Market Refresh",
                                onClick = { priceViewModel.refreshAllPrices() }
                            )
                            SettingsActionItem(
                                icon = Icons.Default.DeleteSweep,
                                title = "Clear Recent Searches",
                                onClick = { priceViewModel.clearSearchHistory() }
                            )
                        }
                    }
                }

                // ACCOUNT ACTIONS
                item {
                    SettingsSectionTitle("ACCOUNT")
                    GlassSettingsCard {
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            SettingsActionItem(
                                icon = Icons.AutoMirrored.Filled.Logout,
                                title = "Sign Out",
                                titleColor = BeiPriceDropRed,
                                onClick = { showSignOutConfirm = true }
                            )
                        }
                    }
                }
                
                item {
                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }

    // Modal Components
    if (showThemeSelector) {
        AlertDialog(
            onDismissRequest = { showThemeSelector = false },
            title = { Text("App Theme", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground) },
            text = {
                Column {
                    listOf("System Default" to 0, "Light Mode" to 1, "Dark Mode" to 2).forEach { (label, mode) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { 
                                    priceViewModel.updateThemeMode(mode)
                                    showThemeSelector = false 
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.themeMode == mode,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = BeiAccentGreen)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(label, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {},
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (showLanguageSelector) {
        AlertDialog(
            onDismissRequest = { showLanguageSelector = false },
            title = { Text("Language", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground) },
            text = {
                Column {
                    listOf("English" to "en", "Kiswahili" to "sw").forEach { (label, code) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { 
                                    priceViewModel.updateLanguage(code)
                                    val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(code)
                                    AppCompatDelegate.setApplicationLocales(appLocale)
                                    showLanguageSelector = false 
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.preferredLanguage == code,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = BeiAccentGreen)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(label, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {},
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (showRegionSelector) {
        AlertDialog(
            onDismissRequest = { showRegionSelector = false },
            title = { Text("Region", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground) },
            text = {
                Column {
                    listOf("Tanzania", "Kenya", "Uganda").forEach { region ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { 
                                    priceViewModel.updateRegion(region)
                                    showRegionSelector = false 
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.preferredRegion == region,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = BeiAccentGreen)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(region, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {},
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (showProfileEdit) {
        AlertDialog(
            onDismissRequest = { 
                keyboardController?.hide()
                focusManager.clearFocus()
                showProfileEdit = false 
            },
            title = { Text("Edit Identity", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    TextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    
                    TextField(
                        value = tempBio,
                        onValueChange = { tempBio = it },
                        label = { Text("Bio") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            },
            confirmButton = {
                GlassButton(
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        authViewModel.updateProfile(tempName)
                        priceViewModel.updateProfile(tempName, user?.email ?: uiState.userEmail, tempBio)
                        showProfileEdit = false
                    },
                    containerColor = MaterialTheme.colorScheme.onBackground
                ) {
                    Text("Update", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.background)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    showProfileEdit = false 
                }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = { Text("Sign Out", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Black) },
            text = { 
                Text("Are you sure you want to sign out?", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)) 
            },
            confirmButton = {
                GlassButton(
                    onClick = {
                        authViewModel.signOut()
                        showSignOutConfirm = false
                        onBack()
                    },
                    containerColor = BeiPriceDropRed
                ) {
                    Text("Sign Out", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(28.dp)
        )
    }
}

@Composable
fun ProfileStatItem(label: String, count: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            count,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            label,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
        letterSpacing = 2.sp,
        modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
    )
}

@Composable
fun GlassSettingsCard(content: @Composable () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
        shape = RoundedCornerShape(32.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(24.dp)) {
            content()
        }
    }
}

@Composable
fun NotificationToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = BeiAccentGreen,
                uncheckedThumbColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                uncheckedTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
fun SettingsActionItem(
    icon: ImageVector, 
    title: String, 
    subtitle: String? = null,
    titleColor: Color? = null, 
    onClick: () -> Unit
) {
    val actualTitleColor = titleColor ?: MaterialTheme.colorScheme.onBackground
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = actualTitleColor.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, color = actualTitleColor, fontSize = 15.sp)
            if (subtitle != null) {
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f), fontWeight = FontWeight.Black)
            }
        }
        Spacer(Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f), modifier = Modifier.size(20.dp))
    }
}
