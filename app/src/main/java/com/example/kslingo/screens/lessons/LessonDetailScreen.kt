package com.example.kslingo.screens.lessons

import android.content.Context
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kslingo.R
import com.example.kslingo.data.model.Lesson
import com.example.kslingo.data.repository.LessonsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonDetailScreen(
    navController: NavController,
    lessonId: String?
) {
    val context = LocalContext.current
    val lessonsRepository = remember { LessonsRepository(context) }

    // Find the lesson by ID
    val lesson = lessonsRepository.getAlphabetLessons().find { it.id == lessonId }
        ?: lessonsRepository.getNumberLessons().find { it.id == lessonId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = lesson?.title ?: "Lesson",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6A35EE)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        // Navigate back to category screen
                        navController.navigate("lesson_category/${lesson?.categoryId}") {
                            popUpTo("lesson_category/${lesson?.categoryId}") { inclusive = false }
                        }
                    }) {
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
        if (lesson != null) {
            LessonContent(
                lesson = lesson,
                lessonsRepository = lessonsRepository,
                navController = navController,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Lesson not found")
            }
        }
    }
}

@Composable
fun LessonContent(
    lesson: Lesson,
    lessonsRepository: LessonsRepository,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val lessonContent = lesson.content.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
            .verticalScroll(rememberScrollState())
    ) {
        // Sign Display Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Sign Letter
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6A35EE)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = lessonContent?.sign ?: "?",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Sign Image (Placeholder)
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.LightGray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (lessonContent?.imageRes != null) {
                        Image(
                            painter = painterResource(id = lessonContent.imageRes),
                            contentDescription = "KSL sign for ${lessonContent.sign}",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Fallback placeholder
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Video placeholder",
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Sign Demonstration",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "(Image/Video Placeholder)",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Kenyan Sign Language",
                    color = Color(0xFF6A35EE),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Description Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "How to sign",
                        tint = Color(0xFF6A35EE),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "How to Sign",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = lessonContent?.description ?: "Description not available",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            }
        }
        // Practice Section - UPDATED with proper completion logic
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (lesson.isCompleted) "Lesson Completed!" else "Ready to Practice?",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = if (lesson.isCompleted) {
                        "Great job! You've completed this lesson."
                    } else {
                        "Try making the sign yourself. Use your camera to get feedback."
                    },
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!lesson.isCompleted) {
                        // Practice with Camera Button
                        Button(
                            onClick = { /* Navigate to camera practice */ },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF6A35EE)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = ButtonDefaults.outlinedButtonBorder
                        ) {
                            Text("Practice with Camera")
                        }

                        // Mark Complete Button
                        Button(
                            onClick = {
                                // Mark lesson as completed
                                lessonsRepository.markLessonCompleted(lesson.id)

                                // Get the next lesson
                                val nextLesson = lessonsRepository.getNextLesson(lesson.id)

                                if (nextLesson != null) {
                                    // Navigate to next lesson
                                    navController.navigate("lesson_detail/${nextLesson.id}") {
                                        popUpTo("lesson_detail/${lesson.id}") { inclusive = true }
                                    }
                                } else {
                                    // No next lesson, go back to category
                                    navController.navigate("lesson_category/${lesson.categoryId}") {
                                        popUpTo("lesson_detail/${lesson.id}") { inclusive = true }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6A35EE),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Complete",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Mark Complete")
                            }
                        }
                    } else {
                        // Lesson already completed - show different options
                        Button(
                            onClick = {
                                navController.navigate("lesson_category/${lesson.categoryId}") {
                                    popUpTo("lesson_detail/${lesson.id}") { inclusive = true }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6A35EE),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Back to Lessons")
                        }

                        Button(
                            onClick = {
                                // Practice again
                                // Could reset progress or just go to practice screen
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF6A35EE)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = ButtonDefaults.outlinedButtonBorder
                        ) {
                            Text("Practice Again")
                        }
                    }
                }

                // Show next lesson preview if available
                if (!lesson.isCompleted) {
                    val nextLesson = lessonsRepository.getNextLesson(lesson.id)
                    if (nextLesson != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Next: ${nextLesson.title}",
                            color = Color(0xFF6A35EE),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}