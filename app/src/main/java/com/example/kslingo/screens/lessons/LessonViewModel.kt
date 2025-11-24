package com.example.kslingo.screens.lessons

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kslingo.data.model.LessonCategory
import android.content.Context
import androidx.compose.animation.core.copy
import androidx.compose.ui.semantics.getOrNull
import androidx.core.util.remove
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kslingo.data.model.Lesson
import com.example.kslingo.data.repository.LessonStateRepository
import com.example.kslingo.data.repository.LessonsRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LessonViewModel(application: Application) : AndroidViewModel(application) {

    private val lessonsRepository = LessonsRepository(application)
    private val lessonStateRepository = LessonStateRepository(application)

    private val _lessonCategories = MutableStateFlow<List<LessonCategory>>(emptyList())
    val lessonCategories: StateFlow<List<LessonCategory>> = _lessonCategories.asStateFlow()
    private val _lessons = MutableStateFlow<List<Lesson>>(emptyList())
    val lessons: StateFlow<List<Lesson>> = _lessons.asStateFlow()
    init {
        loadLessonsWithProgress()
    }
    fun loadLessonsWithProgress() {
        viewModelScope.launch {
            val completedLessonIds = lessonStateRepository.getCompletedLessons()

            // 2. Get the static list of all categories and their lessons.
            val allCategories = lessonsRepository.getLessonCategories()

            // 3. Combine the data to create the final list for the UI.
            val categoriesWithProgress = allCategories.map { category ->
                // Count how many lessons in this category are completed.
                val completedLessonsInCategory = category.lessons.count { lesson ->
                    completedLessonIds.contains(lesson.id)
                }

                // Determine if this category should be locked.
                // A category is unlocked if the previous one is fully completed.
                val isLocked = if (category.id == allCategories.firstOrNull()?.id) {
                    // The first category is always unlocked.
                    false
                } else {
                    val previousCategoryIndex = allCategories.indexOf(category) - 1
                    val previousCategory = allCategories.getOrNull(previousCategoryIndex)
                    if (previousCategory != null) {
                        // Check if the number of completed lessons in the previous category
                        // is less than its total number of lessons.
                        val completedInPrevious = previousCategory.lessons.count { completedLessonIds.contains(it.id) }
                        completedInPrevious < previousCategory.totalLessons
                    } else {
                        true // Should not happen, but a safe fallback
                    }
                }

                // Use the 'copy' method of the data class to create an updated instance.
                category.copy(
                    completedLessons = completedLessonsInCategory,
                    isLocked = isLocked
                )
            }
            _lessonCategories.value = categoriesWithProgress
          }
    }


private val _completedLessons = MutableStateFlow(0)
    val completedLessons: StateFlow<Int> = _completedLessons.asStateFlow()

    private var currentCategoryId: String? = null

    fun loadLessons(categoryId: String?) {
        currentCategoryId = categoryId
        val fetchedLessons = when (categoryId) {
            "alphabets" -> lessonsRepository.getAlphabetLessons()
            "numbers" -> lessonsRepository.getNumberLessons()
            "phrases" -> lessonsRepository.getPhrasesLessons()
            "colors" -> lessonsRepository.getColorLessons()
            "family" -> lessonsRepository.getFamilyLessons()
            "language" -> lessonsRepository.getLanguageLessons()
            else -> emptyList()
        }
        updateLessons(fetchedLessons)
    }

    fun markLessonCompleted(lessonId: String) {
        viewModelScope.launch {
            lessonsRepository.markLessonCompleted(lessonId)
            // Reload to update lock/unlock states for all categories
            loadLessonsWithProgress()
        }
    }

    fun refreshProgress(){
        viewModelScope.launch{
            loadLessonsWithProgress()
        }
    }

    private fun updateLessons(allLessons: List<Lesson>) {
        val completed = allLessons.count { it.isCompleted }
        _completedLessons.value = completed
        _lessons.value = allLessons
    }
}
