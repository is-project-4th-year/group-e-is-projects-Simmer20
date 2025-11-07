package com.example.kslingo.data.repository

import android.content.Context
import android.util.Log
import com.example.kslingo.R
import com.example.kslingo.data.model.Achievement
import com.example.kslingo.data.model.LearningStats
import com.example.kslingo.data.model.UserProfile
import com.example.kslingo.data.model.QuizResult
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserProfileRepository(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val lessonStateRepository = LessonStateRepository(context)
    private val quizStateRepository = QuizStateRepository(context)

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    init {
        // This listener ensures that whenever the auth state changes (login/logout),
        // the user profile is automatically fetched or cleared.
        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                // User is logged in, fetch their profile on a background thread.
                CoroutineScope(Dispatchers.IO).launch {
                    _userProfile.value = getUserProfile()
                }
            } else {
                // User is logged out, clear the profile data.
                _userProfile.value = null
            }
        }
    }

    /**
     * Fetches the complete user profile, combining local progress with Firestore data.
     * If a user document doesn't exist in Firestore, it creates one.
     */
    suspend fun getUserProfile(): UserProfile? {
        val currentUser = auth.currentUser ?: return null

        return try {
            Log.d("PROFILE_DEBUG", "Fetching profile for user: ${currentUser.uid}")
            val userDoc = db.collection("users").document(currentUser.uid).get().await()

            // 1. Get REAL, user-specific progress from the local repositories.
            val completedLessonsCount = lessonStateRepository.getCompletedLessons().size
            val quizResults = quizStateRepository.getQuizResults()
            val averageScore = quizResults.map { it.score }.average().takeIf { !it.isNaN() } ?: 0.0

            if (userDoc.exists()) {
                // User exists in Firestore, build the profile with fresh local data.
                Log.d("PROFILE_DEBUG", "User document exists. Building profile with fresh progress.")
                val progressMap = userDoc.get("progress") as? Map<String, Any> ?: emptyMap()

                val profile = UserProfile(
                    userId = currentUser.uid,
                    email = userDoc.getString("email") ?: currentUser.email ?: "",
                    username = userDoc.getString("username") ?: "KSL Learner",
                    displayName = userDoc.getString("displayName") ?: currentUser.displayName ?: "KSL Learner",
                    profilePictureUrl = userDoc.getString("profilePictureUrl") ?: currentUser.photoUrl?.toString(),
                    joinedDate = userDoc.getTimestamp("createdAt") ?: Timestamp.now(),
                    learningStats = LearningStats(
                        totalLessonsCompleted = completedLessonsCount,
                        totalPracticeTime = safeGetInt(progressMap["totalPracticeTime"]),
                        accuracy = averageScore,
                        currentStreak = calculateCurrentStreak(currentUser.uid),
                        longestStreak = safeGetInt(progressMap["longestStreak"]),
                        level = calculateLevel(completedLessonsCount),
                        xp = calculateXp(completedLessonsCount, quizResults)
                    ),
                    achievements = getRealAchievements(completedLessonsCount, quizResults, calculateCurrentStreak(currentUser.uid))
                )

                // 2. Update Firestore with the latest calculated progress.
                updateUserProgress(currentUser.uid, profile.learningStats)
                profile

            } else {
                // User does not exist in Firestore, create a new document for them.
                Log.d("PROFILE_DEBUG", "No document found. Creating new user with initial progress.")
                createUserWithRealProgress(currentUser, completedLessonsCount, quizResults)
            }
        } catch (e: Exception) {
            Log.e("PROFILE_DEBUG", "Error fetching profile: ${e.message}", e)
            // Fallback to a basic profile if Firestore fails.
            createBasicProfileWithProgress(currentUser)
        }
    }

    /**
     * Creates a new user document in Firestore with their initial progress.
     */
    private suspend fun createUserWithRealProgress(
        user: FirebaseUser,
        completedLessonsCount: Int,
        quizResults: List<QuizResult>
    ): UserProfile {
        val learningStats = LearningStats(
            totalLessonsCompleted = completedLessonsCount,
            totalPracticeTime = 0,
            accuracy = quizResults.map { it.score }.average().takeIf { !it.isNaN() } ?: 0.0,
            currentStreak = calculateCurrentStreak(user.uid),
            longestStreak = 0,
            level = calculateLevel(completedLessonsCount),
            xp = calculateXp(completedLessonsCount, quizResults)
        )

        val userData = hashMapOf(
            "email" to user.email,
            "username" to (user.displayName ?: "KSL Learner"),
            "displayName" to (user.displayName ?: "KSL Learner"),
            "profilePictureUrl" to (user.photoUrl?.toString() ?: ""),
            "createdAt" to Timestamp.now(),
            "progress" to learningStats // Store the whole stats object
        )

        try {
            db.collection("users").document(user.uid).set(userData).await()
        } catch (e: Exception) {
            Log.e("PROFILE_DEBUG", "Failed to save new user document: ${e.message}")
        }

        return UserProfile(
            userId = user.uid,
            email = user.email ?: "",
            username = userData["username"] as String,
            displayName = userData["displayName"] as String,
            profilePictureUrl = user.photoUrl?.toString(),
            joinedDate = Timestamp.now(),
            learningStats = learningStats,
            achievements = getRealAchievements(completedLessonsCount, quizResults, learningStats.currentStreak)
        )
    }

    /**
     * Updates the 'progress' map in Firestore for a given user.
     */
    private suspend fun updateUserProgress(userId: String, stats: LearningStats) {
        try {
            // Update the 'progress' sub-document in one go.
            db.collection("users").document(userId).update("progress", stats).await()
        } catch (e: Exception) {
            Log.e("PROFILE_DEBUG", "Failed to update progress: ${e.message}")
        }
    }

    /**
     * Updates the display name and username in both Firebase Auth and Firestore.
     */
    suspend fun updateProfile(updatedProfile: UserProfile): Boolean {
        val user = auth.currentUser ?: return false
        return try {
            // Update in Firebase Auth
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(updatedProfile.displayName)
                .build()
            user.updateProfile(profileUpdates).await()

            // Update in Firestore
            val updates = mapOf(
                "username" to updatedProfile.username,
                "displayName" to updatedProfile.displayName
            )
            db.collection("users").document(user.uid).update(updates).await()
            true
        } catch (e: Exception) {
            Log.e("PROFILE_DEBUG", "Error updating profile: ${e.message}")
            false
        }
    }

    /**
     * Increments the total practice time in Firestore.
     */
    suspend fun updatePracticeTime(minutes: Int) {
        auth.currentUser?.uid?.let { userId ->
            try {
                db.collection("users").document(userId)
                    .update("progress.totalPracticeTime", FieldValue.increment(minutes.toLong()))
                    .await()
            } catch (e: Exception) {
                Log.e("PROFILE_DEBUG", "Error updating practice time: ${e.message}")
            }
        }
    }

    // --- Helper & Calculation Functions ---

    private fun safeGetInt(value: Any?): Int {
        return (value as? Number)?.toInt() ?: 0
    }

    private fun calculateLevel(lessonsCompleted: Int): Int {
        return 1 + (lessonsCompleted / 5) // Level up every 5 lessons
    }

    private fun calculateXp(lessonsCompleted: Int, quizResults: List<QuizResult>): Int {
        val lessonXp = lessonsCompleted * 10
        val quizXp = quizResults.sumOf { it.score } / 10 // 1 XP for every 10 points
        return lessonXp + quizXp
    }

    private fun calculateCurrentStreak(userId: String): Int {
        // This should use a user-specific preference file.
        val prefsName = "streak_prefs_$userId"
        val sharedPref = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        // In a real app, you would add date-checking logic here.
        return sharedPref.getInt("current_streak", 0)
    }

    private fun getRealAchievements(
        completedLessons: Int,
        quizResults: List<QuizResult>,
        currentStreak: Int
    ): List<Achievement> {
        val achievements = mutableListOf<Achievement>()

        // Using a set for quick lookup of unlocked achievement IDs
        val unlockedIds = mutableSetOf<String>()

        if (completedLessons >= 1) unlockedIds.add("first_lesson")
        if (completedLessons >= 5) unlockedIds.add("fast_learner")
        if (completedLessons >= 10) unlockedIds.add("dedicated_learner")
        if (quizResults.any { it.score >= 90 }) unlockedIds.add("quiz_master")
        if (currentStreak >= 3) unlockedIds.add("consistent_learner")

        // Define all possible achievements
        val allAchievements = listOf(
            Achievement("first_lesson", "First Steps", "Complete your first KSL lesson", R.drawable.kslingo_logo),
            Achievement("fast_learner", "Fast Learner", "Complete 5 lessons", R.drawable.kslingo_logo),
            Achievement("dedicated_learner", "Dedicated Learner", "Complete 10 lessons", R.drawable.kslingo_logo),
            Achievement("quiz_master", "Quiz Master", "Score 90% or higher on any quiz", R.drawable.kslingo_logo),
            Achievement("consistent_learner", "Consistent Learner", "Maintain a 3-day streak", R.drawable.kslingo_logo)
        )

        // Return a single list of achievements with the correct 'unlocked' status
        return allAchievements.map { it.copy(unlocked = unlockedIds.contains(it.id)) }
    }

    /**
     * Creates a basic, local-only profile when Firestore is unreachable.
     */
    private fun createBasicProfileWithProgress(user: FirebaseUser): UserProfile {
        Log.d("PROFILE_DEBUG", "Creating a basic fallback profile for UI.")
        return UserProfile(
            userId = user.uid,
            email = user.email ?: "",
            username = user.displayName ?: "KSL Learner",
            displayName = user.displayName ?: "KSL Learner",
            profilePictureUrl = user.photoUrl?.toString(),
            joinedDate = Timestamp.now(),
            learningStats = LearningStats(), // Default empty stats
            achievements = emptyList()
        )
    }
}
