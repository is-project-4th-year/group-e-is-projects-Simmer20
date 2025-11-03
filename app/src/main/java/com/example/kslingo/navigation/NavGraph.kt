package com.example.kslingo.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.example.kslingo.screens.auth.ForgotPasswordScreen
import com.example.kslingo.screens.auth.LoginScreen
import com.example.kslingo.screens.auth.SignUpScreen
import com.example.kslingo.screens.auth.TwoFAScreen
import com.example.kslingo.screens.home.HomeScreen
import com.example.kslingo.screens.onboarding.OnboardingScreen
import com.example.kslingo.screens.onboarding.SplashScreen
import androidx.compose.runtime.Composable
import com.example.kslingo.screens.dictionary.DictionaryScreen
import com.example.kslingo.screens.lessons.LessonsScreen
import com.example.kslingo.screens.practice.PracticeScreen
import com.example.kslingo.screens.progress.ProgressScreen
import com.example.kslingo.screens.settings.SettingsScreen
import com.example.kslingo.screens.quiz.QuizSelectionScreen
import com.example.kslingo.screens.quiz.QuizQuestionsScreen
import com.example.kslingo.screens.quiz.QuizResultsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"  // Start directly with splash
    ) {
        // Splash Screen
        composable("splash") {SplashScreen(navController = navController)        }
        // Onboarding Screen
        composable("onboarding") {OnboardingScreen(navController = navController)}
        // Login Screen
        composable("login") {LoginScreen(navController = navController)}
        // Signup Screen
        composable("signup") {SignUpScreen(navController = navController)}
        // Forgot Password Screen
        composable("forgot_password") {ForgotPasswordScreen(navController = navController)}
        // 2FA Screen
        composable("two_fa/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            TwoFAScreen(
                navController = navController,
                userId = userId
            )
        }

        // Main App screens
        composable("home") {HomeScreen(navController = navController)}
        composable("lessons") {LessonsScreen(navController = navController)}
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


@Composable
fun ProfileScreen(navController: NavHostController) {
    TODO("Not yet implemented")
}