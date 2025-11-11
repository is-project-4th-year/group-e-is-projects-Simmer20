package com.example.kslingo.screens.lessons

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kslingo.data.model.Lesson
import com.example.kslingo.data.repository.LessonsRepository
import com.example.kslingo.data.repository.LessonStateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LessonCategoryDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val lessonRepository = LessonsRepository(application)
    private val lessonStateRepository = LessonStateRepository(application)

    // Holds the list of lessons with their real-time lock status
    private val _unlockedLessons = MutableStateFlow<List<Lesson>>(emptyList())
    val unlockedLessons: StateFlow<List<Lesson>> = _unlockedLessons.asStateFlow()

    // Holds the count of completed lessons for the header
    private val _completedLessonsCount = MutableStateFlow(0)
    val completedLessonsCount: StateFlow<Int> = _completedLessonsCount.asStateFlow()

    /**
     * Loads a specific category's lessons and determines their lock status based on REAL progress.
     */
    fun loadLessonsForCategory(categoryId: String) {
        viewModelScope.launch {
            // 1. Get all lessons for the specified category from the static repository.
            val categoryLessons = lessonRepository.getLessonsForCategory(categoryId)

            if (categoryLessons.isNotEmpty()) {
                // 2. Get the list of lessons with updated lock status from the state repository.
                // This function correctly unlocks lessons based on whether the previous one is complete.
                val lessonsWithLockState = lessonStateRepository.getUnlockedLessons(categoryLessons)
                _unlockedLessons.value = lessonsWithLockState

                // 3. Get the set of ALL completed lesson IDs to calculate the count for this category.
                val allCompletedIds = lessonStateRepository.getCompletedLessons()
                val completedInCategory = lessonsWithLockState.count { it.id in allCompletedIds }
                _completedLessonsCount.value = completedInCategory
            }
        }
    }
}
