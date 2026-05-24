package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.IronPulseApp
import com.example.api.GeminiClient
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class GymViewModel : ViewModel() {

    private val repository = IronPulseApp.instance.repository

    // --- Core State Flows from Room ---
    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val workouts: StateFlow<List<WorkoutTemplate>> = repository.workouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workoutLogs: StateFlow<List<WorkoutLog>> = repository.workoutLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val communityPosts: StateFlow<List<CommunityPost>> = repository.communityPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<GymNotification>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- UI/UX Temporary App States ---

    // Auth screen states
    val currentAuthEmail = MutableStateFlow("")
    val currentAuthPassword = MutableStateFlow("")
    val currentAuthName = MutableStateFlow("")
    val currentAuthPhone = MutableStateFlow("")
    val otpSentCode = MutableStateFlow("")
    val otpInputCode = MutableStateFlow("")
    val isOtpScreenActive = MutableStateFlow(false)
    val biometricEnabled = MutableStateFlow(true)

    // Selection
    val selectedWorkout = MutableStateFlow<WorkoutTemplate?>(null)

    // Calorie Tracking States (Today-based)
    private val _todayDateString = MutableStateFlow("")
    val todayMeals: StateFlow<List<MealLog>> = _todayDateString
        .flatMapLatest { date -> repository.getMealsByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayWater: StateFlow<WaterLog?> = _todayDateString
        .flatMapLatest { date -> repository.getWaterByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Chatbot States
    val aiChatHistory = MutableStateFlow<List<Pair<String, Boolean>>>(
        listOf(
            "Welcome elite! I am Flash, your futuristic AI Coach. Ask me to draft a custom routine, posture correction concepts, or Indian high-protein macros." to false
        )
    )
    val isGeneratingAi = MutableStateFlow(false)
    val currentChatInput = MutableStateFlow("")

    // Indian Foods Database (Simulated Catalog)
    val indianFoodsCatalog = listOf(
        FoodItem("Paneer Bhurji", "Veg", 280, 18, 10, 14),
        FoodItem("Chicken Tikka", "Non-Veg", 310, 32, 4, 8),
        FoodItem("Masala Oats & Eggs", "Non-Veg", 340, 20, 25, 12),
        FoodItem("Dal Makhani & Roti", "Veg", 450, 14, 60, 10),
        FoodItem("Whey Isolate shake", "Veg", 130, 25, 2, 1),
        FoodItem("Idli Sambar (3 pcs)", "Veg", 240, 6, 45, 2),
        FoodItem("Moong Dal Chilla", "Veg", 180, 12, 28, 2),
        FoodItem("Almond & Chia Bowl", "Veg", 220, 8, 15, 12)
    )

    // Paid Membership Upgrades System
    val upgradeModalActive = MutableStateFlow(false)
    val selectedSubscriptionType = MutableStateFlow("PRO") // PRO, ELITE
    val couponCode = MutableStateFlow("")
    val referralCode = MutableStateFlow("")
    val discountApplied = MutableStateFlow(0f) // Amount INR subtracted
    val activeGstPercent = 18f
    val generatedInvoiceText = MutableStateFlow<String?>(null)
    val paymentSuccessAnimationTriggered = MutableStateFlow(false)

    // Community Create Field
    val customPostCaption = MutableStateFlow("")
    val beforeWeightTxt = MutableStateFlow("85 kg")
    val afterWeightTxt = MutableStateFlow("76 kg")
    val isTransformationPost = MutableStateFlow(false)

    // Social Gamification Trophies / Leaderboards
    val leaderboardsUsers = listOf(
        LeaderboardEntry("Vikram Malhotra", 4520, "Elite"),
        LeaderboardEntry("Aryan Sen", 3280, "Elite"),
        LeaderboardEntry("Siddharth Sharma (You)", 1250, "Basic"),
        LeaderboardEntry("Rohan Bhatia", 1120, "Pro"),
        LeaderboardEntry("Ankita Paul", 980, "Basic")
    )

    // Admin Dashboard values overriding
    val adminModeEnabled = MutableStateFlow(false)
    val adminBroadcastMsg = MutableStateFlow("")
    val totalUsersSimulated = MutableStateFlow(1240)
    val totalRevenueSimulated = MutableStateFlow(342900) // ₹
    val totalAiTokensUsed = MutableStateFlow(42100)

    init {
        // Hydrate today's tracking date string
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        _todayDateString.value = dateFormat.format(Date())

        // Periodically refresh tracking flows
        viewModelScope.launch {
            repository.workouts.collect { list ->
                // Ensure list items can update safely
            }
        }
    }

    // --- Action Methods ---

    fun login(email: String, pwh: String) {
        viewModelScope.launch {
            val user = userProfile.value ?: UserProfile()
            repository.saveProfile(user.copy(email = email, name = email.substringBefore("@"), isLoggedIn = true))
            repository.addNotification("Sign-in Security Event", "Welcome back! Biometric lock session has been registered from your phone.", "SECURITY")
        }
    }

    fun signup(name: String, email: String, pwh: String) {
        viewModelScope.launch {
            repository.saveProfile(UserProfile(name = name, email = email, isLoggedIn = true, streak = 1, xp = 100))
            repository.addNotification("Account Created", "Welcome to Iron Pulse: Premium ecosystem starts now.", "SECURITY")
        }
    }

    fun enterGuestMode() {
        viewModelScope.launch {
            repository.saveProfile(UserProfile(name = "Guest Renegade", email = "guest@ironpulse.fit", isLoggedIn = true))
        }
    }

    fun logout() {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            repository.saveProfile(current.copy(isLoggedIn = false))
        }
    }

    fun sendOtp(phone: String) {
        currentAuthPhone.value = phone
        val randomOtp = (1000..9999).random().toString()
        otpSentCode.value = randomOtp
        isOtpScreenActive.value = true
        viewModelScope.launch {
            repository.addNotification("OTP Verification Code", "Verification code for Iron Pulse is: $randomOtp.", "SECURITY")
        }
    }

    fun verifyOtp(code: String) {
        if (code == otpSentCode.value) {
            viewModelScope.launch {
                val user = userProfile.value ?: UserProfile()
                repository.saveProfile(user.copy(name = "User ${currentAuthPhone.value.takeLast(4)}", isLoggedIn = true))
                isOtpScreenActive.value = false
            }
        }
    }

    fun logWater(ml: Int) {
        viewModelScope.launch {
            repository.logWater(ml)
        }
    }

    fun logFood(food: FoodItem) {
        viewModelScope.launch {
            repository.logMeal(food.name, food.category, "Inter-day", food.calories, food.protein, food.carbs, food.fat)
        }
    }

    fun removeMeal(id: Int) {
        viewModelScope.launch {
            repository.deleteMealLog(id)
        }
    }

    fun completeWorkout(workout: WorkoutTemplate) {
        viewModelScope.launch {
            repository.logCompletedWorkout(
                WorkoutLog(
                    workoutId = workout.id,
                    title = workout.title,
                    dateString = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date()),
                    durationMinutes = workout.durationMinutes,
                    caloriesBurned = workout.caloriesBurned
                )
            )
            repository.addNotification(
                "Workout Logged: ${workout.title}",
                "You scored +150 XP. Your weekly streak is now at ${(userProfile.value?.streak ?: 0) + 1} days!",
                "COACH"
            )
        }
    }

    // --- AI GENERATION ---

    fun askChatBot(prompt: String) {
        val messageText = prompt.trim()
        if (messageText.isEmpty()) return

        aiChatHistory.value = aiChatHistory.value + (messageText to true)
        currentChatInput.value = ""
        isGeneratingAi.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val systemInstruction = """
                You are Flash, the futuristic premium AI Head Coach at Iron Pulse, a luxury fitness startup.
                Your tone is bold, elite, futuristic, brief, highly motivational and extremely precise.
                You are talking to Siddharth Sharma who wants to build 'Lean Muscle Hypertrophy'.
                Keep answers compact (max 3-4 bullet points) to fit inside a mobile screen beautifully.
                If they ask for a workout plan or Indian macro counts, provide highly tailored Indian details: paneer, chicken, idli, dal, etc. 
                If they mention posture, explain standard form pointers visually.
            """.trimIndent()

            val aiReply = GeminiClient.getCoachResponse(systemInstruction, messageText)
            aiChatHistory.value = aiChatHistory.value + (aiReply to false)
            isGeneratingAi.value = false
        }
    }

    fun requestAiCustomWorkoutPlan(durationStr: String, targetMuscle: String) {
        isGeneratingAi.value = true
        aiChatHistory.value = aiChatHistory.value + ("Draft an AI Workout Plan for $targetMuscle ($durationStr min)" to true)
        
        viewModelScope.launch(Dispatchers.IO) {
            val systemInstruction = "You are Flash, the Iron Pulse Head Coach. Draft a premium strength training workout template. Response must be brief and structured as JSON-like key specs for 3 exercises."
            val apiPrompt = "Please generate an elite custom strength routine for target muscle: '$targetMuscle' with total duration: $durationStr minutes. Formulate incline progressions, progressive overload structures, and precise rest counts."
            val replyText = GeminiClient.getCoachResponse(systemInstruction, apiPrompt)

            // Save this generated workout in our local database for immediate loading!
            val id = "ai_custom_" + UUID.randomUUID().toString().take(6)
            val template = WorkoutTemplate(
                id = id,
                title = "AI Coach Routine: $targetMuscle",
                category = "Strength",
                difficulty = "Intermediate",
                durationMinutes = durationStr.toIntOrNull() ?: 45,
                caloriesBurned = 350,
                exercisesJson = """[
                    {"name": "$targetMuscle Progression Alpha", "setsReps": "3 Sets x 8-10 reps", "rest": "90s Rest"},
                    {"name": "$targetMuscle Hypertrophy Beta", "setsReps": "3 Sets x 12 reps", "rest": "60s Rest"},
                    {"name": "Dynamic Conditioning finisher", "setsReps": "3 Sets x Failure", "rest": "45s Rest"}
                ]""",
                isPremium = false,
                coverUrl = "ai_generation"
            )
            repository.insertWorkouts(listOf(template))
            
            aiChatHistory.value = aiChatHistory.value + ("I've processed your custom routine and integrated it directly into your Workout Screen! Look out for 'AI Coach Routine: $targetMuscle'. Let's conquer it! 🦾" to false)
            isGeneratingAi.value = false
            repository.addNotification("Custom AI Workout Ready", "Flash has drafted a tailored hyper-focused progressive routine for $targetMuscle.", "COACH")
        }
    }

    // --- PURCHASE GATEWAY SIMULATION ---

    fun applyPromoCoupon(code: String) {
        couponCode.value = code
        if (code.lowercase() == "pulse50" || code.lowercase() == "iron50") {
            discountApplied.value = 150f
        } else {
            discountApplied.value = 0f
        }
    }

    fun processSubCheckout() {
        // Razorpay / Stripe Integration Simulation
        val costBeforeDiscount = if (selectedSubscriptionType.value == "PRO") 299f else 999f
        val netBase = maxOf(0f, costBeforeDiscount - discountApplied.value)
        val gstAmount = netBase * (activeGstPercent / 100f)
        val finalAmountINR = netBase + gstAmount

        generatedInvoiceText.value = """
            ===========================================
                      IRON PULSE LUXURY GYM LTD.
                     INVOICE / TAX RECEIPT (GST)
            ===========================================
            Cust Email: ${userProfile.value?.email ?: "siddharth@ironpulse.fit"}
            Plan Tier: ${selectedSubscriptionType.value} (Recurring Membership)
            Base Cost: ₹${costBeforeDiscount}
            Discount Applied: -₹${discountApplied.value}
            Net Taxable: ₹${netBase}
            GST Charged (18%): ₹${String.format("%.2f", gstAmount)}
            -------------------------------------------
            TOTAL IND-RUPEES: ₹${String.format("%.2f", finalAmountINR)}
            -------------------------------------------
            Status: TRANSACTION SUCCESSFUL (RAZORPAY SECURE)
            Merchant ID: TXNPROD_RAW_82A15
            Reference No: IP-${(100000..999999).random()}
            ===========================================
            Thank you for climbing the Iron Pulse leaderboard!
        """.trimIndent()

        paymentSuccessAnimationTriggered.value = true
    }

    fun confirmUpgradeUnlock() {
        val tier = selectedSubscriptionType.value
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            repository.saveProfile(current.copy(subscriptionTier = tier, xp = current.xp + 500))
            
            // Increment simulated SaaS stats!
            totalRevenueSimulated.value = totalRevenueSimulated.value + (if (tier == "PRO") 299 else 999)
            
            repository.addNotification("Subscription Upgraded", "Welcome to $tier membership status! All premium content and HD routines are unlocked.", "PAYMENT")
            paymentSuccessAnimationTriggered.value = false
            upgradeModalActive.value = false
        }
    }

    // --- SOCIAL / COMMUNITY CREATION ---

    fun publishCommunityPost() {
        val cpText = customPostCaption.value.trim()
        if (cpText.isEmpty()) return

        viewModelScope.launch {
            repository.insertPost(
                CommunityPost(
                    username = userProfile.value?.name ?: "Siddharth",
                    userTitle = "Iron Athlete",
                    caption = cpText,
                    likesCount = 0,
                    commentsCount = 0,
                    isLiked = false,
                    timeAgo = "Just Now",
                    hasTransformImage = isTransformationPost.value,
                    beforeImage = if (isTransformationPost.value) beforeWeightTxt.value else "",
                    afterImage = if (isTransformationPost.value) afterWeightTxt.value else ""
                )
            )
            repository.addNotification("Social Feed Updated", "You published a transformation story inside the gym community feed. Keep motivating!", "SOCIAL")
            
            // Add gamification score for sharing!
            val profile = userProfile.value ?: UserProfile()
            repository.saveProfile(profile.copy(xp = profile.xp + 50))

            // Clear inputs
            customPostCaption.value = ""
            isTransformationPost.value = false
        }
    }

    fun heartPost(post: CommunityPost) {
        viewModelScope.launch {
            repository.togglePostLike(post.id)
        }
    }

    // --- ADMIN DASHBOARD SYSTEM OVERRIDES ---

    fun broadcastAdminAnnouncement() {
        val text = adminBroadcastMsg.value.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            repository.addNotification("[GLOBAL BROADCAST]", text, "COACH")
            adminBroadcastMsg.value = ""
            totalAiTokensUsed.value = totalAiTokensUsed.value + 1200
        }
    }

    fun forceResetDatabase() {
        viewModelScope.launch {
            repository.resetMockTracks()
            repository.addNotification("Database Reset Successfully", "Admin cleared tracking logs and re-calibrated local presets.", "SECURITY")
        }
    }
}

// --- Dynamic Helper Data Classes ---

data class FoodItem(
    val name: String,
    val category: String, // Veg, Non-Veg
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int
)

data class LeaderboardEntry(
    val name: String,
    val xp: Int,
    val tier: String
)
