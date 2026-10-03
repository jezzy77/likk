package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Clinical Triage Severity Levels
 */
enum class TriageEmergencyLevel(
    val title: String,
    val subtitle: String,
    val containerColor: Color,
    val borderColor: Color,
    val contentColor: Color,
    val icon: ImageVector
) {
    CRITICAL(
        title = "CRITICAL EMERGENCY (RED)",
        subtitle = "IMMEDIATE LIFE THREAT - CALL 911 / EMS NOW",
        containerColor = Color(0xFF330005),
        borderColor = Color(0xFFFF2A4B),
        contentColor = Color(0xFFFF4D6A),
        icon = Icons.Default.Campaign
    ),
    HIGH(
        title = "HIGH RISK (ORANGE)",
        subtitle = "URGENT MEDICAL ATTENTION & ACTIVE INTERVENTION NEEDED",
        containerColor = Color(0xFF2E1700),
        borderColor = Color(0xFFFF9100),
        contentColor = Color(0xFFFFAB40),
        icon = Icons.Default.Warning
    ),
    MODERATE(
        title = "MODERATE RISK (YELLOW)",
        subtitle = "ACTIVE HARM REDUCTION MONITORING & DE-ESCALATION",
        containerColor = Color(0xFF262100),
        borderColor = Color(0xFFFFD600),
        contentColor = Color(0xFFFFEE58),
        icon = Icons.Default.Info
    ),
    STABLE(
        title = "MILD / STABLE (GREEN)",
        subtitle = "PHYSIOLOGICAL SIGNS WITHIN TOLERABLE RANGE",
        containerColor = Color(0xFF00240F),
        borderColor = Color(0xFF00E676),
        contentColor = Color(0xFF69F0AE),
        icon = Icons.Default.CheckCircle
    ),
    IDLE(
        title = "ASSESSMENT READY",
        subtitle = "Select patient's physiological signs below to evaluate emergency status",
        containerColor = Color(0xFF131C33),
        borderColor = Color(0xFF00FFCC),
        contentColor = Color(0xFF00FFCC),
        icon = Icons.Default.Healing
    )
}

/**
 * Physiological Signs Enums
 */
enum class ConsciousnessState(val label: String, val description: String, val isCritical: Boolean) {
    ALERT("Alert & Responsive", "Awake, oriented, talking normally", false),
    CONFUSED("Confused / Disoriented", "Slurred speech, derealization, hallucinations", false),
    STUPOR("Stupor / Drowsy", "Lethargic, responds only to pain or sternal rub", true),
    UNRESPONSIVE("Completely Unresponsive", "Cannot be aroused, limp, no response to shout/pain", true)
}

enum class BreathingSign(val label: String, val description: String, val isCritical: Boolean) {
    NORMAL("Normal Breathing", "Steady rhythm, ~12-20 breaths/min", false),
    SLOW_SHALLOW("Slow / Shallow (<8 bpm)", "Very infrequent breaths, barely moving chest", true),
    AGONAL_SNORING("Gasping / Agonal Snoring", "Choking sounds, irregular heavy snoring, 'death rattle'", true),
    RAPID_HYPERVENTILATE("Rapid / Hyperventilating", "Panting, fast breathing (>25 bpm), panic feeling", false),
    WHEEZING("Wheezing / Airway Tightness", "Stridor or struggling to inhale air", true)
}

enum class PulseSign(val label: String, val description: String, val isCritical: Boolean) {
    NORMAL("Normal (60-100 BPM)", "Steady regular pulse at rest", false),
    TACHYCARDIA("Racing / Pounding (>120 BPM)", "Hard pounding in chest, rapid heartbeat", false),
    BRADYCARDIA("Faint / Very Slow (<50 BPM)", "Weak pulse, difficult to detect", true),
    IRREGULAR("Irregular / Fluttering", "Skipping beats, erratic rhythm sensation", false)
}

enum class SkinTempSign(val label: String, val description: String, val isCritical: Boolean) {
    NORMAL("Warm & Dry", "Natural skin color and normal temperature", false),
    CYANOTIC_COLD("Cold, Clammy, Blue/Pale Lips", "Oxygen deprivation: bluish lips, nail beds, cold sweat", true),
    HYPERTHERMIC_HOT("Burning Hot / Extreme Fever", "Severe overheating, hot dry skin or drenched in sweat", true),
    SWEATY_FLUSHED("Sweaty & Flushed Red", "Mildly warm, red face, dilated capillaries", false)
}

enum class PupilReaction(val label: String, val description: String, val isCritical: Boolean) {
    NORMAL("Normal Reaction", "Pupils dilate and constrict with light", false),
    PINPOINT("Pinpoint Constricted Pupils", "Tiny dot pupils, unresponsive to dim light (Classic Opioid sign)", true),
    DILATED("Wide Dilated Pupils", "Very large black pupils covering iris (Stimulants / Psychedelics)", false),
    ASYMMETRIC("Unequal Pupil Size", "One pupil large, one small (Acute Neurological warning)", true)
}

enum class MotorSymptom(val id: String, val label: String, val isCritical: Boolean) {
    ACTIVE_SEIZURES("active_seizures", "Active Seizures / Convulsions", true),
    MUSCLE_RIGIDITY("muscle_rigidity", "Severe Muscle Rigidity (Stiff / Lockjaw)", true),
    TREMORS("tremors", "Tremors / Involuntary Shaking", false),
    SEVERE_CHEST_PAIN("severe_chest_pain", "Crushing Chest Pain / Pressure", true),
    PERSISTENT_VOMITING("persistent_vomiting", "Uncontrollable Vomiting / Choking Risk", true),
    PANIC_TERROR("panic_terror", "Severe Panic, Paranoia or Dread", false)
}

/**
 * Triage Evaluation Output
 */
data class EmergencyTriageAssessment(
    val level: TriageEmergencyLevel,
    val primarySyndrome: String,
    val summaryMessage: String,
    val immediateActions: List<String>,
    val showNaloxonePrompt: Boolean,
    val showRecoveryPositionPrompt: Boolean,
    val showCoolingPrompt: Boolean,
    val showSeizurePrompt: Boolean,
    val showBreathingPacer: Boolean,
    val dispatcherScript: String
)

/**
 * Clinical assessment algorithm based on emergency toxicology & harm reduction protocols.
 */
fun evaluatePhysiologicalSigns(
    consciousness: ConsciousnessState?,
    breathing: BreathingSign?,
    pulse: PulseSign?,
    skinTemp: SkinTempSign?,
    pupils: PupilReaction?,
    motorSigns: Set<MotorSymptom>
): EmergencyTriageAssessment {
    // If nothing selected yet
    if (consciousness == null && breathing == null && pulse == null && skinTemp == null && pupils == null && motorSigns.isEmpty()) {
        return EmergencyTriageAssessment(
            level = TriageEmergencyLevel.IDLE,
            primarySyndrome = "No Physiological Signs Selected",
            summaryMessage = "Tap the physiological categories below to input observable vital signs (Consciousness, Breathing, Pulse, Skin, Pupils, and Symptoms).",
            immediateActions = emptyList(),
            showNaloxonePrompt = false,
            showRecoveryPositionPrompt = false,
            showCoolingPrompt = false,
            showSeizurePrompt = false,
            showBreathingPacer = false,
            dispatcherScript = ""
        )
    }

    val isUnresponsive = consciousness == ConsciousnessState.UNRESPONSIVE
    val isStupor = consciousness == ConsciousnessState.STUPOR
    val isSlowBreathing = breathing == BreathingSign.SLOW_SHALLOW || breathing == BreathingSign.AGONAL_SNORING
    val isCyanotic = skinTemp == SkinTempSign.CYANOTIC_COLD
    val isPinpoint = pupils == PupilReaction.PINPOINT
    val isSeizure = MotorSymptom.ACTIVE_SEIZURES in motorSigns
    val isChestPain = MotorSymptom.SEVERE_CHEST_PAIN in motorSigns
    val isHyperthermic = skinTemp == SkinTempSign.HYPERTHERMIC_HOT
    val isMuscleRigid = MotorSymptom.MUSCLE_RIGIDITY in motorSigns
    val isBradycardia = pulse == PulseSign.BRADYCARDIA
    val isTachycardia = pulse == PulseSign.TACHYCARDIA
    val isHyperventilating = breathing == BreathingSign.RAPID_HYPERVENTILATE
    val isVomiting = MotorSymptom.PERSISTENT_VOMITING in motorSigns
    val isPanic = MotorSymptom.PANIC_TERROR in motorSigns

    // 1. Suspected Opioid / Central Depressant Overdose (Opioid Triad: Coma + Respiratory Depression + Miosis/Cyanosis)
    val isOpioidOverdose = (isSlowBreathing || isUnresponsive || isStupor) && (isPinpoint || isCyanotic || isBradycardia)

    // 2. Critical Emergency Conditions
    val isCritical = isOpioidOverdose || isUnresponsive || isSlowBreathing || isCyanotic || isSeizure || isChestPain || isHyperthermic

    if (isCritical) {
        val syndrome = when {
            isOpioidOverdose -> "CRITICAL: SUSPECTED OPIOID / DEPRESSANT OVERDOSE"
            isSeizure -> "CRITICAL: ACTIVE SEIZURE EMERGENCY"
            isChestPain -> "CRITICAL: ACUTE CARDIAC DISTRESS / CHEST PAIN"
            isHyperthermic && isMuscleRigid -> "CRITICAL: SEROTONIN TOXICITY / SEVERE HYPERTHERMIA"
            isHyperthermic -> "CRITICAL: SEVERE STIMULANT HYPERTHERMIA / HEATSTROKE"
            isSlowBreathing -> "CRITICAL: SEVERE RESPIRATORY DEPRESSION (HYPOXIA)"
            isUnresponsive -> "CRITICAL: PROFOUND UNCONSCIOUSNESS / COMA"
            else -> "CRITICAL MEDICAL EMERGENCY"
        }

        val actions = mutableListOf<String>()
        actions.add("DIAL 911 / EMS IMMEDIATELY. State clearly that the person is in medical distress.")
        if (isOpioidOverdose || isPinpoint || isSlowBreathing) {
            actions.add("ADMINISTER NALOXONE (NARCAN): 1 full spray into one nostril. Repeat in 2-3 minutes if no response.")
        }
        if (isSeizure) {
            actions.add("PROTECT AIRWAY & HEAD: Place a soft jacket or folded cloth under head. Clear away sharp or hard objects.")
            actions.add("DO NOT restrain their movement and NEVER insert anything into their mouth.")
        }
        if (isUnresponsive || isStupor) {
            actions.add("RECOVERY POSITION: If breathing, roll onto side to prevent asphyxiation from vomit.")
            actions.add("RESCUE BREATHS & CPR: If breathing stops completely, begin chest compressions at 100-120 BPM.")
        }
        if (isHyperthermic) {
            actions.add("AGGRESSIVE COOLING: Move to air-conditioned area, remove heavy layers, apply cool wet towels to neck, armpits, and groin.")
        }
        if (isChestPain) {
            actions.add("KEEP CALM & SEATED: Keep patient in a comfortable semi-seated posture. Restrict all physical exertion.")
        }

        val dispatcherSummary = buildString {
            append("Emergency: Person is ")
            append(consciousness?.label ?: "in severe distress")
            if (breathing != null) append(", breathing is ${breathing.label}")
            if (skinTemp != null) append(", skin is ${skinTemp.label}")
            if (pupils != null) append(", pupils are ${pupils.label}")
            if (motorSigns.isNotEmpty()) append(", showing: ${motorSigns.joinToString { it.label }}")
            append(". Suspected ${syndrome.lowercase()}. Immediate ambulance required.")
        }

        return EmergencyTriageAssessment(
            level = TriageEmergencyLevel.CRITICAL,
            primarySyndrome = syndrome,
            summaryMessage = "Physiological indicators show an imminent life threat requiring urgent professional emergency response and immediate first-aid intervention.",
            immediateActions = actions,
            showNaloxonePrompt = isOpioidOverdose || isPinpoint || isSlowBreathing,
            showRecoveryPositionPrompt = isUnresponsive || isStupor || isVomiting,
            showCoolingPrompt = isHyperthermic,
            showSeizurePrompt = isSeizure,
            showBreathingPacer = false,
            dispatcherScript = dispatcherSummary
        )
    }

    // 3. High Risk Conditions (Orange)
    val isHighRisk = (consciousness == ConsciousnessState.CONFUSED && isTachycardia) ||
            isMuscleRigid ||
            isVomiting ||
            (pupils == PupilReaction.ASYMMETRIC)

    if (isHighRisk) {
        val syndrome = when {
            isMuscleRigid -> "HIGH RISK: MUSCLE RIGIDITY & HYPERTONIA"
            isVomiting -> "HIGH RISK: SEVERE NAUSEA & ASPIRATION RISK"
            pupils == PupilReaction.ASYMMETRIC -> "HIGH RISK: ASYMMETRIC NEUROLOGICAL SIGNS"
            else -> "HIGH RISK: SEVERE STIMULANT OVERLOAD / TOXICITY"
        }

        val actions = listOf(
            "Do NOT leave the person alone. Constantly monitor breathing and level of consciousness.",
            if (isVomiting) "Keep person seated upright or in recovery position. Do not allow them to lie flat on their back." else "Have them sit down in a quiet, low-stimulus space.",
            if (isMuscleRigid) "Watch for rising body temperature or confusion. Seek urgent urgent medical evaluation." else "Sip cool water with electrolytes slowly if conscious.",
            "If consciousness declines or breathing slows, immediately dial 911."
        )

        val dispatcherSummary = "High Risk: Person is experiencing ${syndrome.lowercase()} with signs: ${consciousness?.label ?: ""}, pulse: ${pulse?.label ?: ""}, symptoms: ${motorSigns.joinToString { it.label }}."

        return EmergencyTriageAssessment(
            level = TriageEmergencyLevel.HIGH,
            primarySyndrome = syndrome,
            summaryMessage = "Patient exhibits physiological warning signs that could escalate rapidly into a medical emergency. Close active monitoring is critical.",
            immediateActions = actions,
            showNaloxonePrompt = false,
            showRecoveryPositionPrompt = isVomiting || consciousness == ConsciousnessState.CONFUSED,
            showCoolingPrompt = skinTemp == SkinTempSign.SWEATY_FLUSHED,
            showSeizurePrompt = false,
            showBreathingPacer = false,
            dispatcherScript = dispatcherSummary
        )
    }

    // 4. Moderate Risk (Yellow - Panic, Hyperventilation, Challenging Psychological State)
    val isModerateRisk = isHyperventilating || isPanic || isTachycardia || (consciousness == ConsciousnessState.CONFUSED)

    if (isModerateRisk) {
        val syndrome = when {
            isHyperventilating || isPanic -> "MODERATE RISK: ACUTE PANIC & HYPERVENTILATION"
            isTachycardia -> "MODERATE RISK: ADRENERGIC STIMULATION / ELEVATED PULSE"
            else -> "MODERATE RISK: DISORIENTATION / ALTERED PERCEPTION"
        }

        val actions = listOf(
            "GUIDED PACED BREATHING: Hyperventilation blows off carbon dioxide, causing dizziness and extremity tingling. Use the Box Breathing pacer below.",
            "GROUNDING & REASSURANCE: Reassure the person that this is a temporary substance effect, that they are safe, and that this state will pass.",
            "CHANGE ENVIRONMENT: Move away from loud music, strobe lights, and crowds into a quiet, dim, comfortable room.",
            "HYDRATION: Sip cool water gently. Avoid caffeine or other stimulants."
        )

        return EmergencyTriageAssessment(
            level = TriageEmergencyLevel.MODERATE,
            primarySyndrome = syndrome,
            summaryMessage = "Symptoms are likely linked to sympathetic arousal, hyperventilation, or sensory overwhelm. De-escalation and controlled breathing will provide rapid relief.",
            immediateActions = actions,
            showNaloxonePrompt = false,
            showRecoveryPositionPrompt = false,
            showCoolingPrompt = false,
            showSeizurePrompt = false,
            showBreathingPacer = true,
            dispatcherScript = "Moderate Risk: Subject is hyperventilating/experiencing severe panic. Heart rate elevated, breathing rapid."
        )
    }

    // 5. Stable / Mild (Green)
    return EmergencyTriageAssessment(
        level = TriageEmergencyLevel.STABLE,
        primarySyndrome = "MILD / TOLERABLE PHYSIOLOGICAL STATE",
        summaryMessage = "All entered physiological indicators are currently within stable boundaries. Continue to practice standard harm reduction and hydration.",
        immediateActions = listOf(
            "Stay adequately hydrated with small sips of water or electrolyte beverages.",
            "Rest in a comfortable environment and take regular breaks.",
            "Have a sober peer or friend check in periodically."
        ),
        showNaloxonePrompt = false,
        showRecoveryPositionPrompt = false,
        showCoolingPrompt = false,
        showSeizurePrompt = false,
        showBreathingPacer = false,
        dispatcherScript = ""
    )
}

/**
 * Complete Symptom Checker UI Component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomCheckerComponent(
    modifier: Modifier = Modifier,
    onEmergencyCall: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // State for physiological signs inputs
    var selectedConsciousness by rememberSaveable { mutableStateOf<ConsciousnessState?>(null) }
    var selectedBreathing by rememberSaveable { mutableStateOf<BreathingSign?>(null) }
    var selectedPulse by rememberSaveable { mutableStateOf<PulseSign?>(null) }
    var selectedSkinTemp by rememberSaveable { mutableStateOf<SkinTempSign?>(null) }
    var selectedPupils by rememberSaveable { mutableStateOf<PupilReaction?>(null) }
    var selectedMotorSigns by rememberSaveable { mutableStateOf(setOf<MotorSymptom>()) }

    // Evaluation
    val assessment = remember(
        selectedConsciousness,
        selectedBreathing,
        selectedPulse,
        selectedSkinTemp,
        selectedPupils,
        selectedMotorSigns
    ) {
        evaluatePhysiologicalSigns(
            selectedConsciousness,
            selectedBreathing,
            selectedPulse,
            selectedSkinTemp,
            selectedPupils,
            selectedMotorSigns
        )
    }

    // Modal or Guide dialog states
    var showNaloxoneGuideDialog by remember { mutableStateOf(false) }
    var showRecoveryPositionDialog by remember { mutableStateOf(false) }
    var showCoolingGuideDialog by remember { mutableStateOf(false) }
    var showSeizureGuideDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("symptom_checker_component"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // COMPONENT HEADER
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.dp, Color(0xFF00FFCC).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color(0xFF00FFCC), Color(0xFF0099FF))
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonitorHeart,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Emergency Symptom Checker",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Physiological Vitals & Triage Engine",
                                fontSize = 11.sp,
                                color = Color(0xFF00FFCC)
                            )
                        }
                    }

                    // Reset button
                    if (selectedConsciousness != null || selectedBreathing != null || selectedPulse != null ||
                        selectedSkinTemp != null || selectedPupils != null || selectedMotorSigns.isNotEmpty()
                    ) {
                        TextButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedConsciousness = null
                                selectedBreathing = null
                                selectedPulse = null
                                selectedSkinTemp = null
                                selectedPupils = null
                                selectedMotorSigns = emptySet()
                            },
                            modifier = Modifier.testTag("symptom_reset_button")
                        ) {
                            Text("Reset", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Observe the person's physical state. Select their active signs below to immediately identify potential overdose, serotonin toxicity, respiratory collapse, or hyperthermia.",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Color.LightGray
                )
            }
        }

        // DYNAMIC TRIAGE RESULT BANNER (Pinned Top Result)
        TriageStatusBanner(
            assessment = assessment,
            onEmergencyCall = {
                onEmergencyCall()
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
                context.startActivity(intent)
            },
            onCopyScript = { script ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("911 Dispatcher Script", script)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Copied dispatcher script to clipboard!", Toast.LENGTH_SHORT).show()
            }
        )

        // SPECIALIZED FIRST-AID PROTOCOL SHORTCUTS (Conditional on Triage)
        if (assessment.level != TriageEmergencyLevel.IDLE) {
            ProtocolQuickActionRow(
                assessment = assessment,
                onOpenNaloxone = { showNaloxoneGuideDialog = true },
                onOpenRecovery = { showRecoveryPositionDialog = true },
                onOpenCooling = { showCoolingGuideDialog = true },
                onOpenSeizure = { showSeizureGuideDialog = true }
            )
        }

        // INTERACTIVE BOX BREATHING PACER (For Panic/Hyperventilation)
        if (assessment.showBreathingPacer) {
            InteractiveBoxBreathingWidget()
        }

        // SECTION 1: CONSCIOUSNESS & MENTAL STATUS
        PhysiologicalGroupCard(
            title = "1. Consciousness & Alertness",
            subtitle = "Responsiveness to voice and touch",
            icon = Icons.Default.Psychology
        ) {
            ConsciousnessState.values().forEach { state ->
                PhysiologicalSignOptionItem(
                    label = state.label,
                    description = state.description,
                    isSelected = selectedConsciousness == state,
                    isCritical = state.isCritical,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedConsciousness = if (selectedConsciousness == state) null else state
                    },
                    testTag = "sign_consciousness_${state.name.lowercase()}"
                )
            }
        }

        // SECTION 2: BREATHING & RESPIRATORY PATTERN
        PhysiologicalGroupCard(
            title = "2. Breathing & Respiratory Pattern",
            subtitle = "Rate, chest movement, and respiratory sounds",
            icon = Icons.Default.Air
        ) {
            BreathingSign.values().forEach { sign ->
                PhysiologicalSignOptionItem(
                    label = sign.label,
                    description = sign.description,
                    isSelected = selectedBreathing == sign,
                    isCritical = sign.isCritical,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedBreathing = if (selectedBreathing == sign) null else sign
                    },
                    testTag = "sign_breathing_${sign.name.lowercase()}"
                )
            }
        }

        // SECTION 3: HEART RATE & PULSE
        PhysiologicalGroupCard(
            title = "3. Heart Rate & Pulse",
            subtitle = "Radial/carotid pulse rhythm and speed",
            icon = Icons.Default.Favorite
        ) {
            PulseSign.values().forEach { sign ->
                PhysiologicalSignOptionItem(
                    label = sign.label,
                    description = sign.description,
                    isSelected = selectedPulse == sign,
                    isCritical = sign.isCritical,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedPulse = if (selectedPulse == sign) null else sign
                    },
                    testTag = "sign_pulse_${sign.name.lowercase()}"
                )
            }
        }

        // SECTION 4: SKIN, LIPS & BODY TEMPERATURE
        PhysiologicalGroupCard(
            title = "4. Skin Perfusion & Temperature",
            subtitle = "Oxygenation color, moisture, and warmth",
            icon = Icons.Default.Thermostat
        ) {
            SkinTempSign.values().forEach { sign ->
                PhysiologicalSignOptionItem(
                    label = sign.label,
                    description = sign.description,
                    isSelected = selectedSkinTemp == sign,
                    isCritical = sign.isCritical,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedSkinTemp = if (selectedSkinTemp == sign) null else sign
                    },
                    testTag = "sign_skintemp_${sign.name.lowercase()}"
                )
            }
        }

        // SECTION 5: PUPIL REACTION
        PhysiologicalGroupCard(
            title = "5. Pupil Reaction",
            subtitle = "Pupillary constriction or dilation",
            icon = Icons.Default.Visibility
        ) {
            PupilReaction.values().forEach { reaction ->
                PhysiologicalSignOptionItem(
                    label = reaction.label,
                    description = reaction.description,
                    isSelected = selectedPupils == reaction,
                    isCritical = reaction.isCritical,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedPupils = if (selectedPupils == reaction) null else reaction
                    },
                    testTag = "sign_pupils_${reaction.name.lowercase()}"
                )
            }
        }

        // SECTION 6: MOTOR & SEVERE PHYSICAL SIGNS
        PhysiologicalGroupCard(
            title = "6. Critical Motor & Neurological Signs",
            subtitle = "Check any active symptoms present",
            icon = Icons.Default.Warning
        ) {
            MotorSymptom.values().forEach { motor ->
                val isChecked = motor in selectedMotorSigns
                PhysiologicalSignOptionItem(
                    label = motor.label,
                    description = if (motor.isCritical) "High priority emergency indicator" else "Supportive monitoring sign",
                    isSelected = isChecked,
                    isCritical = motor.isCritical,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedMotorSigns = if (isChecked) {
                            selectedMotorSigns - motor
                        } else {
                            selectedMotorSigns + motor
                        }
                    },
                    testTag = "sign_motor_${motor.id}"
                )
            }
        }
    }

    // NALOXONE / NARCAN FIRST AID MODAL
    if (showNaloxoneGuideDialog) {
        FirstAidGuideModal(
            title = "Naloxone (Narcan) Protocol",
            icon = Icons.Default.MedicalServices,
            iconTint = Color(0xFFFF2A4B),
            onDismiss = { showNaloxoneGuideDialog = false },
            steps = listOf(
                "CHECK RESPONSIVENESS: Shout their name and firmly rub your knuckles on the center of their breastbone (sternal rub). If no response, act immediately.",
                "PEEL & INSERT: Peel open the blister pack. Hold the nozzle with your fingers and place the tip of the nozzle in either nostril until your fingers touch the bottom of their nose.",
                "PRESS PLUNGER FIRMLY: Press the plunger firmly to release the entire 4mg dose into their nose. Do not prime or test the device beforehand.",
                "EVALUATE & REPEAT: Wait 2 to 3 minutes. If they do not wake up or breathing remains under 8 bpm, administer a SECOND dose into the OTHER nostril.",
                "RECOVERY POSITION: Once breathing resumes, place them on their side to prevent choking if they vomit."
            )
        )
    }

    // RECOVERY POSITION FIRST AID MODAL
    if (showRecoveryPositionDialog) {
        FirstAidGuideModal(
            title = "Recovery Position Guide",
            icon = Icons.Default.AirlineSeatFlat,
            iconTint = Color(0xFF00FFCC),
            onDismiss = { showRecoveryPositionDialog = false },
            steps = listOf(
                "KNEEL BESIDE THEM: Ensure the person is lying on their back with their legs straight out.",
                "EXTEND NEAR ARM: Place their arm closest to you at a right angle to their body with the palm facing up.",
                "CROSS FAR ARM: Bring their far arm across their chest and hold the back of their hand against their nearest cheek.",
                "BEND FAR LEG: With your other hand, pull their far knee up so their foot is flat on the ground.",
                "ROLL ONTO SIDE: Pull the bent knee towards you so they smoothly roll onto their side facing you.",
                "TILT HEAD BACK: Gently tilt their head back and lift their chin to ensure their airway remains completely open and saliva/vomit drains freely."
            )
        )
    }

    // ACTIVE COOLING FIRST AID MODAL
    if (showCoolingGuideDialog) {
        FirstAidGuideModal(
            title = "Active Cooling Protocol",
            icon = Icons.Default.AcUnit,
            iconTint = Color(0xFF00E5FF),
            onDismiss = { showCoolingGuideDialog = false },
            steps = listOf(
                "MOVE TO COOL SPACE: Immediately transition person out of heat, direct sun, or crowded dance floors into an air-conditioned room or shaded draft.",
                "REMOVE HEAVY LAYERS: Loosen tight collars, remove jackets, hats, and non-breathable clothing.",
                "APPLY COLD DAMP CLOTHS: Place cold, damp towels or ice packs wrapped in cloth on major vascular zones: the neck, armpits, and groin.",
                "MIST & FAN: Spray or sprinkle lukewarm/cool water over their body while vigorously fanning them to promote rapid evaporative heat loss.",
                "DO NOT USE ICE BATHS: Avoid plunging an unconscious or confused person into an ice bath, as severe shivering or vasoconstriction can paradoxically spike internal core temperatures."
            )
        )
    }

    // SEIZURE SAFETY FIRST AID MODAL
    if (showSeizureGuideDialog) {
        FirstAidGuideModal(
            title = "Seizure Safety Protocol",
            icon = Icons.Default.Shield,
            iconTint = Color(0xFFFF9100),
            onDismiss = { showSeizureGuideDialog = false },
            steps = listOf(
                "PROTECT THE HEAD: Place a soft jacket, pillow, or folded cloth directly underneath their head to prevent traumatic head injury.",
                "CLEAR DANGER: Move furniture, glass, tables, and sharp items away from their thrashing limbs.",
                "NEVER RESTRAIN: Do NOT attempt to pin the person down or restrict their convulsions. Let the seizure run its course naturally.",
                "NEVER PUT ANYTHING IN MOUTH: Do NOT insert objects, fingers, or spoons between their teeth. They will not swallow their tongue.",
                "TIME THE SEIZURE: Note the start time. Any seizure lasting longer than 3 minutes, or recurring seizures without waking up (Status Epilepticus), is a life-threatening emergency requiring immediate 911 dispatch.",
                "POST-SEIZURE CARE: Once the convulsions stop, immediately roll them onto their side into the recovery position and gently wipe their airway."
            )
        )
    }
}

/**
 * Top Status & Triage Banner
 */
@Composable
fun TriageStatusBanner(
    assessment: EmergencyTriageAssessment,
    onEmergencyCall: () -> Unit,
    onCopyScript: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("triage_status_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = assessment.level.containerColor),
        border = BorderStroke(2.dp, assessment.level.borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = assessment.level.icon,
                        contentDescription = null,
                        tint = assessment.level.contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = assessment.level.title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp,
                        color = assessment.level.contentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = assessment.primarySyndrome,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = assessment.summaryMessage,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = Color.White.copy(alpha = 0.9f)
            )

            // Immediate Action Steps Checklist
            if (assessment.immediateActions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = assessment.level.borderColor.copy(alpha = 0.4f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "ACTIONABLE FIRST-AID STEPS:",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp,
                    color = assessment.level.contentColor
                )
                Spacer(modifier = Modifier.height(6.dp))

                assessment.immediateActions.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(assessment.level.borderColor.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = step,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = Color.White.copy(alpha = 0.95f),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // High Priority 911 Call Bar for Critical / High
            if (assessment.level == TriageEmergencyLevel.CRITICAL) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onEmergencyCall,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2A4B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("triage_call_911_action_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CALL 911 (EMERGENCY SERVICES) NOW",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // Copy 911 Dispatcher Script Button
            if (assessment.dispatcherScript.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onCopyScript(assessment.dispatcherScript) },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, assessment.level.borderColor.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("triage_copy_script_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = assessment.level.contentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Copy 911 Dispatcher Summary Script",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Protocol Quick Action Row for First-Aid Modals
 */
@Composable
fun ProtocolQuickActionRow(
    assessment: EmergencyTriageAssessment,
    onOpenNaloxone: () -> Unit,
    onOpenRecovery: () -> Unit,
    onOpenCooling: () -> Unit,
    onOpenSeizure: () -> Unit
) {
    Column {
        Text(
            text = "FIRST-AID PROTOCOL GUIDES",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            color = Color.LightGray
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (assessment.showNaloxonePrompt || assessment.level == TriageEmergencyLevel.CRITICAL) {
                item {
                    ProtocolShortcutChip(
                        title = "Narcan / Naloxone",
                        icon = Icons.Default.MedicalServices,
                        accentColor = Color(0xFFFF2A4B),
                        onClick = onOpenNaloxone,
                        testTag = "protocol_chip_naloxone"
                    )
                }
            }
            if (assessment.showRecoveryPositionPrompt || assessment.level == TriageEmergencyLevel.CRITICAL || assessment.level == TriageEmergencyLevel.HIGH) {
                item {
                    ProtocolShortcutChip(
                        title = "Recovery Position",
                        icon = Icons.Default.AirlineSeatFlat,
                        accentColor = Color(0xFF00FFCC),
                        onClick = onOpenRecovery,
                        testTag = "protocol_chip_recovery"
                    )
                }
            }
            if (assessment.showCoolingPrompt || assessment.level == TriageEmergencyLevel.CRITICAL) {
                item {
                    ProtocolShortcutChip(
                        title = "Active Cooling",
                        icon = Icons.Default.AcUnit,
                        accentColor = Color(0xFF00E5FF),
                        onClick = onOpenCooling,
                        testTag = "protocol_chip_cooling"
                    )
                }
            }
            if (assessment.showSeizurePrompt || assessment.level == TriageEmergencyLevel.CRITICAL) {
                item {
                    ProtocolShortcutChip(
                        title = "Seizure Safety",
                        icon = Icons.Default.Shield,
                        accentColor = Color(0xFFFF9100),
                        onClick = onOpenSeizure,
                        testTag = "protocol_chip_seizure"
                    )
                }
            }
        }
    }
}

@Composable
fun ProtocolShortcutChip(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131C33)),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * Interactive Box Breathing Pacer (De-escalation for Panic & Hyperventilation)
 */
@Composable
fun InteractiveBoxBreathingWidget() {
    var phaseIndex by remember { mutableStateOf(0) }
    var secondsRemaining by remember { mutableStateOf(4) }
    var isActive by remember { mutableStateOf(true) }

    val phases = listOf("INHALE", "HOLD", "EXHALE", "HOLD")

    LaunchedEffect(isActive) {
        if (isActive) {
            while (true) {
                for (phase in 0..3) {
                    phaseIndex = phase
                    for (sec in 4 downTo 1) {
                        secondsRemaining = sec
                        delay(1000)
                    }
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("box_breathing_widget"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF081B26)),
        border = BorderStroke(1.5.dp, Color(0xFF00FFCC))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Air,
                        contentDescription = null,
                        tint = Color(0xFF00FFCC),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Interactive Box Breathing Pacer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
                TextButton(onClick = { isActive = !isActive }) {
                    Text(if (isActive) "Pause" else "Resume", color = Color(0xFF00FFCC), fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Animated Visual Breathing Circle
            val circleScale by animateFloatAsState(
                targetValue = when (phases[phaseIndex]) {
                    "INHALE" -> 1.3f
                    "HOLD" -> if (phaseIndex == 1) 1.3f else 0.8f
                    "EXHALE" -> 0.8f
                    else -> 0.8f
                },
                animationSpec = tween(durationMillis = 4000, easing = LinearEasing),
                label = "breath_scale"
            )

            Box(
                modifier = Modifier
                    .size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp * circleScale)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF00FFCC).copy(alpha = 0.35f), Color(0xFF0099FF).copy(alpha = 0.05f))
                            ),
                            shape = CircleShape
                        )
                        .border(BorderStroke(2.dp, Color(0xFF00FFCC)), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = phases[phaseIndex],
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            color = Color(0xFF00FFCC)
                        )
                        Text(
                            text = "$secondsRemaining s",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Match breathing to the rhythm (4s in, 4s hold, 4s out, 4s hold). Restores carbon dioxide balance and halts panic hyperventilation.",
                fontSize = 11.sp,
                lineHeight = 15.sp,
                textAlign = TextAlign.Center,
                color = Color.LightGray
            )
        }
    }
}

/**
 * Reusable Group Card for Physiological Signs
 */
@Composable
fun PhysiologicalGroupCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131C33)),
        border = BorderStroke(1.dp, Color(0xFF233554))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF00FFCC),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                content = content
            )
        }
    }
}

/**
 * Reusable Single Option Row for Signs
 */
@Composable
fun PhysiologicalSignOptionItem(
    label: String,
    description: String,
    isSelected: Boolean,
    isCritical: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val haptic = LocalHapticFeedback.current
    val accentColor = if (isCritical) Color(0xFFFF2A4B) else Color(0xFF00FFCC)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                accentColor.copy(alpha = 0.15f)
            } else {
                Color(0xFF0B132B)
            }
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) accentColor else Color(0xFF1E2D4A)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(
                        color = if (isSelected) accentColor else Color.Transparent,
                        shape = CircleShape
                    )
                    .border(
                        BorderStroke(1.5.dp, if (isSelected) accentColor else Color.Gray),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = if (isSelected) Color.White else Color.LightGray
                    )
                    if (isCritical) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFF2A4B).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "CRITICAL",
                                color = Color(0xFFFF2A4B),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * High Polish Step-by-Step First Aid Guide Modal
 */
@Composable
fun FirstAidGuideModal(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    steps: List<String>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = iconTint),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("UNDERSTOOD", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                steps.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(iconTint.copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = iconTint
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = step,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = Color.LightGray,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.testTag("first_aid_guide_modal")
    )
}
