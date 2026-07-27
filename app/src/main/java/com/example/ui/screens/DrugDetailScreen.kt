package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrugDetailScreen(
    drugId: String,
    onBack: () -> Unit,
    onAIChatClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { RepositoryProvider.getRepository(context) }
    val customSubstances by repository.allSubstances.collectAsState(initial = emptyList())

    val drug = remember(drugId, customSubstances) {
        val staticDrug = DrugDatabase.drugs.firstOrNull { it.id == drugId }
        if (staticDrug != null) {
            staticDrug
        } else {
            customSubstances.firstOrNull { it.id == drugId }?.toDrug()
        }
    }

    val reviews by repository.getReviewsForDrug(drugId).collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    // Interactive review form states
    var showWriteReviewForm by remember { mutableStateOf(false) }
    var formPillName by remember { mutableStateOf("") }
    var formShapeImprint by remember { mutableStateOf("") }
    var formColorHex by remember { mutableStateOf("#FF0D47A1") } // Default to Blue
    var formColorName by remember { mutableStateOf("Blue") }
    var formPotency by remember { mutableStateOf("Strong (200mg+)") }
    var formDangerAlerts by remember { mutableStateOf("") }
    var formDescription by remember { mutableStateOf("") }
    var formRating by remember { mutableStateOf(5) }
    var formUserAlias by remember { mutableStateOf("") }
    var formLocation by remember { mutableStateOf("United States") }
    
    // Filtering and sorting state variables for reports
    var selectedLocationFilter by remember { mutableStateOf("All") } // "All", "United States", "Germany", "Canada", "Australia", "United Kingdom", "Global"
    var sortByMostUpvoted by remember { mutableStateOf(false) } // false: recent, true: most upvoted

    val processedReviews = remember(reviews, selectedLocationFilter, sortByMostUpvoted) {
        var list = reviews
        if (selectedLocationFilter != "All") {
            list = list.filter { it.location.equals(selectedLocationFilter, ignoreCase = true) }
        }
        if (sortByMostUpvoted) {
            list = list.sortedByDescending { it.upvotes }
        } else {
            list = list.sortedByDescending { it.timestamp }
        }
        list
    }

    if (drug == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Substance not found")
        }
        return
    }

    // Get relevant drug interactions
    val relevantInteractions = remember(drugId) {
        DrugDatabase.interactions.filter { it.drugA == drugId || it.drugB == drugId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(drug.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onAIChatClicked(drug.name) },
                        modifier = Modifier.testTag("detail_ai_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Ask AI",
                            tint = Color(0xFF00FFCC)
                        )
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // HERO HEADER WITH GRADIENT
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(drug.category.colorHex).copy(alpha = 0.25f),
                                    MaterialTheme.colorScheme.surface
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = drug.category.displayName.uppercase(),
                                color = Color(drug.category.colorHex),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Icon(
                                imageVector = when (drug.category) {
                                    DrugCategory.STIMULANT -> Icons.Default.Bolt
                                    DrugCategory.PSYCHEDELIC -> Icons.Default.Psychology
                                    DrugCategory.DISSOCIATIVE -> Icons.Default.CloudQueue
                                    DrugCategory.DEPRESSANT -> Icons.Default.Bed
                                    DrugCategory.OPIOID -> Icons.Default.HeartBroken
                                },
                                contentDescription = null,
                                tint = Color(drug.category.colorHex)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = drug.name,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = drug.chemicalName,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Street Names: " + drug.streetNames.joinToString(", "),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = drug.description,
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // DETAILED AI COMPANION CARD FOR THIS DRUG
            item {
                Card(
                    onClick = { onAIChatClicked(drug.name) },
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF00FFCC).copy(alpha = 0.08f)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF00FFCC).copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("detail_ask_ai_card")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = Color(0xFF00FFCC),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ADVISORAI PRE-CONTEXT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00FFCC),
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Ask AI about ${drug.name}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Get immediate educational advice on specific dosage calculations, complex mixing profiles, or Marquis testing results for ${drug.name}.",
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = Color.LightGray.copy(alpha = 0.8f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFF00FFCC),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // DOSAGES AND METHOD OF ADMINISTRATION
            item {
                Column {
                    Text(
                        "Dosages & Harm Reduction Scale",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    drug.dosageOral?.let { oral ->
                        DosageCard(title = "Oral Intake (Ingested)", info = oral)
                    }

                    if (drug.dosageOral != null && drug.dosageInsufflated != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    drug.dosageInsufflated?.let { insuff ->
                        DosageCard(title = "Nasal / Insufflation", info = insuff)
                    }
                }
            }

            // TIMELINE: ONSET / DURATION / HALF-LIFE
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
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Chemical Action Timeline", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        TimelineRow(label = "Onset (Time to feel effects)", value = drug.onset, progress = 0.2f, color = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.height(12.dp))
                        TimelineRow(label = "Duration (Total active period)", value = drug.duration, progress = 0.6f, color = Color(0xFF2196F3))
                        Spacer(modifier = Modifier.height(12.dp))
                        TimelineRow(label = "Biological Half-Life", value = drug.halfLife, progress = 0.4f, color = Color(0xFFFF9800))
                    }
                }
            }

            // SET & SETTING / ENVIRONMENTAL GUIDANCE
            item {
                Column {
                    Text(
                        "Which Environment to Avoid or Go To",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // GO TO (SAFE ENVIRONMENT)
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE8F5E9)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Safe Environments",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                drug.goodEnvironments.forEach { env ->
                                    Text(
                                        text = "• $env",
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        color = Color(0xFF1B5E20),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // AVOID (DANGEROUS ENVIRONMENT)
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = Color(0xFFC62828),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Avoid / Danger",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFFC62828)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                drug.badEnvironments.forEach { env ->
                                    Text(
                                        text = "• $env",
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        color = Color(0xFFB71C1C),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // REAGENT TEST OUTCOMES FOR THIS DRUG
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Science, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Expected Reagent Test Colors", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            drug.reagents.forEach { rColor ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Visual color blob
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(rColor.hexColor))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = rColor.reagentName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = rColor.colorName,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // DANGEROUS COMBINATIONS
            if (relevantInteractions.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            "Dangerous Combinations & Interactions",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            relevantInteractions.forEach { interaction ->
                                val otherDrugId = if (interaction.drugA == drugId) interaction.drugB else interaction.drugA
                                val otherDrugName = DrugDatabase.drugs.firstOrNull { it.id == otherDrugId }?.name ?: otherDrugId.uppercase()

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(interaction.riskLevel.colorHex).copy(alpha = 0.08f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.TrendingFlat,
                                                    contentDescription = null,
                                                    tint = Color(interaction.riskLevel.colorHex)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "${drug.name} + $otherDrugName",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        Color(interaction.riskLevel.colorHex).copy(alpha = 0.15f),
                                                        shape = RoundedCornerShape(6.dp)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = interaction.riskLevel.displayName.uppercase(),
                                                    color = Color(interaction.riskLevel.colorHex),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = interaction.description,
                                            fontSize = 12.sp,
                                            lineHeight = 17.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // HARM REDUCTION ADVICE / SPECIFIC TIPS
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Essential Harm Reduction Guidelines",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        drug.tips.forEach { tip ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("•", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = tip,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // ADVANCED DEEP DIVE ARCHIVES
            drug.deepDive?.let { deepDive ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Educational Chemical & Community Archives",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            Text(
                                "Synthesis Overview & Precursor Safety Map",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Color.Black,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = deepDive.synthesisOverview,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color(0xFF00FFCC),
                                    lineHeight = 16.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Reddit Safety Broadcasts ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFF4500), shape = RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("REDDIT FILES", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            deepDive.redditSafetyFiles.forEach { file ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                                        Icon(
                                            Icons.Default.Campaign,
                                            contentDescription = null,
                                            tint = Color(0xFFFF4500),
                                            modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(file, fontSize = 11.sp, lineHeight = 15.sp)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "GitHub Source Repositories ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.onSurface, shape = RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("GITHUB OPEN-SOURCE", color = MaterialTheme.colorScheme.surface, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            deepDive.githubArchives.forEach { archive ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                                        Icon(
                                            Icons.Default.Code,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(archive, fontSize = 11.sp, lineHeight = 15.sp)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                "Advanced Chemical Identification Properties",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    deepDive.chemicalProperties.forEach { (key, value) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(key, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                            Text(value, fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f).padding(start = 16.dp))
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // COMMUNITY PILL REPORTS HEADER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RateReview,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Community Pill Reports",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Text(
                        text = "(${processedReviews.size} of ${reviews.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // REGIONAL LOCATION FILTER & SAFETY SORT BAR
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Region horizontal chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Region Filter",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Filter by region:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    val locationFilters = listOf(
                        "All" to "🌎 All",
                        "United States" to "🇺🇸 USA",
                        "Germany" to "🇩🇪 DE",
                        "Canada" to "🇨🇦 CA",
                        "Australia" to "🇦🇺 AU",
                        "United Kingdom" to "🇬🇧 UK",
                        "Global" to "🌐 Global"
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(locationFilters) { (regionKey, label) ->
                            val isSelected = selectedLocationFilter == regionKey
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedLocationFilter = regionKey },
                                label = { Text(label, fontSize = 11.sp) },
                                modifier = Modifier.testTag("filter_region_${regionKey.lowercase().replace(" ", "_")}")
                            )
                        }
                    }
                    
                    // Safety sorting chip row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { sortByMostUpvoted = !sortByMostUpvoted }
                        ) {
                            Icon(
                                imageVector = if (sortByMostUpvoted) Icons.Default.TrendingUp else Icons.Default.Schedule,
                                contentDescription = "Sort Mode",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (sortByMostUpvoted) "Sorted by: Top Upvoted / Safety Rate" else "Sorted by: Most Recent Reports",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        // Small toggle chip button for quick switching
                        TextButton(
                            onClick = { sortByMostUpvoted = !sortByMostUpvoted },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(28.dp).testTag("sort_votes_button")
                        ) {
                            Text(
                                if (sortByMostUpvoted) "Show Recent" else "Show Top-Rated",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // REPORT A PILL TOGGLE BUTTON
            item {
                Button(
                    onClick = { showWriteReviewForm = !showWriteReviewForm },
                    modifier = Modifier.fillMaxWidth().testTag("toggle_review_form_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showWriteReviewForm) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (showWriteReviewForm) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = if (showWriteReviewForm) Icons.Default.Close else Icons.Default.AddComment,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (showWriteReviewForm) "Cancel Safe Report" else "Report a Pressed Pill / Batch")
                }
            }

            // REPORT SUBMISSION FORM INLINE
            if (showWriteReviewForm) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                "Publish Pressed Pill Safety Report",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            OutlinedTextField(
                                value = formPillName,
                                onValueChange = { formPillName = it },
                                label = { Text("Pill / Tablet Name (e.g. Yellow Tesla)") },
                                modifier = Modifier.fillMaxWidth().testTag("form_pill_name"),
                                singleLine = true
                            )
                            
                            OutlinedTextField(
                                value = formShapeImprint,
                                onValueChange = { formShapeImprint = it },
                                label = { Text("Pill Shape & Imprint (e.g. Shield/Tesla face)") },
                                modifier = Modifier.fillMaxWidth().testTag("form_shape"),
                                singleLine = true
                            )
                            
                            // Preset Color Selection Row
                            Column {
                                Text("Pill Color:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(6.dp))
                                val presetColors = listOf(
                                    "#FF0D47A1" to "Blue",
                                    "#FFFFB300" to "Yellow",
                                    "#FFFF4081" to "Pink",
                                    "#FF2E7D32" to "Green",
                                    "#FFFF6F00" to "Orange",
                                    "#FFEAEAEA" to "White"
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(presetColors) { (hex, name) ->
                                        val isSelected = formColorHex == hex
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(android.graphics.Color.parseColor(hex)))
                                                .border(
                                                    width = if (isSelected) 3.dp else 1.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                                    shape = CircleShape
                                                )
                                                .clickable {
                                                    formColorHex = hex
                                                    formColorName = name
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = if (hex == "#FFEAEAEA" || hex == "#FFFFB300") Color.Black else Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // Potency Selection Row
                            Column {
                                Text("Observed / Analyzed Potency:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(6.dp))
                                val presetPotencies = listOf(
                                    "Light", "Normal", "Strong (200mg+)", "Dangerously High"
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(presetPotencies) { pot ->
                                        val isSelected = formPotency == pot
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { formPotency = pot },
                                            label = { Text(pot, fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }
                            
                            OutlinedTextField(
                                value = formDangerAlerts,
                                onValueChange = { formDangerAlerts = it },
                                label = { Text("Danger Alerts (e.g. Warning: Contains PMMA!)") },
                                placeholder = { Text("Leave blank if none or safe") },
                                modifier = Modifier.fillMaxWidth().testTag("form_danger_alerts"),
                                singleLine = true
                            )
                            
                            OutlinedTextField(
                                value = formDescription,
                                onValueChange = { formDescription = it },
                                label = { Text("Detailed Review / Markings / Reagent results") },
                                modifier = Modifier.fillMaxWidth().height(100.dp).testTag("form_description"),
                                maxLines = 4
                            )
                            
                            // Star rating row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Overall Quality Rating: ", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.width(8.dp))
                                repeat(5) { index ->
                                    val ratingValue = index + 1
                                    val isStarSelected = formRating >= ratingValue
                                    IconButton(
                                        onClick = { formRating = ratingValue },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isStarSelected) Icons.Default.Star else Icons.Outlined.StarBorder,
                                            contentDescription = "Star $ratingValue",
                                            tint = if (isStarSelected) Color(0xFFFFD54F) else MaterialTheme.colorScheme.outlineVariant
                                        )
                                    }
                                }
                            }
                            
                            OutlinedTextField(
                                value = formUserAlias,
                                onValueChange = { formUserAlias = it },
                                label = { Text("Your Alias (Default: Anonymous Sitter)") },
                                modifier = Modifier.fillMaxWidth().testTag("form_alias"),
                                singleLine = true
                            )
                            
                            // Location / Region Selector Row
                            Column {
                                Text("Report Location / Region:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(6.dp))
                                val regions = listOf(
                                    "United States" to "🇺🇸 USA",
                                    "Germany" to "🇩🇪 DE",
                                    "Canada" to "🇨🇦 CA",
                                    "Australia" to "🇦🇺 AU",
                                    "United Kingdom" to "🇬🇧 UK",
                                    "Global" to "🌐 Global"
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(regions) { (region, label) ->
                                        val isSelected = formLocation == region
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { formLocation = region },
                                            label = { Text(label, fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }
                            
                            Button(
                                onClick = {
                                    if (formPillName.isNotBlank() && formDescription.isNotBlank()) {
                                        coroutineScope.launch {
                                            repository.insert(
                                                PillReview(
                                                    drugId = drugId,
                                                    pillName = formPillName,
                                                    shape = if (formShapeImprint.isBlank()) "Standard Circular" else formShapeImprint,
                                                    colorHex = formColorHex,
                                                    colorName = formColorName,
                                                    potency = formPotency,
                                                    dangerAlerts = formDangerAlerts,
                                                    description = formDescription,
                                                    rating = formRating,
                                                    userAlias = if (formUserAlias.isBlank()) "Anonymous Sitter" else formUserAlias,
                                                    location = formLocation
                                                )
                                            )
                                            // Reset
                                            formPillName = ""
                                            formShapeImprint = ""
                                            formColorHex = "#FF0D47A1"
                                            formColorName = "Blue"
                                            formPotency = "Strong (200mg+)"
                                            formDangerAlerts = ""
                                            formDescription = ""
                                            formRating = 5
                                            formUserAlias = ""
                                            formLocation = "United States"
                                            showWriteReviewForm = false
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("submit_review_button"),
                                enabled = formPillName.isNotBlank() && formDescription.isNotBlank()
                            ) {
                                Text("Publish Educational Report")
                            }
                        }
                    }
                }
            }

            // LIST REVIEWS FOR THIS DRUG
            if (processedReviews.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No reports matching the selected filters. Be the first to publish a safety report!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        }
                    }
                }
            } else {
                items(processedReviews) { review ->
                    val parsedColor = remember(review.colorHex) {
                        try {
                            Color(android.graphics.Color.parseColor(review.colorHex))
                        } catch (e: Exception) {
                            Color.Gray
                        }
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("pill_review_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(width = 16.dp, height = 24.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(parsedColor)
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = review.pillName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "${review.colorName} • Shape: ${review.shape}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            // Country Flag and Region Badge
                                            Box(
                                                modifier = Modifier
                                                    .background(MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = when (review.location.lowercase()) {
                                                        "united states", "usa" -> "🇺🇸 USA"
                                                        "germany" -> "🇩🇪 DE"
                                                        "canada" -> "🇨🇦 CA"
                                                        "australia" -> "🇦🇺 AU"
                                                        "united kingdom", "uk" -> "🇬🇧 UK"
                                                        else -> "🌐 ${review.location}"
                                                    },
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                Row {
                                    repeat(5) { index ->
                                        Icon(
                                            imageVector = if (index < review.rating) Icons.Default.Star else Icons.Outlined.StarBorder,
                                            contentDescription = null,
                                            tint = if (index < review.rating) Color(0xFFFFD54F) else MaterialTheme.colorScheme.outlineVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                val isHighStr = review.potency.contains("Danger", ignoreCase = true) || review.potency.contains("High", ignoreCase = true) || review.potency.contains("Strong", ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isHighStr) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = review.potency.uppercase(),
                                        color = if (isHighStr) Color(0xFFC62828) else Color(0xFF2E7D32),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            if (review.dangerAlerts.isNotBlank() && !review.dangerAlerts.contains("Safe", ignoreCase = true)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .border(1.dp, Color(0xFFD32F2F).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFFEBEE).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Alert",
                                            tint = Color(0xFFD32F2F),
                                            modifier = Modifier.size(16.dp).padding(top = 1.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = review.dangerAlerts,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp,
                                            color = Color(0xFFC62828),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = review.description,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = review.userAlias,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                val dateStr = remember(review.timestamp) {
                                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                                    sdf.format(java.util.Date(review.timestamp))
                                }
                                Text(
                                    text = dateStr,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            // INTERACTIVE VOTING SYSTEM (Upvotes / Downvotes safety indicator)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Was this safety report helpful?",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // UPVOTE BUTTON (Helpful / Safe batch report)
                                    SuggestionChip(
                                        onClick = {
                                            coroutineScope.launch {
                                                repository.upvoteReview(review.id)
                                            }
                                        },
                                        label = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.ThumbUp,
                                                    contentDescription = "Upvote",
                                                    tint = Color(0xFF2E7D32),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${review.upvotes}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF2E7D32)
                                                )
                                            }
                                        },
                                        modifier = Modifier.height(28.dp).testTag("upvote_${review.id}")
                                    )
                                    
                                    // DOWNVOTE BUTTON (Unreliable / Dangerous profile alert)
                                    SuggestionChip(
                                        onClick = {
                                            coroutineScope.launch {
                                                repository.downvoteReview(review.id)
                                            }
                                        },
                                        label = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.ThumbDown,
                                                    contentDescription = "Downvote",
                                                    tint = Color(0xFFC62828),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${review.downvotes}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFC62828)
                                                )
                                            }
                                        },
                                        modifier = Modifier.height(28.dp).testTag("downvote_${review.id}")
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Add trailing padding item to avoid overlap
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun DosageCard(
    title: String,
    info: DosageInfo
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                DosageItem(modifier = Modifier.weight(1f), label = "Threshold", value = info.threshold)
                DosageItem(modifier = Modifier.weight(1f), label = "Light", value = info.light)
                DosageItem(modifier = Modifier.weight(1f), label = "Common", value = info.common)
                DosageItem(modifier = Modifier.weight(1f), label = "Strong", value = info.strong)
            }

            info.warningNote?.let { note ->
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFFBC02D),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = note,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun DosageItem(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun TimelineRow(
    label: String,
    value: String,
    progress: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}
