package com.example.kslingo.data.repository

import android.content.Context
import android.util.Log
import com.example.kslingo.R
import com.example.kslingo.data.model.Achievement
import com.example.kslingo.data.model.LearningStats
import com.example.kslingo.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class UserProfileRepository(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val lessonStateRepository = LessonStateRepository(context)
    private val quizStateRepository = QuizStateRepository(context)

    private fun safeGetInt(value: Any?): Int {
        return value?.toString()?.toIntOrNull() ?: 0
    }
    private fun createBasicProfileWithProgress(user: FirebaseUser): UserProfile {
        Log.d("PROFILE_DEBUG", "Creating a basic fallback profile for UI.")
        return UserProfile(
            userId = user.uid,
            email = user.email ?: "",
            username = user.displayName ?: "KSL Learner",
            displayName = user.displayName ?: "KSL Learner",
            profilePictureUrl = user.photoUrl?.toString(),
            joinedDate = Timestamp.now(),
            learningStats = LearningStats( // Default empty stats
                totalLessonsCompleted = 0,
                totalPracticeTime = 0,
                accuracy = 0.0,
                currentStreak = 0,
                longestStreak = 0,
                level = 1,
                xp = 0
            ),
            achievements = emptyList() // No achievements
        )
    }


    suspend fun getUserProfile(): UserProfile? {
        val currentUser = auth.currentUser ?: return null

        return try {
            Log.d("PROFILE_DEBUG", "Fetching profile for user: ${currentUser.uid}")

            val userDoc = db.collection("users").document(currentUser.uid).get().await()

            // Get real progress data
            val completedLessons = lessonStateRepository.getCompletedLessons().size
            val quizResults = quizStateRepository.getQuizResults()
            val totalQuizScore = quizResults.map { it.score }.average()

            if (userDoc.exists()) {
                Log.d("PROFILE_DEBUG", "User document exists")

                val progressMap = userDoc.get("progress") as? Map<String, Any> ?: mapOf()

                val profile = UserProfile(
                    userId = currentUser.uid,
                    email = userDoc.getString("email") ?: currentUser.email ?: "",
                    username = userDoc.getString("username") ?: currentUser.displayName ?: "KSL Learner",
                    displayName = currentUser.displayName ?: userDoc.getString("username") ?: "KSL Learner",
                    profilePictureUrl = currentUser.photoUrl?.toString(),
                    joinedDate = userDoc.getTimestamp("createdAt") ?: com.google.firebase.Timestamp.now(),
                    learningStats = LearningStats(
                        totalLessonsCompleted = completedLessons, // REAL DATA
                        totalPracticeTime = safeGetInt(progressMap["totalPracticeTime"]),
                        accuracy = if (completedLessons > 0) (totalQuizScore * 0.7) + 30 else 0.0, // Simulated accuracy
                        currentStreak = calculateCurrentStreak(),
                        longestStreak = safeGetInt(progressMap["longestStreak"]),
                        level = calculateLevel(completedLessons),
                        xp = completedLessons * 10 + (totalQuizScore * 0.3).toInt() // XP based on progress
                    ),
                    achievements = getRealAchievements(currentUser.uid, completedLessons, quizResults)
                )

                // Update Firestore with real progress
                updateUserProgress(currentUser.uid, completedLessons, profile.learningStats)

                profile
            } else {
                Log.d("PROFILE_DEBUG", "Creating new user document")
                createUserWithRealProgress(currentUser, completedLessons, quizResults)
            }
        } catch (e: Exception) {
            Log.e("PROFILE_DEBUG", "Error: ${e.message}", e)
            createBasicProfileWithProgress(currentUser)
        }
    }

    private suspend fun createUserWithRealProgress(
        user: com.google.firebase.auth.FirebaseUser,
        completedLessons: Int,
        quizResults: List<com.example.kslingo.data.model.QuizResult>
    ): UserProfile {
        val learningStats = LearningStats(
            totalLessonsCompleted = completedLessons,
            totalPracticeTime = 0,
            accuracy = if (completedLessons > 0) 50.0 else 0.0,
            currentStreak = calculateCurrentStreak(),
            longestStreak = 0,
            level = calculateLevel(completedLessons),
            xp = completedLessons * 10
        )

        val userData = hashMapOf(
            "email" to user.email,
            "username" to (user.displayName ?: "KSL Learner"),
            "createdAt" to com.google.firebase.Timestamp.now(),
            "progress" to hashMapOf(
                "lessonsCompleted" to completedLessons,
                "totalPracticeTime" to 0,
                "accuracy" to learningStats.accuracy,
                "currentStreak" to learningStats.currentStreak,
                "longestStreak" to learningStats.longestStreak,
                "xp" to learningStats.xp
            )
        )

        try {
            db.collection("users").document(user.uid).set(userData).await()
        } catch (e: Exception) {
            Log.e("PROFILE_DEBUG", "Failed to save user document: ${e.message}")
        }

        return UserProfile(
            userId = user.uid,
            email = user.email ?: "",
            username = user.displayName ?: "KSL Learner",
            displayName = user.displayName ?: "KSL Learner",
            profilePictureUrl = user.photoUrl?.toString(),
            joinedDate = com.google.firebase.Timestamp.now(),
            learningStats = learningStats,
            achievements = getRealAchievements(user.uid, completedLessons, quizResults)
        )
    }

    private suspend fun updateUserProgress(
        userId: String,
        completedLessons: Int,
        learningStats: LearningStats
    ) {
        try {
            db.collection("users").document(userId)
                .update(
                    "progress.lessonsCompleted", completedLessons,
                    "progress.accuracy", learningStats.accuracy,
                    "progress.currentStreak", learningStats.currentStreak,
                    "progress.longestStreak", learningStats.longestStreak,
                    "progress.xp", learningStats.xp
                )
                .await()
        } catch (e: Exception) {
            Log.e("PROFILE_DEBUG", "Failed to update progress: ${e.message}")
        }
    }

    private fun calculateCurrentStreak(): Int {
        // Simple streak calculation - in real app, track daily logins
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPref.getInt("current_streak", 0)
    }

    private fun calculateLevel(lessonsCompleted: Int): Int {
        return when {
            lessonsCompleted >= 20 -> 5
            lessonsCompleted >= 15 -> 4
            lessonsCompleted >= 10 -> 3
            lessonsCompleted >= 5 -> 2
            else -> 1
        }
    }

    private suspend fun getRealAchievements(
        userId: String,
        completedLessons: Int,
        quizResults: List<com.example.kslingo.data.model.QuizResult>
    ): List<Achievement> {
        val achievements = mutableListOf<Achievement>()

        // First Lesson Achievement
        if (completedLessons >= 1) {
            achievements.add(Achievement(
                id = "first_lesson",
                title = "First Steps",
                description = "Complete your first KSL lesson",
                iconRes = R.drawable.kslingo_logo,
                unlocked = true
            ))
        }

        // Fast Learner Achievement
        if (completedLessons >= 5) {
            achievements.add(Achievement(
                id = "fast_learner",
                title = "Fast Learner",
                description = "Complete 5 lessons",
                iconRes = R.drawable.kslingo_logo,
                unlocked = true
            ))
        }

        // Quiz Master Achievement
        val hasPerfectQuiz = quizResults.any { it.score >= 90 }
        if (hasPerfectQuiz) {
            achievements.add(Achievement(
                id = "quiz_master",
                title = "Quiz Master",
                description = "Score 90% or higher on any quiz",
                iconRes = R.drawable.kslingo_logo,
                unlocked = true
            ))
        }

        // Alphabet Master Achievement
        val alphabetLessons = completedLessons
        if (alphabetLessons >= 3) {
            achievements.add(Achievement(
                id = "alphabet_expert",
                title = "Alphabet Expert",
                description = "Complete multiple alphabet lessons",
                iconRes = R.drawable.kslingo_logo,
                unlocked = true
            ))
        }

        // Streak Achievement
        val currentStreak = calculateCurrentStreak()
        if (currentStreak >= 3) {
            achievements.add(Achievement(
                id = "consistent_learner",
                title = "Consistent Learner",
                description = "Maintain a 3-day learning streak",
                iconRes = R.drawable.kslingo_logo,
                unlocked = true
            ))
        }

        // Add locked achievements for motivation
        if (completedLessons < 10) {
            achievements.add(Achievement(
                id = "dedicated_learner",
                title = "Dedicated Learner",
                description = "Complete 10 lessons",
                iconRes = R.drawable.kslingo_logo,
                unlocked = false
            ))
        }

        return achievements
    }

    suspend fun updateProfile(updatedProfile: UserProfile): Boolean {
        val user = auth.currentUser ?: return false

        return try {
            // Update in Firebase Auth
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(updatedProfile.displayName)
                .build()
            user.updateProfile(profileUpdates).await()

            // Update in Firestore
            val updates = hashMapOf<String, Any>(
                "username" to updatedProfile.username,
                "displayName" to updatedProfile.displayName
            )

            db.collection("users").document(user.uid)
                .update(updates)
                .await()

            true
        } catch (e: Exception) {
            Log.e("PROFILE_DEBUG", "Error updating profile: ${e.message}")
            false
        }
    }

    suspend fun updatePracticeTime(minutes: Int) {
        val user = auth.currentUser ?: return

        try {
            db.collection("users").document(user.uid)
                .update("progress.totalPracticeTime", com.google.firebase.firestore.FieldValue.increment(minutes.toLong()))
                .await()
        } catch (e: Exception) {
            Log.e("PROFILE_DEBUG", "Error updating practice time: ${e.message}")
        }
    }
        // ... other helper functions like safeGetInt ...
    }
