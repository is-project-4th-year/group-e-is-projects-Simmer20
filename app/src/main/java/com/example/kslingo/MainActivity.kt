package com.example.kslingo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kslingo.navigation.ProfileScreen
import com.example.kslingo.screens.settings.SettingsScreen
import com.example.kslingo.screens.auth.ForgotPasswordScreen
import com.example.kslingo.screens.auth.LoginScreen
import com.example.kslingo.screens.auth.SignupScreen
import com.example.kslingo.screens.home.HomeScreen
import com.example.kslingo.screens.onboarding.OnboardingScreen
import com.example.kslingo.screens.onboarding.SplashScreen
import com.example.kslingo.ui.theme.KSLingoTheme
import com.google.firebase.FirebaseApp
import com.example.kslingo.screens.auth.TwoFAScreen
import com.example.kslingo.screens.dictionary.DictionaryScreen
import com.example.kslingo.screens.lessons.LessonCategoryScreen
import com.example.kslingo.screens.lessons.LessonDetailScreen
import com.example.kslingo.screens.lessons.LessonsScreen
import com.example.kslingo.screens.practice.PracticeScreen
import com.example.kslingo.screens.progress.ProgressScreen
import com.example.kslingo.screens.quiz.QuizQuestionsScreen
import com.example.kslingo.screens.quiz.QuizResultsScreen
import com.example.kslingo.screens.quiz.QuizSelectionScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)

        setContent {
            KSLingoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") { SplashScreen(navController) }
        composable("onboarding") { OnboardingScreen(navController) }
        composable("signup") { SignupScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("forgot_password") { ForgotPasswordScreen(navController) }
        composable ("two_fa/{userId}") {
            backStackEntry -> val userId = backStackEntry.arguments?.getString("userId")
            TwoFAScreen(navController, userId) }
        composable("home") { HomeScreen(navController) }

        // Main App screens
        composable("home") {HomeScreen(navController = navController)}
        composable("lessons") {LessonsScreen(navController = navController)}
        composable("lesson_category/{categoryId}") {backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")
            LessonCategoryScreen(navController, categoryId)}
        composable("lesson_detail/{lessonId}") { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId")
            LessonDetailScreen(navController, lessonId)
            }
        composable("practice") { PracticeScreen(navController = navController)}
        composable("progress") { ProgressScreen(navController = navController)}
        composable("dictionary") { DictionaryScreen(navController = navController)}
        composable("profile") { ProfileScreen(navController = navController)}
        composable("settings") { SettingsScreen(navController = navController)}
        composable("quiz_selection") { QuizSelectionScreen(navController) }
        composable("quiz_questions/{quizId}") { backStackEntry ->
            val quizId = backStackEntry.arguments?.getString("quizId")
            QuizQuestionsScreen(navController, quizId)
        }
        composable("quiz_results/{quizId}/{score}/{totalQuestions}") { backStackEntry ->
            val quizId = backStackEntry.arguments?.getString("quizId")
            val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
            val totalQuestions = backStackEntry.arguments?.getString("totalQuestions")?.toIntOrNull() ?: 0
            QuizResultsScreen(navController, quizId, score, totalQuestions)
        }
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KSLingoTheme {
        AppNavigation()
    }
}


@Composable
fun ProfileScreen(navController: NavHostController) {
    TODO("Not yet implemented")
}
