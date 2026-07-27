package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DrugDatabase
import com.example.data.ReagentTestKit
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestingScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Interactive simulator state
    var selectedSimDrug by remember { mutableStateOf("MDMA") }
    var selectedSimReagent by remember { mutableStateOf("Marquis Reagent") }
    var simState by remember { mutableStateOf("idle") } // idle -> dropping -> reacted
    var simColor by remember { mutableStateOf(Color.White) }

    val reagentKits = remember { DrugDatabase.reagentTests }

    // Drop simulator color transitions
    LaunchedEffect(simState, selectedSimDrug, selectedSimReagent) {
        if (simState == "dropping") {
            delay(1200) // Simulated drop time
            
            // Determine result color
            val matchedKit = reagentKits.firstOrNull { it.name == selectedSimReagent }
            val colorDesc = matchedKit?.typicalColorChanges?.get(selectedSimDrug) ?: "No reaction / Clear"
            
            simColor = when {
                colorDesc.contains("Purple") || colorDesc.contains("Violet") || colorDesc.contains("Black") -> Color(0xFF1E0326)
                colorDesc.contains("Green") -> Color(0xFF004D40)
                colorDesc.contains("Orange") || colorDesc.contains("Brown") -> Color(0xFF8D6E63)
                colorDesc.contains("Blue") -> Color(0xFF0D47A1)
                colorDesc.contains("Pink") -> Color(0xFFEC407A)
                else -> Color(0xFFECEFF1) // Clear/grayish
            }
            simState = "reacted"
        } else if (simState == "idle") {
            simColor = Color(0xFFE0E0E0)
        }
    }

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SCIENTIFIC EXPLANATION PANEL
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Science, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Why Reagent Testing Matters",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Reagent test kits contain strong acids that react with specific chemicals to cause distinct color changes. Since illicit drugs can contain highly toxic fillers (like Fentanyl or PMMA) without any change in physical smell or appearance, testing your substances is a vital harm-reduction requirement.",
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // INTERACTIVE DROP SIMULATOR
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Interactive Reaction Simulator",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Select Drug
                        Text("1. Select Substance to Test:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("MDMA", "LSD", "Amphetamine", "Heroin").forEach { drug ->
                                FilterChip(
                                    selected = selectedSimDrug == drug,
                                    onClick = { 
                                        selectedSimDrug = drug
                                        simState = "idle"
                                    },
                                    label = { Text(drug, fontSize = 11.sp) }
                                )
                            }
                        }

                        // Select Reagent
                        Text("2. Select Chemical Reagent Drop:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Marquis Reagent", "Ehrlich Reagent", "Mecke Reagent", "Simon's Reagent").forEach { reagent ->
                                FilterChip(
                                    selected = selectedSimReagent == reagent,
                                    onClick = { 
                                        selectedSimReagent = reagent
                                        simState = "idle"
                                    },
                                    label = { Text(reagent.substringBefore(" "), fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Simulator Window (White ceramic plate simulation)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                when (simState) {
                                    "idle" -> {
                                        Text("Crumb of $selectedSimDrug placed on plate.", fontSize = 12.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Box(modifier = Modifier.size(10.dp).background(Color.LightGray, RoundedCornerShape(5.dp)))
                                    }
                                    "dropping" -> {
                                        Text("Squeezing $selectedSimReagent droplet...", fontSize = 12.sp, color = Color.DarkGray)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        // Simple dropping animation placeholder
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                    }
                                    "reacted" -> {
                                        val matchedKit = reagentKits.firstOrNull { it.name == selectedSimReagent }
                                        val colorDesc = matchedKit?.typicalColorChanges?.get(selectedSimDrug) ?: "No color change"
                                        Text("REACTION SUCCESSFUL:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                        Text(colorDesc, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = simColor)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(24.dp).background(simColor, RoundedCornerShape(12.dp)))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Reaction Complete", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { simState = "dropping" },
                            enabled = simState != "dropping",
                            modifier = Modifier.fillMaxWidth().testTag("simulate_test_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("SQUEEZE REAGENT DROPLET", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // REAGENT DESCRIPTIONS SECTION
            item {
                Text(
                    "Standard Testing Reagents",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(reagentKits) { kit ->
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
                            Text(kit.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                            Icon(Icons.Default.Science, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                        }
                        Text(kit.description, fontSize = 12.sp, modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Text("TARGETS: ${kit.targetSubstances}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("How to Test:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        kit.instructions.forEachIndexed { idx, step ->
                            Text("${idx + 1}. $step", fontSize = 11.sp, lineHeight = 15.sp, modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                }
            }

            // HARM REDUCTION LINKS & RESOURCES (MANDATORY REQUIREMENT)
            item {
                Column(modifier = Modifier.padding(bottom = 24.dp)) {
                    Text(
                        "Legitimate Reagent Test Suppliers & Guides",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    SupplierCard(
                        name = "DanceSafe",
                        description = "Peer-based, non-profit harm-reduction group selling reagents and providing peer support guides.",
                        url = "https://dancesafe.org",
                        onOpen = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://dancesafe.org"))
                            context.startActivity(intent)
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SupplierCard(
                        name = "Bunk Police",
                        description = "Reputable producer of test kits and critical drug warning information.",
                        url = "https://bunkpolice.com",
                        onOpen = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://bunkpolice.com"))
                            context.startActivity(intent)
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SupplierCard(
                        name = "RollSafe / TripSit",
                        description = "Excellent web-based safety manuals, dosage calculations, and drug information sheets.",
                        url = "https://rollsafe.org",
                        onOpen = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://rollsafe.org"))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SupplierCard(
    name: String,
    description: String,
    url: String,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, fontSize = 11.sp, lineHeight = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(url, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                Button(
                    onClick = onOpen,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.height(32.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
    }
}
