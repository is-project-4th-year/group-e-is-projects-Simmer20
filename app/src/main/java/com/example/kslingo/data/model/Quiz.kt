package com.example.kslingo.data.model

data class Quiz(
    val id: String,
    val title: String,
    val description: String,
    val type: QuizType,
    val categoryId: String? = null,
    val questions: List<QuizQuestion>,
    val totalQuestions: Int = 5,
    val timeLimit: Int? = 60,
    val passingScore: Int = 70
)

enum class QuizType {
    TOPICAL,
    MIXED
}

data class QuizQuestion(
    val id: String,
    val question: String,
    val mediaType: MediaType,
    val mediaResource: Int,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String
)

enum class MediaType {
    IMAGE, VIDEO
}

data class QuizResult(
    val quizId: String,
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val timeSpent: Long, // in seconds
    val date: Long = System.currentTimeMillis()
)
