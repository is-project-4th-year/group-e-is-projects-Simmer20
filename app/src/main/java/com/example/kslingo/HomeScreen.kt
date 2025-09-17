package com.example.kslingo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.runtime.remember

@Composable
fun HomeScreen(navController: NavController) {
    val authService = remember { FirebaseAuthService() }
    val currentUser = authService.getCurrentUser()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to KSLingo!",
            style = MaterialTheme.typography.headlineLarge.copy(
                color = Color(0xFF6A35EE),
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Logged in as: ${currentUser?.email ?: "Unknown"}",
            color = Color.Gray,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = {
                authService.signOut()
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6A35EE),
                contentColor = Color.White
            )
        ) {
            Text("Sign Out", fontSize = 16.sp)
        }
    }
}