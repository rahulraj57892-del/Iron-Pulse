package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Room Entities ---

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Siddharth Sharma",
    val email: String = "siddharth@ironpulse.fit",
    val weight: Float = 78.5f,
    val height: Float = 178f,
    val fitnessGoal: String = "Lean Muscle Hypertrophy",
    val subscriptionTier: String = "BASIC", // BASIC, PRO, ELITE
    val streak: Int = 5,
    val xp: Int = 1250,
    val isLoggedIn: Boolean = false,
    val lastSyncTime: String = "Just Now"
)

@Entity(tableName = "workouts")
data class WorkoutTemplate(
    @PrimaryKey val id: String,
    val title: String,
    val category: String, // "Strength", "HIIT", "Cardio", "Yoga", "Bodybuilding"
    val difficulty: String, // "Beginner", "Intermediate", "Elite"
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val exercisesJson: String, // Holds lists of exercises
    val isPremium: Boolean,
    val coverUrl: String = "" // Mimics gym wallpaper
)

@Entity(tableName = "workout_logs")
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workoutId: String,
    val title: String,
    val dateString: String,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val pointsScored: Int = 150
)

@Entity(tableName = "meal_logs")
data class MealLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // "Veg", "Non-Veg", "Vegan"
    val mealType: String, // "Breakfast", "Lunch", "Dinner", "Snacks"
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val dateString: String
)

@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey val dateString: String, // Unique per day (e.g. "2026-05-24")
    val milliliters: Int
)

@Entity(tableName = "community_posts")
data class CommunityPost(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val userTitle: String, // e.g. "Pro Coach", "Elite Athlete"
    val caption: String,
    val likesCount: Int,
    val commentsCount: Int,
    val isLiked: Boolean,
    val timeAgo: String,
    val hasTransformImage: Boolean = false,
    val beforeImage: String = "",
    val afterImage: String = ""
)

@Entity(tableName = "notifications")
data class GymNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val category: String, // "COACH", "SECURITY", "PAYMENT", "SOCIAL"
    val timeString: String,
    val isRead: Boolean = false
)

// --- Room DAOs ---

@Dao
interface GymDao {
    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfile)

    @Query("SELECT * FROM workouts")
    fun getAllWorkouts(): Flow<List<WorkoutTemplate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkouts(workouts: List<WorkoutTemplate>)

    @Query("SELECT * FROM workout_logs ORDER BY id DESC")
    fun getWorkoutLogs(): Flow<List<WorkoutLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun logCompletedWorkout(log: WorkoutLog)

    @Query("SELECT * FROM meal_logs WHERE dateString = :date ORDER BY id DESC")
    fun getMealLogsByDate(date: String): Flow<List<MealLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun logMeal(meal: MealLog)

    @Query("DELETE FROM meal_logs WHERE id = :id")
    suspend fun deleteMealLog(id: Int)

    @Query("SELECT * FROM water_logs WHERE dateString = :date LIMIT 1")
    fun getWaterLogByDate(date: String): Flow<WaterLog?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWaterLog(waterLog: WaterLog)

    @Query("SELECT * FROM community_posts ORDER BY id DESC")
    fun getCommunityPosts(): Flow<List<CommunityPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CommunityPost)

    @Query("UPDATE community_posts SET likesCount = CASE WHEN isLiked = 1 THEN likesCount - 1 ELSE likesCount + 1 END, isLiked = 1 - isLiked WHERE id = :postId")
    suspend fun togglePostLike(postId: Int)

    @Query("SELECT * FROM notifications ORDER BY id DESC")
    fun getNotifications(): Flow<List<GymNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: GymNotification)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    @Query("DELETE FROM workout_logs")
    suspend fun clearWorkoutLogs()

    @Query("DELETE FROM meal_logs")
    suspend fun clearMealLogs()
}

// --- App Database ---

@Database(
    entities = [
        UserProfile::class,
        WorkoutTemplate::class,
        WorkoutLog::class,
        MealLog::class,
        WaterLog::class,
        CommunityPost::class,
        GymNotification::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GymDatabase : RoomDatabase() {
    abstract fun gymDao(): GymDao
}
