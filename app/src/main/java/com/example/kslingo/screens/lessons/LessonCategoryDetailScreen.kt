package com.example.kslingo.screens.lessons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kslingo.data.model.Lesson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonCategoryDetailScreen(
    navController: NavController,
    categoryId: String,
    viewModel: LessonCategoryDetailViewModel = viewModel()
) {
    // Collect the state from the ViewModel
    val lessons by viewModel.unlockedLessons.collectAsState()
    val completedCount by viewModel.completedLessonsCount.collectAsState()

    // Use LaunchedEffect to load data every time the screen is shown
    LaunchedEffect(key1 = categoryId) {
        viewModel.loadLessonsForCategory(categoryId)
    }

    val totalLessons = lessons.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lessons") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Progress Header
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                color = Color(0xFF6A35EE),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Your Progress",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        // Display the LIVE progress count
                        text = "$completedCount/$totalLessons lessons completed",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Lessons List
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(lessons) { lesson ->
                    LessonListItem(
                        lesson = lesson,
                        onLessonClick = {
                            // Only navigate if the lesson is not locked
                            if (!lesson.isLocked) {
                                navController.navigate("lesson/${lesson.id}")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LessonListItem(lesson: Lesson, onLessonClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onLessonClick, enabled = !lesson.isLocked),
        colors = CardDefaults.cardColors(
            containerColor = if (lesson.isLocked) Color.LightGray.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .let {
                        if (lesson.isLocked) it.background(Color.Gray.copy(alpha = 0.3f))
                        else it.background(MaterialTheme.colorScheme.primaryContainer)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (lesson.isLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.White
                    )
                } else {
                    Text(
                        text = lesson.title.first().toString(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Title and Description
            Column {
                Text(
                    text = lesson.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (lesson.isLocked) Color.Gray else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = lesson.description,
                    fontSize = 14.sp,
                    color = if (lesson.isLocked) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
