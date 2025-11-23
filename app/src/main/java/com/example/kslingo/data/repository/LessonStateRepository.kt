package com.example.kslingo.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.kslingo.data.model.Lesson
import com.example.kslingo.data.model.LessonCategory
import com.google.firebase.auth.FirebaseAuth

class LessonStateRepository(private val context: Context) {

    private var userPrefs: SharedPreferences? = null

    private val completedLessonsInMemory = mutableSetOf<String>()

    init {
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                Log.d("LessonStateRepo", "User ${firebaseUser.uid} logged in. Loading their progress.")
                val prefsName = "lesson_prefs_${firebaseUser.uid}"
                userPrefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
                loadCompletedLessonsFromPrefs()
            } else {
                Log.d("LessonStateRepo", "User logged out. Clearing local data.")
                completedLessonsInMemory.clear()
                userPrefs?.edit()?.clear()?.apply()
                userPrefs = null
            }
        }
    }

    fun reloadCompleteLessons() {
        loadCompletedLessonsFromPrefs()
    }

    private fun loadCompletedLessonsFromPrefs(): Set<String> {
        completedLessonsInMemory.clear() // Clear any previous user's data
        val completedSet = userPrefs?.getStringSet("completed_lessons", emptySet()) ?: emptySet()
        completedLessonsInMemory.addAll(completedSet)
        Log.d("LessonStateRepo", "Loaded ${completedLessonsInMemory.size} completed lessons from prefs.")
        return completedLessonsInMemory.toSet()
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
                else -> isLessonCompleted(allLessons[index - 1].id)
            }
            lesson.copy(isLocked = !isUnlocked)
        }
    }

    fun getUnlockedCategories(allCategories: List<LessonCategory>): List<LessonCategory> {
        val unlockedCategories = mutableListOf<LessonCategory>()
        var previousCategoryCompleted = true
        for (category in allCategories) {
            val lessons = category.lessons // get lessons for the category
            val isUnlocked = previousCategoryCompleted
            val isCompleted = lessons.all { isLessonCompleted(it.id) }
            unlockedCategories.add(category.copy(isLocked = !isUnlocked, isCompleted = isCompleted))
            previousCategoryCompleted = isCompleted
        }
        return unlockedCategories
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