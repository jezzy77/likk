package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuthManager
import com.example.utils.SecureCredentialHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingScreen(
    onSignUpSuccess: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager.getInstance(context) }
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var isSignInMode by rememberSaveable { mutableStateOf(true) } // true: Sign In, false: Sign Up
    
    // Credentials input states
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var codeInput by rememberSaveable { mutableStateOf("") }
    var twoFaInput by rememberSaveable { mutableStateOf("") }
    
    // Verification state holders
    var generatedCode by rememberSaveable { mutableStateOf("") }
    var generated2FaCode by rememberSaveable { mutableStateOf("") }
    var currentStep by rememberSaveable { mutableStateOf(1) } // 1: Info/credentials, 2: Verification code entry (Sign Up), 3: 2FA verification entry (Sign In)
    
    var isError by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf("") }

    // Google / Passkey chooser dialogue states
    var showGoogleChooser by remember { mutableStateOf(false) }
    var showPasskeyPrompt by remember { mutableStateOf(false) }
    var isAuthProcessing by remember { mutableStateOf(false) }
    var showCustomGoogleInput by remember { mutableStateOf(false) }
    var customGoogleEmail by remember { mutableStateOf("") }
    var googleApiError by remember { mutableStateOf<String?>(null) }
    var passkeyApiError by remember { mutableStateOf<String?>(null) }

    // Neon pulsation animations
    val infiniteTransition = rememberInfiniteTransition(label = "neon_glow")
    val neonAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neon_alpha"
    )

    val scaleFactor by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neon_scale"
    )

    fun validateAndSubmit() {
        val emailTrimmed = email.trim()
        val passwordTrimmed = password

        if (isSignInMode) {
            // Sign In Logic
            if (currentStep == 1) {
                if (emailTrimmed.isEmpty()) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isError = true
                    errorMessage = "Email cannot be empty."
                } else if (!emailTrimmed.contains("@") || !emailTrimmed.contains(".")) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isError = true
                    errorMessage = "Please enter a valid email address."
                } else if (passwordTrimmed.isEmpty()) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isError = true
                    errorMessage = "Password cannot be empty."
                } else {
                    val user = authManager.verifyCredentials(emailTrimmed, passwordTrimmed)
                    if (user != null) {
                        isError = false
                        if (user.has2FA) {
                            // Prompt 2FA
                            generated2FaCode = (100000..999999).random().toString()
                            currentStep = 3 // Go to 2FA screen
                        } else {
                            // Direct Sign In Success
                            authManager.setSignedInUser(emailTrimmed)
                            onSignUpSuccess(emailTrimmed)
                        }
                    } else {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isError = true
                        errorMessage = "Invalid email or password. Please try again."
                    }
                }
            } else if (currentStep == 3) {
                // 2FA Verification
                if (twoFaInput.trim() == generated2FaCode) {
                    isError = false
                    authManager.setSignedInUser(emailTrimmed)
                    onSignUpSuccess(emailTrimmed)
                } else {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isError = true
                    errorMessage = "Incorrect 2FA code. Please check your SMS or Authenticator simulator code."
                }
            }
        } else {
            // Sign Up Logic
            if (currentStep == 1) {
                if (emailTrimmed.isEmpty()) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isError = true
                    errorMessage = "Email cannot be empty."
                } else if (!emailTrimmed.contains("@") || !emailTrimmed.contains(".")) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isError = true
                    errorMessage = "Please enter a valid email address."
                } else if (passwordTrimmed.length < 6) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isError = true
                    errorMessage = "Password must be at least 6 characters."
                } else if (authManager.userExists(emailTrimmed)) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isError = true
                    errorMessage = "Email is already registered. Please sign in instead."
                } else {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isError = false
                    // Generate email verification code
                    generatedCode = (100000..999999).random().toString()
                    currentStep = 2 // Go to code verification screen
                }
            } else if (currentStep == 2) {
                // Registration verification
                if (codeInput.trim() == generatedCode) {
                    isError = false
                    authManager.registerUser(emailTrimmed, passwordTrimmed)
                    authManager.setSignedInUser(emailTrimmed)
                    onSignUpSuccess(emailTrimmed)
                } else {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isError = true
                    errorMessage = "Incorrect verification code. Please check the simulated email banner."
                }
            }
        }
    }

    fun attemptLoginDirect(loginEmail: String, loginPass: String) {
        val emailTrimmed = loginEmail.trim()
        val passwordTrimmed = loginPass
        if (emailTrimmed.isEmpty() || passwordTrimmed.isEmpty()) return
        
        val user = authManager.verifyCredentials(emailTrimmed, passwordTrimmed)
        if (user != null) {
            isError = false
            if (user.has2FA) {
                email = emailTrimmed
                password = passwordTrimmed
                generated2FaCode = (100000..999999).random().toString()
                currentStep = 3 // Go to 2FA screen
            } else {
                authManager.setSignedInUser(emailTrimmed)
                onSignUpSuccess(emailTrimmed)
            }
        } else {
            // fallback: set the fields so the user sees them and can try again or see error
            email = emailTrimmed
            password = passwordTrimmed
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            isError = true
            errorMessage = "Invalid credentials. Please enter manually."
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E1A)) // Dark space/cosmic background
    ) {
        // Subtle ambient background light
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp)
                .blur(80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00FFCC).copy(alpha = 0.15f),
                            Color(0xFFFF007F).copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .navigationBarsPadding()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // NEON LOGO CONTAINER
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Neon Painted Pill/Capsule Logo
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Under-glow/blur effect for neon feel
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(14.dp)
                    ) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF00FFCC).copy(alpha = neonAlpha * 0.5f), Color.Transparent)
                            ),
                            radius = size.minDimension / 1.2f
                        )
                    }

                    // Main crisp neon pill vectors
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 3.dp.toPx()
                        
                        drawCircle(
                            color = Color(0xFF00FFCC).copy(alpha = 0.25f),
                            radius = (size.minDimension / 2.0f) * scaleFactor,
                            style = Stroke(width = strokeWidth * 0.5f)
                        )

                        rotate(45f) {
                            val capWidth = 26.dp.toPx()
                            val capHeight = 66.dp.toPx()
                            val left = (size.width - capWidth) / 2
                            val top = (size.height - capHeight) / 2

                            drawRoundRect(
                                color = Color(0xFF00FFCC),
                                topLeft = androidx.compose.ui.geometry.Offset(left, top),
                                size = androidx.compose.ui.geometry.Size(capWidth, capHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(capWidth / 2, capWidth / 2),
                                style = Stroke(width = strokeWidth)
                            )

                            clipRect(
                                left = left - 10f,
                                top = top - 10f,
                                right = left + capWidth + 10f,
                                bottom = top + (capHeight / 2)
                            ) {
                                drawRoundRect(
                                    color = Color(0xFFFF007F),
                                    topLeft = androidx.compose.ui.geometry.Offset(left, top),
                                    size = androidx.compose.ui.geometry.Size(capWidth, capHeight),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(capWidth / 2, capWidth / 2),
                                    style = Stroke(width = strokeWidth)
                                )
                            }

                            drawLine(
                                color = Color.White,
                                start = androidx.compose.ui.geometry.Offset(left, top + capHeight / 2),
                                end = androidx.compose.ui.geometry.Offset(left + capWidth, top + capHeight / 2),
                                strokeWidth = strokeWidth * 0.8f
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "SubstanceID",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 2.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("landing_neon_title")
                )

                Text(
                    text = "SAFE • REAGENT • SCIENTIFIC",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 3.sp,
                    color = Color(0xFFFF007F),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "The ultimate chemical spot-testing guide, dose metrics library, and emergency response kit in your pocket.",
                    fontSize = 12.sp,
                    color = Color.LightGray.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }

            // EMAIL SIGNUP / SIGN IN CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF131B30).copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // MODE SELECTOR TAB ROW
                    if (currentStep == 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .background(Color(0xFF090D1A), shape = RoundedCornerShape(12.dp))
                                .padding(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(
                                        if (isSignInMode) Color(0xFF1B2544) else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        isSignInMode = true
                                        isError = false
                                    }
                                    .testTag("tab_sign_in"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "SIGN IN",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSignInMode) Color.White else Color.Gray
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(
                                        if (!isSignInMode) Color(0xFF1B2544) else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        isSignInMode = false
                                        isError = false
                                    }
                                    .testTag("tab_sign_up"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "SIGN UP",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (!isSignInMode) Color.White else Color.Gray
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    if (currentStep == 1) {
                        // STEP 1: CREDENTIALS ENTRY
                        Text(
                            text = if (isSignInMode) "Sign In to Your Account" else "Create Harm Reduction Account",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        
                        Text(
                            text = if (isSignInMode) "Access your synchronized profile & sitters list." else "Sign up with a valid email to access offline logs.",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
                            textAlign = TextAlign.Center
                        )

                        // SAVED CLOUD SANDBOX ACCOUNTS (Quick Tap)
                        if (isSignInMode) {
                            val savedUsers = remember { authManager.getUsers() }
                            if (savedUsers.isNotEmpty()) {
                                Text(
                                    text = "SAVED CLOUD SANDBOX ACCOUNTS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF00FFCC),
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                                )
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                                ) {
                                    savedUsers.forEach { savedUser ->
                                        Card(
                                            modifier = Modifier
                                                .width(135.dp)
                                                .clickable {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    attemptLoginDirect(savedUser.email, savedUser.passwordHash)
                                                }
                                                .testTag("quick_tap_user_${savedUser.email}"),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (email.trim().lowercase() == savedUser.email.lowercase()) {
                                                    Color(0xFF00FFCC).copy(alpha = 0.15f)
                                                } else {
                                                    Color(0xFF1E294B)
                                                }
                                            ),
                                            border = androidx.compose.foundation.BorderStroke(
                                                width = 1.dp,
                                                color = if (email.trim().lowercase() == savedUser.email.lowercase()) {
                                                    Color(0xFF00FFCC)
                                                } else {
                                                    Color.Transparent
                                                }
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    imageVector = if (savedUser.isGoogle) Icons.Default.AccountCircle else if (savedUser.isPasskey) Icons.Default.Fingerprint else Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = if (savedUser.isGoogle) Color(0xFF4285F4) else Color(0xFF00FFCC),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = savedUser.email.substringBefore("@"),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = if (savedUser.has2FA) "2FA Enabled" else "Quick Sign-In",
                                                    fontSize = 8.sp,
                                                    color = if (savedUser.has2FA) Color(0xFFFF007F) else Color.LightGray,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                isError = false
                            },
                            label = { Text("Email Address", color = Color.LightGray) },
                            placeholder = { Text("name@example.com", color = Color.DarkGray) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AlternateEmail,
                                    contentDescription = null,
                                    tint = Color(0xFF00FFCC)
                                )
                            },
                            isError = isError,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00FFCC),
                                unfocusedBorderColor = Color.DarkGray,
                                cursorColor = Color(0xFF00FFCC),
                                focusedLabelColor = Color(0xFF00FFCC),
                                unfocusedLabelColor = Color.Gray
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("landing_email_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        var passwordVisibility by remember { mutableStateOf(false) }
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                isError = false
                            },
                            label = { Text("Password", color = Color.LightGray) },
                            placeholder = { Text("At least 6 characters", color = Color.DarkGray) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF00FFCC)
                                )
                            },
                            isError = isError,
                            singleLine = true,
                            visualTransformation = if (passwordVisibility) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                                    Icon(
                                        imageVector = if (passwordVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = Color.Gray
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { validateAndSubmit() }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00FFCC),
                                unfocusedBorderColor = Color.DarkGray,
                                cursorColor = Color(0xFF00FFCC),
                                focusedLabelColor = Color(0xFF00FFCC),
                                unfocusedLabelColor = Color.Gray
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("landing_password_input")
                        )

                        if (isError) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp, start = 4.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { validateAndSubmit() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00FFCC),
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("landing_primary_auth_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    if (isSignInMode) "SIGN IN SECURELY" else "SEND REGISTRATION CODE",
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        // DIVIDER FOR THIRD PARTY AUTH
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.DarkGray))
                            Text(
                                "OR CONTINUE WITH",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 8.dp),
                                letterSpacing = 1.sp
                            )
                            Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.DarkGray))
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Google & Passkey Buttons side by side
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Google Button
                            Button(
                                onClick = {
                                    isAuthProcessing = true
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    scope.launch {
                                        SecureCredentialHelper.signInWithGoogle(
                                            context = context,
                                            onSuccess = { realEmail, realName ->
                                                isAuthProcessing = false
                                                authManager.registerUser(realEmail, "google_oauth_pass", isGoogle = true)
                                                authManager.setSignedInUser(realEmail)
                                                onSignUpSuccess(realEmail)
                                            },
                                            onError = { errorMsg, exception ->
                                                isAuthProcessing = false
                                                googleApiError = errorMsg
                                                showGoogleChooser = true
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("google_auth_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D2845)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .background(Color.White, shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "G",
                                            color = Color(0xFF4285F4),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Google", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            // Passkey Button
                            Button(
                                onClick = {
                                    isAuthProcessing = true
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    scope.launch {
                                        SecureCredentialHelper.signInWithPasskey(
                                            context = context,
                                            onSuccess = { assertionResponse ->
                                                isAuthProcessing = false
                                                val passkeyEmail = email.trim().ifEmpty { "fl74660@gmail.com" }
                                                authManager.registerUser(passkeyEmail, "passkey_secure_hardware_token", isPasskey = true)
                                                authManager.setSignedInUser(passkeyEmail)
                                                onSignUpSuccess(passkeyEmail)
                                            },
                                            onError = { errorMsg, exception ->
                                                isAuthProcessing = false
                                                passkeyApiError = errorMsg
                                                showPasskeyPrompt = true
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("passkey_auth_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D2845)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = "Passkey icon",
                                        tint = Color(0xFF00FFCC),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Passkey", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    } else if (currentStep == 2) {
                        // STEP 2: ENTER REGISTRATION VERIFICATION CODE (SIGN UP)
                        Text(
                            text = "Confirm Registration Code",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        
                        Text(
                            text = "A secure 6-digit confirmation code was sent to: $email",
                            fontSize = 10.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                        )

                        // Simulated SMTP notification banner
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFF007F).copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF007F).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = Color(0xFFFF007F),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "EMAIL OUTBOX: Registration code: $generatedCode",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color.White
                                )
                            }
                        }

                        OutlinedTextField(
                            value = codeInput,
                            onValueChange = {
                                if (it.length <= 6) {
                                    codeInput = it
                                    isError = false
                                }
                            },
                            label = { Text("6-Digit Code", color = Color.LightGray) },
                            placeholder = { Text("123456", color = Color.DarkGray) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.VpnKey,
                                    contentDescription = null,
                                    tint = Color(0xFF00FFCC)
                                )
                            },
                            isError = isError,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { validateAndSubmit() }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00FFCC),
                                unfocusedBorderColor = Color.DarkGray,
                                cursorColor = Color(0xFF00FFCC),
                                focusedLabelColor = Color(0xFF00FFCC),
                                unfocusedLabelColor = Color.Gray
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("landing_code_input")
                        )

                        if (isError) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp, start = 4.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    currentStep = 1
                                    isError = false
                                    codeInput = ""
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.LightGray
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Text("BACK", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = { validateAndSubmit() },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00FFCC),
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(44.dp)
                                    .testTag("landing_verify_button")
                            ) {
                                Text(
                                    "VERIFY & SIGN UP",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else if (currentStep == 3) {
                        // STEP 3: ENTER 2FA SECURITY CODE (SIGN IN)
                        Text(
                            text = "Two-Factor Verification",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        
                        Text(
                            text = "2FA has been activated on your profile. Complete authentication below.",
                            fontSize = 10.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                        )

                        // Simulated 2FA notification banner
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF00FFCC).copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00FFCC).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Smartphone,
                                    contentDescription = null,
                                    tint = Color(0xFF00FFCC),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "2FA SIMULATOR: Temporary PIN is: $generated2FaCode",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color.White
                                )
                            }
                        }

                        OutlinedTextField(
                            value = twoFaInput,
                            onValueChange = {
                                if (it.length <= 6) {
                                    twoFaInput = it
                                    isError = false
                                }
                            },
                            label = { Text("6-Digit 2FA Token", color = Color.LightGray) },
                            placeholder = { Text("123456", color = Color.DarkGray) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.VpnKey,
                                    contentDescription = null,
                                    tint = Color(0xFF00FFCC)
                                )
                            },
                            isError = isError,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { validateAndSubmit() }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00FFCC),
                                unfocusedBorderColor = Color.DarkGray,
                                cursorColor = Color(0xFF00FFCC),
                                focusedLabelColor = Color(0xFF00FFCC),
                                unfocusedLabelColor = Color.Gray
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("landing_2fa_input")
                        )

                        if (isError) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp, start = 4.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    currentStep = 1
                                    isError = false
                                    twoFaInput = ""
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.LightGray
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(44.dp)
                            ) {
                                Text("CANCEL", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = { validateAndSubmit() },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00FFCC),
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(44.dp)
                                    .testTag("landing_2fa_verify_button")
                            ) {
                                Text(
                                    "VERIFY PIN",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // INTERACTIVE GOOGLE ACCOUNT CHOOSER OVERLAY
    if (showGoogleChooser) {
        AlertDialog(
            onDismissRequest = { if (!isAuthProcessing) showGoogleChooser = false },
            confirmButton = {},
            dismissButton = {
                if (!isAuthProcessing) {
                    TextButton(onClick = { showGoogleChooser = false }) {
                        Text("CANCEL", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
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
                        "Sign in to SubstanceID",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (googleApiError != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF131B30),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4285F4).copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
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
                                    "The real Google Credential Manager SDK was executed, returning: \"$googleApiError\".\n\n" +
                                    "To sync real production accounts:\n" +
                                    "1. Configure your Web Client ID in SecureCredentialHelper.\n" +
                                    "2. Register your app SHA-1 fingerprint in your Google Play Console.\n\n" +
                                    "Choose a sandbox profile below to continue testing:",
                                    fontSize = 10.sp,
                                    lineHeight = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (isAuthProcessing) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Color(0xFF4285F4))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Authenticating with Google API...",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else if (showCustomGoogleInput) {
                        OutlinedTextField(
                            value = customGoogleEmail,
                            onValueChange = { customGoogleEmail = it },
                            label = { Text("Google Email Address", color = Color(0xFF4285F4)) },
                            placeholder = { Text("username@gmail.com", color = Color.Gray) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4285F4),
                                unfocusedBorderColor = Color.LightGray,
                                cursorColor = Color(0xFF4285F4)
                            ),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).testTag("google_custom_email_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showCustomGoogleInput = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("BACK", color = Color.Gray)
                            }

                            Button(
                                onClick = {
                                    val trimmed = customGoogleEmail.trim()
                                    if (trimmed.contains("@") && trimmed.contains(".")) {
                                        isAuthProcessing = true
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        scope.launch {
                                            delay(1500)
                                            isAuthProcessing = false
                                            showGoogleChooser = false
                                            showCustomGoogleInput = false
                                            authManager.registerUser(trimmed, "google_oauth_pass", isGoogle = true)
                                            authManager.setSignedInUser(trimmed)
                                            onSignUpSuccess(trimmed)
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4285F4),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.weight(1f).testTag("google_custom_email_submit")
                            ) {
                                Text("SIGN IN")
                            }
                        }
                    } else {
                        Text(
                            "Choose a saved Google account to sign in securely:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Soren Vance Account Row
                        Surface(
                            onClick = {
                                isAuthProcessing = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                scope.launch {
                                    delay(1200)
                                    isAuthProcessing = false
                                    showGoogleChooser = false
                                    val gEmail = "fl74660@gmail.com"
                                    authManager.registerUser(gEmail, "google_oauth_pass", isGoogle = true)
                                    authManager.setSignedInUser(gEmail)
                                    onSignUpSuccess(gEmail)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("google_account_select_soren"),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFF4285F4), shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "S",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Soren Vance",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "fl74660@gmail.com",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Custom Generic Account Option
                        Surface(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showCustomGoogleInput = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.Gray, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Use another account",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // INTERACTIVE PASSKEY / BIOMETRIC OVERLAY
    if (showPasskeyPrompt) {
        AlertDialog(
            onDismissRequest = { if (!isAuthProcessing) showPasskeyPrompt = false },
            confirmButton = {},
            dismissButton = {
                if (!isAuthProcessing) {
                    TextButton(onClick = { showPasskeyPrompt = false }) {
                        Text("CANCEL", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = Color(0xFF00FFCC),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Verify with Passkey",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (passkeyApiError != null) {
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
                                        "Real Passkey API Execution Report",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF00FFCC)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "The real Android Credential Manager SDK was executed, returning: \"$passkeyApiError\".\n\n" +
                                    "To verify with real hardware passkeys in production:\n" +
                                    "1. Configure and host .well-known/assetlinks.json on substanceid.example.com linking to your app package name.\n" +
                                    "2. Ensure your physical device has fingerprint or face verification enabled.\n\n" +
                                    "You may continue testing using the local secure enclave simulator below:",
                                    fontSize = 10.sp,
                                    lineHeight = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (isAuthProcessing) {
                        Spacer(modifier = Modifier.height(12.dp))
                        CircularProgressIndicator(color = Color(0xFF00FFCC))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Consulting Credential Provider...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        val passkeyEmail = remember { email.trim().ifEmpty { "fl74660@gmail.com" } }
                        Text(
                            "SubstanceID wants to sign in using your saved Passkey for $passkeyEmail:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(Color(0xFF00FFCC).copy(alpha = 0.15f), shape = CircleShape)
                                .clickable {
                                    isAuthProcessing = true
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    scope.launch {
                                        delay(1500)
                                        isAuthProcessing = false
                                        showPasskeyPrompt = false
                                        authManager.registerUser(passkeyEmail, "passkey_secure_hardware_token", isPasskey = true)
                                        authManager.setSignedInUser(passkeyEmail)
                                        onSignUpSuccess(passkeyEmail)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Biometric scanning sensor representation",
                                tint = Color(0xFF00FFCC),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Tap the fingerprint sensor to authenticate instantly",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}
