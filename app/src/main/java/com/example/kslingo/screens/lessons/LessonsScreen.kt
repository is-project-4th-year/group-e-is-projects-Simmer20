package com.example.kslingo.screens.lessons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kslingo.data.model.LessonCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonsScreen(navController: NavController, lessonViewModel: LessonViewModel = viewModel()) {
    // Get the categories list as a state from the ViewModel.
    // The UI will automatically recompose when this state changes.
    val categories by lessonViewModel.lessonCategories.collectAsState()

    // This will re-run the data loading logic every time the screen is displayed.
    // This is useful to ensure progress is updated after completing a lesson.
    LaunchedEffect(Unit) {
        lessonViewModel.loadLessonsWithProgress()
    }

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
            Text(
                text = "Choose a Category",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(16.dp)
            )

            Text(
                text = "Complete a category to unlock the next one!",
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (categories.isEmpty()) {
                // Show a loading indicator while the data is being fetched.
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // Once data is available, display the list of categories.
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
                                    // Navigate to the list of lessons within that category
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonCategoryCard(
    category: LessonCategory,
    onCategoryClick: () -> Unit
) {
    // Calculate progress based on the dynamic data from the ViewModel.
    val progress = if (category.totalLessons > 0) {
        category.completedLessons.toFloat() / category.totalLessons.toFloat()
    } else {
        0f
    }

    val cardColor = if (category.isLocked) Color.White.copy(alpha = 0.7f) else Color.White

    Card(
        onClick = onCategoryClick,
        enabled = !category.isLocked, // The card is not clickable if locked
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (category.isLocked) 1.dp else 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            val iconColor = if (category.isLocked) Color.Gray else Color(category.color)
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (category.isLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.Gray,
                        modifier = Modifier.size(30.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = category.iconRes),
                        contentDescription = category.title,
                        tint = iconColor,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.size(16.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.title,
                    color = if (category.isLocked) Color.Gray else Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = category.description,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bar and Text
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Progress", color = Color.Gray, fontSize = 12.sp)
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
                        color = iconColor,
                        trackColor = Color.LightGray.copy(alpha = 0.3f)
                    )
                }
            }

            // Arrow Icon (only shows if not locked)
            if (!category.isLocked) {
                Spacer(modifier = Modifier.size(8.dp))
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start",
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
