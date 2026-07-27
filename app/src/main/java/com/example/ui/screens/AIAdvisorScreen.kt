package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.api.Content
import com.example.api.GeminiApiClient
import com.example.api.Part
import com.example.data.DrugDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class UiChatMessage(
    val id: String,
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER, SYSTEM, ADVISOR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAdvisorScreen(
    initialSubstanceContext: String? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    
    val isApiConfigured = remember { GeminiApiClient.isApiKeyConfigured() }
    
    // Quick suggestion prompts
    val suggestions = listOf(
        "Is MDMA dangerous with alcohol?",
        "How do Fentanyl test strips work?",
        "LSD safety & duration guide",
        "Explain Ketamine bladder risks"
    )

    var inputQuery by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    
    // Messages list
    val messages = remember {
        mutableStateListOf<UiChatMessage>().apply {
            add(
                UiChatMessage(
                    id = "welcome",
                    sender = MessageSender.ADVISOR,
                    text = if (initialSubstanceContext != null) {
                        "Hello! I am AdvisorAI. I see you are looking at **$initialSubstanceContext**. Ask me any harm-reduction, dosage, or interactive safety questions about this substance!"
                    } else {
                        "Welcome to **AdvisorAI**. Ask me any non-judgmental, scientifically objective questions regarding chemical properties, safety thresholds, or drug combinations. Safety always comes first."
                    }
                )
            )
        }
    }
    
    val listState = rememberLazyListState()
    
    // Auto scroll to bottom when message list changes
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Process chat response
    fun handleSendMessage(queryText: String) {
        if (queryText.isBlank() || isSending) return
        
        val userQuery = queryText.trim()
        inputQuery = ""
        focusManager.clearFocus()
        
        messages.add(
            UiChatMessage(
                id = "user_${System.currentTimeMillis()}",
                sender = MessageSender.USER,
                text = userQuery
            )
        )
        
        isSending = true
        
        coroutineScope.launch {
            // Simulate reading delay
            delay(800)
            
            if (isApiConfigured) {
                // Call real Gemini API
                val apiHistory = messages.filter { it.sender != MessageSender.SYSTEM }.map { msg ->
                    Content(
                        parts = listOf(Part(text = msg.text)),
                        role = if (msg.sender == MessageSender.USER) "user" else "model"
                    )
                }
                
                val result = GeminiApiClient.chatWithAi(userQuery, apiHistory)
                result.fold(
                    onSuccess = { responseText ->
                        messages.add(
                            UiChatMessage(
                                id = "adv_${System.currentTimeMillis()}",
                                sender = MessageSender.ADVISOR,
                                text = responseText
                            )
                        )
                    },
                    onFailure = { err ->
                        // API failure fallback
                        val fallbackResponse = getLocalSymptomOrSubstanceResponse(userQuery)
                        messages.add(
                            UiChatMessage(
                                id = "adv_err_${System.currentTimeMillis()}",
                                sender = MessageSender.ADVISOR,
                                text = "*(Network/API Error. Switched to local fallback)*\n\n$fallbackResponse"
                            )
                        )
                    }
                )
            } else {
                // Local intelligent fallback simulation
                delay(1000)
                val fallbackResponse = getLocalSymptomOrSubstanceResponse(userQuery)
                messages.add(
                    UiChatMessage(
                        id = "adv_fallback_${System.currentTimeMillis()}",
                        sender = MessageSender.ADVISOR,
                        text = fallbackResponse
                    )
                )
            }
            
            isSending = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = Color(0xFF00FFCC),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AdvisorAI Chat",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("ai_advisor_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF090E1A)
                )
            )
        },
        containerColor = Color(0xFF050811),
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // OFFLINE BANNER IF API KEY NOT CONFIGURED
            if (!isApiConfigured) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE65100).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFFE65100).copy(alpha = 0.4f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.SignalCellularNoSim,
                            contentDescription = "Offline Mode",
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Local Safety Companion Mode: Active queries will be processed locally using integrated clinical data.",
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            color = Color(0xFFFFB74D),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // CHAT HISTORY
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        ChatBubble(message = message)
                    }
                    
                    if (isSending) {
                        item {
                            TypingIndicatorBubble()
                        }
                    }
                }
            }

            // SUGGESTION CHIPS
            if (!isSending && messages.size <= 2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    suggestions.forEach { suggestion ->
                        SuggestionChip(
                            onClick = { handleSendMessage(suggestion) },
                            label = { Text(suggestion, fontSize = 11.sp, color = Color(0xFF00FFCC)) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color(0xFF00FFCC).copy(alpha = 0.08f)
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = Color(0xFF00FFCC).copy(alpha = 0.25f),
                                borderWidth = 1.dp
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // INPUT CONTROLS PANEL
            Surface(
                color = Color(0xFF090E1A),
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputQuery,
                        onValueChange = { inputQuery = it },
                        placeholder = { Text("Ask about interactions, dosage, risks...", color = Color.Gray, fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_advisor_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF050811),
                            unfocusedContainerColor = Color(0xFF050811),
                            focusedBorderColor = Color(0xFF00FFCC).copy(alpha = 0.8f),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3,
                        singleLine = false
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    FilledIconButton(
                        onClick = { handleSendMessage(inputQuery) },
                        enabled = inputQuery.isNotBlank() && !isSending,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xFF00FFCC),
                            contentColor = Color.Black,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.2f),
                            disabledContentColor = Color.Gray
                        ),
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("ai_advisor_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: UiChatMessage) {
    val isUser = message.sender == MessageSender.USER
    val containerColor = if (isUser) Color(0xFF00FFCC).copy(alpha = 0.15f) else Color(0xFF0D1527)
    val borderColor = if (isUser) Color(0xFF00FFCC).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f)
    val alignment = if (isUser) Alignment.End else Alignment.Start
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(containerColor, shape = RoundedCornerShape(16.dp))
                .border(BorderStroke(1.dp, borderColor), shape = RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = if (isUser) "You" else "AdvisorAI",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUser) Color(0xFF00FFCC) else Color(0xFFFF007F),
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                // Markdown style bolding parser helper
                Text(
                    text = message.text,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun TypingIndicatorBubble() {
    Box(
        modifier = Modifier
            .widthIn(max = 200.dp)
            .background(Color(0xFF0D1527), shape = RoundedCornerShape(16.dp))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), shape = RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("AdvisorAI is formulating safety guidance", fontSize = 11.sp, color = Color.Gray)
            CircularProgressIndicator(
                modifier = Modifier.size(10.dp),
                color = Color(0xFF00FFCC),
                strokeWidth = 1.5.dp
            )
        }
    }
}

/**
 * Advanced, clinically accurate rule-based response builder based on local databases
 * for high security local fallback compliance.
 */
fun getLocalSymptomOrSubstanceResponse(query: String): String {
    val q = query.lowercase()
    
    // Check combinations
    if (q.contains("mix") || q.contains("combine") || q.contains("interaction") || q.contains("and") && (q.contains("alcohol") || q.contains("xanax") || q.contains("mdma") || q.contains("ketamine"))) {
        return """
            ### ⚠️ CRITICAL INTERACTION WARNINGS
            Combining multiple chemical agents increases cardiorespiratory risk exponentially:
            
            1. **Alcohol + Benzodiazepines (Xanax/Alprazolam) / Opioids**:
               - **Risk Level**: **Lethal/Extreme Danger**
               - **Mechanics**: Severe synergistic respiratory depression. The combination shuts down the central nervous system's automated drive to breathe, leading to unconsciousness, asphyxiation, or brain death.
            
            2. **Alcohol + Ketamine**:
               - **Risk Level**: **Severe Danger**
               - **Mechanics**: Major respiratory suppression, severe motor ataxia (inability to stand/move), and high risk of vomiting while unconscious, leading to fatal choking (asphyxiation).
            
            3. **MDMA (Stimulants) + SSRIs / MAOIs (Antidepressants)**:
               - **Risk Level**: **Highly Fatal**
               - **Mechanics**: Triggers a life-threatening excess of serotonin known as **Serotonin Syndrome** (severe fever, muscle rigidity, seizures, rapid heart failure).
            
            4. **Stimulants (Cocaine/Amphetamines) + Alcohol**:
               - **Risk Level**: **High Cardiovascular Danger**
               - **Mechanics**: Produces an active metabolite (Cocaethylene) in the liver which is significantly more cardiotoxic than cocaine itself, risking sudden heart failure.
        """.trimIndent()
    }
    
    // Check Fentanyl
    if (q.contains("fent") || q.contains("strip") || q.contains("test")) {
        return """
            ### 🧪 FENTANYL TESTING METHODOLOGY
            Fentanyl is a synthetic opioid 50-100x stronger than morphine. It is invisible, odorless, and tasteless. 
            
            **How to run a Fentanyl Test Strip (FTS):**
            1. **Dissolve All Product**: The "chocolate chip cookie effect" means fentanyl might gather in one tiny spot. For safety, dissolve your entire planned dose in water (10mg/ml for MDMA/meth; 20mg/ml for cocaine/ketamine).
            2. **Insert Strip**: Dip the test strip into the liquid up to the designated line. Hold for 15 seconds.
            3. **Wait & Read**: Place the strip flat on a clean surface.
               - **One Line (C)**: **POSITIVE** (fentanyl detected). Discard immediately.
               - **Two Lines (C & T)**: **NEGATIVE** (no fentanyl detected).
            
            *Important*: Standard reagent drop kits (Marquis, Ehrlich) do NOT detect fentanyl. You must use dedicated FTS strips.
        """.trimIndent()
    }
    
    // Substance Specific
    if (q.contains("mdma") || q.contains("ecstasy") || q.contains("molly") || q.contains("bean")) {
        val staticDrug = DrugDatabase.drugs.firstOrNull { it.id == "mdma" }
        return """
            ### 💊 MDMA (Empathy/Stimulant) Safety Profile
            
            - **Common Oral Dosage**: ${staticDrug?.dosageOral?.common ?: "75 - 120 mg"}
            - **Onset**: ${staticDrug?.onset ?: "20 - 60 minutes"}
            - **Duration**: ${staticDrug?.duration ?: "3 - 5 hours"}
            
            **Clinical Reagent Chart:**
            - **Marquis**: Instantly turns Deep Purple to Pitch Black. (A slow yellow turn indicates dangerous bath salts / cathinones).
            - **Simon's**: Deep Blue (differentiates MDMA from MDA).
            
            **Crucial Harm Reduction:**
            1. **Three-Month Rule**: Wait at least 12 weeks between sessions to allow serotonin levels and receptors to recover.
            2. **Hydration Control**: Drink 250ml water/hr if resting, or up to 500ml/hr if dancing. Do NOT overhydrate rapidly (risks fatal water poisoning).
            3. **Hyperthermia risk**: Take regular breaks to cool down.
        """.trimIndent()
    }
    
    if (q.contains("lsd") || q.contains("acid") || q.contains("lucy") || q.contains("blotter")) {
        val staticDrug = DrugDatabase.drugs.firstOrNull { it.id == "lsd" }
        return """
            ### 🌈 LSD (Psychedelic) Safety Profile
            
            - **Common Oral Dosage**: ${staticDrug?.dosageOral?.common ?: "50 - 150 mcg"}
            - **Onset**: ${staticDrug?.onset ?: "20 - 90 minutes"}
            - **Duration**: ${staticDrug?.duration ?: "8 - 12 hours"}
            
            **Clinical Reagent Chart:**
            - **Ehrlich**: Turns Light Purple/Pink. (Must react to verify presence of indoles like LSD; NBOMe substances do NOT react).
            
            **Crucial Harm Reduction:**
            1. **Set & Setting**: Ensure a comfortable, safe, and sensory-controlled environment. Have a trusted sober trip sitter nearby.
            2. **No Redosing**: LSD has a very long duration. Do not redose early.
            3. **HPPD Awareness**: Respect integration periods between trips to minimize prolonged sensory changes.
        """.trimIndent()
    }
    
    if (q.contains("ketamine") || q.contains("special k") || q.contains("ket")) {
        val staticDrug = DrugDatabase.drugs.firstOrNull { it.id == "ketamine" }
        return """
            ### 🐴 Ketamine (Dissociative Anesthetic) Profile
            
            - **Onset**: 5 - 15 minutes (insufflated)
            - **Duration**: 45 - 90 minutes
            - **Class**: Arylcyclohexylamine (Dissociative)
            
            **Clinical Reagent Chart:**
            - **Mandelin**: Turns Deep Orange/Brown.
            
            **Crucial Harm Reduction:**
            1. **Bladder Health**: Chronic or heavy usage causes irreversible, severe scarring of the bladder wall (ulcerative cystitis), leading to blood in urine and eventual bladder removal. Keep usage highly infrequent.
            2. **K-Hole Hazards**: In high doses, physical movement becomes impossible. Always sit or lie down in a safe spot to prevent falls.
            3. **Vomiting/Choking**: Never consume Ketamine near or after heavy meals.
        """.trimIndent()
    }

    if (q.contains("cocaine") || q.contains("coke") || q.contains("blow")) {
        val staticDrug = DrugDatabase.drugs.firstOrNull { it.id == "cocaine" }
        return """
            ### ❄️ Cocaine (Stimulant) Safety Profile
            
            - **Onset**: 1 - 5 minutes (insufflated)
            - **Duration**: 30 - 60 minutes
            - **Class**: Tropane Alkaloid (Stimulant)
            
            **Clinical Reagent Chart:**
            - **Scott Reagent**: Turns Cobalt Blue (suggests Cocaine presence).
            
            **Crucial Harm Reduction:**
            1. **Cocaethylene Formation**: Avoid mixing with alcohol. The combined toxicity significantly spikes risk of sudden stroke or myocardial infarction.
            2. **Cardiovascular Strain**: Cocaine drastically increases heart rate and blood pressure. Avoid mixing with other stimulants (caffeine, adderall).
            3. **Nasal Care**: Flush nasal passages with saline spray post-session to reduce mucosal tissue erosion.
        """.trimIndent()
    }
    
    if (q.contains("alprazolam") || q.contains("xanax") || q.contains("benzo")) {
        val staticDrug = DrugDatabase.drugs.firstOrNull { it.id == "alprazolam" }
        return """
            ### 💊 Alprazolam (Benzodiazepine/Depressant) Profile
            
            - **Onset**: 15 - 45 minutes
            - **Duration**: 6 - 12 hours
            - **Class**: Benzodiazepine (Central Nervous System Depressant)
            
            **Crucial Harm Reduction:**
            1. **Extreme Mixing Danger**: Under no circumstances mix with alcohol, barbiturates, or opioids. This combination is a leading cause of accidental overdose deaths.
            2. **Physical Dependence**: Benzodiazepine withdrawal from long-term use is highly dangerous and can cause severe seizures, psychosis, and death. Never halt heavy use cold turkey; consult medical professionals for a gradual taper.
            3. **Fentanyl Alert**: Pressed Xanax tablets on the black market are frequently contaminated with lethal doses of Fentanyl or synthetic designer benzos (like flualprazolam). Test strip screening is mandatory.
        """.trimIndent()
    }

    return """
        I am AdvisorAI, your local harm reduction assistant. 
        
        I can provide detailed scientific safety thresholds, expected reagent reactions, and drug combination safety protocols for major substances. 
        
        **Try asking about:**
        - *"Is MDMA safe with alcohol?"*
        - *"How do Fentanyl test strips work?"*
        - *"What is a safe dosage for Ketamine?"*
        - *"LSD safety & duration guide"*
    """.trimIndent()
}
