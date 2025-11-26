// In ProgressScreen.kt
package com.example.kslingo.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kslingo.data.model.LearningStats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(navController: NavController, progressViewModel: ProgressViewModel = viewModel()) {
    // Collect the user profile state from the ViewModel
    val userProfile by progressViewModel.userProfile.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Progress", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8F8F8),
                    titleContentColor = Color(0xFF6A35EE)
                )
            )
        }
    ) { paddingValues ->
        // Check for loading state (if profile is null initially)
        if (userProfile == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Once data is loaded, display the main content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF8F8F8))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // User Level & XP Section
                item {
                    userProfile?.learningStats?.let { stats ->
                        LevelProgressCard(stats.level, stats.xp)
                    }
                }

                // Streak Section
                item {
                    userProfile?.learningStats?.let { stats ->
                        StreakCard(stats.currentStreak, stats.longestStreak)
                    }
                }

                // General Stats Section
                item {
                    userProfile?.learningStats?.let { stats ->
                        OverallStatsCard(stats)
                    }
                }
            }
        }
    }
}

@Composable
fun LevelProgressCard(level: Int, xp: Int) {
    val xpForNextLevel = level * 100 // Example logic: 100 XP per level
    val progress = xp.toFloat() / xpForNextLevel.toFloat()

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Your Level",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6A35EE)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = level.toString(),
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Experience Points: $xp / $xpForNextLevel", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF6A35EE),
                        trackColor = Color.LightGray.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
fun StreakCard(currentStreak: Int, longestStreak: Int) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = "Streak",
                tint = Color(0xFFFF9800),
                modifier = Modifier.size(40.dp)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$currentStreak Days",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800)
                )
                Text(text = "Current Streak", color = Color.Gray, fontSize = 14.sp)
            }
            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp), color = Color.Gray.copy(alpha = 0.5f)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$longestStreak Days",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Text(text = "Longest Streak", color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun OverallStatsCard(stats: LearningStats) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Overall Statistics",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Top
            ) {
                StatItem(
                    icon = Icons.Default.CheckCircle,
                    value = stats.totalLessonsCompleted.toString(),
                    label = "Lessons Done"
                )
                StatItem(
                    icon = Icons.Default.Timer,
                    value = "${stats.totalPracticeTime} min",
                    label = "Practice Time"
                )
                StatItem(
                    icon = Icons.Default.Leaderboard,
                    value = "${stats.accuracy.toInt()}%",
                    label = "Accuracy"
                )
            }
        }
    }
}

@Composable
fun StatItem(icon: ImageVector, value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.width(90.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF6A35EE),
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ContinueLearningCard(
    lessonName: String,
    progress: Int,
    onContinueClicked: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF6A35EE)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onContinueClicked() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // The text content from your snippet
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Continue Learning",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = lessonName,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Linear Progress Indicator
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = progress / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color.White, // Progress bar color
                    trackColor = Color.White.copy(alpha = 0.3f) // Background of the bar
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$progress% completed",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.End) // Align percentage to the right
                )
            }
            // Add a "play" icon to suggest it's clickable
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Default.PlayCircle,
                contentDescription = "Continue",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}
