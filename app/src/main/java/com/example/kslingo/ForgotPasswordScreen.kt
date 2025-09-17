package com.example.kslingo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    val authService = remember { FirebaseAuthService() }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Messages
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                fontSize = 14.sp
            )
        }

        if (successMessage.isNotEmpty()) {
            Text(
                text = successMessage,
                color = Color(0xFF4CAF50),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                fontSize = 14.sp
            )
        }

        // Header
        Text(
            text = "Reset Password",
            style = MaterialTheme.typography.headlineLarge.copy(
                color = Color(0xFF6A35EE),
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Enter your email and we'll send you a password reset link",
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 32.dp),
            fontSize = 16.sp
        )

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMessage = ""; successMessage = "" },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            isError = errorMessage.isNotEmpty()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Send Reset Link Button
        Button(
            onClick = {
                if (email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    isLoading = true
                    errorMessage = ""
                    successMessage = ""

                    coroutineScope.launch {
                        val result = authService.sendPasswordResetEmail(email)

                        isLoading = false

                        if (result.isSuccess) {
                            successMessage = "Password reset email sent! Check your inbox."
                        } else {
                            errorMessage = result.exceptionOrNull()?.message ?: "Failed to send reset email"
                        }
                    }
                } else {
                    errorMessage = "Please enter a valid email address"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6A35EE),
                contentColor = Color.White
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Send Reset Link", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Back to Login
        TextButton(
            onClick = { navController.popBackStack() }
        ) {
            Text("Back to Login", color = Color(0xFF6A35EE), fontSize = 16.sp)
        }
    }
}