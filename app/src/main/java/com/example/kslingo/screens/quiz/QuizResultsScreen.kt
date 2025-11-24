package com.example.kslingo.screens.quiz

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.kslingo.data.model.QuizResult
import com.example.kslingo.data.repository.QuizRepository
import com.example.kslingo.data.repository.QuizStateRepository

@Composable
fun QuizResultsScreen(
    navController: NavController,
    quizId: String?,
    score: Int,
    totalQuestions: Int
) {
    val context = LocalContext.current
    val quizRepository = remember { QuizRepository(context) }
    val quizStateRepository = remember { QuizStateRepository(context) }

    val quiz = quizRepository.getQuizById(quizId ?: "")
    val percentage = if (totalQuestions > 0) (score.toFloat() / totalQuestions.toFloat() * 100).toInt() else 0
    val isPassed = percentage >= (quiz?.passingScore ?: 70)
    val previousBest = quizStateRepository.getBestScore(quizId ?: "")

    // Save result when screen loads
    LaunchedEffect(Unit) {
        val result = QuizResult(
            quizId = quizId ?: "",
            score = percentage,
            totalQuestions = totalQuestions,
            correctAnswers = score,
            timeSpent = 0L // You can track this if you implement timing
        )
        quizStateRepository.saveQuizResult(result)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Result Icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    if (isPassed) Color(0xFF4CAF50).copy(alpha = 0.1f)
                    else Color(0xFFFF9800).copy(alpha = 0.1f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPassed) Icons.Default.Celebration else Icons.Default.Close,
                contentDescription = if (isPassed) "Passed" else "Failed",
                tint = if (isPassed) Color(0xFF4CAF50) else Color(0xFFF44336),
                modifier = Modifier.size(48.dp)
            )
        } // <<< *** FIX: THE BOX COMPOSABLE CORRECTLY ENDS HERE ***

        // All the content below is now correctly placed in the parent Column

        Spacer(modifier = Modifier.height(24.dp))

        // Result Title
        Text(
            text = if (isPassed) "Quiz Passed! 🎉" else "Keep Practicing",
            color = if (isPassed) Color(0xFF4CAF50) else Color(0xFFF44336),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = quiz?.title ?: "Quiz",
            color = Color.Gray,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Score Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp), // Adjusted padding slightly for better look
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$percentage%",
                    color = Color(0xFF6A35EE),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Your Score",
                    color = Color.Gray,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Correct Answers:",
                        color = Color.Gray
                    )
                    Text(
                        text = "$score/$totalQuestions",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Passing Score:",
                        color = Color.Gray
                    )
                    Text(
                        text = "${quiz?.passingScore ?: 70}%",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (previousBest > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Previous Best:",
                            color = Color.Gray
                        )
                        Text(
                            text = "$previousBest%",
                            color = Color(0xFFFF9800),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (percentage > previousBest && previousBest > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = "New record",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = "New Personal Best!",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f)) // Use weight to push buttons to the bottom

        // Feedback Message
        Text(
            text = when {
                percentage >= 90 -> "Outstanding! You're a KSL star! 🌟"
                percentage >= 80 -> "Excellent work! You're mastering KSL! 💫"
                isPassed -> "Good job! You passed the quiz! ✅"
                percentage >= 60 -> "Almost there! Review and try again. 📚"
                else -> "Keep practicing! You'll get better! 💪"
            },
            color = Color.Gray,
            fontSize = 16.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp)) // Adjust spacing

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    navController.navigate("quiz_questions/${quizId}") {
                        // Pop up to the quiz selection screen to avoid building up the backstack
                        popUpTo("quiz_selection")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6A35EE),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Quiz, // Consider Icons.Default.Refresh
                        contentDescription = "Retry",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Try Again", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.LightGray) // Use BorderStroke for outlined style
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Back to Home", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}