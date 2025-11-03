package com.example.kslingo.screens.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kslingo.data.model.Quiz
import com.example.kslingo.data.model.QuizQuestion
import com.example.kslingo.data.repository.QuizRepository
import kotlinx.coroutines.delay
import java.util.Timer
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizQuestionsScreen(
    navController: NavController,
    quizId: String?
) {
    val context = LocalContext.current
    val quizRepository = remember { QuizRepository(context) }

    val quiz = quizRepository.getQuizById(quizId ?: "")
    val questions = quizRepository.getSmartQuestionsForQuiz(quizId ?: "")

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var showResult by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(quiz?.timeLimit ?: 0) }
    var isQuizCompleted by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }

    val currentQuestion = questions.getOrNull(currentQuestionIndex)

    // Timer effect
    LaunchedEffect(key1 = currentQuestionIndex, key2 = quiz?.timeLimit) {
        if (quiz?.timeLimit != null) {
            timeLeft = quiz.timeLimit
            while (timeLeft > 0 && currentQuestionIndex == questions.indexOf(currentQuestion)) {
                delay(1000)
                timeLeft--
            }
            // Auto-submit if time runs out
            if (timeLeft == 0 && !showResult) {
                showResult = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = quiz?.title ?: "Quiz",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6A35EE)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        // Show confirmation dialog before exiting
                        navController.popBackStack()
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
        if (isQuizCompleted) {
            // Navigate to results screen
            LaunchedEffect(Unit) {
                navController.navigate("quiz_results/${quizId}/$score/${questions.size}") {
                    popUpTo("quiz_questions/${quizId}") { inclusive = true }
                }
            }
        } else if (currentQuestion != null) {
            QuizQuestionContent(
                quiz = quiz!!,
                question = currentQuestion,
                currentQuestionIndex = currentQuestionIndex,
                totalQuestions = questions.size,
                selectedAnswer = selectedAnswer,
                showResult = showResult,
                timeLeft = timeLeft,
                onAnswerSelected = { answer ->
                    if (!showResult) {
                        selectedAnswer = answer
                    }
                },
                onNextQuestion = {
                    if (selectedAnswer == currentQuestion.correctAnswer) {
                        score++
                    }

                    if (currentQuestionIndex < questions.size - 1) {
                        currentQuestionIndex++
                        selectedAnswer = null
                        showResult = false
                    } else {
                        isQuizCompleted = true
                    }
                },
                onSubmitAnswer = {
                    showResult = true
                },
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            // No questions available (user hasn't learned enough)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Not Enough Progress",
                        color = Color(0xFF6A35EE),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Complete more lessons to unlock this quiz",
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { navController.popBackStack() }
                    ) {
                        Text("Back to Quizzes")
                    }
                }
            }
        }
    }
}

@Composable
fun QuizQuestionContent(
    quiz: Quiz,
    question: QuizQuestion,
    currentQuestionIndex: Int,
    totalQuestions: Int,
    selectedAnswer: String?,
    showResult: Boolean,
    timeLeft: Int,
    onAnswerSelected: (String) -> Unit,
    onNextQuestion: () -> Unit,
    onSubmitAnswer: () -> Unit,
    modifier: Modifier = Modifier

) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
            .padding(16.dp)
    ) {
        // Progress and Timer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Question ${currentQuestionIndex + 1}/$totalQuestions",
                color = Color.Gray,
                fontSize = 14.sp
            )

            if (quiz.timeLimit != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Time left",
                        tint = if (timeLeft < 10) Color.Red else Color(0xFF6A35EE),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "${timeLeft}s",
                        color = if (timeLeft < 10) Color.Red else Color(0xFF6A35EE),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = (currentQuestionIndex + 1).toFloat() / totalQuestions.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Color(0xFF6A35EE),
            trackColor = Color.LightGray.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Question Card
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = question.question,
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Media Display
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    when (question.mediaType) {
                        com.example.kslingo.data.model.MediaType.IMAGE -> {
                            Image(
                                painter = painterResource(id = question.mediaResource),
                                contentDescription = "Sign demonstration",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        com.example.kslingo.data.model.MediaType.VIDEO -> {
                            // Video placeholder - you can integrate ExoPlayer here
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play video",
                                    tint = Color(0xFF6A35EE),
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "Video Demonstration",
                                    color = Color(0xFF6A35EE),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Options
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            question.options.forEach { option ->
                val isSelected = selectedAnswer == option
                val isCorrect = option == question.correctAnswer
                val showCorrect = showResult && isCorrect
                val showIncorrect = showResult && isSelected && !isCorrect

                val backgroundColor = when {
                    showCorrect -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                    showIncorrect -> Color(0xFFF44336).copy(alpha = 0.1f)
                    isSelected -> Color(0xFF6A35EE).copy(alpha = 0.1f)
                    else -> Color.White
                }

                val borderColor = when {
                    showCorrect -> Color(0xFF4CAF50)
                    showIncorrect -> Color(0xFFF44336)
                    isSelected -> Color(0xFF6A35EE)
                    else -> Color.LightGray.copy(alpha = 0.5f)
                }

                Card(
                    onClick = { if (!showResult) onAnswerSelected(option) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp),
                   border = BorderStroke(
                       width = if (isSelected ||showCorrect || showIncorrect) 2.dp else 1.dp,
                       color = borderColor
                   )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        showCorrect -> Color(0xFF4CAF50)
                                        showIncorrect -> Color(0xFFF44336)
                                        isSelected -> Color(0xFF6A35EE)
                                        else -> Color.LightGray.copy(alpha = 0.3f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (question.options.indexOf(option)) {
                                    0 -> "A"
                                    1 -> "B"
                                    2 -> "C"
                                    3 -> "D"
                                    else -> "?"
                                },
                                color = when {
                                    showCorrect || showIncorrect || isSelected -> Color.White
                                    else -> Color.Gray
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.size(12.dp))

                        Text(
                            text = option,
                            color = when {
                                showCorrect -> Color(0xFF4CAF50)
                                showIncorrect -> Color(0xFFF44336)
                                isSelected -> Color(0xFF6A35EE)
                                else -> Color.Black
                            },
                            fontSize = 16.sp,
                            fontWeight = if (isSelected || showCorrect || showIncorrect) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Next Button or Result Explanation
        if (showResult) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Explanation
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF6A35EE).copy(alpha = 0.05f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = if (selectedAnswer == question.correctAnswer) "✓ Correct!" else "✗ Incorrect",
                            color = if (selectedAnswer == question.correctAnswer) Color(0xFF4CAF50) else Color(0xFFF44336),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = question.explanation,
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }

                // Next Button
                Button(
                    onClick = onNextQuestion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6A35EE),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (currentQuestionIndex < totalQuestions - 1) "Next Question" else "See Results",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else if (selectedAnswer != null && quiz.timeLimit == null) {
            // Submit button for untimed quizzes
            Button(
                onClick = { onSubmitAnswer() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6A35EE),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Submit Answer",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}