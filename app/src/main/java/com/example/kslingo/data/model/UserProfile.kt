package com.example.kslingo.data.model

import com.google.firebase.Timestamp

data class UserProfile(
    val userId: String,
    val email: String,
    val username: String,
    val displayName: String,
    val profilePictureUrl: String? = null,
    val joinedDate: Timestamp? = null,
    val learningStats: LearningStats = LearningStats(),
    val achievements: List<Achievement> = emptyList()
)

data class LearningStats(
    val totalLessonsCompleted: Int = 0,
    val totalPracticeTime: Int = 0, // in minutes
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val accuracy: Double = 0.0,
    val level: Int = 1,
    val xp: Int = 0,
    val currentLessonId: String? = "alphabet_01",
    val currentLessonName: String = "A",
    val currentLessonProgress: Int = 0

)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: Int,
    val unlocked: Boolean = false,
    val unlockedDate: Timestamp? = null
)