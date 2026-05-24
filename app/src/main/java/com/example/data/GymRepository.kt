package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

class GymRepository(private val dao: GymDao) {

    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    val workouts: Flow<List<WorkoutTemplate>> = dao.getAllWorkouts()
    val workoutLogs: Flow<List<WorkoutLog>> = dao.getWorkoutLogs()
    val communityPosts: Flow<List<CommunityPost>> = dao.getCommunityPosts()
    val notifications: Flow<List<GymNotification>> = dao.getNotifications()

    fun getMealsByDate(date: String): Flow<List<MealLog>> = dao.getMealLogsByDate(date)
    fun getWaterByDate(date: String): Flow<WaterLog?> = dao.getWaterLogByDate(date)

    // --- Write Actions ---

    suspend fun saveProfile(profile: UserProfile) {
        dao.saveUserProfile(profile)
    }

    suspend fun logCompletedWorkout(log: WorkoutLog) {
        dao.logCompletedWorkout(log)
        // Add points and update user profile stats on completion!
        val currentProfile = userProfile.firstOrNull() ?: UserProfile()
        val updatedProfile = currentProfile.copy(
            xp = currentProfile.xp + log.pointsScored,
            streak = currentProfile.streak + 1,
            lastSyncTime = "Last updated on completion of ${log.title}"
        )
        dao.saveUserProfile(updatedProfile)
    }

    suspend fun logMeal(name: String, category: String, mealType: String, calories: Int, protein: Int, carbs: Int, fat: Int) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = dateFormat.format(Date())
        dao.logMeal(MealLog(
            name = name,
            category = category,
            mealType = mealType,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat,
            dateString = dateString
        ))
    }

    suspend fun logWater(milliliters: Int) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = dateFormat.format(Date())
        val current = getWaterByDate(dateString).firstOrNull() ?: WaterLog(dateString, 0)
        dao.saveWaterLog(WaterLog(dateString, current.milliliters + milliliters))
    }

    suspend fun togglePostLike(postId: Int) {
        dao.togglePostLike(postId)
    }

    suspend fun deleteMealLog(id: Int) {
        dao.deleteMealLog(id)
    }

    suspend fun insertWorkouts(workouts: List<WorkoutTemplate>) {
        dao.insertWorkouts(workouts)
    }

    suspend fun insertPost(post: CommunityPost) {
        dao.insertPost(post)
    }

    suspend fun addNotification(title: String, message: String, category: String) {
        val timeFormat = SimpleDateFormat("HH:mm a", Locale.getDefault())
        dao.insertNotification(GymNotification(
            title = title,
            message = message,
            category = category,
            timeString = timeFormat.format(Date())
        ))
    }

    suspend fun markNotificationsRead() {
        dao.markAllNotificationsAsRead()
    }

    suspend fun resetMockTracks() {
        dao.clearMealLogs()
        dao.clearWorkoutLogs()
        val profile = userProfile.firstOrNull() ?: UserProfile()
        dao.saveUserProfile(profile.copy(xp = 1250, streak = 5))
    }

    // --- Populate Sample Database Presets ---
    suspend fun loadInitialPresets() {
        // 1. Initial Profile
        val existingProfile = userProfile.firstOrNull()
        if (existingProfile == null) {
            dao.saveUserProfile(UserProfile()) // Default User
        }

        // 2. Initial Workouts Catalog
        val existingWorkouts = workouts.firstOrNull()
        if (existingWorkouts.isNullOrEmpty()) {
            val templates = listOf(
                WorkoutTemplate(
                    id = "titan_chest",
                    title = "Titan Chest Progressive Overload",
                    category = "Strength",
                    difficulty = "Intermediate",
                    durationMinutes = 45,
                    caloriesBurned = 480,
                    isPremium = false,
                    coverUrl = "titan_chest",
                    exercisesJson = """[
                        {"name": "Incline Dumbbell Press", "setsReps": "4 Sets x 8-10 Reps", "rest": "90s Rest"},
                        {"name": "Flat Barbell Bench Press", "setsReps": "4 Sets x 6-8 Reps", "rest": "120s Rest"},
                        {"name": "Weighted Chest Dips", "setsReps": "3 Sets x 10 Reps", "rest": "60s Rest"},
                        {"name": "High-to-Low Cable Flyes", "setsReps": "3 Sets x 12-15 Reps", "rest": "45s Rest"}
                    ]"""
                ),
                WorkoutTemplate(
                    id = "vaporwave_hiit",
                    title = "Vaporwave HIIT Shred",
                    category = "HIIT",
                    difficulty = "Elite",
                    durationMinutes = 30,
                    caloriesBurned = 550,
                    isPremium = false,
                    coverUrl = "vapor_hiit",
                    exercisesJson = """[
                        {"name": "Kettlebell Swings", "setsReps": "4 Sets x 45s Active", "rest": "15s Rest"},
                        {"name": "Dumbbell Thrusters", "setsReps": "4 Sets x 40s Active", "rest": "20s Rest"},
                        {"name": "Plyometric Box Jumps", "setsReps": "4 Sets x 30s Active", "rest": "30s Rest"},
                        {"name": "Burpees-to-Tuck Jumps", "setsReps": "4 Sets x 45s Active", "rest": "15s Rest"}
                    ]"""
                ),
                WorkoutTemplate(
                    id = "cyberpunk_quads",
                    title = "Cyberpunk Quads and Glutes",
                    category = "Bodybuilding",
                    difficulty = "Intermediate",
                    durationMinutes = 55,
                    caloriesBurned = 520,
                    isPremium = true, // Unlocked with Pro/Elite
                    coverUrl = "quad_shred",
                    exercisesJson = """[
                        {"name": "Deficit Barbell Squats", "setsReps": "4 Sets x 8 Reps", "rest": "120s Rest"},
                        {"name": "Bulgarian Split Squats", "setsReps": "3 Sets x 10 Reps/leg", "rest": "90s Rest"},
                        {"name": "Hack Squats (1 & 1/4 Reps)", "setsReps": "3 Sets x 12 Reps", "rest": "90s Rest"},
                        {"name": "Seated Leg Extensions (Drop Set)", "setsReps": "4 Sets x 15 Reps", "rest": "60s Rest"}
                    ]"""
                ),
                WorkoutTemplate(
                    id = "zen_yoga",
                    title = "Holographic Vinyasa Flow",
                    category = "Yoga",
                    difficulty = "Beginner",
                    durationMinutes = 40,
                    caloriesBurned = 180,
                    isPremium = false,
                    coverUrl = "zen_yoga",
                    exercisesJson = """[
                        {"name": "Sun Salutation A", "setsReps": "5 Cycles Flow", "rest": "None"},
                        {"name": "Warrior III Balance Focus", "setsReps": "3 Sets x 45s hold", "rest": "30s Rest"},
                        {"name": "Crow Pose Crane Practice", "setsReps": "4 Attempts", "rest": "45s Rest"},
                        {"name": "Deep Pigeon Stretch & Shavasana", "setsReps": "10 min deep hold", "rest": "Complete"}
                    ]"""
                ),
                WorkoutTemplate(
                    id = "katana_hybrid",
                    title = "Katana Athletic Conditioning",
                    category = "Cardio",
                    difficulty = "Elite",
                    durationMinutes = 35,
                    caloriesBurned = 460,
                    isPremium = true, // Unlocked with Elite
                    coverUrl = "katana_cardio",
                    exercisesJson = """[
                        {"name": "Assault Bike Sprints", "setsReps": "8 Rounds x 20s Max Out", "rest": "10s Rest"},
                        {"name": "Sled Pushes (Weighted)", "setsReps": "4 Sets x 30 Meters", "rest": "60s Rest"},
                        {"name": "Battling Rope Double Waves", "setsReps": "4 Sets x 40s Active", "rest": "20s Rest"}
                    ]"""
                )
            )
            dao.insertWorkouts(templates)
        }

        // 3. Initial Community Feed Matches
        val existingPosts = communityPosts.firstOrNull()
        if (existingPosts.isNullOrEmpty()) {
            val feed = listOf(
                CommunityPost(
                    username = "Aryan Sen",
                    userTitle = "Elite Coach",
                    caption = "Progress is not overnight. From a flat bench of 60kg in Dec 24, to a solid double-plate 100kg clean rep today! The key? Sticking strictly to the Titan Chest Progressive Overload plan in Pro mode. 🦾\n#gymmotivation #ironpulse #powerlifting",
                    likesCount = 284,
                    commentsCount = 42,
                    isLiked = false,
                    timeAgo = "2 Hours Ago",
                    hasTransformImage = true,
                    beforeImage = "68 kg - Soft build",
                    afterImage = "76 kg - Lean shredded"
                ),
                CommunityPost(
                    username = "Pooja Roy",
                    userTitle = "Vinyasa Trainer",
                    caption = "Calming flow inside the holographic Yoga studio this morning. It's not just about standard reps, but flexibility & mobility under heavy weight training loads. Complete recovery mode on!",
                    likesCount = 145,
                    commentsCount = 19,
                    isLiked = true,
                    timeAgo = "5 Hours Ago",
                    hasTransformImage = false
                ),
                CommunityPost(
                    username = "Vikram Malhotra",
                    userTitle = "Iron Warrior",
                    caption = "Just finished the Elite 'Katana Athletic Conditioning' routine. Heart rate touched 185 bpm, pure neon energy in the gym today. Highly recommend to everyone on the Elite Membership tiers. It's worth every single rupee! 🏆",
                    likesCount = 412,
                    commentsCount = 85,
                    isLiked = false,
                    timeAgo = "1 Day Ago",
                    hasTransformImage = true,
                    beforeImage = "Bulk phase - 92 kg",
                    afterImage = "Cut phase - 83.5 kg"
                )
            )
            for (post in feed) {
                dao.insertPost(post)
            }
        }

        // 4. Initial Notifications
        val existingNotifications = notifications.firstOrNull()
        if (existingNotifications.isNullOrEmpty()) {
            val alerts = listOf(
                GymNotification(
                    title = "Elite Workout Plan Unlocked",
                    message = "Congratulations Siddharth! Your Elite-tier subscription unlocks Katana Conditioning and custom AI coaching recommendations.",
                    category = "PAYMENT",
                    timeString = "09:00 AM",
                    isRead = false
                ),
                GymNotification(
                    title = "Hydration Alert",
                    message = "Hydration reminder: Target 500ml of cold electrolytes as suggested by Coach Flash.",
                    category = "COACH",
                    timeString = "12:15 PM",
                    isRead = false
                ),
                GymNotification(
                    title = "Aryan liked your post",
                    message = "Elite Coach Aryan Sen liked your chest day progressive log.",
                    category = "SOCIAL",
                    timeString = "Yesterday",
                    isRead = true
                )
            )
            for (alert in alerts) {
                dao.insertNotification(alert)
            }
        }
    }
}
