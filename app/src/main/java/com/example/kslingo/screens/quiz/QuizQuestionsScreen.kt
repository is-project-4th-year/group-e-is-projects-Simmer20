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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kslingo.data.model.MediaType
import com.example.kslingo.data.model.Quiz
import com.example.kslingo.data.model.QuizQuestion
import com.example.kslingo.data.repository.QuizRepository
import kotlinx.coroutines.delay


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
    LaunchedEffect(key1 = currentQuestion, key2 = showResult) {
        if (quiz?.timeLimit != null && !showResult) {
            timeLeft = quiz.timeLimit
            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
            }
            // Auto-submit if time runs out and an answer hasn't been submitted
            if (!showResult) {
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
                        // Consider showing a confirmation dialog before exiting
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
                    // Score is tallied when submitting, so we only handle navigation here
                    if (currentQuestionIndex < questions.size - 1) {
                        currentQuestionIndex++
                        selectedAnswer = null
                        showResult = false
                        // Reset timer for the new question if applicable
                        if (quiz.timeLimit != null) {
                            timeLeft = quiz.timeLimit
                        }
                    } else {
                        isQuizCompleted = true
                    }
                },
                onSubmitAnswer = {
                    if (selectedAnswer == currentQuestion.correctAnswer) {
                        score++
                    }
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
                        textAlign = TextAlign.Center
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
                        tint = if (timeLeft < 10 && timeLeft % 2 == 0) Color.Red else Color(0xFF6A35EE),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "${timeLeft}s",
                        color = if (timeLeft < 10 && timeLeft % 2 == 0) Color.Red else Color(0xFF6A35EE),
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
                    textAlign = TextAlign.Center,
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
                        MediaType.IMAGE -> {
                            Image(
                                painter = painterResource(id = question.mediaResource),
                                contentDescription = "Sign demonstration",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        MediaType.VIDEO -> {
                            // Video placeholder
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
                        width = if (isSelected || showCorrect || showIncorrect) 2.dp else 1.dp,
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
                            val letter = when (question.options.indexOf(option)) {
                                0 -> "A"
                                1 -> "B"
                                2 -> "C"
                                3 -> "D"
                                else -> "?"
                            }
                            Text(
                                text = letter,
                                color = if (isSelected || showCorrect || showIncorrect) Color.White else Color.Gray,
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
                                else -> Color.Black
                            },
                            fontSize = 16.sp,
                            fontWeight = if (isSelected || showCorrect || showIncorrect) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // This spacer pushes the button to the bottom
        Spacer(modifier = Modifier.weight(1f))

        // This button is now unified for submitting and moving to the next question
        if (selectedAnswer != null) {
            Button(
                onClick = {
                    if (showResult) {
                        onNextQuestion()
                    } else {
                        onSubmitAnswer()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(top = 8.dp), // Add some padding from the elements above
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6A35EE)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (showResult) {
                        if (currentQuestionIndex < totalQuestions - 1) "Next Question" else "See Results"
                    } else {
                        "Submit"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
