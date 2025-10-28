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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.kslingo.data.model.LessonCategory
import com.example.kslingo.data.repository.LessonsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonsScreen(navController: NavController) {
    val context = LocalContext.current
    val lessonsRepository = remember { LessonsRepository(context) }
    val categories = lessonsRepository.getLessonCategories()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Learn KSL",
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
            // Header
            Text(
                text = "Choose a Category",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(16.dp)
            )

            Text(
                text = "Complete lessons to unlock new categories",
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Categories List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(categories) { category ->
                    LessonCategoryCard(
                        category = category,
                        onCategoryClick = {
                            if (!category.isLocked) {
                                navController.navigate("lesson_category/${category.id}")
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
fun LessonCategoryCard(
    category: LessonCategory,
    onCategoryClick: () -> Unit
) {
    val progress = if (category.totalLessons > 0) {
        category.completedLessons.toFloat() / category.totalLessons.toFloat()
    } else {
        0f
    }

    Card(
        onClick = onCategoryClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
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
            // Icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(category.color).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (category.isLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    // You can replace this with actual icons later
                    Text(
                        text = when (category.id) {
                            "alphabets" -> "ABC"
                            "numbers" -> "123"
                            else -> "👋"
                        },
                        color = Color(category.color),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.size(16.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category.title,
                        color = if (category.isLocked) Color.Gray else Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (category.isLocked) {
                        Spacer(modifier = Modifier.size(8.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = category.description,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Progress",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${category.completedLessons}/${category.totalLessons}",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(category.color),
                        trackColor = Color.LightGray.copy(alpha = 0.3f)
                    )
                }
            }

            // Arrow or Lock
            if (!category.isLocked) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start",
                    tint = Color(category.color),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}