package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import com.example.data.AuthManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(
    userEmail: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager.getInstance(context) }
    val emergencyContactName = remember(userEmail) { authManager.getEmergencyContactName(userEmail) ?: "" }
    val emergencyContactNumber = remember(userEmail) { authManager.getEmergencyContactNumber(userEmail) ?: "" }
    var isMetronomeActive by remember { mutableStateOf(false) }
    var metronomePulse by remember { mutableStateOf(false) }

    // Start a coroutine to pulse the metronome at 110 BPM (approx 545ms per beat)
    LaunchedEffect(isMetronomeActive) {
        if (isMetronomeActive) {
            while (true) {
                metronomePulse = true
                delay(80)
                metronomePulse = false
                delay(465) // ~545ms interval for 110 BPM
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EMERGENCY GUIDE", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F)) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("emergency_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFFD32F2F))
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // IMMEDIATE LIFE THREAT WARNING
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_emergency_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E0000) // Deep red high-contrast background
                    ),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF3B30)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Section Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Emergency Warning",
                                tint = Color(0xFFFF3B30),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "QUICK EMERGENCY RESPONDER",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // INDICATORS: WHEN TO CALL EMERGENCY SERVICES
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFD32F2F), shape = RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "WHEN TO CALL EMERGENCY SERVICES:",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Call immediately if the person exhibits any of these critical symptoms:\n" +
                                    "• UNRESPONSIVE / unconscious (cannot wake them up)\n" +
                                    "• SEVERE BREATHING DIFFICULTY (shallow, slow, or snoring sounds)\n" +
                                    "• BLUE/GRAY/PALE lips, fingernails, or cold clammy skin\n" +
                                    "• SEIZURES or continuous uncontrollable shaking\n" +
                                    "• EXTREME OVERHEATING (hot dry skin or severe confusion)",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // PROTOCOL: IMMEDIATE OVERDOSE RESPONSE PROTOCOL
                        Text(
                            "⚡ IMMEDIATE OVERDOSE RESPONSE",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFF3B30),
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val protocolSteps = listOf(
                            "1. SHAKE & SHOUT: Gently shake their shoulders and shout their name to check responsiveness.",
                            "2. DEPLOY NALOXONE / NARCAN: If you suspect opioid/fentanyl exposure, spray Narcan up one nostril. Repeat in 2-3 mins if they don't respond.",
                            "3. START RESCUE BREATHING & CPR: If breathing is shallow or absent, start chest compressions. Use the CPR Metronome below.",
                            "4. RECOVERY POSITION: If breathing but unconscious, roll them onto their side (recovery position) to prevent aspiration."
                        )

                        protocolSteps.forEach { step ->
                            Text(
                                text = step,
                                color = Color.White.copy(alpha = 0.95f),
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ACTION BUTTON 1: CALL 911 / LOCAL RESCUERS
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD32F2F),
                                contentColor = Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("call_911_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("CALL 911 (EMERGENCY SERVICES) NOW", fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // ACTION BUTTON 2: CALL PERSONAL EMERGENCY CONTACT
                        if (emergencyContactNumber.isNotEmpty()) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$emergencyContactNumber"))
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE65100), // High-contrast orange
                                    contentColor = Color.White
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("call_personal_emergency_contact_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ContactPhone, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "CALL $emergencyContactName ($emergencyContactNumber)".uppercase(),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp))
                                    .border(
                                        androidx.compose.foundation.BorderStroke(
                                            1.dp, 
                                            Color.White.copy(alpha = 0.2f)
                                        ), 
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No custom emergency contact set.\nConfigure one in Profile settings for speed dial.",
                                    color = Color.LightGray.copy(alpha = 0.8f),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // INTERACTIVE SYMPTOM TRIAGE & HARM REDUCTION CHECKER
            item {
                SymptomCheckerSection(
                    context = context,
                    haptic = LocalHapticFeedback.current
                )
            }

            // CRITICAL ACTION WORKFLOW
            item {
                Text(
                    "Emergency Protocol Checklist",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(emergencySteps) { step ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFFD32F2F).copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = step.stepNumber.toString(),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F),
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = step.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = step.description,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // CPR COMPRESSION METRONOME TOOL
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "CPR Compression Metronome",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "Chest compressions during CPR must be delivered at a rate of 100 to 120 compressions per minute. Use this visual/audio metronome to pace yourself.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Pulsing Visual Target
                        Box(
                            modifier = Modifier.size(90.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val pulseScale by animateFloatAsState(
                                targetValue = if (metronomePulse) 1.25f else 0.9f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                                label = "pulse"
                            )

                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .background(
                                        color = if (metronomePulse) Color(0xFFD32F2F) else Color(0xFFD32F2F).copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(35.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            // Outline pulse
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = Color(0xFFD32F2F).copy(alpha = if (metronomePulse) 0.5f else 0f),
                                    radius = (size.minDimension / 2f) * pulseScale,
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (isMetronomeActive) "110 COMPRESSIONS / MINUTE" else "METRONOME DEACTIVATED",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isMetronomeActive) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { isMetronomeActive = !isMetronomeActive },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isMetronomeActive) Color.DarkGray else Color(0xFFD32F2F)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("metronome_toggle_button")
                        ) {
                            Text(
                                if (isMetronomeActive) "STOP METRONOME" else "START CPR METRONOME",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // EXTRA SUPPORT & SUBSTANCE RESOURCES
            item {
                Column(modifier = Modifier.padding(bottom = 24.dp)) {
                    Text(
                        "National Support Contacts",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    SupportLinkCard(
                        title = "SAMHSA National Helpline",
                        subtitle = "Free, confidential, 24/7 treatment referral and mental health support.",
                        number = "1-800-662-4357",
                        onCall = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:18006624357"))
                            context.startActivity(intent)
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SupportLinkCard(
                        title = "Never Use Alone Inc.",
                        subtitle = "A toll-free phone number for individuals using drugs alone. A operator will stay on the line and dispatch EMS if you stop responding.",
                        number = "1-800-484-3731",
                        onCall = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:18004843731"))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

data class EmergencyStep(
    val stepNumber: Int,
    val title: String,
    val description: String
)

val emergencySteps = listOf(
    EmergencyStep(
        stepNumber = 1,
        title = "Assess Responsiveness & Breathe",
        description = "Gently shake the person and shout their name. Check if their chest is rising and falling. Look at their fingernails and lips—if they are blue, pale, or grey, oxygen levels are dangerously low."
    ),
    EmergencyStep(
        stepNumber = 2,
        title = "Call Emergency Responders (911)",
        description = "Immediately call 911 or your local emergency line. Provide your exact location. State that the person is unconscious or struggling to breathe. You are protected by 'Good Samaritan Laws' in most jurisdictions."
    ),
    EmergencyStep(
        stepNumber = 3,
        title = "Administer Naloxone (Narcan) if Opioid Overdose",
        description = "If you suspect an opioid overdose (heroin, oxycodone, counterfeit pressed pills/fentanyl): Spray 1 full dose of Naloxone into one nostril. Wait 2-3 minutes. If they do not respond, administer a second dose in the other nostril."
    ),
    EmergencyStep(
        stepNumber = 4,
        title = "Perform CPR if Not Breathing",
        description = "Lay the person flat on their back on a hard surface. Place your hands in the center of their chest and compress firmly at 100-120 beats per minute (use the metronome below). Deliver 30 compressions followed by 2 rescue breaths."
    ),
    EmergencyStep(
        stepNumber = 5,
        title = "Place in Recovery Position if Breathing",
        description = "If they are breathing but unconscious, roll them onto their side with their knee bent to prop them up. This stabilizes their airway and prevents choking/asphyxiation on vomit."
    )
)

@Composable
fun SupportLinkCard(
    title: String,
    subtitle: String,
    number: String,
    onCall: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, fontSize = 11.sp, lineHeight = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(number, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                Button(
                    onClick = onCall,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.height(32.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
    }
}

enum class SymptomSeverity {
    LOW, MEDIUM, HIGH
}

data class Symptom(
    val id: String,
    val name: String,
    val severity: SymptomSeverity,
    val category: String,
    val description: String
)

val symptomsList = listOf(
    Symptom("slow_breathe", "Slow/shallow breathing (<8 breaths/min)", SymptomSeverity.HIGH, "Breathing", "Highly indicative of depressant or opioid overdose."),
    Symptom("blue_lips", "Blue/grayish lips or fingernails", SymptomSeverity.HIGH, "Breathing", "Indicates severe oxygen deprivation (hypoxia)."),
    Symptom("chest_pain", "Severe chest pain or pressure", SymptomSeverity.HIGH, "Heart & Chest", "Sign of cardiac distress or potential heart attack."),
    Symptom("unresponsive", "Completely unresponsive to touch/shout", SymptomSeverity.HIGH, "Mental State", "Critical state requiring immediate rescue actions."),
    Symptom("seizure", "Seizures or continuous shaking", SymptomSeverity.HIGH, "Physical", "Severe medical emergency. Protect head and airway."),
    Symptom("overheating", "Extreme overheating (hyperthermia)", SymptomSeverity.HIGH, "Physical", "Risk of heatstroke from stimulants or MDMA."),
    
    Symptom("rapid_heart", "Extremely rapid/racing heart rate", SymptomSeverity.MEDIUM, "Heart & Chest", "Often caused by stimulants, anxiety, or panic."),
    Symptom("panic_anxiety", "Severe panic, anxiety, or paranoia", SymptomSeverity.MEDIUM, "Mental State", "Common in challenging trips or heavy stimulant use."),
    Symptom("hyperventilate", "Hyperventilating (rapid breathing)", SymptomSeverity.MEDIUM, "Breathing", "Frequently linked to panic attacks or heavy anxiety."),
    
    Symptom("confusion", "Confusion, loops, or derealization", SymptomSeverity.LOW, "Mental State", "Typical psychedelic/dissociative onset effect."),
    Symptom("muscle_rigidity", "Muscle tension or jaw clenching", SymptomSeverity.LOW, "Physical", "Common physical effect of stimulants or entactogens."),
    Symptom("stomach_upset", "Mild nausea or stomach cramps", SymptomSeverity.LOW, "Physical", "Common onset side effect of many substances.")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomCheckerSection(
    context: android.content.Context,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    var selectedSymptomIds by remember { mutableStateOf(setOf<String>()) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    
    val categories = listOf("All", "Breathing", "Heart & Chest", "Mental State", "Physical")
    
    val filteredSymptoms = remember(selectedCategoryFilter) {
        if (selectedCategoryFilter == "All") {
            symptomsList
        } else {
            symptomsList.filter { it.category == selectedCategoryFilter }
        }
    }
    
    val selectedSymptoms = remember(selectedSymptomIds) {
        symptomsList.filter { it.id in selectedSymptomIds }
    }
    
    val triageSeverity = remember(selectedSymptoms) {
        when {
            selectedSymptoms.any { it.severity == SymptomSeverity.HIGH } -> SymptomSeverity.HIGH
            selectedSymptoms.any { it.severity == SymptomSeverity.MEDIUM } -> SymptomSeverity.MEDIUM
            selectedSymptoms.isNotEmpty() -> SymptomSeverity.LOW
            else -> null
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("symptom_checker_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when (triageSeverity) {
                SymptomSeverity.HIGH -> Color(0xFFD32F2F).copy(alpha = 0.6f)
                SymptomSeverity.MEDIUM -> Color(0xFFFF9800).copy(alpha = 0.6f)
                SymptomSeverity.LOW -> Color(0xFF4CAF50).copy(alpha = 0.6f)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = when (triageSeverity) {
                        SymptomSeverity.HIGH -> Icons.Default.Warning
                        SymptomSeverity.MEDIUM -> Icons.Default.Warning
                        SymptomSeverity.LOW -> Icons.Default.CheckCircle
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = when (triageSeverity) {
                        SymptomSeverity.HIGH -> Color(0xFFD32F2F)
                        SymptomSeverity.MEDIUM -> Color(0xFFFF9800)
                        SymptomSeverity.LOW -> Color(0xFF4CAF50)
                        else -> MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Symptom Triage Checker",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Select any active symptoms to assess risk level and view targeted harm reduction or emergency procedures.",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategoryFilter == category
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            selectedCategoryFilter = category
                        },
                        label = { Text(category, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("symptom_category_$category")
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                filteredSymptoms.forEach { symptom ->
                    val isChecked = symptom.id in selectedSymptomIds
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                selectedSymptomIds = if (isChecked) {
                                    selectedSymptomIds - symptom.id
                                } else {
                                    selectedSymptomIds + symptom.id
                                }
                            }
                            .testTag("symptom_card_${symptom.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isChecked) {
                                when (symptom.severity) {
                                    SymptomSeverity.HIGH -> Color(0xFFD32F2F).copy(alpha = 0.12f)
                                    SymptomSeverity.MEDIUM -> Color(0xFFFF9800).copy(alpha = 0.12f)
                                    SymptomSeverity.LOW -> Color(0xFF4CAF50).copy(alpha = 0.12f)
                                }
                            } else {
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            }
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isChecked) {
                                when (symptom.severity) {
                                    SymptomSeverity.HIGH -> Color(0xFFD32F2F).copy(alpha = 0.7f)
                                    SymptomSeverity.MEDIUM -> Color(0xFFFF9800).copy(alpha = 0.7f)
                                    SymptomSeverity.LOW -> Color(0xFF4CAF50).copy(alpha = 0.7f)
                                }
                            } else {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            }
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    selectedSymptomIds = if (checked == true) {
                                        selectedSymptomIds + symptom.id
                                    } else {
                                        selectedSymptomIds - symptom.id
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = when (symptom.severity) {
                                        SymptomSeverity.HIGH -> Color(0xFFD32F2F)
                                        SymptomSeverity.MEDIUM -> Color(0xFFFF9800)
                                        SymptomSeverity.LOW -> Color(0xFF4CAF50)
                                    }
                                ),
                                modifier = Modifier.testTag("symptom_checkbox_${symptom.id}").size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = symptom.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = when (symptom.severity) {
                                                    SymptomSeverity.HIGH -> Color(0xFFD32F2F).copy(alpha = 0.2f)
                                                    SymptomSeverity.MEDIUM -> Color(0xFFFF9800).copy(alpha = 0.2f)
                                                    SymptomSeverity.LOW -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                                },
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = symptom.severity.name,
                                            color = when (symptom.severity) {
                                                SymptomSeverity.HIGH -> Color(0xFFD32F2F)
                                                SymptomSeverity.MEDIUM -> Color(0xFFFF9800)
                                                SymptomSeverity.LOW -> Color(0xFF4CAF50)
                                            },
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = symptom.description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 14.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            AnimatedVisibility(
                visible = selectedSymptomIds.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("triage_result_panel"),
                    colors = CardDefaults.cardColors(
                        containerColor = when (triageSeverity) {
                            SymptomSeverity.HIGH -> Color(0xFFD32F2F).copy(alpha = 0.08f)
                            SymptomSeverity.MEDIUM -> Color(0xFFFF9800).copy(alpha = 0.08f)
                            SymptomSeverity.LOW -> Color(0xFF4CAF50).copy(alpha = 0.08f)
                            else -> MaterialTheme.colorScheme.surface
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when (triageSeverity) {
                            SymptomSeverity.HIGH -> Color(0xFFD32F2F).copy(alpha = 0.4f)
                            SymptomSeverity.MEDIUM -> Color(0xFFFF9800).copy(alpha = 0.4f)
                            SymptomSeverity.LOW -> Color(0xFF4CAF50).copy(alpha = 0.4f)
                            else -> MaterialTheme.colorScheme.outlineVariant
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (triageSeverity) {
                                        SymptomSeverity.HIGH -> Icons.Default.Warning
                                        SymptomSeverity.MEDIUM -> Icons.Default.Warning
                                        else -> Icons.Default.CheckCircle
                                    },
                                    contentDescription = null,
                                    tint = when (triageSeverity) {
                                        SymptomSeverity.HIGH -> Color(0xFFD32F2F)
                                        SymptomSeverity.MEDIUM -> Color(0xFFFF9800)
                                        else -> Color(0xFF4CAF50)
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (triageSeverity) {
                                        SymptomSeverity.HIGH -> "TRIAGE: RED (CRITICAL RISK)"
                                        SymptomSeverity.MEDIUM -> "TRIAGE: YELLOW (MODERATE RISK)"
                                        else -> "TRIAGE: GREEN (LOW/MILD)"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = when (triageSeverity) {
                                        SymptomSeverity.HIGH -> Color(0xFFD32F2F)
                                        SymptomSeverity.MEDIUM -> Color(0xFFFF9800)
                                        else -> Color(0xFF4CAF50)
                                    },
                                    modifier = Modifier.testTag("triage_status_text")
                                )
                            }
                            
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    selectedSymptomIds = emptySet()
                                },
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("clear_symptoms_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Selected Symptoms",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = when (triageSeverity) {
                                SymptomSeverity.HIGH -> "IMMEDIATE EMERGENCY ACTION REQUIRED:"
                                SymptomSeverity.MEDIUM -> "HARM REDUCTION & ACTIVE MONITORING:"
                                else -> "COMFORT MEASURES & REASSURANCE:"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 0.5.sp
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val specificAdvice = remember(selectedSymptomIds) {
                            val adviceList = mutableListOf<String>()
                            if ("slow_breathe" in selectedSymptomIds || "blue_lips" in selectedSymptomIds) {
                                adviceList.add("Opioid overdose likely. ADMINISTER NALOXONE (NARCAN) immediately.")
                                adviceList.add("Roll onto side into the RECOVERY POSITION to keep airway clear.")
                            }
                            if ("unresponsive" in selectedSymptomIds) {
                                adviceList.add("Unconsciousness is a top-tier emergency. Call 911 now.")
                                adviceList.add("If breathing stops, immediately start chest compressions (see metronome below).")
                            }
                            if ("chest_pain" in selectedSymptomIds) {
                                adviceList.add("Suspected heart distress. Do NOT allow physical exertion. Call 911.")
                            }
                            if ("seizure" in selectedSymptomIds) {
                                adviceList.add("Place soft item under their head, clear surrounding hard objects. Call 911.")
                                adviceList.add("Do NOT restrain them or place anything in their mouth.")
                            }
                            if ("overheating" in selectedSymptomIds) {
                                adviceList.add("Stimulant hyperthermia risk. Move to cool/shaded room immediately.")
                                adviceList.add("Apply cool, damp towels to forehead, armpits, and neck. Sip cool water.")
                            }
                            if ("rapid_heart" in selectedSymptomIds || "hyperventilate" in selectedSymptomIds) {
                                adviceList.add("Sit down, close eyes, and execute slow, rhythmic box breathing (4s inhale, 4s hold, 4s exhale, 4s hold).")
                                adviceList.add("Excessive anxiety mimicking cardiac arrest is common. Keep monitoring, but offer absolute reassurance.")
                            }
                            if ("panic_anxiety" in selectedSymptomIds) {
                                adviceList.add("Challenging psychedelic or stimulant state. Transition to a calm, softly-lit, sensory-minimal environment.")
                                adviceList.add("Have a trusted sober person stay nearby, speaking in soothing tones. Avoid crowded spots.")
                            }
                            if ("confusion" in selectedSymptomIds) {
                                adviceList.add("Standard sensory disorientation from hallucinogens or dissociatives. Safe trip sitter supervision is highly recommended.")
                            }
                            if ("muscle_rigidity" in selectedSymptomIds) {
                                adviceList.add("Common stimulant tension. Stay hydrated with electrolytes. Chew gum to protect teeth.")
                            }
                            if ("stomach_upset" in selectedSymptomIds) {
                                adviceList.add("Onset nausea. Sit upright, drink ginger tea or small sips of water. Avoid lying flat immediately.")
                            }
                            if (adviceList.isEmpty()) {
                                adviceList.add("Stay calm, sit comfortably, and continuously monitor breathing and heart rate.")
                            }
                            adviceList
                        }
                        
                        specificAdvice.forEach { bullet ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "•",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = when (triageSeverity) {
                                        SymptomSeverity.HIGH -> Color(0xFFD32F2F)
                                        SymptomSeverity.MEDIUM -> Color(0xFFFF9800)
                                        else -> Color(0xFF4CAF50)
                                    },
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                                Text(
                                    text = bullet,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        if (triageSeverity == SymptomSeverity.HIGH) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .testTag("triage_call_911_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("CALL EMERGENCY (911)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else if (triageSeverity == SymptomSeverity.MEDIUM) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:18006624357"))
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .testTag("triage_call_helpline_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("CALL HELPLINE (1-800-662-4357)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

