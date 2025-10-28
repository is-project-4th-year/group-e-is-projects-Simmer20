package com.example.kslingo.screens.lessons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kslingo.data.model.Lesson
import com.example.kslingo.data.repository.LessonsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonCategoryScreen(
    navController: NavController,
    categoryId: String?
) {
    val context = LocalContext.current
    val lessonsRepository = remember { LessonsRepository(context) }

    val lessons = when (categoryId) {
        "alphabets" -> lessonsRepository.getAlphabetLessons()
        "numbers" -> lessonsRepository.getNumberLessons()
        else -> emptyList()
    }

    val categoryTitle = when (categoryId) {
        "alphabets" -> "KSL Alphabets"
        "numbers" -> "KSL Numbers"
        else -> "Lessons"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = categoryTitle,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6A35EE)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF6A35EE)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F8F8))
        ) {
            // Progress Summary
            val completedLessons = lessons.count { it.isCompleted }
            val totalLessons = lessons.size

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF6A35EE)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Your Progress",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$completedLessons/$totalLessons lessons completed",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                }
            }

            // Lessons List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(lessons) { lesson ->
                    LessonCard(
                        lesson = lesson,
                        onLessonClick = {
                            if (!lesson.isLocked) {
                                navController.navigate("lesson_detail/${lesson.id}")
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun LessonCard(
    lesson: Lesson,
    onLessonClick: () -> Unit
) {
    Card(
        onClick = onLessonClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lesson Number/Icon
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            lesson.isCompleted -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                            lesson.isLocked -> Color.Gray.copy(alpha = 0.1f)
                            else -> Color(0xFF6A35EE).copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    lesson.isCompleted -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    lesson.isLocked -> {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    else -> {
                        Text(
                            text = lesson.title.takeLast(1), // Shows "A", "B", etc.
                            color = Color(0xFF6A35EE),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.size(16.dp))

            // Lesson Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = lesson.title,
                    color = if (lesson.isLocked) Color.Gray else Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = lesson.description,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Action Icon
            if (!lesson.isLocked && !lesson.isCompleted) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start Lesson",
                    tint = Color(0xFF6A35EE),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}