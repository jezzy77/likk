package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipsScreen(
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Care & Hydration", "Testing Guide", "Interactions", "Safety Checklist", "Trusted Repositories")

    // Checklist items state
    val checklistItems = remember {
        mutableStateListOf(
            "Verify chemical content using reagent testing kits & Fentanyl test strips.",
            "Determine dose threshold and avoid double-dosing or redosing early.",
            "Designate a sober supervisor (Trip Sitter) who remains clear-headed.",
            "Equip the environment with Naloxone (Narcan) and know how to use it.",
            "Establish a safe, comfortable, temperature-controlled decompression room.",
            "Acquire hydration supplies (electrolyte drinks, water) and set reminder timers.",
            "Ensure trusted friends know your exact location and substance details."
        )
    }
    val checklistChecked = remember {
        mutableStateListOf(false, false, false, false, false, false, false)
    }

    // Hydration alarm simulation state
    var isHydrationAlarmActive by remember { mutableStateOf(false) }
    var hydrationAlertTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(isHydrationAlarmActive) {
        if (isHydrationAlarmActive) {
            // Simulate a short 5-second interval for testing, normally 1 hour
            delay(5000)
            hydrationAlertTriggered = true
        }
    }

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // HORIZONTAL CATEGORY SWITCHER (ELEGANT AND ACCESSIBLE)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(tabs) { index, tabTitle ->
                    val isSelected = selectedTabIndex == index
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        label = {
                            Text(
                                text = tabTitle,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("harm_tab_$index").height(40.dp)
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // TAB CONTENT CONTAINER
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTabIndex) {
                    0 -> HydrationAndCareTab(
                        isHydrationAlarmActive = isHydrationAlarmActive,
                        hydrationAlertTriggered = hydrationAlertTriggered,
                        onToggleAlarm = {
                            if (isHydrationAlarmActive) {
                                isHydrationAlarmActive = false
                                hydrationAlertTriggered = false
                            } else {
                                isHydrationAlarmActive = true
                                hydrationAlertTriggered = false
                            }
                        },
                        onDismissAlert = { hydrationAlertTriggered = false }
                    )
                    1 -> TestingGuideTab()
                    2 -> InteractionMatrixTab()
                    3 -> ChecklistTab(
                        checklistItems = checklistItems,
                        checklistChecked = checklistChecked,
                        onToggleItem = { index ->
                            checklistChecked[index] = !checklistChecked[index]
                        }
                    )
                    4 -> TrustedRepositoriesTab()
                }
            }
        }
    }
}

// ==========================================
// TAB 1: HYDRATION & PHYSICAL CARE
// ==========================================
@Composable
fun HydrationAndCareTab(
    isHydrationAlarmActive: Boolean,
    hydrationAlertTriggered: Boolean,
    onToggleAlarm: () -> Unit,
    onDismissAlert: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // OVERVIEW HERO
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Temperature & Hydration",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Critical guidelines on fluid intake, thermoregulation, and electrolyte balance when consuming stimulants or psychedelics.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // HYDRATION ALARM CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Visual Hydration Timer",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        if (isHydrationAlarmActive) {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "ACTIVE (5s TEST)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Overheating (hyperthermia) is a primary risk with amphetamines and MDMA. Activate this timer to simulate safe hydration alerts every few seconds.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AnimatedVisibility(
                        visible = hydrationAlertTriggered,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF00ACC1))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "DRINK WATER PROMPT",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        "Take 250ml (1 cup) of water or an electrolyte drink. Take a break to cool off.",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                                IconButton(onClick = onDismissAlert) {
                                    Icon(Icons.Default.Check, contentDescription = "Dismiss", tint = Color.White)
                                }
                            }
                        }
                    }

                    Button(
                        onClick = onToggleAlarm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isHydrationAlarmActive) Color.DarkGray else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("hydration_timer_button")
                    ) {
                        Text(
                            if (isHydrationAlarmActive) "STOP WATER ALARM" else "START HYDRATION TIMER",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // CLINICAL EVIDENCE RULES
        item {
            Text(
                "Evidence-Based Physical Guidelines",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QueryStats, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Dosing Fluid Amounts Safely", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "• If Active/Dancing: Sip approx. 500ml (2 cups) of fluid per hour to compensate for heavy sweating.\n" +
                                "• If Resting/Sitting: Sip approx. 250ml (1 cup) of fluid per hour max. Do not force fluids as MDMA causes water retention (SIADH), which can trigger fatal hyponatremia (water intoxication) if excess water is consumed.",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalActivity, contentDescription = null, tint = Color(0xFFF57C00), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Electrolytes vs. Pure Water", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Drinking massive quantities of pure water dilutes sodium levels in your blood. Always prioritize electrolyte-infused beverages (sports drinks, coconut water) to maintain healthy neural osmotic pressure and prevent brain swelling.",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Thermostat, contentDescription = null, tint = Color(0xFF7B1FA2), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Serotonin Syndrome & Hyperthermia", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Extreme core temperature spikes (above 104°F/40°C) cause organ breakdown. Watch for warning indicators: high heart rate, complete lack of sweating despite heavy dancing, dilated pupils, muscle stiffness, and severe confusion. Seek immediate emergency services and ice packs.",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ==========================================
// TAB 2: TESTING TECHNIQUES
// ==========================================
@Composable
fun TestingGuideTab() {
    var expandedStepIndex by remember { mutableStateOf(-1) }

    val reagentSteps = listOf(
        TestingStep(
            title = "1. Prepare Ceramic Surface",
            icon = Icons.Default.CleaningServices,
            color = Color(0xFF1E88E5),
            shortDesc = "Reagents contain highly corrosive acids. Always test on ceramic.",
            longDesc = "Scrape a white ceramic plate or the underside of a mug. Wipe it completely clean of dust or contaminants with rubbing alcohol. Do not test on plastic, wood, or paper which will corrode and contaminate color readouts."
        ),
        TestingStep(
            title = "2. Use a Microscopic Sample",
            icon = Icons.Default.ZoomIn,
            color = Color(0xFF43A047),
            shortDesc = "A tiny speck is all that is required for accurate reactions.",
            longDesc = "Crush your tablet or crystals. Separate a minute crumb (roughly the size of a single pinhead or poppy seed). Using too much substance creates thick black sludge that obscures visual secondary color transitions."
        ),
        TestingStep(
            title = "3. Squeeze Exactly One Droplet",
            icon = Icons.Default.Opacity,
            color = Color(0xFFE53935),
            shortDesc = "Hold reagent bottle vertically. Do not touch bottle tip to substance.",
            longDesc = "Carefully squeeze a single droplet of the reagent directly onto the sample crumb. Holding the bottle 1-2 cm above avoids static suction of chemical powder back into the bottle, which ruins the rest of your kit."
        ),
        TestingStep(
            title = "4. Watch the First 60 Seconds",
            icon = Icons.Default.Schedule,
            color = Color(0xFF8E24AA),
            shortDesc = "The initial reaction speed and color shift are critical clues.",
            longDesc = "Focus entirely on the immediate contact point. Note the colors that flash within 1-5 seconds. For example, a true Marquis reaction to MDMA turns deep purple, then transitions to pitch black with bubbling in less than 3 seconds. Disregard any slow color changes that happen after 2 minutes due to oxygen oxidation."
        ),
        TestingStep(
            title = "5. Fentanyl Strip Dilution",
            icon = Icons.Default.Science,
            color = Color(0xFF00ACC1),
            shortDesc = "Fentanyl strips require a precise water ratio to avoid false results.",
            longDesc = "Fentanyl strips are highly sensitive. Dissolve 10mg of your powder in 1ml of lukewarm water (or 1 full cup of water for 100mg if testing entire batch). Dip strip up to the MAX line for 15 seconds. Hold flat for 2 minutes. ONE line is POSITIVE for Fentanyl (Discard Substance!). TWO lines is NEGATIVE."
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.FactCheck,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Interactive Spot Testing Guide",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Tap each step below to learn accurate, clinical-grade testing mechanics. Reagents can burn skin; handle with absolute care.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        item {
            Text(
                "Scientific Spot-Testing Steps",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        itemsIndexed(reagentSteps) { index, step ->
            val isExpanded = expandedStepIndex == index
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedStepIndex = if (isExpanded) -1 else index }
                    .testTag("testing_step_card_$index"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 2.dp else 1.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(step.color.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = step.icon,
                                contentDescription = null,
                                tint = step.color,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = step.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = step.shortDesc,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand details",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = step.longDesc,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

data class TestingStep(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val shortDesc: String,
    val longDesc: String
)

// ==========================================
// TAB 3: SUBSTANCE INTERACTIONS
// ==========================================
@Composable
fun InteractionMatrixTab() {
    val combinations = listOf(
        InteractionCombo("Alcohol", "Ketamine / GHB", "CRITICAL DANGER", "Extreme respiratory depression, blackouts, and choking risk on vomit. Fatal.", Color(0xFFD32F2F)),
        InteractionCombo("Alcohol", "Cocaine / Stimulants", "HIGH RISK", "Forms Cocaethylene in the liver, which is significantly more toxic to cardiorespiratory pathways.", Color(0xFFE65100)),
        InteractionCombo("Opioids", "Benzodiazepines / Alcohol", "CRITICAL DANGER", "Primary cause of fatal overdoses. Synergistic central nervous system shutdown.", Color(0xFFD32F2F)),
        InteractionCombo("MDMA", "MAOIs / SSRI Antidepressants", "CRITICAL DANGER", "Can trigger fatal Serotonin Syndrome or blunt MDMA entirely, leading to dangerous redosing.", Color(0xFFD32F2F)),
        InteractionCombo("MDMA", "Cocaine", "HIGH RISK", "Cocaine blocks serotonin transporters, blunting MDMA's empathy while heavily increasing cardiotoxic strain.", Color(0xFFE65100)),
        InteractionCombo("Cannabis", "Psychedelics (LSD/Shrooms)", "UNPREDICTABLE", "Heavily intensifies visual and mental loop processing. Frequent cause of acute panic attacks.", Color(0xFF7B1FA2)),
        InteractionCombo("Ketamine", "Stimulants (Speed/Cocaine)", "MODERATE RISK", "Highly conflicting signals to heart rate and blood pressure. Avoid high physical exertion.", Color(0xFFF57C00))
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Dangerous,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Substance Combination Risks",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Many medical emergencies originate from combining incompatible drug families. Review these synergistic biological risks.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        item {
            Text(
                "Critical Combination Warning Matrix",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        itemsIndexed(combinations) { index, combo ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = combo.drugA,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = " + ",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = combo.drugB,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(combo.badgeColor.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = combo.riskLevel,
                                color = combo.badgeColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = combo.description,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

data class InteractionCombo(
    val drugA: String,
    val drugB: String,
    val riskLevel: String,
    val description: String,
    val badgeColor: Color
)

// ==========================================
// TAB 4: SAFETY CHECKLIST
// ==========================================
@Composable
fun ChecklistTab(
    checklistItems: List<String>,
    checklistChecked: List<Boolean>,
    onToggleItem: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.PlaylistAddCheck,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Harm Reduction Checklist",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Confirm these physical, mental, and environmental safety pillars are verified prior to any session.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Session Safety Protocol",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                val checkedCount = checklistChecked.count { it }
                Text(
                    text = "$checkedCount / ${checklistItems.size} Done",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        itemsIndexed(checklistItems) { index, itemText ->
            val isChecked = checklistChecked[index]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToggleItem(index) }
                    .testTag("checklist_card_$index"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { onToggleItem(index) },
                        modifier = Modifier.testTag("checklist_checkbox_$index")
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = itemText,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = if (isChecked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // SET & SETTING SUMMARY CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalHospital, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("The Golden Rule: Set & Setting", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• Set (Mindset): Never consume substances to escape severe emotional pain, grief, or panic. It frequently amplifies negative emotions, causing terrifying psychedelic loops or compulsive stimulant abuse.\n\n" +
                                "• Setting (Environment): Always establish a physical space where you feel secure, warm, and surrounded by people who respect your boundaries and can respond rationally in an emergency.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ==========================================
// TAB 5: TRUSTED REPOSITORIES & ORGS
// ==========================================
data class RepositoryInfo(
    val name: String,
    val url: String,
    val specialty: String,
    val category: String,
    val description: String,
    val color: Color,
    val icon: ImageVector,
    val isOnion: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrustedRepositoriesTab() {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val repositories = remember {
        listOf(
            RepositoryInfo(
                name = "DanceSafe Coalition",
                url = "https://www.dancesafe.org/",
                specialty = "Testing Kits & Education",
                category = "Testing & Alerts",
                description = "DanceSafe is a peer-led harm reduction organization promoting health and safety in electronic music and nightlife communities. They offer on-site pill checking, chemical reagent testing kits, fentanyl test strips, and factual drug education booklets.",
                color = Color(0xFF00FFCC),
                icon = Icons.Default.Science
            ),
            RepositoryInfo(
                name = "TripSit Platform",
                url = "https://tripsit.me/",
                specialty = "24/7 Peer Distress Support",
                category = "Peer Support",
                description = "TripSit is an online community focused on reducing the harm of substance use. They run a 24/7 live IRC/Discord chat system where people in distress can talk to compassionate, trained peers, alongside an interactive drug interaction matrix chart.",
                color = Color(0xFF00B0FF),
                icon = Icons.Default.Forum
            ),
            RepositoryInfo(
                name = "National Harm Reduction Coalition",
                url = "https://harmreduction.org/",
                specialty = "Overdose Prevention & Policy",
                category = "Research & Education",
                description = "A US-based advocacy group dedicated to providing resources for syringe services, Naloxone (Narcan) access, overdose prevention training, and public policy changes that respect the dignity of people who use drugs.",
                color = Color(0xFFFF1744),
                icon = Icons.Default.LocalHospital
            ),
            RepositoryInfo(
                name = "Erowid Psychoactive Vaults",
                url = "https://www.erowid.org/",
                specialty = "Substance Database & Reports",
                category = "Research & Education",
                description = "Erowid is a member-supported non-profit educational library providing factual, documented data on psychoactive plants and chemicals, including peer-reviewed experience reports, dosage grids, legislative history, and media analysis.",
                color = Color(0xFFFF9100),
                icon = Icons.Default.FactCheck
            ),
            RepositoryInfo(
                name = "PsychonautWiki Tor Portal",
                url = "http://vvedndyt433kopnhv6vejxnut54y5752vpxshjaqmj7ftwiu6quiv2ad.onion/",
                specialty = "Secure Tor Encyclopedia",
                category = "Research & Education",
                description = "A comprehensive, privacy-respecting wiki detailing substance effects, dosage thresholds, and durations. This secure Onion Router (Tor) address protects browser history and ensures anonymous access to clinical databases.",
                color = Color(0xFFD500F9),
                icon = Icons.Default.Opacity,
                isOnion = true
            ),
            RepositoryInfo(
                name = "Drugs and Me Toolkit",
                url = "https://drugsand.me/en/",
                specialty = "Neurochemistry & Guidebooks",
                category = "Research & Education",
                description = "Drugs and Me is an interactive social enterprise offering detailed education about the physical and psychological consequences of recreational drug use, neurochemistry maps, and step-by-step safety tools.",
                color = Color(0xFF1DE9B6),
                icon = Icons.Default.PlaylistAddCheck
            ),
            RepositoryInfo(
                name = "DrugWise UK Hub",
                url = "https://www.drugwise.org.uk/",
                specialty = "Evidence-Based UK Policy",
                category = "Research & Education",
                description = "DrugWise promotes evidence-based information on drugs, alcohol, and tobacco. They offer independent, objective, non-judgmental facts about substance regulation, history, and healthcare policies.",
                color = Color(0xFF90A4AE),
                icon = Icons.Default.QueryStats
            ),
            RepositoryInfo(
                name = "Saferparty Zurich Platform",
                url = "https://www.saferparty.ch/",
                specialty = "Swiss Mobile Lab Analysis",
                category = "Testing & Alerts",
                description = "A Swiss harm reduction platform backed by mobile laboratory checking. They publish highly accurate, real-time HPLC analysis alerts, warning consumers against dangerously high-dosage MDMA tablets or contaminated street powders.",
                color = Color(0xFFFF5252),
                icon = Icons.Default.Campaign
            )
        )
    }

    val filteredRepos = remember(searchQuery, selectedFilter) {
        repositories.filter { repo ->
            val matchesSearch = repo.name.contains(searchQuery, ignoreCase = true) ||
                    repo.specialty.contains(searchQuery, ignoreCase = true) ||
                    repo.description.contains(searchQuery, ignoreCase = true)
            val matchesFilter = selectedFilter == "All" || repo.category == selectedFilter
            matchesSearch && matchesFilter
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // TAB HEADER HERO CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Harm Reduction Directories",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Trusted external organizations and clinical databases. Expand your knowledge with peer-reviewed facts and emergency networks.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // INTERACTIVE FILTER SECTIONS
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search repositories...", color = Color.Gray, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear Search", tint = Color.Gray)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("repo_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Category Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    val filters = listOf("All", "Testing & Alerts", "Peer Support", "Research & Education")
                    items(filters) { filter ->
                        val isSelected = selectedFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.height(34.dp)
                        )
                    }
                }
            }
        }

        // DIRECTORY LIST
        if (filteredRepos.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No matching organizations found.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(filteredRepos) { repo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            BorderStroke(1.dp, repo.color.copy(alpha = 0.25f)),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .testTag("repo_card_${repo.name.lowercase().replace(" ", "_")}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Title / Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(repo.color.copy(alpha = 0.15f), shape = RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = repo.icon,
                                    contentDescription = null,
                                    tint = repo.color,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = repo.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    text = repo.url,
                                    fontSize = 11.sp,
                                    color = repo.color,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(top = 1.dp)
                                )
                            }

                            // Onion warning / specialty chip
                            Box(
                                modifier = Modifier
                                    .background(repo.color.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (repo.isOnion) "ONION" else repo.category.uppercase(),
                                    color = repo.color,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Specialty Field
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.OfflinePin,
                                contentDescription = null,
                                tint = repo.color.copy(alpha = 0.7f),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Specialty: ${repo.specialty}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Description
                        Text(
                            text = repo.description,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Tor Warning for onion url
                        if (repo.isOnion) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = repo.color.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = repo.color, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Onion Route: You must have a Tor-enabled browser (like Tor Browser for Android) installed and configured on your device to resolve this site.",
                                        fontSize = 10.sp,
                                        lineHeight = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Launch Website
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(repo.url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "No web browser found to open link.", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = repo.color,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("OPEN WEBSITE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Copy Link
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Harm Reduction Link", repo.url)
                                    clipboard.setPrimaryClip(clip)
                                    android.widget.Toast.makeText(context, "Copied URL to Clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("COPY URL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
