package com.example.kslingo.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.kslingo.data.model.Lesson
import com.example.kslingo.data.model.LessonCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LessonStateRepository(private val context: Context) {

    private var userPrefs: SharedPreferences? = null
    private val completedLessonsInMemory = mutableSetOf<String>()
    private val auth = FirebaseAuth.getInstance()

    init {
        // Immediately load data for the current user, if any
        handleUserChange(auth.currentUser)

        // Listen for subsequent authentication state changes
        auth.addAuthStateListener {
            handleUserChange(it.currentUser)
        }
    }

    private fun handleUserChange(firebaseUser: FirebaseUser?) {
        if (firebaseUser != null) {
            // User is signed in
            val prefsName = "lesson_prefs_${firebaseUser.uid}"
            if (userPrefs?.toString()?.contains(prefsName) == true) {
                // Same user, no need to reload
                return
            }
            Log.d("LessonStateRepo", "User changed to ${firebaseUser.uid}. Loading their progress.")
            userPrefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            loadCompletedLessonsFromPrefs()
        } else {
            // User is signed out
            Log.d("LessonStateRepo", "User logged out. Clearing local data.")
            completedLessonsInMemory.clear()
            userPrefs = null
        }
    }

    private fun loadCompletedLessonsFromPrefs() {
        completedLessonsInMemory.clear()
        val completedSet = userPrefs?.getStringSet("completed_lessons", emptySet()) ?: emptySet()
        completedLessonsInMemory.addAll(completedSet)
        Log.d("LessonStateRepo", "Loaded ${completedLessonsInMemory.size} completed lessons from prefs.")
    }

    fun markLessonCompleted(lessonId: String) {
        if (userPrefs == null) {
            Log.w("LessonStateRepo", "Cannot mark lesson completed, no user is logged in.")
            return
        }
        completedLessonsInMemory.add(lessonId)
        userPrefs?.edit()
            ?.putStringSet("completed_lessons", completedLessonsInMemory)
            ?.apply()
        Log.d("LessonStateRepo", "Lesson '$lessonId' marked as complete.")
    }

    fun isLessonCompleted(lessonId: String): Boolean {
        return completedLessonsInMemory.contains(lessonId)
    }

    fun getCompletedLessons(): Set<String> {
        return completedLessonsInMemory.toSet()
    }

    fun getNextLessonId(currentLessonId: String, allLessons: List<Lesson>): String? {
        val currentIndex = allLessons.indexOfFirst { it.id == currentLessonId }
        return if (currentIndex >= 0 && currentIndex < allLessons.size - 1) {
            allLessons[currentIndex + 1].id
        } else {
            null
        }
    }

    fun getUnlockedLessons(allLessons: List<Lesson>): List<Lesson> {
        if (allLessons.isEmpty()) return emptyList()

        return allLessons.mapIndexed { index, lesson ->
            val isUnlocked = if (index == 0) {
                true // The first lesson is always unlocked
            } else {
                isLessonCompleted(allLessons[index - 1].id)
            }
            lesson.copy(isLocked = !isUnlocked)
        }
    }

    fun getUnlockedCategories(allCategories: List<LessonCategory>): List<LessonCategory> {
        var previousCategoryCompleted = true
        return allCategories.map { category ->
            val isUnlocked = previousCategoryCompleted
            val isCompleted = category.lessons.all { isLessonCompleted(it.id) }
            previousCategoryCompleted = isCompleted
            category.copy(isLocked = !isUnlocked, isCompleted = isCompleted)
        }
    }
}
