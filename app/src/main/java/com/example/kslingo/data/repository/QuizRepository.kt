package com.example.kslingo.data.repository

import android.content.Context
import androidx.compose.ui.graphics.Color
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
        val hasCompletedPhrases = completedLessons.any { it.startsWith("phrase") }
        val hasCompletedColors = completedLessons.any { it.startsWith("color") }
        val hasCompletedFamily = completedLessons.any { it.startsWith("family") }
        val hasCompletedLanguage = completedLessons.any { it.startsWith("language") }

        val quizzes = mutableListOf<Quiz>()

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
        if (hasCompletedPhrases) {
            quizzes.add(
                Quiz(
                    id = "quiz_phrases",
                    title = "Phrases Quiz",
                    description = "Test your knowledge of KSL phrases",
                    type = QuizType.TOPICAL,
                    categoryId = "phrases",
                    questions = getMixedQuestions(),
                    totalQuestions = min(5, completedLessons.count { it.startsWith("phrase") }),
                    timeLimit = 30,
                    passingScore = 70
                )
            )
        }
        if (hasCompletedColors) {
            quizzes.add(
                Quiz(
                    id = "quiz_colors",
                    title = "Colors Quiz",
                    description = "Test your knowledge of KSL colors",
                    type = QuizType.TOPICAL,
                    categoryId = "colors",
                    questions = getMixedQuestions(),
                    totalQuestions = min(5, completedLessons.count { it.startsWith("color") }),
                    timeLimit = 30,
                    passingScore = 70
                )
            )
        }
        if (hasCompletedFamily) {
            quizzes.add(
                Quiz(
                    id = "quiz_family",
                    title = "Family Quiz",
                    description = "Test your knowledge of KSL family members",
                    type = QuizType.TOPICAL,
                    categoryId = "family",
                    questions = getMixedQuestions(),
                    totalQuestions = min(5, completedLessons.count { it.startsWith("family") }),
                    timeLimit = 30,
                    passingScore = 70
                )
            )
        }
        if (hasCompletedLanguage) {
            quizzes.add(
                Quiz(
                    id = "quiz_language",
                    title = "Language Quiz",
                    description = "Test your knowledge of KSL language",
                    type = QuizType.TOPICAL,
                    categoryId = "language",
                    questions = getMixedQuestions(),
                    totalQuestions = min(5, completedLessons.count { it.startsWith("language") }),
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
        if (completedLessons.any { it.startsWith("phrase") }) {
            questions.addAll(getPhraseQuestions().shuffled().take(1))
        }
        if (completedLessons.any { it.startsWith("color") }) {
            questions.addAll(getColorsQuestions().shuffled().take(1))
        }
        if (completedLessons.any { it.startsWith("family") }) {
            questions.addAll(getFamilyQuestions().shuffled().take(1))
        }
        if (completedLessons.any { it.startsWith("language") }) {
            questions.addAll(getLanguageQuestions().shuffled().take(1))
        }

        return questions.shuffled()
    }
    fun getSmartQuestionsForQuiz(quizId: String): List<QuizQuestion> {
        val completedLessons = lessonStateRepository.getCompletedLessons()

        return when (quizId) {
            "quiz_alphabets" -> {
                getAlphabetQuestions().filter { question ->
                    val letter = question.correctAnswer.toLowerCase()
                    completedLessons.any { it.contains(letter) }
                }.shuffled()
            }
            "quiz_numbers" -> {
                getNumberQuestions().filter { question ->
                    val number = question.correctAnswer
                    completedLessons.any { it.contains(number) }
                }.shuffled()
            }
            "quiz_phrases" -> {
                getPhraseQuestions().filter { question ->
                    val phrase = question.correctAnswer.toLowerCase()
                    completedLessons.any { it.contains(phrase) }
                }.shuffled()
            }
            "quiz_colors" -> {
                getColorsQuestions().filter { question ->
                    val color = question.correctAnswer.toLowerCase()
                    completedLessons.any { it.contains(color) }
                }.shuffled()
            }
            "quiz_family" -> {
                getFamilyQuestions().filter { question ->
                    val familyMember = question.correctAnswer.toLowerCase()
                    completedLessons.any { it.contains(familyMember) }
                }.shuffled()
            }
            "quiz_language" -> {
                getLanguageQuestions().filter { question ->
                    val language = question.correctAnswer.toLowerCase()
                    completedLessons.any { it.contains(language) }
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
                mediaResource = R.drawable.sign_a,
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
                mediaResource = R.drawable.sign_1,
                options = listOf("1", "2", "3", "4"),
                correctAnswer = "1",
                explanation = "This is the sign for number 1 - index finger extended."
            ),
            QuizQuestion(
                id = "q_num_2",
                question = "What number is being signed?",
                mediaType = MediaType.IMAGE,
                mediaResource = R.drawable.sign_2,
                options = listOf("1", "2", "3", "4"),
                correctAnswer = "2",
                explanation = "This is the sign for number 2 - index and middle finger extended."
            ),
            QuizQuestion(
                id = "q_num_3",
                question = "How many fingers are shown in this sign?",
                mediaType = MediaType.IMAGE,
                mediaResource = R.drawable.sign_3,
                options = listOf("2", "3", "4", "5"),
                correctAnswer = "3",
                explanation = "The sign shows 3 fingers extended for number 3."
            ),
            QuizQuestion(
                id = "q_num_4",
                question = "What number is being signed?",
                mediaType = MediaType.IMAGE,
                mediaResource = R.drawable.sign_4,
                options = listOf("1", "2", "3", "4"),
                correctAnswer = "4",
                explanation = "This is the sign for number 4 - ring and little finger extended."
            )
        )
    }
    private fun getPhraseQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                id = "q_phrase_1",
                question = "What phrase is being signed?",
                mediaType = MediaType.IMAGE,
                mediaResource = R.drawable.sign_stop,
                options = listOf("Stop", "Know", "Please", "Sorry"),
                correctAnswer = "Stop",
                explanation = "This is the sign for the phrase 'Stop'."
            ),
            QuizQuestion(
                id = "q_phrase_2",
                question = "What phrase is being signed?",
                mediaType = MediaType.IMAGE,
                mediaResource = R.drawable.sign_please,
                options = listOf("Stop", "Know", "Please", "Sorry"),
                correctAnswer = "Please",
                explanation = "This is the sign for the phrase 'Please'."
            ),
            QuizQuestion(
                id = "q_phrase_3",
                question = "What phrase is being signed?",
                mediaType = MediaType.IMAGE,
                mediaResource = R.drawable.sign_sorry,
                options = listOf("Stop", "Know", "Please", "Sorry"),
                correctAnswer = "Sorry",
                explanation = "This is the sign for the phrase 'Sorry'."
            )
        )
    }
    private fun getColorsQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                id = "q_color_1",
                question = "What color is being signed?",
                mediaType = MediaType.IMAGE,
                mediaResource = R.drawable.sign_red,
                options = listOf("Red", "Green", "Black", "White"),
                correctAnswer = "Red",
                explanation = "This is the sign for color red."
            ),
            QuizQuestion(
                id = "q_color_2",
                question = "What color is being signed?",
                mediaType = MediaType.IMAGE,
                mediaResource = R.drawable.sign_green,
                options = listOf("Red", "Green", "Black", "White"),
                correctAnswer = "Green",
                explanation = "This is the sign for color green."
            ),
            QuizQuestion(
                id = "q_color_3",
                question = "What color is being signed?",
                mediaType = MediaType.IMAGE,
                mediaResource = R.drawable.sign_black,
                options = listOf("Red", "Green", "Black", "White"),
                correctAnswer = "Black",
                explanation = "This is the sign for color black."
            )
        )
    }
    private fun getFamilyQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                id = "q_family_1",
                question = "What family member is being signed?",
                mediaType = MediaType.IMAGE,
                mediaResource = R.drawable.sign_mother,
                options = listOf("Mother", "Father", "Boy", "Girl"),
                correctAnswer = "Mother",
                explanation = "This is the sign for the mother."
            ),
            QuizQuestion(
                id = "q_family_2",
                question = "What family member is being signed?",
                mediaType = MediaType.IMAGE,
                mediaResource = R.drawable.sign_father,
                options = listOf("Mother", "Father", "Boy", "Girl"),
                correctAnswer = "Father",
                explanation = "This is the sign for the father."
            ),
            QuizQuestion(
                id = "q_family_3",
                question = "What family member is being signed?",
                mediaType = MediaType.IMAGE,
                mediaResource = R.drawable.sign_boy,
                options = listOf("Mother", "Father", "Boy", "Girl"),
                correctAnswer = "Boy",
                explanation = "This is the sign for the boy."
            )
        )
    }
    private fun getLanguageQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                id = "q_language_1",
                question = "What language is being signed?",
                mediaType = MediaType.IMAGE,
                mediaResource = R.drawable.sign_english,
                options = listOf("English", "Swahili", "Spanish", "French"),
                correctAnswer = "English",
                explanation = "This is the sign for the English language."
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
