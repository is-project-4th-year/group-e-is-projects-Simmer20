package com.example.kslingo.data.repository

import android.content.Context
import com.example.kslingo.data.model.Lesson
import com.example.kslingo.data.model.LessonCategory
import com.example.kslingo.data.model.LessonContent
import com.example.kslingo.data.model.LessonType
import com.example.kslingo.R
class LessonsRepository(private val context: Context) {

    private val lessonStateRepository = LessonStateRepository(context)

    fun getLessonCategories(): List<LessonCategory> {
        val alphabetLessons = getAlphabetLessons()
        val numberLessons = getNumberLessons()

        val completedAlphabets = alphabetLessons.count { lessonStateRepository.isLessonCompleted(it.id) }
        val completedNumbers = numberLessons.count { lessonStateRepository.isLessonCompleted(it.id) }

        return listOf(
            LessonCategory(
                id = "alphabets",
                title = "Alphabets",
                description = "Learn KSL alphabet signs",
                iconRes = R.drawable.sign_a,
                color = 0xFF6A35EE,
                totalLessons = alphabetLessons.size,
                completedLessons = completedAlphabets,
                isLocked = false // Alphabets are always unlocked
            ),
            LessonCategory(
                id = "numbers",
                title = "Numbers",
                description = "Learn to sign numbers 0-20",
                iconRes = R.drawable.sign_b,
                color = 0xFF4CAF50,
                totalLessons = numberLessons.size,
                completedLessons = completedNumbers,
                isLocked = completedAlphabets < alphabetLessons.size // Locked until all alphabets completed
            )
        )
    }

    fun getAlphabetLessons(): List<Lesson> {
        val allLessons = listOf(
            Lesson(
                id = "alphabet_a",
                categoryId = "alphabets",
                title = "Letter A",
                description = "Learn the sign for letter A",
                type = LessonType.ALPHABET,
                content = listOf(
                    LessonContent(
                        sign = "A",
                        imageRes = R.drawable.sign_a,
                        description = "Make a fist with your thumb resting on the side of your index finger. Hold your hand up with palm facing forward."
                    )
                ),
                isCompleted = false,
                isLocked = false
            ),
            Lesson(
                id = "alphabet_b",
                categoryId = "alphabets",
                title = "Letter B",
                description = "Learn the sign for letter B",
                type = LessonType.ALPHABET,
                content = listOf(
                    LessonContent(
                        sign = "B",
                        imageRes = R.drawable.sign_b,
                        description = "Hold your hand flat with all fingers straight and together, thumb across the palm. Palm faces forward."
                    )
                ),
                isCompleted = false,
                isLocked = true
            ),
            Lesson(
                id = "alphabet_c",
                categoryId = "alphabets",
                title = "Letter C",
                description = "Learn the sign for letter C",
                type = LessonType.ALPHABET,
                content = listOf(
                    LessonContent(
                        sign = "C",
                        imageRes = R.drawable.sign_c,
                        description = "Curve your hand to form a C shape, with fingers slightly separated. Palm faces sideways."
                    )
                ),
                isCompleted = false,
                isLocked = true
            )
        )

        return lessonStateRepository.getUnlockedLessons(allLessons).map { lesson ->
            lesson.copy(isCompleted = lessonStateRepository.isLessonCompleted(lesson.id))
        }
    }

    fun getNumberLessons(): List<Lesson> {
        // Return number lessons (they'll be locked until alphabets are completed)
        return listOf(
            Lesson(
                id = "number_0",
                categoryId = "numbers",
                title = "Number 0",
                description = "Learn to sign zero",
                type = LessonType.NUMBER,
                content = listOf(
                    LessonContent(
                        sign = "0",
                        imageRes = R.drawable.kslingo_logo,
                        description = "Closed fist with palm facing forward"
                    )
                ),
                isCompleted = false,
                isLocked = true
            )
            // Add more number lessons...
        ).map { lesson ->
            lesson.copy(isCompleted = lessonStateRepository.isLessonCompleted(lesson.id))
        }
    }

    fun markLessonCompleted(lessonId: String) {
        lessonStateRepository.markLessonCompleted(lessonId)
    }

    fun getNextLesson(currentLessonId: String): Lesson? {
        val allLessons = getAlphabetLessons() + getNumberLessons()
        val nextLessonId = lessonStateRepository.getNextLessonId(currentLessonId, allLessons)
        return allLessons.find { it.id == nextLessonId }
    }
}