package com.example.kslingo.data.repository

import android.content.Context
import com.example.kslingo.data.model.QuizResult

class QuizStateRepository(private val context: Context) {
    private val sharedPref = context.getSharedPreferences("quiz_progress", Context.MODE_PRIVATE)

    fun saveQuizResult(result: QuizResult) {
        val results = getQuizResults().toMutableList()
        results.add(result)

        // Save as JSON string (simplified - in real app use proper serialization)
        val resultsJson = results.joinToString("|") { "${it.quizId},${it.score},${it.date}" }
        sharedPref.edit().putString("quiz_results", resultsJson).apply()
    }

    fun getQuizResults(): List<QuizResult> {
        val resultsJson = sharedPref.getString("quiz_results", "") ?: ""
        return if (resultsJson.isNotEmpty()) {
            resultsJson.split("|").mapNotNull { data ->
                val parts = data.split(",")
                if (parts.size == 3) {
                    QuizResult(
                        quizId = parts[0],
                        score = parts[1].toIntOrNull() ?: 0,
                        totalQuestions = 5, // Default
                        correctAnswers = 0, // Would need to store this
                        timeSpent = 0L,
                        date = parts[2].toLongOrNull() ?: 0L
                    )
                } else null
            }
        } else {
            emptyList()
        }
    }

    fun getBestScore(quizId: String): Int {
        return getQuizResults()
            .filter { it.quizId == quizId }
            .maxOfOrNull { it.score } ?: 0
    }

    fun isQuizPassed(quizId: String): Boolean {
        val quiz = QuizRepository(context).getQuizById(quizId)
        val bestScore = getBestScore(quizId)
        return bestScore >= (quiz?.passingScore ?: 70)
    }
}