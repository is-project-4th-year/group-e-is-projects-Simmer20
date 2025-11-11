package com.example.kslingo.data.repository

import android.content.Context
import com.example.kslingo.R
import com.example.kslingo.data.model.Lesson
import com.example.kslingo.data.model.LessonCategory
import com.example.kslingo.data.model.LessonContent
import com.example.kslingo.data.model.LessonType

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
                lessons = alphabetLessons,
                totalLessons = alphabetLessons.size,
                completedLessons = completedAlphabets,
                isLocked = false
            ),
            LessonCategory(
                id = "numbers",
                title = "Numbers",
                description = "Learn to sign numbers 0–10",
                iconRes = R.drawable.sign_1,
                color = 0xFF4CAF50,
                lessons = numberLessons,
                totalLessons = numberLessons.size,
                completedLessons = completedNumbers,
                isLocked = completedAlphabets < alphabetLessons.size
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
                        description = "Make a fist with your thumb resting on the side of your index finger. Palm facing forward."
                    )
                )
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
                )
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
                        description = "Curve your hand to form a C shape with fingers slightly apart. Palm faces sideways."
                    )
                )
            ),
            Lesson(
                id = "alphabet_d",
                categoryId = "alphabets",
                title = "Letter D",
                description = "Learn the sign for letter D",
                type = LessonType.ALPHABET,
                content = listOf(
                    LessonContent(
                        sign = "D",
                        imageRes = R.drawable.sign_d,
                        description = "Raise your index finger, keep others curled in, touch thumb to middle finger. Palm faces outward."
                    )
                )
            ),
            Lesson(
                id = "alphabet_e",
                categoryId = "alphabets",
                title = "Letter E",
                description = "Learn the sign for letter E",
                type = LessonType.ALPHABET,
                content = listOf(
                    LessonContent(
                        sign = "E",
                        imageRes = R.drawable.sign_e,
                        description = "Curl your fingers to touch your thumb forming a small O shape, palm facing forward."
                    )
                )
            ),
            Lesson(
                id = "alphabet_f",
                categoryId = "alphabets",
                title = "Letter F",
                description = "Learn the sign for letter F",
                type = LessonType.ALPHABET,
                content = listOf(
                    LessonContent(
                        sign = "F",
                        imageRes = R.drawable.sign_f,
                        description = "Touch the tip of your thumb and index finger to form a circle, keep other fingers up. Palm facing forward."
                    )
                )
            ),
            Lesson(
                id = "alphabet_g",
                categoryId = "alphabets",
                title = "Letter G",
                description = "Learn the sign for letter G",
                type = LessonType.ALPHABET,
                content = listOf(
                    LessonContent(
                        sign = "G",
                        imageRes = R.drawable.sign_g,
                        description = "Hold your index finger and thumb parallel, palm facing sideways as if showing a small object."
                    )
                )
            ),
            Lesson(
                id = "alphabet_h",
                categoryId = "alphabets",
                title = "Letter H",
                description = "Learn the sign for letter H",
                type = LessonType.ALPHABET,
                content = listOf(
                    LessonContent(
                        sign = "H",
                        imageRes = R.drawable.sign_h,
                        description = "Extend index and middle fingers together, palm facing sideways, as if pointing horizontally."
                    )
                )
            ),
            Lesson(
                id = "alphabet_i",
                categoryId = "alphabets",
                title = "Letter I",
                description = "Learn the sign for letter I",
                type = LessonType.ALPHABET,
                content = listOf(
                    LessonContent(
                        sign = "I",
                        imageRes = R.drawable.sign_i,
                        description = "Extend your little finger, keep others curled in with thumb over them. Palm facing forward."
                    )
                )
            )
        )

        return lessonStateRepository.getUnlockedLessons(allLessons).map { lesson ->
            lesson.copy(isCompleted = lessonStateRepository.isLessonCompleted(lesson.id))
        }
    }

    fun getNumberLessons(): List<Lesson> {
        val allLessons = listOf(
            Lesson("number_1", "numbers", "Number 1", "Learn to sign one", LessonType.NUMBER,
                listOf(LessonContent("1", R.drawable.sign_1, "Extend index finger, others curled inward."))),
            Lesson("number_2", "numbers", "Number 2", "Learn to sign two", LessonType.NUMBER,
                listOf(LessonContent("2", R.drawable.sign_2, "Extend index and middle fingers, palm facing forward."))),
            Lesson("number_3", "numbers", "Number 3", "Learn to sign three", LessonType.NUMBER,
                listOf(LessonContent("3", R.drawable.sign_3, "Extend thumb, index, and middle fingers."))),
            Lesson("number_4", "numbers", "Number 4", "Learn to sign four", LessonType.NUMBER,
                listOf(LessonContent("4", R.drawable.sign_4, "Extend all fingers except thumb."))),
            Lesson("number_5", "numbers", "Number 5", "Learn to sign five", LessonType.NUMBER,
                listOf(LessonContent("5", R.drawable.sign_5, "Open all fingers with palm facing outward."))),
            Lesson("number_6", "numbers", "Number 6", "Learn to sign six", LessonType.NUMBER,
                listOf(LessonContent("6", R.drawable.sign_6, "Touch thumb to pinky finger, others extended."))),
            Lesson("number_7", "numbers", "Number 7", "Learn to sign seven", LessonType.NUMBER,
                listOf(LessonContent("7", R.drawable.sign_7, "Touch thumb to ring finger, others extended."))),
            Lesson("number_8", "numbers", "Number 8", "Learn to sign eight", LessonType.NUMBER,
                listOf(LessonContent("8", R.drawable.sign_8, "Touch thumb to middle finger, others extended."))),
            Lesson("number_9", "numbers", "Number 9", "Learn to sign nine", LessonType.NUMBER,
                listOf(LessonContent("9", R.drawable.sign_9, "Touch thumb to index finger forming small circle."))),
            Lesson("number_10", "numbers", "Number 10", "Learn to sign ten", LessonType.NUMBER,
                listOf(LessonContent("10", R.drawable.sign_10, "Make a fist and shake it side to side slightly.")))
        )

        return lessonStateRepository.getUnlockedLessons(allLessons).map { lesson ->
            lesson.copy(isCompleted = lessonStateRepository.isLessonCompleted(lesson.id))
        }
    }

    fun markLessonCompleted(lessonId: String) {
        lessonStateRepository.markLessonCompleted(lessonId)
    }
    fun getLessonsForCategory(categoryID: String): List<Lesson>{
        return getLessonCategories().find{ it.id == categoryID }?.lessons ?: emptyList()
    }
    fun getNextLesson(currentLessonId: String): Lesson? {
        val allLessons = getAlphabetLessons() + getNumberLessons()
        val nextLessonId = lessonStateRepository.getNextLessonId(currentLessonId, allLessons)
        return allLessons.find { it.id == nextLessonId }
    }
    }


