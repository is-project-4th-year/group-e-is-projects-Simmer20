package com.example.kslingo.data.repository

import android.content.Context
import com.example.kslingo.R
import com.example.kslingo.data.model.MediaType
import com.example.kslingo.data.model.Quiz
import com.example.kslingo.data.model.QuizQuestion
import com.example.kslingo.data.model.QuizType
import kotlin.math.min

class QuizRepository(private val context: Context) {
    private val lessonStateRepository = LessonStateRepository(context)

    fun getAvailableQuizzes(): List<Quiz> {
        val completedLessons = lessonStateRepository.getCompletedLessons()
        val hasCompletedAlphabets = completedLessons.any { it.startsWith("alphabet") }
        val hasCompletedNumbers = completedLessons.any { it.startsWith("number") }

        val quizzes = mutableListOf<Quiz>()

        // Only show alphabets quiz if user has learned some alphabets
        if (hasCompletedAlphabets) {
            quizzes.add(
                Quiz(
                    id = "quiz_alphabets",
                    title = "Alphabets Quiz",
                    description = "Test your knowledge of KSL alphabet signs",
                    type = QuizType.TOPICAL,
                    categoryId = "alphabets",
                    questions = getAlphabetQuestions(),
                    totalQuestions = min(5, completedLessons.count { it.startsWith("alphabet") }),
                    timeLimit = 30,
                    passingScore = 70
                )
            )
        }

        // Only show numbers quiz if user has learned some numbers
        if (hasCompletedNumbers) {
            quizzes.add(
                Quiz(
                    id = "quiz_numbers",
                    title = "Numbers Quiz",
                    description = "Test your knowledge of KSL number signs",
                    type = QuizType.TOPICAL,
                    categoryId = "numbers",
                    questions = getNumberQuestions(),
                    totalQuestions = min(5, completedLessons.count { it.startsWith("number") }),
                    timeLimit = 30,
                    passingScore = 70
                )
            )
        }

        // Mixed quiz only available if user has completed at least 3 lessons
        if (completedLessons.size >= 3) {
            quizzes.add(
                Quiz(
                    id = "quiz_mixed",
                    title = "Mixed Quiz",
                    description = "Random questions from all learned categories",
                    type = QuizType.MIXED,
                    questions = getMixedQuestionsBasedOnProgress(),
                    totalQuestions = min(10, completedLessons.size),
                    timeLimit = 45,
                    passingScore = 70
                )
            )
        }

        return quizzes
    }

    private fun getMixedQuestionsBasedOnProgress(): List<QuizQuestion> {
        val completedLessons = lessonStateRepository.getCompletedLessons()
        val questions = mutableListOf<QuizQuestion>()

        // Add questions only from completed lessons
        if (completedLessons.any { it.startsWith("alphabet") }) {
            questions.addAll(getAlphabetQuestions().shuffled().take(3))
        }

        if (completedLessons.any { it.startsWith("number") }) {
            questions.addAll(getNumberQuestions().shuffled().take(2))
        }

        return questions.shuffled()
    }
    fun getSmartQuestionsForQuiz(quizId: String): List<QuizQuestion> {
        val completedLessons = lessonStateRepository.getCompletedLessons()

        return when (quizId) {
            "quiz_alphabets" -> {
                // Only include questions for learned alphabet letters
                getAlphabetQuestions().filter { question ->
                    val letter = question.correctAnswer.toLowerCase()
                    completedLessons.any { it.contains(letter) }
                }.shuffled()
            }
            "quiz_numbers" -> {
                // Only include questions for learned numbers
                getNumberQuestions().filter { question ->
                    val number = question.correctAnswer
                    completedLessons.any { it.contains(number) }
                }.shuffled()
            }
            "quiz_mixed" -> getMixedQuestionsBasedOnProgress()
            else -> emptyList()
        }
    }
    private fun getAlphabetQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                id = "q_alpha_1",
                question = "What letter is being signed?",
                mediaType = MediaType.IMAGE,
                mediaResource = R.drawable.sign_a, // Use your sign images
                options = listOf("A", "B", "C", "D"),
                correctAnswer = "A",
                explanation = "This is the sign for letter A - a fist with thumb resting on the side."
            ),
            QuizQuestion(
                id = "q_alpha_2",
                question = "What letter is being signed?",
                mediaType = MediaType.IMAGE,
                mediaResource = R.drawable.sign_b,
                options = listOf("A", "B", "C", "D"),
                correctAnswer = "B",
                explanation = "This is the sign for letter B - flat hand with fingers together."
            ),
            QuizQuestion(
                id = "q_alpha_3",
                question = "What letter is being signed?",
                mediaType = MediaType.IMAGE,
                mediaResource = R.drawable.sign_c,
                options = listOf("A", "B", "C", "D"),
                correctAnswer = "C",
                explanation = "This is the sign for letter C - curved hand forming a C shape."
            ),
            // Add more alphabet questions...
            QuizQuestion(
                id = "q_alpha_4",
                question = "Which word starts with the signed letter?",
                mediaType = MediaType.IMAGE,
                mediaResource = R.drawable.sign_a,
                options = listOf("Apple", "Book", "Cat", "Dog"),
                correctAnswer = "Apple",
                explanation = "The sign shows letter A, and 'Apple' starts with A."
            )
        )
    }

    private fun getNumberQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                id = "q_num_1",
                question = "What number is being signed?",
                mediaType = MediaType.IMAGE,
                mediaResource = R.drawable.sign_a, // You'll need to create these
                options = listOf("1", "2", "3", "4"),
                correctAnswer = "1",
                explanation = "This is the sign for number 1 - index finger extended."
            ),
            QuizQuestion(
                id = "q_num_2",
                question = "What number is being signed?",
                mediaType = MediaType.IMAGE,
                mediaResource = R.drawable.sign_b,
                options = listOf("1", "2", "3", "4"),
                correctAnswer = "2",
                explanation = "This is the sign for number 2 - index and middle finger extended."
            ),
            // Add more number questions...
            QuizQuestion(
                id = "q_num_3",
                question = "How many fingers are shown in this sign?",
                mediaType = MediaType.IMAGE,
                mediaResource = R.drawable.sign_c,
                options = listOf("2", "3", "4", "5"),
                correctAnswer = "3",
                explanation = "The sign shows 3 fingers extended for number 3."
            )
        )
    }

    private fun getMixedQuestions(): List<QuizQuestion> {
        return getAlphabetQuestions().take(3) + getNumberQuestions().take(2)
    }

    fun getQuizById(quizId: String): Quiz? {
        return getAvailableQuizzes().find { it.id == quizId }
    }
}