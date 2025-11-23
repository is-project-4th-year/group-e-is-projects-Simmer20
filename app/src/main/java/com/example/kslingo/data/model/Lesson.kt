package com.example.kslingo.data.model

data class Lesson (
    val id: String,
    val categoryId: String,
    val title: String,
    val description: String,
    val type: LessonType,
    val content: List<LessonContent>,
    val isCompleted: Boolean = false,
    val isLocked: Boolean = true
)

enum class LessonType {
    ALPHABET, NUMBER, GREETINGS, PHRASES, COLORS, FAMILY, LANGUAGES
}

data class LessonContent (
    val sign: String,
    val imageRes: Int,
    val description: String,

)