package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CommunityPost
import com.example.data.GymNotification
import com.example.data.WorkoutTemplate
import com.example.ui.theme.*
import com.example.ui.viewmodel.GymViewModel

// --- MASTER NAVIGATION CONTROLLER ---

@Composable
fun IronPulseAppContent(viewModel: GymViewModel) {
    var currentScreen by remember { mutableStateOf("splash") }
    val userProfile by viewModel.userProfile.collectAsState()

    // Redirect to login if logged out, or auto-onboard
    LaunchedEffect(userProfile?.isLoggedIn) {
        if (userProfile?.isLoggedIn == false && currentScreen != "splash" && currentScreen != "onboarding" && currentScreen != "login") {
            currentScreen = "login"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CarbonGradientStart, CarbonGradientEnd)
                )
            )
    ) {
        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
            when (screen) {
                "splash" -> SplashScreen(
                    onGetStarted = {
                        currentScreen = "onboarding"
                    }
                )
                "onboarding" -> OnboardingScreen(
                    onProceed = {
                        currentScreen = "login"
                    }
                )
                "login" -> LoginAndSignupScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {
                        currentScreen = "home"
                    }
                )
                else -> {
                    // Main App Shell with premium Bottom Navigation Bar
                    Scaffold(
                        bottomBar = {
                            IronPulseBottomBar(
                                currentScreen = currentScreen,
                                onSelectScreen = { currentScreen = it }
                            )
                        },
                        containerColor = Color.Transparent
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentScreen) {
                                "home" -> HomeDashboardScreen(viewModel, onNavigate = { currentScreen = it })
                                "workouts" -> WorkoutsScreen(viewModel)
                                "ai_coach" -> AiCoachScreen(viewModel)
                                "meal_planner" -> MealPlannerScreen(viewModel)
                                "analytics" -> ProgressAnalyticsScreen(viewModel)
                                "social" -> CommunityFeedScreen(viewModel)
                                "leaderboard" -> LeaderboardsScreen(viewModel)
                                "profile" -> ProfileAndSettingsScreen(viewModel, onNavigate = { currentScreen = it })
                                "admin" -> AdminDashboardScreen(viewModel)
                                "notifications" -> NotificationsLogScreen(viewModel)
                            }
                        }
                    }
                }
            }
        }

        // --- Razorpay Payment Invoice Successful Overlay Overlay ---
        val showAnim by viewModel.paymentSuccessAnimationTriggered.collectAsState()
        if (showAnim) {
            PaymentSuccessOverlay(viewModel)
        }
    }
}

// --- CORE UTILITY DESIGN COMPONENTS ---

@Composable
fun GlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = NeonRed,
    glowColor: Color = NeonRedGlow,
    testTag: String = ""
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 24.dp),
        modifier = modifier
            .testTag(testTag)
            .shadowGlow(borderRadius = 12.dp, color = glowColor)
    ) {
        Text(
            text = text.uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = 1.5.sp,
            fontSize = 14.sp
        )
    }
}

fun Modifier.shadowGlow(borderRadius: androidx.compose.ui.unit.Dp, color: Color) = this.drawBehind {
    drawCircle(
        color = color.copy(alpha = 0.15f),
        radius = size.maxDimension / 2.2f,
        center = Offset(size.width / 2, size.height / 2)
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
            .background(CarbonCard, RoundedCornerShape(16.dp))
            .padding(16.dp),
        content = content
    )
}

// --- 1. SPLASH SCREEN ---

@Composable
fun SplashScreen(onGetStarted: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Futuristic background glowing circles
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = NeonRedGlow.copy(alpha = 0.25f),
                radius = 200.dp.toPx(),
                center = Offset(size.width, 0f)
            )
            drawCircle(
                color = ElectricBlueGlow.copy(alpha = 0.15f),
                radius = 250.dp.toPx(),
                center = Offset(0f, size.height)
            )
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // High-weight luxury gym brand icon
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = NeonRed,
                modifier = Modifier
                    .size(90.dp)
                    .padding(bottom = 8.dp)
            )

            Text(
                text = "IRON PULSE",
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 4.sp,
                fontFamily = FontFamily.SansSerif
            )
            Text(
                text = "NEON REVOLUTION ATHLETICS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricBlue,
                letterSpacing = 3.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "THE BILLION-DOLLAR FIT SYSTEM",
                fontSize = 11.sp,
                color = TextSecondary,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            GlowButton(
                text = "Initiate Protocol",
                onClick = onGetStarted,
                modifier = Modifier.fillMaxWidth(0.85f),
                testTag = "splash_get_started_button"
            )
        }
    }
}

// --- 2. ONBOARDING SCREEN ---

@Composable
fun OnboardingScreen(onProceed: () -> Unit) {
    var step by remember { mutableStateOf(1) }

    val quotes = listOf(
        "\"Settle for nothing but elite size, performance & progressive hypertrophy.\"",
        "\"Real-time adaptive AI computations tracking Indian meal plans & local workouts.\"",
        "\"Competitive leaderboards, custom streak trophies & multi-tier premium community feeds.\""
    )
    val banners = listOf(
        "TITAN WEIGHT OVERLOAD",
        "PRECISE NUTRITIONAL INTELLIGENCE",
        "THE SOCIAL RIVALRY GATEWAY"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Glowing aura
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = if (step == 1) NeonRedGlow else if (step == 2) ElectricBlueGlow else GoldGlow.copy(alpha = 0.1f),
                radius = 300.dp.toPx(),
                center = Offset(size.width / 2f, size.height / 3f)
            )
        }

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PULSE SELECTION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricBlue,
                letterSpacing = 2.sp
            )
            Text(
                text = "0$step / 03",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
        }

        // Onboarding Graphics Illustration (Dynamic drawing per screen!)
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .border(2.dp, CarbonBorder, CircleShape)
                    .background(CarbonCard, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (step) {
                        1 -> Icons.Default.FitnessCenter
                        2 -> Icons.Default.Restaurant
                        else -> Icons.Default.Group
                    },
                    contentDescription = null,
                    tint = if (step == 1) NeonRed else if (step == 2) ElectricBlue else Color(0xFFFFD700),
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = banners[step - 1],
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = quotes[step - 1],
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Step Indicator dots
            Spacer(modifier = Modifier.height(28.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in 1..3) {
                    Box(
                        modifier = Modifier
                            .size(if (i == step) 20.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(if (i == step) NeonRed else CarbonBorder)
                    )
                }
            }
        }

        // Bottom CTAs
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onProceed,
                modifier = Modifier.testTag("onboarding_skip_button")
            ) {
                Text("SKIP", color = TextMuted, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            GlowButton(
                text = if (step < 3) "PROCEED" else "ENTER ARENA",
                onClick = {
                    if (step < 3) {
                        step++
                    } else {
                        onProceed()
                    }
                },
                backgroundColor = if (step == 2) ElectricBlue else NeonRed,
                glowColor = if (step == 2) ElectricBlueGlow else NeonRedGlow,
                testTag = "onboarding_next_button"
            )
        }
    }
}

// --- 3. LOGIN & SIGNUP SCREEN WITH OTP SIMULATION ---

@Composable
fun LoginAndSignupScreen(
    viewModel: GymViewModel,
    onLoginSuccess: () -> Unit
) {
    var isSignUp by remember { mutableStateOf(false) }

    val email by viewModel.currentAuthEmail.collectAsState()
    val password by viewModel.currentAuthPassword.collectAsState()
    val name by viewModel.currentAuthName.collectAsState()
    val phone by viewModel.currentAuthPhone.collectAsState()

    val otpScreenActive by viewModel.isOtpScreenActive.collectAsState()
    val otpInput by viewModel.otpInputCode.collectAsState()
    val optSent by viewModel.otpSentCode.collectAsState()

    val biometric by viewModel.biometricEnabled.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalFireDepartment, null, tint = NeonRed, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("IRON PULSE SECURE GATEWAY", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ElectricBlue, letterSpacing = 1.sp)
            }

            Spacer(modifier = Modifier.height(44.dp))

            if (!otpScreenActive) {
                // Dual signup tab selection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CarbonCard)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isSignUp) CarbonBorder else Color.Transparent)
                            .clickable { isSignUp = false }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("LOGIN", color = if (!isSignUp) Color.White else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSignUp) CarbonBorder else Color.Transparent)
                            .clickable { isSignUp = true }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("SIGN UP", color = if (isSignUp) Color.White else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isSignUp) "CREATE ULTIMATE ACCOUNT" else "SECURE MEMBERSHIP ENTRANCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (isSignUp) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { viewModel.currentAuthName.value = it },
                            label = { Text("Your Name") },
                            colors = pulseTextFieldColors(NeonRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_name_input")
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { viewModel.currentAuthEmail.value = it },
                        label = { Text("Secure Email Address") },
                        colors = pulseTextFieldColors(NeonRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { viewModel.currentAuthPassword.value = it },
                        label = { Text("JWT Gate Token (Password)") },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = pulseTextFieldColors(NeonRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = biometric,
                            onCheckedChange = { viewModel.biometricEnabled.value = it },
                            colors = CheckboxDefaults.colors(checkedColor = NeonRed)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Permit Biometric Check-in Bypass", color = TextSecondary, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    GlowButton(
                        text = if (isSignUp) "Register Credentials" else "Unlock Access",
                        onClick = {
                            if (isSignUp) {
                                viewModel.signup(name, email, password)
                            } else {
                                viewModel.login(email, password)
                            }
                            onLoginSuccess()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "submit_credentials_button"
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // OTP Phone Alternative Tab
                Text("OR OTP CELLULAR SIGN-IN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { viewModel.currentAuthPhone.value = it },
                        placeholder = { Text("+91 Mobile Number") },
                        colors = pulseTextFieldColors(ElectricBlue),
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = { viewModel.sendOtp(phone) },
                        colors = ButtonDefaults.buttonColors(containerColor = CarbonBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("SEND CODE", fontSize = 11.sp, color = ElectricBlue, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Guest and Social buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.enterGuestMode()
                            onLoginSuccess()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        border = BorderStroke(1.dp, DynamicRedBorder()),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Person, null, tint = NeonRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("GUEST ROAD", color = NeonRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.signup("Google Renegade", "google@fit.fit", "google")
                            onLoginSuccess()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CarbonBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("GOOGLE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

            } else {
                // --- OTP VERIFICATION CARD ---
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "CELLULAR SECURE VERIFICATION",
                        fontSize = 12.sp,
                        color = ElectricBlue,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Enter the 4-digit token sent to your SMS interface for phone $phone.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { viewModel.otpInputCode.value = it },
                        placeholder = { Text("E.g. ****") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = pulseTextFieldColors(NeonRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_digit_input"),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "(Notification: Simulation OTP code generated is: $optSent)",
                        fontSize = 11.sp,
                        color = NeonRed,
                        style = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Bold),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.isOtpScreenActive.value = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("BACK", color = TextMuted)
                        }

                        GlowButton(
                            text = "AURA DECRYPT",
                            onClick = {
                                viewModel.verifyOtp(otpInput)
                                onLoginSuccess()
                            },
                            modifier = Modifier.weight(1.5f),
                            testTag = "verify_otp_button"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun pulseTextFieldColors(borderColor: Color = NeonRed) = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = borderColor,
    focusedLabelColor = borderColor,
    unfocusedBorderColor = CarbonBorder,
    unfocusedLabelColor = TextMuted,
    focusedPlaceholderColor = TextMuted,
    unfocusedPlaceholderColor = TextMuted
)

@Composable
fun DynamicRedBorder(): Color = NeonRed

// --- 4. HOME DASHBOARD SCREEN ---

@Composable
fun HomeDashboardScreen(
    viewModel: GymViewModel,
    onNavigate: (String) -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val todayMeals by viewModel.todayMeals.collectAsState()
    val todayWater by viewModel.todayWater.collectAsState()

    // Calculate aggregated calorie totals
    val consumedCalories = todayMeals.sumOf { it.calories }
    val baseTarget = 2500
    val progressPercent = consumedCalories.toFloat() / baseTarget.toFloat()

    val formattedSyncTime = userProfile?.lastSyncTime ?: "Just Now"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcoming Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "WELCOME BACK, ATHLETE",
                        fontSize = 11.sp,
                        color = ElectricBlue,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = userProfile?.name ?: "Siddharth Sharma",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.Green))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Server Sync: $formattedSyncTime",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Streak bubble click to trigger premium subscriptions comparison
                Box(
                    modifier = Modifier
                        .clickable { onNavigate("notifications") }
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(CarbonCard)
                        .border(1.dp, CarbonBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Notifications, null, tint = ElectricBlue, modifier = Modifier.size(22.dp))
                }
            }
        }

        // Streak & XP Fire Badge Box (Gamification)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassCard(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalFireDepartment, null, tint = NeonRed, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("STREAK", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Text("${userProfile?.streak ?: 5} DAYS", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                GlassCard(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("XP WALLET", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Text("${userProfile?.xp ?: 1250} XP", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Active Subscription Promo Banner
        item {
            val tier = userProfile?.subscriptionTier ?: "BASIC"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        1.dp,
                        if (tier != "BASIC") ElectricBlue else CarbonBorder,
                        RoundedCornerShape(16.dp)
                    )
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(CarbonCard, CardGradientStart)
                        )
                    )
                    .clickable { viewModel.upgradeModalActive.value = true }
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (tier != "BASIC") ElectricBlue else NeonRed)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    tier,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("MEMBERSHIP PASS", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (tier != "BASIC") "Your Futuristic Gym Access is Active" else "Unlock AI Coach & HD Routines Today!",
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Paid, 
                        contentDescription = null, 
                        tint = if (tier != "BASIC") ElectricBlue else TextSecondary,
                        modifier = Modifier
                            .size(34.dp)
                            .weight(0.3f)
                    )
                }
            }
        }

        // Indian Calorie Tracking Summary (MyFitnessPal alternative)
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("NUTRITION METRICS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricBlue, letterSpacing = 1.sp)
                    Text("GOAL: $baseTarget KCAL", fontSize = 11.sp, color = TextSecondary)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$consumedCalories kcal",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Total Intake Checked",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier.size(56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = progressPercent.coerceIn(0f, 1f),
                            color = ElectricBlue,
                            trackColor = CarbonBorder,
                            strokeWidth = 6.dp
                        )
                        Text(
                            "${(progressPercent * 100).toInt()}%",
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Indicator Bar
                LinearProgressIndicator(
                    progress = progressPercent.coerceIn(0f, 1f),
                    color = NeonRed,
                    trackColor = CarbonBorder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        }

        // Live Liquid Hydration reminders (Water tracker)
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("WATER LOG HYDRATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricBlue)
                        Text("${todayWater?.milliliters ?: 0} ml of 3000 ml", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.logWater(250) },
                            colors = ButtonDefaults.buttonColors(containerColor = CarbonBorder),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("+250ml", fontSize = 12.sp, color = ElectricBlue, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.logWater(500) },
                            colors = ButtonDefaults.buttonColors(containerColor = CarbonBorder),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("+500ml", fontSize = 12.sp, color = ElectricBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quick Navigation Launch Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("INTELLIGENT MATRIX", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onNavigate("ai_coach") },
                        colors = ButtonDefaults.buttonColors(containerColor = CarbonCard),
                        border = BorderStroke(1.dp, CarbonBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Icon(Icons.Default.Chat, null, tint = NeonRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CHAT FLASH", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onNavigate("workouts") },
                        colors = ButtonDefaults.buttonColors(containerColor = CarbonCard),
                        border = BorderStroke(1.dp, CarbonBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Icon(Icons.Default.FitnessCenter, null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("WORKOUTS", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // System Settings & Admin Gateways
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(onClick = { viewModel.upgradeModalActive.value = true }) {
                    Text("MEMBERSHIP CHOICES", fontSize = 11.sp, color = ElectricBlue, fontWeight = FontWeight.Bold)
                }

                val currentProfile by viewModel.userProfile.collectAsState()
                val isEmailCorrect = currentProfile?.email?.contains("rahul") == true
                val forceOpenAdmin = isEmailCorrect || currentProfile?.email?.contains("admin") == true
                
                TextButton(onClick = { 
                    viewModel.adminModeEnabled.value = true
                    onNavigate("admin")
                }) {
                    Text("ADMIN CONSOLE ${if (forceOpenAdmin) "🛡️" else "⚙️"}", fontSize = 11.sp, color = NeonRed, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Floating Plan Dialog Overlay
    val upgradeActive by viewModel.upgradeModalActive.collectAsState()
    if (upgradeActive) {
        MembershipPlanDialog(viewModel)
    }
}

// --- 5. WORKOUTS CATALOGUE & DYNAMIC GENERATOR ---

@Composable
fun WorkoutsScreen(viewModel: GymViewModel) {
    val workouts by viewModel.workouts.collectAsState()
    val activeSelection by viewModel.selectedWorkout.collectAsState()
    val isGenerating by viewModel.isGeneratingAi.collectAsState()

    var customMuscleText by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableStateOf("45") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper Title
        item {
            Column {
                Text(
                    text = "ATHLETIC CONDITIONING",
                    fontSize = 11.sp,
                    color = ElectricBlue,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "REPS CATALOGUE",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        // DYNAMIC AI WORKOUT GENERATION BOX (Unique customization)
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Android, null, tint = ElectricBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "FLASH AI INSTANT ROUTINE DESIGN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = customMuscleText,
                    onValueChange = { customMuscleText = it },
                    placeholder = { Text("E.g. Upper Chest Peak, Quads Shred") },
                    colors = pulseTextFieldColors(ElectricBlue),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("30", "45", "60").forEach { min ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedDuration == min) ElectricBlue else CarbonBorder)
                                    .clickable { selectedDuration = min }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "$min min",
                                    fontSize = 11.sp,
                                    color = if (selectedDuration == min) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = NeonRed)
                    } else {
                        GlowButton(
                            text = "Draft AI Plan",
                            onClick = {
                                if (customMuscleText.isNotEmpty()) {
                                    viewModel.requestAiCustomWorkoutPlan(selectedDuration, customMuscleText)
                                }
                            },
                            backgroundColor = ElectricBlue,
                            glowColor = ElectricBlueGlow
                        )
                    }
                }
            }
        }

        // Static or Saved Workouts Lists
        item {
            Text("AVAILABLE CONDITIONING TRACKS", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        }

        items(workouts) { workout ->
            val userProfile by viewModel.userProfile.collectAsState()
            val isLocked = workout.isPremium && userProfile?.subscriptionTier == "BASIC"

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CarbonCard)
                    .border(
                        1.dp,
                        if (activeSelection?.id == workout.id) ElectricBlue else CarbonBorder,
                        RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        if (!isLocked) {
                            viewModel.selectedWorkout.value = workout
                        } else {
                            viewModel.upgradeModalActive.value = true
                        }
                    }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (workout.category == "HIIT" || workout.category == "Strength") NeonRed else ElectricBlue)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(workout.category, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(workout.difficulty, fontSize = 11.sp, color = TextSecondary)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(workout.title, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Duration: ${workout.durationMinutes} min  |  Est. Burn: ${workout.caloriesBurned} cal", fontSize = 12.sp, color = TextSecondary)
                    }

                    if (isLocked) {
                        Icon(Icons.Default.Lock, null, tint = NeonRed, modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.Default.ChevronRight, null, tint = TextMuted)
                    }
                }
            }
        }
    }

    // Modal popup showing details of clicked workout
    if (activeSelection != null) {
        Dialog(onDismissRequest = { viewModel.selectedWorkout.value = null }) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                val plan = activeSelection!!
                Text(
                    text = plan.title.uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "${plan.category} • ${plan.durationMinutes} MINUTES • ${plan.caloriesBurned} CALORIES",
                    fontSize = 11.sp,
                    color = ElectricBlue,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("DETAILED PERFORMANCE REPS:", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))

                // Custom dummy exercise list layout deserializing simulation json
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val exerList = listOf(
                        "Heavy Dynamic Press Set",
                        "Decline Progressive Hypertrophy",
                        "Continuous Finishing Velocity rep"
                    )
                    exerList.forEach { exercise ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CarbonBorder)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(exercise, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("3 Sets x 10", color = ElectricBlue, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.selectedWorkout.value = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Text("CLOSE", color = TextSecondary)
                    }

                    GlowButton(
                        text = "Complete session (+150 XP)",
                        onClick = {
                            viewModel.completeWorkout(plan)
                            viewModel.selectedWorkout.value = null
                        },
                        backgroundColor = NeonRed
                    )
                }
            }
        }
    }
}

// --- 6. AI COACH CONVERSATIONS ---

@Composable
fun AiCoachScreen(viewModel: GymViewModel) {
    val chatHistory by viewModel.aiChatHistory.collectAsState()
    val isGenerating by viewModel.isGeneratingAi.collectAsState()
    val textInput by viewModel.currentChatInput.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Chat header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Android, null, tint = NeonRed, modifier = Modifier.size(34.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("FLASH COGNITIVE ENGINE", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.Green))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Model: gemini-3.5-flash online", fontSize = 11.sp, color = ElectricBlue)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chat logs bubble list inside Box Scroll
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
                .background(CarbonCard)
                .padding(12.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(chatHistory) { message ->
                    val isMe = message.second
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (isMe) 12.dp else 0.dp,
                                        bottomEnd = if (isMe) 0.dp else 12.dp
                                    )
                                )
                                .background(if (isMe) NeonRedGlow else CarbonBorder)
                                .padding(12.dp)
                        ) {
                            Text(
                                message.first,
                                color = if (isMe) Color.White else TextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
                
                if (isGenerating) {
                    item {
                        Row(horizontalArrangement = Arrangement.Start) {
                            Text("Flash is drafting elite computations...", color = ElectricBlue, fontSize = 12.sp, style = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Prompt templates triggers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "How to progressive overload chest bench?",
                "Give me 200g high-protein vegetarian macro plan.",
                "How does AI posture checking work?",
                "Provide injury safety advice for split deadlifts."
            ).forEach { promptSpec ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CarbonBorder)
                        .clickable { viewModel.askChatBot(promptSpec) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(promptSpec, fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Input row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { viewModel.currentChatInput.value = it },
                placeholder = { Text("Command Flash...") },
                colors = pulseTextFieldColors(NeonRed),
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = { viewModel.askChatBot(textInput) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(56.dp)
            ) {
                Icon(Icons.Default.Send, null, tint = Color.White)
            }
        }
    }
}

// --- 7. MEAL PLANNER METRICS (Indian Macros alternative to MyFitnessPal) ---

@Composable
fun MealPlannerScreen(viewModel: GymViewModel) {
    val todayMeals by viewModel.todayMeals.collectAsState()

    // Aggregate values
    val currentCals = todayMeals.sumOf { it.calories }
    val currentPro = todayMeals.sumOf { it.protein }
    val currentCarbs = todayMeals.sumOf { it.carbs }
    val currentFat = todayMeals.sumOf { it.fat }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("NUTRITIONAL FUELING CONTROL", fontSize = 11.sp, color = ElectricBlue, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text("INDIAN FOOD CHECKER", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }

        // Live Macro totals overview
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("DAILY MACRO PERCENT TARGET RATIO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricBlue)
                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MacroBarItem("PRO", currentPro, 180, NeonRed, modifier = Modifier.weight(1f))
                    MacroBarItem("CARBS", currentCarbs, 250, ElectricBlue, modifier = Modifier.weight(1f))
                    MacroBarItem("FAT", currentFat, 70, Color(0xFFFFD700), modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = CarbonBorder)
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Energy Captured:", fontSize = 12.sp, color = TextSecondary)
                    Text("$currentCals kcal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Add preloaded food items (Indian Catalog)
        item {
            Text("QUICK-LOG ELITE DIET SELECTION", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                viewModel.indianFoodsCatalog.forEach { food ->
                    Box(
                        modifier = Modifier
                            .width(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CarbonCard)
                            .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
                            .clickable { viewModel.logFood(food) }
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (food.category == "Veg") Color.Green else Color.Red)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(food.category.uppercase(), fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(food.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                            Text("${food.calories} cal | P: ${food.protein}g", fontSize = 11.sp, color = ElectricBlue)
                            Spacer(modifier = Modifier.height(6.dp))
                            TextButton(
                                onClick = { viewModel.logFood(food) },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("+ LOG ITEM", fontSize = 10.sp, color = NeonRed, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Today's logged items lists
        item {
            Text("TODAY'S CAPTURED MEALS", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        }

        if (todayMeals.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No food items logged today. Initiate your dynamic cut!", color = TextSecondary, fontSize = 13.sp)
                }
            }
        }

        items(todayMeals) { meal ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CarbonCard)
                    .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(meal.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${meal.calories} kcal  •  Protein ${meal.protein}g  •  Carbs ${meal.carbs}g", fontSize = 11.sp, color = TextSecondary)
                }

                IconButton(onClick = { viewModel.removeMeal(meal.id) }) {
                    Icon(Icons.Default.Delete, null, tint = TextMuted)
                }
            }
        }
    }
}

@Composable
fun MacroBarItem(label: String, current: Int, goal: Int, activeColor: Color, modifier: Modifier = Modifier) {
    val faction = current.toFloat() / goal.toFloat()
    Column(
        modifier = modifier.padding(horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(CarbonBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(faction.coerceIn(0f, 1f))
                    .background(activeColor)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text("$current / ${goal}g", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

// --- 8. PROGRESS ANALYTICS CANVAS GRAPHS ---

@Composable
fun ProgressAnalyticsScreen(viewModel: GymViewModel) {
    var selectedMetric by remember { mutableStateOf("Weight Progression") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                Text("PERFORMANCE INSIGHT ENGINE", fontSize = 11.sp, color = ElectricBlue, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text("ANALYTIC CHARTS", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }

        // Toggle Selection
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Weight Progression", "Calories Burnt").forEach { tab ->
                    val isSelected = selectedMetric == tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) NeonRed else CarbonCard)
                            .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedMetric = tab }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            tab,
                            fontSize = 12.sp,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // CUSTOM DRAWN ANALYTICS CANVAS (Tesla / SaaS visual standard)
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(selectedMetric.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricBlue, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (selectedMetric == "Weight Progression") "Steady muscular cut: -3.5kg lost since initiation." else "Average calorie burn is up by 12% through AI high-intensity conditioning plans.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Canvas line plot drawing
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(CarbonBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = Path()
                        val height = size.height
                        val width = size.width

                        // Draw simple coordinate bounds
                        val points = if (selectedMetric == "Weight Progression") {
                            // Weight reduction sequence
                            listOf(0.9f, 0.85f, 0.75f, 0.7f, 0.58f, 0.5f)
                        } else {
                            // Calorie peaks
                            listOf(0.3f, 0.55f, 0.45f, 0.8f, 0.72f, 0.95f)
                        }

                        val strokeColor = if (selectedMetric == "Weight Progression") ElectricBlue else NeonRed

                        points.forEachIndexed { index, propVal ->
                            val x = (width / (points.size - 1)) * index
                            val y = height - (height * propVal)
                            if (index == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                            drawCircle(color = strokeColor, radius = 4.dp.toPx(), center = Offset(x, y))
                        }

                        drawPath(path = path, color = strokeColor, style = Stroke(width = 3.dp.toPx()))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val labelSet = if (selectedMetric == "Weight Progression") {
                        listOf("Wk 1 (85)", "Wk 2", "Wk 3", "Wk 4", "Wk 5", "Now (78)")
                    } else {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                    }
                    labelSet.forEach { label ->
                        Text(label, fontSize = 10.sp, color = TextMuted)
                    }
                }
            }
        }

        // Live BMI interactive calculation
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("BMI SMART ANALYZER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Current Weight: 78.5 kg", fontSize = 13.sp, color = TextSecondary)
                        Text("Current Height: 178 cm", fontSize = 13.sp, color = TextSecondary)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonRedGlow)
                            .padding(8.dp)
                    ) {
                        Text("BMI: 24.8 (Normal)", fontSize = 12.sp, color = NeonRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- 9. PAID MEMBERSHIP DIALOG MODAL (Basic, Pro, Elite) ---

@Composable
fun MembershipPlanDialog(viewModel: GymViewModel) {
    val tierType by viewModel.selectedSubscriptionType.collectAsState()
    val referral by viewModel.referralCode.collectAsState()
    val coupon by viewModel.couponCode.collectAsState()
    val discount by viewModel.discountApplied.collectAsState()

    var showBillingPage by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { viewModel.upgradeModalActive.value = false }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, ElectricBlueGlow, RoundedCornerShape(20.dp))
                .background(CarbonDark, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            LazyColumn(
                modifier = Modifier.wrapContentHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElectricBlueGlow)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("18% GST INCLUDED", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = ElectricBlue)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("IRON PULSE MEMBERSHIPS", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Secure Indian Bank UPI Gateway", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                if (!showBillingPage) {
                    // Selection Tiers list
                    val plans = listOf(
                        Triple("BASIC", "₹99 / Mo", "Access generic tracks. Log water hydration."),
                        Triple("PRO", "₹299 / Mo", "Unlock Cyberpunk bodybuilding. Complete food macros database. Daily streaks tracker."),
                        Triple("ELITE", "₹999 / Mo", "Custom real-time Flash AI plan compiler. Full admin capabilities unlocked.")
                    )

                    items(plans) { planSpec ->
                        val isSel = tierType == planSpec.first
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) CarbonBorder else CarbonCard)
                                .border(1.dp, if (isSel) ElectricBlue else CarbonBorder, RoundedCornerShape(12.dp))
                                .clickable { viewModel.selectedSubscriptionType.value = planSpec.first }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(planSpec.first, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isSel) ElectricBlue else Color.White)
                                Text(planSpec.third, fontSize = 11.sp, color = TextSecondary)
                            }
                            Text(planSpec.second, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isSel) ElectricBlue else Color.White)
                        }
                    }

                    // Coupon fields
                    item {
                        Column {
                            Text("PROMO COUPON CODE (Try: pulse50)", fontSize = 10.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = coupon,
                                    onValueChange = { viewModel.applyPromoCoupon(it) },
                                    placeholder = { Text("Enter Code") },
                                    colors = pulseTextFieldColors(NeonRed),
                                    modifier = Modifier.weight(1.5f)
                                )

                                Button(
                                    onClick = { viewModel.applyPromoCoupon(coupon) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CarbonCard),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("APPLY", fontSize = 11.sp, color = ElectricBlue)
                                }
                            }
                            if (discount > 0) {
                                Text("Success: -₹${discount.toInt()} INR coupon discount applied!", color = Color.Green, fontSize = 11.sp)
                            }
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.upgradeModalActive.value = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("CANCEL", color = TextMuted)
                            }

                            GlowButton(
                                text = "CHECKOUT UPI",
                                onClick = { showBillingPage = true },
                                backgroundColor = ElectricBlue,
                                glowColor = ElectricBlueGlow,
                                modifier = Modifier.weight(1.5f)
                            )
                        }
                    }

                } else {
                    // --- BILLING PAGE & PDF INVOICE SIMULATION ---
                    item {
                        Column {
                            Text("GATEWAY PAYMENT VERIFICATION", fontSize = 12.sp, color = ElectricBlue, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(10.dp))

                            // UPI / Indian Channels Choice mock logo
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                listOf("UPI Pin", "Paytm QR", "Razorpay Secure").forEach { term ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(CarbonBorder)
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(term, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Pricing math
                            val planCost = if (tierType == "PRO") 299f else if (tierType == "ELITE") 999f else 99f
                            val discountedCost = maxOf(0f, planCost - discount)
                            val gstCalc = discountedCost * 0.18f
                            val totalInd = discountedCost + gstCalc

                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Plan Charge:", color = TextSecondary, fontSize = 12.sp)
                                    Text("₹$planCost", color = Color.White, fontSize = 12.sp)
                                }
                                if (discount > 0) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Coupon: ", color = Color.Green, fontSize = 12.sp)
                                        Text("-₹$discount", color = Color.Green, fontSize = 12.sp)
                                    }
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("GST Taxes (18%):", color = TextSecondary, fontSize = 12.sp)
                                    Text("₹${String.format("%.2f", gstCalc)}", color = Color.White, fontSize = 12.sp)
                                }
                                Divider(color = CarbonBorder, modifier = Modifier.padding(vertical = 6.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Grand Total INR:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("₹${String.format("%.2f", totalInd)}", color = ElectricBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { showBillingPage = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("REVERT", color = TextSecondary)
                                }

                                GlowButton(
                                    text = "SECURE PAY NOW",
                                    onClick = {
                                        viewModel.processSubCheckout()
                                    },
                                    backgroundColor = NeonRed,
                                    modifier = Modifier.weight(1.5f),
                                    testTag = "pay_now_submit_btn"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Invoice Success Animated Card Details
@Composable
fun PaymentSuccessOverlay(viewModel: GymViewModel) {
    val invoice by viewModel.generatedInvoiceText.collectAsState()

    Dialog(onDismissRequest = { viewModel.paymentSuccessAnimationTriggered.value = false }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color.Green, RoundedCornerShape(16.dp))
                .background(CarbonDark, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.Green,
                    modifier = Modifier.size(68.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("REVOLUTIONARY TIER SECURED!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("GST Tax invoice generated successfully", fontSize = 11.sp, color = TextSecondary)

                Spacer(modifier = Modifier.height(16.dp))

                // Invoice scroll
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(CarbonCard, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = invoice ?: "Invoice compiling error...",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color.Green,
                        lineHeight = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                GlowButton(
                    text = "Unlock elite status",
                    onClick = {
                        viewModel.confirmUpgradeUnlock()
                    },
                    backgroundColor = Color.Green,
                    glowColor = Color.Green.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "confirm_membership_upgrade_btn"
                )
            }
        }
    }
}

// --- 10. SOCIAL COMMUNITY FEED SCREEN WITH BEFORE/AFTER INTERACTIVE SLIDER ---

@Composable
fun CommunityFeedScreen(viewModel: GymViewModel) {
    val feedPosts by viewModel.communityPosts.collectAsState()
    val captionText by viewModel.customPostCaption.collectAsState()
    val isTransformSelected by viewModel.isTransformationPost.collectAsState()

    val beforeWeight by viewModel.beforeWeightTxt.collectAsState()
    val afterWeight by viewModel.afterWeightTxt.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("MEMBER RESULTS SHARING MODULE", fontSize = 11.sp, color = ElectricBlue, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text("COMMUNITY ARENA", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }

        // CREATE SOCIAL POST PANEL
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("PUBLISH YOUR DENSE PROGRESS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = captionText,
                    onValueChange = { viewModel.customPostCaption.value = it },
                    placeholder = { Text("What did you overload peak today athlete? E.g. bench 100kg...") },
                    colors = pulseTextFieldColors(NeonRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("post_caption_field")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Toggle Transformation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isTransformSelected,
                            onCheckedChange = { viewModel.isTransformationPost.value = it },
                            colors = CheckboxDefaults.colors(checkedColor = ElectricBlue)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Include Weight Transformation", color = TextSecondary, fontSize = 12.sp)
                    }

                    if (isTransformSelected) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = beforeWeight,
                                onValueChange = { viewModel.beforeWeightTxt.value = it },
                                placeholder = { Text("Before") },
                                colors = pulseTextFieldColors(ElectricBlue),
                                modifier = Modifier.width(68.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp)
                            )

                            OutlinedTextField(
                                value = afterWeight,
                                onValueChange = { viewModel.afterWeightTxt.value = it },
                                placeholder = { Text("After") },
                                colors = pulseTextFieldColors(ElectricBlue),
                                modifier = Modifier.width(68.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                GlowButton(
                    text = "POST ARENA BROADCAST (+50 XP)",
                    onClick = { if (captionText.isNotEmpty()) viewModel.publishCommunityPost() },
                    backgroundColor = NeonRed,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "post_publish_button"
                )
            }
        }

        // Community items list
        items(feedPosts) { post ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CarbonBorder),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = post.username.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(post.username, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(post.userTitle, fontSize = 10.sp, color = ElectricBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(post.timeAgo, fontSize = 10.sp, color = TextMuted)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = post.caption,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    lineHeight = 19.sp
                )

                // Swap weight comparison panel (if active transformation)
                if (post.hasTransformImage) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp))
                            .background(CarbonBorder.copy(alpha = 0.3f))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("BEFORE PHASE", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Text(post.beforeImage, fontSize = 14.sp, color = NeonRed, fontWeight = FontWeight.ExtraBold)
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
                                .background(CarbonBorder)
                        )
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("AFTER PHASE", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Text(post.afterImage, fontSize = 14.sp, color = ElectricBlue, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Divider(color = CarbonBorder)

                Spacer(modifier = Modifier.height(8.dp))

                // Likes Comments trigger row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { viewModel.heartPost(post) }
                        ) {
                            Icon(
                                imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = if (post.isLiked) NeonRed else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${post.likesCount} LOVES", fontSize = 12.sp, color = TextSecondary)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Comment, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${post.commentsCount} FEEDBACKS", fontSize = 12.sp, color = TextSecondary)
                        }
                    }

                    Icon(Icons.Default.Share, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// --- 11. GAMIFIED LEADERBOARDS ROW TIERING ---

@Composable
fun LeaderboardsScreen(viewModel: GymViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("PULSE HIGHEST RANK ACCOMPLISHMENTS", fontSize = 11.sp, color = ElectricBlue, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text("THE SOCIAL CLIMB LEADERBOARD", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }

        // Leaderboard user tiers overview
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(34.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("YOUR CONSOLE RANK POSITIONS", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("You scored +1250 XP total. Climb above Aryan for Pro status badges!", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }
        }

        items(viewModel.leaderboardsUsers) { entry ->
            val isMe = entry.name.contains("You")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isMe) CarbonBorder else CarbonCard)
                    .border(
                        1.dp,
                        if (isMe) NeonRed else CarbonBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isMe) NeonRed else CarbonBorder),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (viewModel.leaderboardsUsers.indexOf(entry) + 1).toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(entry.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isMe) NeonRed else Color.White)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ElectricBlue.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(entry.tier, fontSize = 8.sp, color = ElectricBlue, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }

                Text("${entry.xp} XP", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// --- 12. PROFILE, SYSTEM SETTINGS AND QR CODE ENTRY PASS SCREEN ---

@Composable
fun ProfileAndSettingsScreen(
    viewModel: GymViewModel,
    onNavigate: (String) -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    var isVerifiedChecks by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("ATHLETIC BIOMETRICS ENGINE", fontSize = 11.sp, color = ElectricBlue, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text("PROFILE & SYNC SETTINGS", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }

        // Profile metadata
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(CarbonBorder),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userProfile?.name?.take(1)?.uppercase() ?: "S",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonRed
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(userProfile?.name ?: "Siddharth Sharma", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(userProfile?.email ?: "siddharth@ironpulse.fit", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ElectricBlue.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "MEMBERSHIP: ${userProfile?.subscriptionTier ?: "BASIC"}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ElectricBlue
                            )
                        }
                    }
                }
            }
        }

        // QR CODE GYM CHECK-IN PASS CARD (Futuristic Microinteraction)
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("PULSE QR CONTACTLESS PASS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricBlue, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Scan this secure dynamic gate token at physical Iron Pulse gym locks.", fontSize = 11.sp, color = TextSecondary, textAlign = TextAlign.Center)

                    Spacer(modifier = Modifier.height(16.dp))

                    // QR simulation vector drawing
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Draw simulated QR matrix paths
                            val step = size.width / 5f
                            for (i in 0..4) {
                                for (j in 0..4) {
                                    if ((i + j) % 2 == 0) {
                                        drawRect(
                                            color = Color.Black,
                                            topLeft = Offset(i * step + 2, j * step + 2),
                                            size = androidx.compose.ui.geometry.Size(step - 4, step - 4)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Expires in 45s  •  SECURE ENCRYPTED JWT", fontSize = 11.sp, color = TextMuted)
                }
            }
        }

        // settings toggle
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("SECURE DEVICE COMPOSITION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Active Biometric Recognition", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Request touch authentication on login bypass", fontSize = 11.sp, color = TextSecondary)
                    }
                    Switch(
                        checked = isVerifiedChecks,
                        onCheckedChange = { isVerifiedChecks = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonRed, checkedTrackColor = NeonRedGlow)
                    )
                }
            }
        }

        // Signout
        item {
            Button(
                onClick = { viewModel.logout() },
                colors = ButtonDefaults.buttonColors(containerColor = CarbonBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signout_button")
            ) {
                Text("DEACTIVATE ACCOUNT PASS (LOGOUT)", color = NeonRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

// --- 13. SECURE PUSH NOTIFICATIONS LOG ---

@Composable
fun NotificationsLogScreen(viewModel: GymViewModel) {
    val logs by viewModel.notifications.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column {
            Text("SECURE PUSH ALERTS HISTORIES", fontSize = 11.sp, color = ElectricBlue, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text("NOTIFICATIONS LOG", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(18.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (logs.isEmpty()) {
                item {
                    Text("No push notification events detected inside the log stream.", color = TextMuted, fontSize = 13.sp)
                }
            }

            items(logs) { alert ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (alert.isRead) CarbonCard else CarbonBorder)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (alert.category == "SECURITY") NeonRed.copy(alpha = 0.2f) else ElectricBlue.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(alert.category, fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(alert.timeString, fontSize = 10.sp, color = TextMuted)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(alert.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(alert.message, fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}

// --- 14. ADMIN PANEL SaaS SYSTEM DASHBOARD OVERRIDES ---

@Composable
fun AdminDashboardScreen(viewModel: GymViewModel) {
    val totalUsers by viewModel.totalUsersSimulated.collectAsState()
    val totalRev by viewModel.totalRevenueSimulated.collectAsState()
    val aiTokens by viewModel.totalAiTokensUsed.collectAsState()
    val broadcastText by viewModel.adminBroadcastMsg.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                Text("SECURE SaaS ADMINISTRATIVE CONTROL", fontSize = 11.sp, color = ElectricBlue, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text("ADMIN PANEL", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }

        // Live stats cards GRID representation
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Text("TOTAL REGISTERED USERS", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Text("$totalUsers Simulated", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Text("TOTAL SaaS REVENUE", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Text("₹${totalRev} INR", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = ElectricBlue)
                    }
                }

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("GEMINI LLM API USAGE COMPUTE", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Text("$aiTokens Model Input tokens", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NeonRed)
                }
            }
        }

        // Push Broadcaster
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("BROADCAST GLOBAL PUSH NOTIFICATION ALERT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = broadcastText,
                    onValueChange = { viewModel.adminBroadcastMsg.value = it },
                    placeholder = { Text("E.g. Attention Athletes! Iron Pulse VIP gym lock system restarts in 10m...") },
                    colors = pulseTextFieldColors(NeonRed),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                GlowButton(
                    text = "Broadcast alert channel",
                    onClick = { viewModel.broadcastAdminAnnouncement() },
                    backgroundColor = NeonRed,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Destructive Reset Buttons (Moderation overrides)
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("DESTRUCTIVE CLEAN ROOM REVIEWS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonRed)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Clean historical tracking logs, revert membership, or restore initial preset template catalogs.", fontSize = 11.sp, color = TextSecondary)

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { viewModel.forceResetDatabase() },
                    colors = ButtonDefaults.buttonColors(containerColor = CarbonBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("FORCE RE-CALIBRATE PRESETS & PURGE LOGS", color = NeonRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

// --- CORE BOTTOM NAVIGATION PILL BAR ---

@Composable
fun IronPulseBottomBar(
    currentScreen: String,
    onSelectScreen: (String) -> Unit
) {
    Surface(
        color = CarbonCard,
        border = BorderStroke(1.dp, CarbonBorder),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding() // Edge-to-Edge compliance!
                    .padding(vertical = 10.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    icon = Icons.Default.Home,
                    label = "Aura",
                    isSelected = currentScreen == "home",
                    onClick = { onSelectScreen("home") }
                )
                BottomNavItem(
                    icon = Icons.Default.FitnessCenter,
                    label = "Log",
                    isSelected = currentScreen == "workouts",
                    onClick = { onSelectScreen("workouts") }
                )
                BottomNavItem(
                    icon = Icons.Default.Restaurant,
                    label = "Meals",
                    isSelected = currentScreen == "meal_planner",
                    onClick = { onSelectScreen("meal_planner") }
                )
                BottomNavItem(
                    icon = Icons.Default.Forum,
                    label = "Arena",
                    isSelected = currentScreen == "social",
                    onClick = { onSelectScreen("social") }
                )
                BottomNavItem(
                    icon = Icons.Default.Leaderboard,
                    label = "Ranks",
                    isSelected = currentScreen == "leaderboard",
                    onClick = { onSelectScreen("leaderboard") }
                )
                BottomNavItem(
                    icon = Icons.Default.Person,
                    label = "Gate",
                    isSelected = currentScreen == "profile",
                    onClick = { onSelectScreen("profile") }
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) NeonRed else TextMuted,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else TextMuted,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
