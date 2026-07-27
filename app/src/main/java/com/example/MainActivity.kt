package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.TranslationHelper
import kotlinx.coroutines.delay

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.data.RepositoryProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val repository = RepositoryProvider.getRepository(applicationContext)
        val substanceRepository = com.example.data.SubstanceRepositoryProvider.getRepository(applicationContext)
        lifecycleScope.launch {
            repository.prepopulateIfEmpty()
            substanceRepository.prepopulateIfEmpty()
        }
        
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val authManager = remember { com.example.data.AuthManager.getInstance(context) }
            val savedEmail = remember { authManager.getSignedInUser() ?: "" }

            var isDarkTheme by rememberSaveable { mutableStateOf(true) }
            var userEmail by rememberSaveable { mutableStateOf(savedEmail) }
            var hasEnteredJungle by rememberSaveable { mutableStateOf(savedEmail.isNotEmpty()) }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                if (userEmail.isEmpty()) {
                    LandingScreen(
                        onSignUpSuccess = { email -> 
                            userEmail = email 
                        }
                    )
                } else if (!hasEnteredJungle) {
                    JungleLandingScreen(
                        userEmail = userEmail,
                        onEnterApp = { hasEnteredJungle = true }
                    )
                } else {
                    MainAppContainer(
                        userEmail = userEmail,
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = { isDarkTheme = it },
                        onSignOutClick = {
                            userEmail = ""
                            hasEnteredJungle = false
                        }
                    )
                }
            }
        }
    }
}

sealed class NavigationTab(val route: String, val label: String, val activeIcon: ImageVector, val inactiveIcon: ImageVector) {
    object Scanner : NavigationTab("scanner", "Scan AI", Icons.Default.CameraAlt, Icons.Default.CameraAlt)
    object Reagents : NavigationTab("testing", "Reagents", Icons.Default.Science, Icons.Default.Science)
    object Home : NavigationTab("dashboard", "Home", Icons.Default.Home, Icons.Default.Home)
    object Checklist : NavigationTab("tips", "Harm Reduction", Icons.Default.Shield, Icons.Default.Shield)
    object Profile : NavigationTab("profile", "Profile", Icons.Default.Person, Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(
    userEmail: String,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    onSignOutClick: () -> Unit = {}
) {
    var selectedLanguage by rememberSaveable { mutableStateOf("en") }
    TranslationHelper.currentLanguage = selectedLanguage

    var isPremium by rememberSaveable { mutableStateOf(false) }
    var showPremiumUpgradeDialog by remember { mutableStateOf(false) }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val haptic = LocalHapticFeedback.current

    // Bottom tabs layout configuration: Home is perfectly placed in the middle (index 2 of 5)
    val tabs = listOf(
        NavigationTab.Scanner,
        NavigationTab.Reagents,
        NavigationTab.Home,
        NavigationTab.Checklist,
        NavigationTab.Profile
    )

    val shouldShowBottomBar = currentRoute in tabs.map { it.route }

    val context = LocalContext.current
    
    // One-Tap Emergency modal state
    var showEmergencyModal by remember { mutableStateOf(false) }
    var selectedRegionIndex by remember { mutableStateOf(0) }

    // PWA dialog simulation state
    var showPwaDialog by remember { mutableStateOf(false) }
    var pwaInstallProgress by remember { mutableStateOf(0f) }
    var isPwaInstalling by remember { mutableStateOf(false) }
    var isPwaInstalled by remember { mutableStateOf(false) }

    LaunchedEffect(isPwaInstalling) {
        if (isPwaInstalling) {
            pwaInstallProgress = 0f
            while (pwaInstallProgress < 1f) {
                delay(100)
                pwaInstallProgress += 0.05f
            }
            isPwaInstalling = false
            isPwaInstalled = true
        }
    }

    Scaffold(
        topBar = {
            if (shouldShowBottomBar) {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Fancy subtle logo indicator
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF00FFCC), Color(0xFFFF007F))
                                        ),
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "SubstanceID",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 19.sp,
                                letterSpacing = 1.sp
                            )
                            if (isPremium) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(Color(0xFFFFD700), Color(0xFFFF8C00))
                                            ),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "PREMIUM",
                                        color = Color.Black,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        // Dark/Light Theme Toggle (Sun/Moon)
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onThemeToggle(!isDarkTheme)
                            },
                            modifier = Modifier.testTag("top_theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Theme Toggle",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Download as PWA offline app icon
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showPwaDialog = true
                            },
                            modifier = Modifier.testTag("pwa_download_button")
                        ) {
                            Icon(
                                imageVector = if (isPwaInstalled) Icons.Default.OfflinePin else Icons.Default.DownloadForOffline,
                                contentDescription = "PWA Install Icon",
                                tint = if (isPwaInstalled) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                            )
                        }

                        // Emergency Red Panic shortcut button (highly visible)
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showEmergencyModal = true
                            },
                            modifier = Modifier.testTag("top_emergency_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = "Emergency Panic Button",
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            if (shouldShowBottomBar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .navigationBarsPadding()
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp, top = 4.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .testTag("app_navigation_bar"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            tabs.forEach { tab ->
                                val isSelected = currentRoute == tab.route
                                val animatedScale by animateFloatAsState(
                                    targetValue = if (isSelected) 1.15f else 1.0f,
                                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                                    label = "icon_scale"
                                )
                                val contentColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                }
                                
                                val translatedLabel = when (tab.route) {
                                    "scanner" -> TranslationHelper.get("nav_scanner", selectedLanguage)
                                    "testing" -> TranslationHelper.get("nav_reagents", selectedLanguage)
                                    "dashboard" -> TranslationHelper.get("nav_home", selectedLanguage)
                                    "tips" -> TranslationHelper.get("nav_tips", selectedLanguage)
                                    "profile" -> TranslationHelper.get("nav_profile", selectedLanguage)
                                    else -> tab.label
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable {
                                            if (currentRoute != tab.route) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                navController.navigate(tab.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        }
                                        .testTag("nav_tab_${tab.route}"),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                                        contentDescription = tab.label,
                                        tint = contentColor,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .graphicsLayer(
                                                scaleX = animatedScale,
                                                scaleY = animatedScale
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = translatedLabel,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = contentColor,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = NavigationTab.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(NavigationTab.Home.route) {
                    DashboardScreen(
                        onDrugSelected = { drugId ->
                            navController.navigate("detail/$drugId")
                        },
                        onEmergencyClicked = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate("emergency")
                        },
                        onTestingClicked = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate(NavigationTab.Reagents.route)
                        },
                        onAIChatClicked = { substance ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val route = if (substance != null) "ai_chat?substance=$substance" else "ai_chat"
                            navController.navigate(route)
                        },
                        selectedLanguage = selectedLanguage
                    )
                }

                composable(
                    route = "detail/{drugId}",
                    arguments = listOf(navArgument("drugId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val drugId = backStackEntry.arguments?.getString("drugId") ?: "mdma"
                    DrugDetailScreen(
                        drugId = drugId,
                        onBack = { navController.popBackStack() },
                        onAIChatClicked = { substance ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate("ai_chat?substance=$substance")
                        }
                    )
                }

                composable(NavigationTab.Scanner.route) {
                    ScannerScreen(
                        isPremium = isPremium,
                        onUpgradeClick = { showPremiumUpgradeDialog = true }
                    )
                }

                composable(NavigationTab.Reagents.route) {
                    TestingScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("emergency") {
                    EmergencyScreen(
                        userEmail = userEmail,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "ai_chat?substance={substance}",
                    arguments = listOf(navArgument("substance") { type = NavType.StringType; nullable = true; defaultValue = null })
                ) { backStackEntry ->
                    val substance = backStackEntry.arguments?.getString("substance")
                    AIAdvisorScreen(
                        initialSubstanceContext = substance,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(NavigationTab.Checklist.route) {
                    TipsScreen()
                }

                composable(NavigationTab.Profile.route) {
                    ProfileScreen(
                        userEmail = userEmail,
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = onThemeToggle,
                        selectedLanguage = selectedLanguage,
                        onLanguageChange = { selectedLanguage = it },
                        isPremium = isPremium,
                        onUpgradeClick = { showPremiumUpgradeDialog = true },
                        onSignOutClick = onSignOutClick
                    )
                }
            }

            // PWA SIMULATED INSTALL DIALOG
            if (showPwaDialog) {
                AlertDialog(
                    onDismissRequest = { showPwaDialog = false },
                    confirmButton = {
                        if (!isPwaInstalled && !isPwaInstalling) {
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    isPwaInstalling = true
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("INSTALL NOW")
                            }
                        } else if (isPwaInstalled) {
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showPwaDialog = false
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("DONE")
                            }
                        }
                    },
                    dismissButton = {
                        if (!isPwaInstalling) {
                            TextButton(onClick = { showPwaDialog = false }) {
                                Text("CLOSE")
                            }
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AppShortcut,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPwaInstalled) "SubstanceID Installed!" else "Download SubstanceID PWA",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    text = {
                        Column {
                            if (!isPwaInstalled && !isPwaInstalling) {
                                Text(
                                    "Save SubstanceID to your home screen. Installing this standalone light application unlocks:",
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("• 100% full offline database access\n• Zero network latency for reagent lookups\n• Lightweight storage footprint (under 3MB)\n• No Google Play account required", fontSize = 12.sp, lineHeight = 17.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else if (isPwaInstalling) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        progress = pwaInstallProgress,
                                        modifier = Modifier.size(48.dp),
                                        strokeWidth = 4.dp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "Caching offline databases... ${(pwaInstallProgress * 100).toInt()}%",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                            } else {
                                Text(
                                    "SubstanceID has been cached offline and successfully added to your device's launcher screen. You can safely launch this guide in flight mode.",
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("pwa_install_dialog")
                )
            }

            // ONE-TAP EMERGENCY MODAL
            if (showEmergencyModal) {
                val currentRegion = regionalEmergencyData[selectedRegionIndex]
                AlertDialog(
                    onDismissRequest = { showEmergencyModal = false },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(
                            onClick = { showEmergencyModal = false },
                            modifier = Modifier.testTag("one_tap_close_button")
                        ) {
                            Text("CLOSE", fontWeight = FontWeight.Bold)
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "One-Tap Emergency",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFD32F2F)
                            )
                        }
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Choose region to localize. Tap the red button to call emergency services directly.",
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Region selection horizontal chips list
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(regionalEmergencyData) { index, region ->
                                    val isSelected = selectedRegionIndex == index
                                    Card(
                                        modifier = Modifier
                                            .heightIn(min = 40.dp)
                                            .testTag("region_chip_${region.regionName.replace(" ", "_")}")
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                selectedRegionIndex = index
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) Color(0xFFD32F2F) else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 14.dp, vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${region.flag} ${region.regionName}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            // Big Red Panic Call Button
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${currentRegion.emergencyNumber}"))
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("one_tap_dial_button_${currentRegion.emergencyNumber}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = "Call Services",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "CALL ${currentRegion.emergencyNumber} (${currentRegion.regionName})",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            // Regional Support Helpline info
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${currentRegion.supportName} Support",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = currentRegion.supportDesc,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${currentRegion.supportNumber}"))
                                            context.startActivity(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(36.dp)
                                            .testTag("one_tap_support_button_${currentRegion.supportNumber}")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Phone,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Call Support (${currentRegion.supportNumber})",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }

                            // Link to Open CPR Metronome & CPR Protocol Guide
                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showEmergencyModal = false
                                    navController.navigate("emergency")
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .testTag("one_tap_open_protocol_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Science, // represents CPR screen
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Open Full CPR Metronome Guide",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("one_tap_emergency_dialog")
                )
            }

            // MOCK INTERACTIVE PREMIUM SUBSCRIPTION CHECKOUT DIALOG
            if (showPremiumUpgradeDialog) {
                var cardNumber by remember { mutableStateOf("") }
                var expiryDate by remember { mutableStateOf("") }
                var cvc by remember { mutableStateOf("") }
                var cardName by remember { mutableStateOf("") }
                
                var isProcessing by remember { mutableStateOf(false) }
                var isSuccess by remember { mutableStateOf(false) }
                
                val dialogScope = rememberCoroutineScope()
                
                AlertDialog(
                    onDismissRequest = { 
                        if (!isProcessing) showPremiumUpgradeDialog = false 
                    },
                    confirmButton = {},
                    dismissButton = {
                        if (!isProcessing && !isSuccess) {
                            TextButton(onClick = { showPremiumUpgradeDialog = false }) {
                                Text("CANCEL", fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSuccess) "Subscription Active!" else "Unlock SubstanceID Premium",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (isSuccess) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .background(Color(0xFF4CAF50).copy(alpha = 0.15f), shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Subscribed Successfully!",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Your Specimen Camera Scanner and Sitter Directory Registration features are now fully unlocked for 7 USD / month.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { showPremiumUpgradeDialog = false },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("DISMISS", fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else if (isProcessing) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(48.dp),
                                        strokeWidth = 4.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Contacting secure gateway...",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Establishing end-to-end encrypted integration",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Text(
                                    text = "Complete your secure subscription checkout. Unlocks unlimited specimen camera scans and official guide listings.",
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                
                                OutlinedTextField(
                                    value = cardName,
                                    onValueChange = { cardName = it },
                                    label = { Text("Cardholder Name") },
                                    placeholder = { Text("Soren Vance") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                OutlinedTextField(
                                    value = cardNumber,
                                    onValueChange = { if (it.length <= 19) cardNumber = it },
                                    label = { Text("Card Number") },
                                    placeholder = { Text("4111 2222 3333 4444") },
                                    singleLine = true,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = expiryDate,
                                        onValueChange = { if (it.length <= 5) expiryDate = it },
                                        label = { Text("Expiry (MM/YY)") },
                                        placeholder = { Text("12/29") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1.2f)
                                    )
                                    OutlinedTextField(
                                        value = cvc,
                                        onValueChange = { if (it.length <= 4) cvc = it },
                                        label = { Text("CVC") },
                                        placeholder = { Text("123") },
                                        singleLine = true,
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                        ),
                                        modifier = Modifier.weight(0.8f)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Button(
                                    onClick = {
                                        if (cardName.isBlank() || cardNumber.length < 12) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        } else {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            isProcessing = true
                                            dialogScope.launch {
                                                delay(1500)
                                                isProcessing = false
                                                isSuccess = true
                                                isPremium = true
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("payment_submit_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("SUBSCRIBE NOW — 7 USD / mo", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("premium_upgrade_dialog")
                )
            }
        }
    }
}

data class RegionalEmergencyInfo(
    val regionName: String,
    val flag: String,
    val emergencyNumber: String,
    val supportName: String,
    val supportNumber: String,
    val supportDesc: String
)

val regionalEmergencyData = listOf(
    RegionalEmergencyInfo(
        regionName = "United States",
        flag = "🇺🇸",
        emergencyNumber = "911",
        supportName = "Never Use Alone",
        supportNumber = "1-800-484-3731",
        supportDesc = "A toll-free phone line for people using drugs alone. An operator stays on line and calls EMS if unresponsive."
    ),
    RegionalEmergencyInfo(
        regionName = "Canada",
        flag = "🇨🇦",
        emergencyNumber = "911",
        supportName = "NORS Hotline",
        supportNumber = "1-888-688-6677",
        supportDesc = "National Overdose Response Service. Confidential, non-judgmental support 24/7."
    ),
    RegionalEmergencyInfo(
        regionName = "United Kingdom",
        flag = "🇬🇧",
        emergencyNumber = "999",
        supportName = "Release Helpline",
        supportNumber = "020-7324-2989",
        supportDesc = "Free and confidential specialist drug advice and support in the UK."
    ),
    RegionalEmergencyInfo(
        regionName = "Europe (EU)",
        flag = "🇪🇺",
        emergencyNumber = "112",
        supportName = "European Support",
        supportNumber = "112",
        supportDesc = "Call 112 for any medical emergencies in all EU member countries."
    ),
    RegionalEmergencyInfo(
        regionName = "Australia",
        flag = "🇦🇺",
        emergencyNumber = "000",
        supportName = "ADIS Support",
        supportNumber = "1800-250-015",
        supportDesc = "Alcohol and Drug Information Service. 24/7 free support across Australia."
    )
)
