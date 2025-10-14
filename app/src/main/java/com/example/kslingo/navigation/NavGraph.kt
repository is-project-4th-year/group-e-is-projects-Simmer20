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




    }
}

@Composable
fun SettingsScreen(navController: NavHostController) {
    TODO("Not yet implemented")
}

@Composable
fun ProfileScreen(navController: NavHostController) {
    TODO("Not yet implemented")
}