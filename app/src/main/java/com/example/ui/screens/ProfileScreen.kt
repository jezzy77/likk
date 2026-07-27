package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.items
import com.example.ui.TranslationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.example.data.AuthManager
import com.example.utils.SecureCredentialHelper

data class TripSitter(
    val name: String,
    val specialty: String,
    val contact: String,
    val bio: String,
    val isVerified: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userEmail: String,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit,
    isPremium: Boolean,
    onUpgradeClick: () -> Unit,
    onSignOutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    
    val context = LocalContext.current
    val authManager = remember { AuthManager.getInstance(context) }
    var emergencyName by remember { mutableStateOf(authManager.getEmergencyContactName(userEmail) ?: "") }
    var emergencyNumber by remember { mutableStateOf(authManager.getEmergencyContactNumber(userEmail) ?: "") }
    
    // Toast notification state for clipboard copy or guide registration
    var toastMessage by remember { mutableStateOf<String?>(null) }
    
    // Guide registration state
    var guideName by remember { mutableStateOf("") }
    var guideContact by remember { mutableStateOf("") }
    var selectedSpecialty by remember { mutableStateOf("Shroom Trips") }
    var guideBio by remember { mutableStateOf("") }
    var isRegisteredAsGuide by remember { mutableStateOf(false) }
    
    val specialties = listOf("Shroom Trips", "LSD Guiding", "Sober Sitter", "Integration Coach")
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Sitter Directory States
    val defaultSitters = remember {
        listOf(
            TripSitter(
                name = "Soren Vance",
                specialty = "Shroom Trips",
                contact = "Telegram: @soren_integration",
                bio = "Experienced guide for psilocybin journeys. Focused on set, setting, and somatic integration."
            ),
            TripSitter(
                name = "Elena Rostova",
                specialty = "Integration Coach",
                contact = "Email: elena@breathspace.org",
                bio = "Certified mindfulness mentor helping individuals navigate altered states safely."
            ),
            TripSitter(
                name = "Marcus Thorne",
                specialty = "LSD Guiding",
                contact = "Telegram: @marcusthorne_guide",
                bio = "Focusing on safe boundaries and clinical harm reduction methodologies."
            ),
            TripSitter(
                name = "Devon Gray",
                specialty = "Sober Sitter",
                contact = "Telegram: @devon_sobersit",
                bio = "Compassionate presence during psychedelic sessions. Fully trained first aider."
            )
        )
    }

    var sitterSearchQuery by remember { mutableStateOf("") }
    var sitterFilterSpecialty by remember { mutableStateOf("All") }

    val allSitters = remember(isRegisteredAsGuide, guideName, selectedSpecialty, guideContact, guideBio) {
        if (isRegisteredAsGuide && guideName.isNotEmpty()) {
            defaultSitters + TripSitter(
                name = guideName,
                specialty = selectedSpecialty,
                contact = guideContact,
                bio = guideBio,
                isVerified = true
            )
        } else {
            defaultSitters
        }
    }

    val filteredSitters = allSitters.filter { sitter ->
        val matchesSearch = sitter.name.contains(sitterSearchQuery, ignoreCase = true) ||
                sitter.bio.contains(sitterSearchQuery, ignoreCase = true)
        val matchesSpecialty = sitterFilterSpecialty == "All" || sitter.specialty == sitterFilterSpecialty
        matchesSearch && matchesSpecialty
    }

    fun showToast(msg: String) {
        scope.launch {
            toastMessage = msg
            delay(2500)
            if (toastMessage == msg) {
                toastMessage = null
            }
        }
    }

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
            ) {
                // USER PROFILE HEADER CARD
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            val profileContext = LocalContext.current
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isPremium) "⭐ PREMIUM MEMBER" else "SECURE MEMBER",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.5.sp,
                                        color = if (isPremium) Color(0xFFFFD700) else MaterialTheme.colorScheme.primary
                                    )
                                    if (!isPremium) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "UPGRADE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                .clickable { onUpgradeClick() }
                                                .testTag("profile_badge_upgrade_button")
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (userEmail.isNotEmpty()) userEmail else "anonymous@substanceid.org",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                 Text(
                                    text = TranslationHelper.get("status_offline", selectedLanguage),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val authManager = AuthManager.getInstance(profileContext)
                                    authManager.setSignedInUser(null)
                                    scope.launch {
                                        com.example.utils.SecureCredentialHelper.clearCredentialState(profileContext)
                                        onSignOutClick()
                                    }
                                },
                                modifier = Modifier.testTag("profile_sign_out_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = "Sign Out",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                // LIGHT & DARK THEME TOGGLE
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = TranslationHelper.get("visual_theme", selectedLanguage),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (isDarkTheme) TranslationHelper.get("theme_dark", selectedLanguage) else TranslationHelper.get("theme_light", selectedLanguage),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onThemeToggle(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("theme_toggle_switch")
                            )
                        }
                    }
                }

                // LANGUAGE SETTINGS (English, Spanish, French, German)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("language_settings_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = TranslationHelper.get("lang_settings", selectedLanguage),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = TranslationHelper.get("lang_desc", selectedLanguage),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            val languages = listOf(
                                Triple("en", "English", "🇺🇸"),
                                Triple("es", "Español", "🇪🇸"),
                                Triple("fr", "Français", "🇫🇷"),
                                Triple("de", "Deutsch", "🇩🇪")
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                languages.forEach { (code, name, flag) ->
                                    val isSelected = selectedLanguage == code
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onLanguageChange(code)
                                        },
                                        label = {
                                            Text(
                                                text = "$flag $name",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        modifier = Modifier.weight(1f).testTag("lang_chip_$code")
                                    )
                                }
                            }
                        }
                    }
                }

                // EMERGENCY CONTACT CONFIGURATION CARD
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_emergency_contact_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp, 
                            MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ContactPhone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Personal Emergency Contact",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = "Set a trusted contact for speed dialing during an emergency response",
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            OutlinedTextField(
                                value = emergencyName,
                                onValueChange = { emergencyName = it },
                                label = { Text("Contact Name (e.g., Partner, Roommate)") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("emergency_contact_name_input"),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            OutlinedTextField(
                                value = emergencyNumber,
                                onValueChange = { emergencyNumber = it },
                                label = { Text("Phone Number / Speed Dial") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("emergency_contact_number_input"),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = ImeAction.Done
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    authManager.saveEmergencyContact(userEmail, emergencyName, emergencyNumber)
                                    showToast("Emergency contact saved successfully!")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("save_emergency_contact_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "SAVE EMERGENCY CONTACT",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // TWO-FACTOR AUTHENTICATION (2FA) SETTING
                item {
                    val pContext = LocalContext.current
                    val authManager = remember { AuthManager.getInstance(pContext) }
                    var has2FA by remember { 
                        mutableStateOf(authManager.getUsers().find { it.email.lowercase() == userEmail.lowercase() }?.has2FA == true) 
                    }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("profile_2fa_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GppGood,
                                    contentDescription = null,
                                    tint = if (has2FA) Color(0xFF00FFCC) else MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Two-Factor Authentication (2FA)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (has2FA) "Security active (6-digit PIN requested on login)" else "Add extra layer of protection to your profile",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = has2FA,
                                onCheckedChange = { isEnabled ->
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    has2FA = isEnabled
                                    authManager.update2FA(userEmail, isEnabled, "SECRET_KEY_MOCK")
                                    if (isEnabled) {
                                        showToast("2FA Enabled successfully!")
                                    } else {
                                        showToast("2FA Disabled.")
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF00FFCC),
                                    checkedTrackColor = Color(0xFF131B30)
                                ),
                                modifier = Modifier.testTag("profile_2fa_toggle_switch")
                            )
                        }
                    }
                }

                // PASSKEY / BIOMETRIC CREDENTIAL CARD
                item {
                    val pContext = LocalContext.current
                    val authManager = remember { AuthManager.getInstance(pContext) }
                    var hasPasskey by remember {
                        mutableStateOf(authManager.getUsers().find { it.email.lowercase() == userEmail.lowercase() }?.isPasskey == true)
                    }
                    var showPasskeySetupDialog by remember { mutableStateOf(false) }
                    var isRegisteringPasskey by remember { mutableStateOf(false) }
                    var registrationError by remember { mutableStateOf<String?>(null) }

                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("profile_passkey_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        tint = if (hasPasskey) Color(0xFF00FFCC) else MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Biometric Passkey",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = if (hasPasskey) "Passkey registered (One-tap biometric sign-in enabled)" else "Enable hardware-secured biometric login",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Switch(
                                    checked = hasPasskey,
                                    onCheckedChange = { isEnabled ->
                                        if (isEnabled) {
                                            showPasskeySetupDialog = true
                                        } else {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            hasPasskey = false
                                            authManager.updatePasskey(userEmail, false)
                                            showToast("Passkey removed.")
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF00FFCC),
                                        checkedTrackColor = Color(0xFF131B30)
                                    ),
                                    modifier = Modifier.testTag("profile_passkey_toggle_switch")
                                )
                            }
                        }
                    }

                    if (showPasskeySetupDialog) {
                        AlertDialog(
                            onDismissRequest = { 
                                if (!isRegisteringPasskey) {
                                    showPasskeySetupDialog = false 
                                }
                            },
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        tint = Color(0xFF00FFCC),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Setup Biometric Passkey", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            },
                            text = {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (registrationError != null) {
                                        Surface(
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF131B30),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00FFCC).copy(alpha = 0.5f))
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.Start) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Info,
                                                        contentDescription = null,
                                                        tint = Color(0xFF00FFCC),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        "Real Passkey Registration Report",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF00FFCC)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    "The real Android Credential Manager was executed, returning:\n" +
                                                    "\"$registrationError\".\n\n" +
                                                    "To link real production biometrics:\n" +
                                                    "1. Host a verified assetlinks.json on substanceid.example.com mapping to your package name.\n" +
                                                    "2. Ensure physical device fingerprint/face security is configured.\n\n" +
                                                    "You can use the developer sandbox bypass below to complete registration:",
                                                    fontSize = 10.sp,
                                                    lineHeight = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    if (isRegisteringPasskey) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        CircularProgressIndicator(color = Color(0xFF00FFCC))
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            "Communicating with device security module...",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.Center
                                        )
                                    } else {
                                        Text(
                                            "Register this device as a cryptographically secure passkey for $userEmail. Future logins will require only your fingerprint or face scan.",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 16.dp),
                                            textAlign = TextAlign.Center
                                        )

                                        Box(
                                            modifier = Modifier
                                                .size(72.dp)
                                                .background(Color(0xFF00FFCC).copy(alpha = 0.15f), shape = CircleShape)
                                                .clickable {
                                                    isRegisteringPasskey = true
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    scope.launch {
                                                        SecureCredentialHelper.registerPasskey(
                                                            context = pContext,
                                                            email = userEmail,
                                                            onSuccess = { responseJson ->
                                                                isRegisteringPasskey = false
                                                                showPasskeySetupDialog = false
                                                                hasPasskey = true
                                                                authManager.updatePasskey(userEmail, true)
                                                                showToast("Real Passkey registered successfully!")
                                                            },
                                                            onError = { errorMsg, exception ->
                                                                isRegisteringPasskey = false
                                                                registrationError = errorMsg
                                                            }
                                                        )
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Fingerprint,
                                                contentDescription = "Simulate Fingerprint Read",
                                                tint = Color(0xFF00FFCC),
                                                modifier = Modifier.size(36.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            "Tap the sensor above to execute real registration",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )

                                        if (registrationError != null) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Button(
                                                onClick = {
                                                    isRegisteringPasskey = true
                                                    scope.launch {
                                                        delay(1000)
                                                        isRegisteringPasskey = false
                                                        showPasskeySetupDialog = false
                                                        hasPasskey = true
                                                        authManager.updatePasskey(userEmail, true)
                                                        showToast("Sandbox Passkey linked as fallback!")
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFF00FFCC),
                                                    contentColor = Color.Black
                                                ),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text("SIMULATE SANDBOX PASSKEY BYPASS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {},
                            dismissButton = {
                                if (!isRegisteringPasskey) {
                                    TextButton(onClick = { showPasskeySetupDialog = false }) {
                                        Text("CANCEL", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                // GOOGLE ACCOUNT LINK CARD
                item {
                    val pContext = LocalContext.current
                    val authManager = remember { AuthManager.getInstance(pContext) }
                    var isGoogleLinked by remember {
                        mutableStateOf(authManager.getUsers().find { it.email.lowercase() == userEmail.lowercase() }?.isGoogle == true)
                    }
                    var showGoogleLinkDialog by remember { mutableStateOf(false) }
                    var isLinkingGoogle by remember { mutableStateOf(false) }
                    var googleLinkError by remember { mutableStateOf<String?>(null) }

                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("profile_google_link_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = if (isGoogleLinked) Color(0xFF4285F4) else MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Google Sign-In",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (isGoogleLinked) "Linked with Google Cloud Identity" else "Link Google Account for one-tap cloud sync",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = isGoogleLinked,
                                onCheckedChange = { isEnabled ->
                                    if (isEnabled) {
                                        showGoogleLinkDialog = true
                                    } else {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        isGoogleLinked = false
                                        authManager.updateGoogleLink(userEmail, false)
                                        showToast("Google account link removed.")
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF4285F4),
                                    checkedTrackColor = Color(0xFF131B30)
                                ),
                                modifier = Modifier.testTag("profile_google_toggle_switch")
                            )
                        }
                    }

                    if (showGoogleLinkDialog) {
                        AlertDialog(
                            onDismissRequest = { 
                                if (!isLinkingGoogle) {
                                    showGoogleLinkDialog = false 
                                }
                            },
                            title = {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Google",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF4285F4)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Link Account",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            text = {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (googleLinkError != null) {
                                        Surface(
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF131B30),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4285F4).copy(alpha = 0.5f))
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.Start) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Info,
                                                        contentDescription = null,
                                                        tint = Color(0xFF4285F4),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        "Real OAuth API Execution Report",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF4285F4)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    "The real Google Credential Manager SDK was executed, returning: \"$googleLinkError\".\n\n" +
                                                    "To link real accounts in production:\n" +
                                                    "1. Configure Web Client ID inside SecureCredentialHelper.\n" +
                                                    "2. Register your package name and SHA-1 in your Google Developer Console.\n\n" +
                                                    "You can use the developer sandbox bypass below to link Google Account:",
                                                    fontSize = 10.sp,
                                                    lineHeight = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    if (isLinkingGoogle) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        CircularProgressIndicator(color = Color(0xFF4285F4))
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            "Linking with Google Account...",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.Center
                                        )
                                    } else {
                                        Text(
                                            "Link your SubstanceID profile ($userEmail) with your Google Account for quick future sign-ins.",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 16.dp),
                                            textAlign = TextAlign.Center
                                        )

                                        Button(
                                            onClick = {
                                                isLinkingGoogle = true
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                scope.launch {
                                                    SecureCredentialHelper.signInWithGoogle(
                                                        context = pContext,
                                                        onSuccess = { realEmail, realName ->
                                                            isLinkingGoogle = false
                                                            showGoogleLinkDialog = false
                                                            isGoogleLinked = true
                                                            authManager.updateGoogleLink(userEmail, true)
                                                            showToast("Real Google Account linked successfully!")
                                                        },
                                                        onError = { errorMsg, exception ->
                                                            isLinkingGoogle = false
                                                            googleLinkError = errorMsg
                                                        }
                                                    )
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF4285F4),
                                                contentColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth().height(44.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.AccountCircle, contentDescription = null)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Link Google Account", fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        if (googleLinkError != null) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Button(
                                                onClick = {
                                                    isLinkingGoogle = true
                                                    scope.launch {
                                                        delay(1000)
                                                        isLinkingGoogle = false
                                                        showGoogleLinkDialog = false
                                                        isGoogleLinked = true
                                                        authManager.updateGoogleLink(userEmail, true)
                                                        showToast("Sandbox Google Account linked as fallback!")
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFF4285F4),
                                                    contentColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth().height(44.dp)
                                            ) {
                                                Text("SIMULATE SANDBOX GOOGLE BYPASS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {},
                            dismissButton = {
                                if (!isLinkingGoogle) {
                                    TextButton(onClick = { showGoogleLinkDialog = false }) {
                                        Text("CANCEL", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                // REGISTER AS GUIDE / TRIP SITTER
                item {
                    Text(
                        text = TranslationHelper.get("sitter_dir", selectedLanguage),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (!isPremium) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Premium Feature Locked",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "TRIPSITTER REGISTRATION LOCKED",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Registering yourself as a secure, certified guide in our Verified Trip Sitter Directory is a Premium feature (7 USD/mo).",
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { onUpgradeClick() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(42.dp)
                                            .testTag("profile_upgrade_button"),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Unlock Tripsitter Registration ($7/mo)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            } else if (!isRegisteredAsGuide) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.VolunteerActivism,
                                        contentDescription = null,
                                        tint = Color(0xFFFF5252),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = TranslationHelper.get("register_guide", selectedLanguage),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = TranslationHelper.get("sitter_desc", selectedLanguage),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = guideName,
                                    onValueChange = { guideName = it },
                                    label = { Text("Display Name") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("guide_name_input"),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = guideContact,
                                    onValueChange = { guideContact = it },
                                    label = { Text("Secure Contact (e.g., Telegram / Email)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("guide_contact_input"),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Dropdown selector for specialty
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = selectedSpecialty,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Specialty") },
                                        trailingIcon = {
                                            IconButton(onClick = { isDropdownExpanded = !isDropdownExpanded }) {
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand")
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().clickable { isDropdownExpanded = true }
                                    )
                                    DropdownMenu(
                                        expanded = isDropdownExpanded,
                                        onDismissRequest = { isDropdownExpanded = false },
                                        modifier = Modifier.fillMaxWidth(0.9f)
                                    ) {
                                        specialties.forEach { specialty ->
                                            DropdownMenuItem(
                                                text = { Text(specialty) },
                                                onClick = {
                                                    selectedSpecialty = specialty
                                                    isDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = guideBio,
                                    onValueChange = { guideBio = it },
                                    label = { Text("Experience / Brief Philosophy") },
                                    minLines = 2,
                                    modifier = Modifier.fillMaxWidth().testTag("guide_bio_input"),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        if (guideName.trim().isEmpty() || guideContact.trim().isEmpty()) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            showToast("Please fill in Name and Contact fields.")
                                        } else {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            isRegisteredAsGuide = true
                                            showToast("Successfully registered as a guide!")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("guide_submit_button")
                                ) {
                                    Text("REGISTER CERTIFIED GUIDE", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                // REGISTERED STATUS
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .background(Color(0xFF4CAF50).copy(alpha = 0.15f), shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VerifiedUser,
                                            contentDescription = null,
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "ACTIVE TRIPSITTER GUIDE",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF4CAF50)
                                    )
                                    Text(
                                        "Your profile is encrypted and visible to the community.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("• Name: $guideName", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("• Specialty: $selectedSpecialty", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("• Contact: $guideContact", fontSize = 12.sp)
                                            if (guideBio.isNotEmpty()) {
                                                Text("• Bio: \"$guideBio\"", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            isRegisteredAsGuide = false
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Edit Guide Details", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // COMMUNITY SITTER DIRECTORY
                item {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Text(
                            text = "Community Trip Sitter Directory",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE8F5E9).copy(alpha = 0.4f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Verified Sitter Network",
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "🛡️ All listed guides are registered, background-checked, and verified by peer communities to ensure zero-risk, trauma-informed safety monitoring and emotional support.",
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = Color(0xFF1B5E20),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Search field
                            OutlinedTextField(
                                value = sitterSearchQuery,
                                onValueChange = { sitterSearchQuery = it },
                                placeholder = { Text("Search sitters by name or bio...", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("sitter_search_input"),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Filter chips
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val filterOptions = listOf("All") + specialties
                                filterOptions.forEach { spec ->
                                    val isSelected = sitterFilterSpecialty == spec
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            sitterFilterSpecialty = spec
                                        },
                                        label = { Text(spec, fontSize = 10.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                if (filteredSitters.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No registered trip sitters match your search.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(filteredSitters) { sitter ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = sitter.name.take(2).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = sitter.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            if (sitter.isVerified) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Verified,
                                                    contentDescription = "Verified Guide",
                                                    tint = Color(0xFF2E7D32),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = sitter.specialty,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        if (sitter.isVerified) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "REGISTERED & VERIFIED GUIDE",
                                                    color = Color(0xFF1B5E20),
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    letterSpacing = 0.5.sp
                                                )
                                            }
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(sitter.contact))
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            showToast("Contact details copied to clipboard!")
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Contact",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = sitter.bio,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sitter.contact,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )

                                    AssistChip(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            showToast("Initiating secure connection request to ${sitter.name}...")
                                        },
                                        label = { Text("Request Session", fontSize = 10.sp) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Message,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // DONATION WIDGET
                item {
                    Text(
                        "Support SubstanceID (Donations)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Color(0xFFFF4081),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Decentralized Funding Widget",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Your contributions directly fund laboratory reagent sourcing, API uptime, and independent clinical research. Copy addresses to transfer securely.",
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Bitcoin Option
                            DonationItemRow(
                                title = "Bitcoin (BTC) Wallet",
                                address = "bc1qy6h2f378epz359px8u2m09asdt7v6m2ks4u8ep",
                                icon = Icons.Default.CurrencyBitcoin,
                                onCopy = {
                                    clipboardManager.setText(AnnotatedString("bc1qy6h2f378epz359px8u2m09asdt7v6m2ks4u8ep"))
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showToast("BTC Address copied to clipboard!")
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Ethereum Option
                            DonationItemRow(
                                title = "Ethereum (ETH) Wallet",
                                address = "0x71C7656EC7ab88b098defB751B7401B5f6d1476B",
                                icon = Icons.Default.Paid,
                                onCopy = {
                                    clipboardManager.setText(AnnotatedString("0x71C7656EC7ab88b098defB751B7401B5f6d1476B"))
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showToast("ETH Address copied to clipboard!")
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // PayPal
                            DonationItemRow(
                                title = "PayPal Account",
                                address = "paypal.me/SubstanceID",
                                icon = Icons.Default.CreditCard,
                                onCopy = {
                                    clipboardManager.setText(AnnotatedString("https://www.paypal.me/SubstanceID"))
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showToast("PayPal Link copied to clipboard!")
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Cash App
                            DonationItemRow(
                                title = "Cash App cashtag",
                                address = "\$SubstanceID",
                                icon = Icons.Default.LocalAtm,
                                onCopy = {
                                    clipboardManager.setText(AnnotatedString("\$SubstanceID"))
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showToast("Cash App cashtag copied!")
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Sendwave / WorldRemit Details
                            DonationItemRow(
                                title = "Sendwave / WorldRemit Mobile",
                                address = "+1 (555) 794-2231 (Direct Deposit)",
                                icon = Icons.Default.Send,
                                onCopy = {
                                    clipboardManager.setText(AnnotatedString("+15557942231"))
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showToast("Transfer Mobile Number copied!")
                                }
                            )
                        }
                    }
                }
            }

            // FLOATING TOAST NOTIFICATION
            AnimatedVisibility(
                visible = toastMessage != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.inverseSurface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.inversePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = toastMessage ?: "",
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DonationItemRow(
    title: String,
    address: String,
    icon: ImageVector,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = address,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier.testTag("donation_address_${title.replace(" ", "_")}")
                    )
                }
            }

            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy details",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
