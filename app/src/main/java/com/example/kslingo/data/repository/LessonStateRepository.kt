package com.example.kslingo.data.repository

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import com.example.kslingo.data.model.Lesson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LessonStateRepository(private val context: Context) {

    private val _completedLessons = mutableSetOf<String>()
    private val sharedPref = context.getSharedPreferences("lesson_progress", Context.MODE_PRIVATE)

    init {
        // Load completed lessons from SharedPreferences
        val completed = sharedPref.getStringSet("completed_lessons", setOf()) ?: setOf()
        _completedLessons.addAll(completed)
    }

    fun markLessonCompleted(lessonId: String) {
        _completedLessons.add(lessonId)
        // Save to SharedPreferences
        sharedPref.edit()
            .putStringSet("completed_lessons", _completedLessons)
            .apply()
    }

    fun isLessonCompleted(lessonId: String): Boolean {
        return _completedLessons.contains(lessonId)
    }

    fun getCompletedLessons(): Set<String> {
        return _completedLessons.toSet()
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
        return allLessons.mapIndexed { index, lesson ->
            // First lesson is always unlocked
            if (index == 0) {
                lesson.copy(isLocked = false)
            } else {
                // Lesson is unlocked if previous lesson is completed
                val previousLessonCompleted = isLessonCompleted(allLessons[index - 1].id)
                lesson.copy(isLocked = !previousLessonCompleted)
            }
        }
    }
}