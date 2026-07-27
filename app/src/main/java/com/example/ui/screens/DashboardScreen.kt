package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.TranslationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onDrugSelected: (String) -> Unit,
    onEmergencyClicked: () -> Unit,
    onTestingClicked: () -> Unit,
    onAIChatClicked: (String?) -> Unit,
    selectedLanguage: String = "en",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // Access the database repository
    val repository = remember { RepositoryProvider.getRepository(context) }
    val books by repository.allBooks.collectAsState(initial = emptyList())
    val customSubstances by repository.allSubstances.collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<DrugCategory?>(null) }

    // Selected book for viewing detail
    var selectedBookForReading by remember { mutableStateOf<SafetyBook?>(null) }
    // State for Add/Edit Book Dialog
    var showAddBookDialog by remember { mutableStateOf(false) }
    var editingBookId by remember { mutableStateOf<Int?>(null) }
    var bookTitleInput by remember { mutableStateOf("") }
    var bookAuthorInput by remember { mutableStateOf("") }
    var bookCategoryInput by remember { mutableStateOf("Harm Reduction") }
    var bookSummaryInput by remember { mutableStateOf("") }
    var bookContentInput by remember { mutableStateOf("") }

    // State for Add Substance Dialog
    var showAddSubstanceDialog by remember { mutableStateOf(false) }
    var substanceNameInput by remember { mutableStateOf("") }
    var substanceChemicalInput by remember { mutableStateOf("") }
    var substanceCategoryInput by remember { mutableStateOf(DrugCategory.STIMULANT) }
    var substanceDescriptionInput by remember { mutableStateOf("") }
    var substanceStreetInput by remember { mutableStateOf("") }
    var substanceOnsetInput by remember { mutableStateOf("") }
    var substanceDurationInput by remember { mutableStateOf("") }
    var substanceHalfLifeInput by remember { mutableStateOf("") }
    var substanceTipsInput by remember { mutableStateOf("") }
    var substanceOralThresholdInput by remember { mutableStateOf("") }
    var substanceOralLightInput by remember { mutableStateOf("") }
    var substanceOralCommonInput by remember { mutableStateOf("") }
    var substanceOralStrongInput by remember { mutableStateOf("") }
    var substanceOralWarningInput by remember { mutableStateOf("") }

    // Search and filter drug database (combining preloaded and dynamic custom drugs)
    val filteredDrugs = remember(searchQuery, selectedCategory, customSubstances) {
        val combined = DrugDatabase.drugs + customSubstances.map { it.toDrug() }
        combined.filter { drug ->
            val matchesSearch = drug.name.contains(searchQuery, ignoreCase = true) ||
                    drug.streetNames.any { it.contains(searchQuery, ignoreCase = true) } ||
                    drug.chemicalName.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == null || drug.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    var selectedTabSegment by remember { mutableStateOf(0) } // 0 = Substances, 1 = Safety Library, 2 = Community & Links

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // UNIFIED TOP SEARCH & CATEGORY FILTER BAR
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(top = innerPadding.calculateTopPadding())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("explore_substances_floating_bar"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0F1629)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF00FFCC).copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        // Quick Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search substances (e.g. MDMA, LSD, Ketamine)", color = Color.Gray, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = Color(0xFF00FFCC)) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = Color.Gray)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dashboard_search_input"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00FFCC),
                                unfocusedBorderColor = Color.DarkGray,
                                cursorColor = Color(0xFF00FFCC),
                                focusedContainerColor = Color(0xFF131C33),
                                unfocusedContainerColor = Color(0xFF131C33)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Category Filter Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedCategory == null,
                                    onClick = { 
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedCategory = null 
                                    },
                                    label = { Text("All Classes", fontSize = 11.sp) },
                                    leadingIcon = if (selectedCategory == null) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        labelColor = Color.LightGray,
                                        selectedLabelColor = Color.Black,
                                        selectedContainerColor = Color(0xFF00FFCC)
                                    )
                                )
                            }
                            items(DrugCategory.values()) { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { 
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedCategory = category 
                                    },
                                    label = { Text(category.displayName, fontSize = 11.sp) },
                                    leadingIcon = if (selectedCategory == category) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        labelColor = Color.LightGray,
                                        selectedLabelColor = Color.Black,
                                        selectedContainerColor = Color(0xFF00FFCC)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp)
            ) {
                // QUICK ACTION STRIP (4 Intuitive Hub Shortcuts)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickShortcutCard(
                            title = "Emergency",
                            subtitle = "911 & CPR",
                            icon = Icons.Default.Campaign,
                            tintColor = Color(0xFFFF007F),
                            modifier = Modifier.weight(1f),
                            onClick = { onEmergencyClicked() }
                        )

                        QuickShortcutCard(
                            title = "AI Advisor",
                            subtitle = "Chat Safety",
                            icon = Icons.Default.Psychology,
                            tintColor = Color(0xFF00FFCC),
                            modifier = Modifier.weight(1f),
                            onClick = { onAIChatClicked(null) }
                        )

                        QuickShortcutCard(
                            title = "Reagents",
                            subtitle = "Spot Labs",
                            icon = Icons.Default.Science,
                            tintColor = Color(0xFF00E5FF),
                            modifier = Modifier.weight(1f),
                            onClick = { onTestingClicked() }
                        )

                        QuickShortcutCard(
                            title = "Manuals",
                            subtitle = "Guides",
                            icon = Icons.Default.MenuBook,
                            tintColor = Color(0xFFFFB300),
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTabSegment = 1 }
                        )
                    }
                }

                // SEGMENTED TAB SELECTOR (Simple navigation between sections)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1629)),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            val tabsList = listOf(
                                "Substances" to Icons.Default.Science,
                                "Safety Library" to Icons.Default.LibraryBooks,
                                "Resources" to Icons.Default.Public
                            )
                            tabsList.forEachIndexed { index, pair ->
                                val isSelected = selectedTabSegment == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .background(
                                            if (isSelected) Color(0xFF00FFCC).copy(alpha = 0.2f) else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            if (isSelected) BorderStroke(1.dp, Color(0xFF00FFCC)) else BorderStroke(0.dp, Color.Transparent),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            selectedTabSegment = index
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = pair.second,
                                            contentDescription = pair.first,
                                            tint = if (isSelected) Color(0xFF00FFCC) else Color.Gray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = pair.first,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // TAB 0: SUBSTANCES DIRECTORY
                if (selectedTabSegment == 0) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SUBSTANCES (${filteredDrugs.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF00FFCC),
                                letterSpacing = 1.5.sp
                            )
                            
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    substanceNameInput = ""
                                    substanceChemicalInput = ""
                                    substanceCategoryInput = DrugCategory.STIMULANT
                                    substanceDescriptionInput = ""
                                    substanceStreetInput = ""
                                    substanceOnsetInput = ""
                                    substanceDurationInput = ""
                                    substanceHalfLifeInput = ""
                                    substanceTipsInput = ""
                                    substanceOralThresholdInput = ""
                                    substanceOralLightInput = ""
                                    substanceOralCommonInput = ""
                                    substanceOralStrongInput = ""
                                    substanceOralWarningInput = ""
                                    showAddSubstanceDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00FFCC).copy(alpha = 0.15f),
                                    contentColor = Color(0xFF00FFCC)
                                ),
                                border = BorderStroke(1.dp, Color(0xFF00FFCC).copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp).testTag("add_substance_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("ADD SUBSTANCE", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (filteredDrugs.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = "No results",
                                    tint = Color.DarkGray,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    "No substances found matching query.",
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        items(filteredDrugs) { drug ->
                            val isCustom = remember(drug.id) {
                                DrugDatabase.drugs.none { it.id == drug.id }
                            }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { 
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onDrugSelected(drug.id) 
                                    }
                                    .testTag("drug_item_${drug.id}"),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1629)),
                                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1.0f)) {
                                            Text(
                                                text = drug.name,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = drug.chemicalName,
                                                fontSize = 11.sp,
                                                color = Color.Gray,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(drug.category.colorHex).copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = drug.category.displayName,
                                                    color = Color(drug.category.colorHex),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            if (isCustom) {
                                                IconButton(
                                                    onClick = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        coroutineScope.launch {
                                                            repository.deleteSubstanceById(drug.id)
                                                        }
                                                    },
                                                    modifier = Modifier.size(28.dp).testTag("delete_substance_${drug.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete Substance",
                                                        tint = Color.Red.copy(alpha = 0.7f),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = drug.description,
                                        fontSize = 12.sp,
                                        color = Color.LightGray.copy(alpha = 0.85f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 16.sp
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Icon(
                                                imageVector = Icons.Default.Tag,
                                                contentDescription = null,
                                                tint = Color(0xFF00FFCC).copy(alpha = 0.6f),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = drug.streetNames.joinToString(", "),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF00FFCC),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "VIEW PROFILE",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF00FFCC)
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = null,
                                                tint = Color(0xFF00FFCC),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // TAB 1: SAFETY LIBRARY & MANUALS
                if (selectedTabSegment == 1) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SAFETY MANUALS (${books.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFFF007F),
                                letterSpacing = 1.5.sp
                            )
                            
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    editingBookId = null
                                    bookTitleInput = ""
                                    bookAuthorInput = ""
                                    bookCategoryInput = "Harm Reduction"
                                    bookSummaryInput = ""
                                    bookContentInput = ""
                                    showAddBookDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF007F).copy(alpha = 0.15f),
                                    contentColor = Color(0xFFFF007F)
                                ),
                                border = BorderStroke(1.dp, Color(0xFFFF007F).copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("PUBLISH MANUAL", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (books.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFFFF007F))
                            }
                        }
                    } else {
                        items(books) { book ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f)), shape = RoundedCornerShape(14.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedBookForReading = book
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1629)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFFF007F).copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = book.category,
                                                color = Color(0xFFFF007F),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    editingBookId = book.id
                                                    bookTitleInput = book.title
                                                    bookAuthorInput = book.author
                                                    bookCategoryInput = book.category
                                                    bookSummaryInput = book.summary
                                                    bookContentInput = book.content
                                                    showAddBookDialog = true
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit Book", tint = Color(0xFF00FFCC), modifier = Modifier.size(16.dp))
                                            }
                                            
                                            IconButton(
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    coroutineScope.launch {
                                                        repository.deleteBook(book.id)
                                                    }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete Book", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = book.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )

                                    Text(
                                        text = "By ${book.author}",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = book.summary,
                                        fontSize = 12.sp,
                                        color = Color.LightGray.copy(alpha = 0.8f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 16.sp
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "READ MANUAL",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00FFCC)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = null,
                                            tint = Color(0xFF00FFCC),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // TAB 2: RESOURCES & COMMUNITY
                if (selectedTabSegment == 2) {
                    item {
                        Text(
                            text = "COMMUNITY & EXTERNAL MEDIA",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF00FFCC),
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            HubCard(
                                title = "Reddit r/ReagentTesting",
                                desc = "Crowdsourced spot-test results, kit verification, and peer advice.",
                                icon = Icons.Default.Forum,
                                tintColor = Color(0xFFFF4500),
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.reddit.com/r/ReagentTesting/"))
                                    context.startActivity(intent)
                                }
                            )

                            HubCard(
                                title = "Harm Reduction Video Tutorials",
                                desc = "Step-by-step video guides on checking for purity, fentanyl strips, and reagent reactions.",
                                icon = Icons.Default.PlayCircle,
                                tintColor = Color(0xFFFF0000),
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=harm+reduction+reagent+testing"))
                                    context.startActivity(intent)
                                }
                            )

                            // Advisor AI Banner
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAIChatClicked(null) },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1629)),
                                border = BorderStroke(1.dp, Color(0xFF00FFCC).copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = null,
                                        tint = Color(0xFF00FFCC),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Consult AdvisorAI Assistant",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Instant answers on dosages, interactions, and testing safety protocols.",
                                            fontSize = 11.sp,
                                            color = Color.LightGray.copy(alpha = 0.8f)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = Color(0xFF00FFCC),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 1. FULL SCREEN BOOK READER BOTTOM SHEET/MODAL
    selectedBookForReading?.let { book ->
        AlertDialog(
            onDismissRequest = { selectedBookForReading = null },
            title = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFF007F).copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(book.category, color = Color(0xFFFF007F), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        IconButton(onClick = { selectedBookForReading = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = book.title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Published by ${book.author}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = book.content,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedBookForReading = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC), contentColor = Color.Black)
                ) {
                    Text("CLOSE READER", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color(0xFF0A0E1A)
        )
    }

    // 2. DIALOG TO ADD/EDIT/UPDATE A MANUAL OR BOOK
    if (showAddBookDialog) {
        AlertDialog(
            onDismissRequest = { showAddBookDialog = false },
            title = {
                Text(
                    text = if (editingBookId == null) "Publish New Safety Book/Tutorial" else "Update Existing Safety Book",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = bookTitleInput,
                                onValueChange = { bookTitleInput = it },
                                label = { Text("Title", color = Color.Gray) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF007F),
                                    unfocusedBorderColor = Color.DarkGray,
                                    cursorColor = Color(0xFFFF007F)
                                )
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = bookAuthorInput,
                                onValueChange = { bookAuthorInput = it },
                                label = { Text("Author", color = Color.Gray) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF007F),
                                    unfocusedBorderColor = Color.DarkGray,
                                    cursorColor = Color(0xFFFF007F)
                                )
                            )
                        }

                        item {
                            // Category Selector Card
                            OutlinedTextField(
                                value = bookCategoryInput,
                                onValueChange = { bookCategoryInput = it },
                                label = { Text("Category / Target Class", color = Color.Gray) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("e.g. Harm Reduction, Trip Sitter, Chemistry") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF007F),
                                    unfocusedBorderColor = Color.DarkGray,
                                    cursorColor = Color(0xFFFF007F)
                                )
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = bookSummaryInput,
                                onValueChange = { bookSummaryInput = it },
                                label = { Text("Short Summary", color = Color.Gray) },
                                maxLines = 2,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF007F),
                                    unfocusedBorderColor = Color.DarkGray,
                                    cursorColor = Color(0xFFFF007F)
                                )
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = bookContentInput,
                                onValueChange = { bookContentInput = it },
                                label = { Text("Book Content & Directives", color = Color.Gray) },
                                minLines = 5,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF007F),
                                    unfocusedBorderColor = Color.DarkGray,
                                    cursorColor = Color(0xFFFF007F)
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (bookTitleInput.isNotBlank() && bookContentInput.isNotBlank()) {
                            val newBook = SafetyBook(
                                id = editingBookId ?: 0,
                                title = bookTitleInput,
                                author = if (bookAuthorInput.isBlank()) "Anonymous Advisor" else bookAuthorInput,
                                category = if (bookCategoryInput.isBlank()) "Harm Reduction" else bookCategoryInput,
                                summary = if (bookSummaryInput.isBlank()) "Comprehensive chemical safety guidebook and active session advice." else bookSummaryInput,
                                content = bookContentInput,
                                timestamp = System.currentTimeMillis()
                            )
                            coroutineScope.launch {
                                repository.insertBook(newBook)
                                showAddBookDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC), contentColor = Color.Black)
                ) {
                    Text("SAVE & PUBLISH", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBookDialog = false }) {
                    Text("CANCEL", color = Color.Gray, fontSize = 11.sp)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color(0xFF0A0E1A)
        )
    }

    // 3. DIALOG TO ADD A CUSTOM SUBSTANCE
    if (showAddSubstanceDialog) {
        AlertDialog(
            onDismissRequest = { showAddSubstanceDialog = false },
            title = {
                Text(
                    text = "Add Custom Substance",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            OutlinedTextField(
                                value = substanceNameInput,
                                onValueChange = { substanceNameInput = it },
                                label = { Text("Substance Name", color = Color.Gray) },
                                placeholder = { Text("e.g. 2C-B") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00FFCC),
                                    unfocusedBorderColor = Color.DarkGray,
                                    cursorColor = Color(0xFF00FFCC)
                                )
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = substanceChemicalInput,
                                onValueChange = { substanceChemicalInput = it },
                                label = { Text("Chemical Name", color = Color.Gray) },
                                placeholder = { Text("e.g. 4-Bromo-2,5-dimethoxyphenethylamine") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00FFCC),
                                    unfocusedBorderColor = Color.DarkGray,
                                    cursorColor = Color(0xFF00FFCC)
                                )
                            )
                        }

                        item {
                            Text("Class / Category", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(DrugCategory.values()) { category ->
                                    FilterChip(
                                        selected = substanceCategoryInput == category,
                                        onClick = { 
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            substanceCategoryInput = category 
                                        },
                                        label = { Text(category.displayName, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            labelColor = Color.LightGray,
                                            selectedLabelColor = Color.Black,
                                            selectedContainerColor = Color(category.colorHex)
                                        )
                                    )
                                }
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = substanceStreetInput,
                                onValueChange = { substanceStreetInput = it },
                                label = { Text("Common / Street Names", color = Color.Gray) },
                                placeholder = { Text("e.g. Nexus, Bees, Tucibi (comma-separated)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00FFCC),
                                    unfocusedBorderColor = Color.DarkGray,
                                    cursorColor = Color(0xFF00FFCC)
                                )
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = substanceDescriptionInput,
                                onValueChange = { substanceDescriptionInput = it },
                                label = { Text("Description & Profile", color = Color.Gray) },
                                placeholder = { Text("Provide a comprehensive description of the substance...") },
                                minLines = 3,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00FFCC),
                                    unfocusedBorderColor = Color.DarkGray,
                                    cursorColor = Color(0xFF00FFCC)
                                )
                            )
                        }

                        item {
                            Text("Timeline (Onset, Duration & Half-Life)", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = substanceOnsetInput,
                                    onValueChange = { substanceOnsetInput = it },
                                    label = { Text("Onset", color = Color.Gray) },
                                    placeholder = { Text("45 - 75 m") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF00FFCC),
                                        unfocusedBorderColor = Color.DarkGray,
                                        cursorColor = Color(0xFF00FFCC)
                                    )
                                )
                                OutlinedTextField(
                                    value = substanceDurationInput,
                                    onValueChange = { substanceDurationInput = it },
                                    label = { Text("Duration", color = Color.Gray) },
                                    placeholder = { Text("4 - 8 h") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF00FFCC),
                                        unfocusedBorderColor = Color.DarkGray,
                                        cursorColor = Color(0xFF00FFCC)
                                    )
                                )
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = substanceHalfLifeInput,
                                onValueChange = { substanceHalfLifeInput = it },
                                label = { Text("Half-Life", color = Color.Gray) },
                                placeholder = { Text("e.g. 5 hours") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00FFCC),
                                    unfocusedBorderColor = Color.DarkGray,
                                    cursorColor = Color(0xFF00FFCC)
                                )
                            )
                        }

                        item {
                            Text("Oral Dosages", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = substanceOralThresholdInput,
                                    onValueChange = { substanceOralThresholdInput = it },
                                    label = { Text("Threshold", color = Color.Gray) },
                                    placeholder = { Text("5 mg") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF00FFCC),
                                        unfocusedBorderColor = Color.DarkGray,
                                        cursorColor = Color(0xFF00FFCC)
                                    )
                                )
                                OutlinedTextField(
                                    value = substanceOralLightInput,
                                    onValueChange = { substanceOralLightInput = it },
                                    label = { Text("Light", color = Color.Gray) },
                                    placeholder = { Text("5 - 15 mg") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF00FFCC),
                                        unfocusedBorderColor = Color.DarkGray,
                                        cursorColor = Color(0xFF00FFCC)
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = substanceOralCommonInput,
                                    onValueChange = { substanceOralCommonInput = it },
                                    label = { Text("Common", color = Color.Gray) },
                                    placeholder = { Text("15 - 25 mg") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF00FFCC),
                                        unfocusedBorderColor = Color.DarkGray,
                                        cursorColor = Color(0xFF00FFCC)
                                    )
                                )
                                OutlinedTextField(
                                    value = substanceOralStrongInput,
                                    onValueChange = { substanceOralStrongInput = it },
                                    label = { Text("Strong", color = Color.Gray) },
                                    placeholder = { Text("25 - 40 mg") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF00FFCC),
                                        unfocusedBorderColor = Color.DarkGray,
                                        cursorColor = Color(0xFF00FFCC)
                                    )
                                )
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = substanceOralWarningInput,
                                onValueChange = { substanceOralWarningInput = it },
                                label = { Text("Dosage Warnings / Notes", color = Color.Gray) },
                                placeholder = { Text("e.g. Doses above 40 mg can be highly overwhelming.") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00FFCC),
                                    unfocusedBorderColor = Color.DarkGray,
                                    cursorColor = Color(0xFF00FFCC)
                                )
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = substanceTipsInput,
                                onValueChange = { substanceTipsInput = it },
                                label = { Text("Harm Reduction Tips (one per line)", color = Color.Gray) },
                                placeholder = { Text("e.g. Measure precisely using a milligram scale.\nAvoid redosing due to steep response curve.") },
                                minLines = 3,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00FFCC),
                                    unfocusedBorderColor = Color.DarkGray,
                                    cursorColor = Color(0xFF00FFCC)
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (substanceNameInput.isNotBlank() && substanceDescriptionInput.isNotBlank()) {
                            val slug = substanceNameInput.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
                            val newSubstance = SubstanceEntity(
                                id = slug,
                                name = substanceNameInput,
                                chemicalName = if (substanceChemicalInput.isBlank()) "Unknown IUPAC Name" else substanceChemicalInput,
                                categoryName = substanceCategoryInput.name,
                                description = substanceDescriptionInput,
                                streetNames = substanceStreetInput,
                                onset = if (substanceOnsetInput.isBlank()) "N/A" else substanceOnsetInput,
                                duration = if (substanceDurationInput.isBlank()) "N/A" else substanceDurationInput,
                                halfLife = if (substanceHalfLifeInput.isBlank()) "N/A" else substanceHalfLifeInput,
                                tips = if (substanceTipsInput.isBlank()) "No specific tips recorded." else substanceTipsInput,
                                dosageOralThreshold = if (substanceOralThresholdInput.isBlank()) "N/A" else substanceOralThresholdInput,
                                dosageOralLight = if (substanceOralLightInput.isBlank()) "N/A" else substanceOralLightInput,
                                dosageOralCommon = if (substanceOralCommonInput.isBlank()) "N/A" else substanceOralCommonInput,
                                dosageOralStrong = if (substanceOralStrongInput.isBlank()) "N/A" else substanceOralStrongInput,
                                dosageOralWarning = substanceOralWarningInput
                            )
                            coroutineScope.launch {
                                repository.insertSubstance(newSubstance)
                                showAddSubstanceDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC), contentColor = Color.Black)
                ) {
                    Text("SAVE SUBSTANCE", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubstanceDialog = false }) {
                    Text("CANCEL", color = Color.Gray, fontSize = 11.sp)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color(0xFF0A0E1A)
        )
    }
}

@Composable
fun QuickShortcutCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tintColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(BorderStroke(1.dp, tintColor.copy(alpha = 0.3f)), shape = RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1629))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(tintColor.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = tintColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = Color.LightGray.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun HubCard(
    title: String,
    desc: String,
    icon: ImageVector,
    tintColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(BorderStroke(1.dp, tintColor.copy(alpha = 0.25f)), shape = RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1629))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = tintColor,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                fontSize = 11.sp,
                color = Color.LightGray.copy(alpha = 0.7f),
                lineHeight = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
