package com.example.kslingo.data.model

data class LessonCategory(
    val id: String,
    val title:String,
    val description: String,
    val iconRes: Int,
    val color: Long,
    val totalLessons: Int,
    val completedLessons: Int,
    val isLocked: Boolean = false
)