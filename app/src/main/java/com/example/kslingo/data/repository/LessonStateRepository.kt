package com.example.kslingo.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.kslingo.data.model.Lesson
import com.google.firebase.auth.FirebaseAuth

class LessonStateRepository(private val context: Context) {

    private var userPrefs: SharedPreferences? = null

    private val completedLessonsInMemory = mutableSetOf<String>()

    init {
        // Add a listener to react instantly when the user logs in or out.
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                // USER LOGGED IN: Initialize prefs and load their specific progress.
                Log.d("LessonStateRepo", "User ${firebaseUser.uid} logged in. Loading their progress.")
                val prefsName = "lesson_prefs_${firebaseUser.uid}"
                userPrefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
                loadCompletedLessonsFromPrefs()
            } else {
                // USER LOGGED OUT: Clear all local data.
                Log.d("LessonStateRepo", "User logged out. Clearing local data.")
                completedLessonsInMemory.clear()
                userPrefs?.edit()?.clear()?.apply() // Clear the prefs file of the user who just logged out
                userPrefs = null // Set to null
            }
        }
    }

    private fun loadCompletedLessonsFromPrefs() {
        completedLessonsInMemory.clear() // Clear any previous user's data
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

    fun getUnlockedLessons(allLessons: List<Lesson>): List<Lesson> {
        if (allLessons.isEmpty()) return emptyList()

        return allLessons.mapIndexed { index, lesson ->
            val isUnlocked = when (index) {
                0 -> true // The first lesson is always unlocked.
                else -> isLessonCompleted(allLessons[index - 1].id) // Unlocked if the previous one is done.
            }
            lesson.copy(isLocked = !isUnlocked)
        }
    }

    fun getNextLessonId(currentLessonId: String, allLessons: List<Lesson>): String? {
        val currentIndex = allLessons.indexOfFirst { it.id == currentLessonId }
        return if (currentIndex != -1 && currentIndex < allLessons.size - 1) {
            allLessons[currentIndex + 1].id
        } else {
            null
        }
    }
}
