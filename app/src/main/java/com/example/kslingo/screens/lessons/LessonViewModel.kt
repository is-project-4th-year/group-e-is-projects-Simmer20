package com.example.kslingo.screens.lessons

import android.app.Application
import androidx.compose.animation.core.copy
import androidx.compose.ui.semantics.getOrNull
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kslingo.data.model.LessonCategory
import com.example.kslingo.data.repository.LessonsRepository
import com.example.kslingo.data.repository.LessonStateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LessonViewModel(application: Application) : AndroidViewModel(application) {

    private val lessonRepository = LessonsRepository(application)
    private val lessonStateRepository = LessonStateRepository(application)
    private val _lessonCategories = MutableStateFlow<List<LessonCategory>>(emptyList())
    val lessonCategories: StateFlow<List<LessonCategory>> = _lessonCategories.asStateFlow()

    init {
        loadLessonsWithProgress()
    }
    fun loadLessonsWithProgress() {
        viewModelScope.launch {
            val completedLessonIds = lessonStateRepository.getCompletedLessons()
            val allCategories = lessonRepository.getLessonCategories()
            val categoriesWithProgress = allCategories.map { category ->
                val completedLessonsInCategory = category.lessons.count { lesson ->
                    completedLessonIds.contains(lesson.id)
                }
                val isLocked = if (category.id == allCategories.firstOrNull()?.id) {
                    false
                } else {
                    // Use the standard Kotlin 'getOrNull' for lists
                    val previousCategory = allCategories.getOrNull(allCategories.indexOf(category) - 1)
                    if (previousCategory != null) {
                        val completedInPrevious = previousCategory.lessons.count { completedLessonIds.contains(it.id) }
                        completedInPrevious < previousCategory.totalLessons
                    } else {
                        true
                    }
                }

                category.copy(
                    completedLessons = completedLessonsInCategory,
                    isLocked = isLocked
                )
            }
            _lessonCategories.value = categoriesWithProgress
        }
    }
}
