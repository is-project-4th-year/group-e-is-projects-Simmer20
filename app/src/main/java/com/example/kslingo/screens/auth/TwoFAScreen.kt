package com.example.kslingo.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.kslingo.data.service.FirebaseAuthService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoFAScreen(
    navController: NavController,
    userId: String? = null
) {
    var verificationCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var countdown by remember { mutableStateOf(0) }
    var generatedCode by remember { mutableStateOf("") }

    val authService = remember { FirebaseAuthService() }
    val coroutineScope = rememberCoroutineScope()

    // Auto-generate and send 2FA code when screen loads
    LaunchedEffect(userId) {
        if (userId != null) {
            isLoading = true
            val result = authService.generate2FACode(userId)
            isLoading = false

            if (result.isSuccess) {
                generatedCode = result.getOrNull() ?: ""
                successMessage = "Verification code sent! For testing, use: $generatedCode"
                countdown = 600 // 10 minutes in seconds
            } else {
                errorMessage = "Failed to generate verification code"
            }
        }
    }

    // Countdown timer
    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000)
            countdown--
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = "2FA",
                tint = Color(0xFF6A35EE),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Two-Factor Authentication",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color(0xFF6A35EE),
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Text(
            text = "Enter the 6-digit code sent to your email",
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // Countdown timer
        if (countdown > 0) {
            Text(
                text = "Code expires in: ${countdown / 60}:${String.format("%02d", countdown % 60)}",
                color = if (countdown < 60) Color.Red else Color(0xFF6A35EE),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

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

        // Verification Code Field
        OutlinedTextField(
            value = verificationCode,
            onValueChange = {
                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                    verificationCode = it
                    errorMessage = ""
                }
            },
            label = { Text("6-digit Code") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = errorMessage.isNotEmpty()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Verify Button
        Button(
            onClick = {
                if (verificationCode.length == 6 && userId != null) {
                    isLoading = true
                    errorMessage = ""

                    coroutineScope.launch {
                        val result = authService.verify2FACode(userId, verificationCode)

                        isLoading = false

                        if (result.isSuccess) {
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            errorMessage = result.exceptionOrNull()?.message ?: "Invalid verification code"
                        }
                    }
                } else {
                    errorMessage = "Please enter a valid 6-digit code"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6A35EE),
                contentColor = Color.White
            ),
            enabled = !isLoading && countdown > 0
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Verify Code", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Resend Code Button
        Button(
            onClick = {
                if (userId != null) {
                    isLoading = true
                    errorMessage = ""
                    successMessage = ""

                    coroutineScope.launch {
                        val result = authService.generate2FACode(userId)

                        isLoading = false

                        if (result.isSuccess) {
                            generatedCode = result.getOrNull() ?: ""
                            successMessage = "New code sent! For testing, use: $generatedCode"
                            countdown = 600
                        } else {
                            errorMessage = "Failed to send new code"
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color(0xFF6A35EE)
            ),
            enabled = !isLoading
        ) {
            Text("Resend Code", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Back to Login
        TextButton(
            onClick = { navController.popBackStack() }
        ) {
            Text("Back to Login", color = Color(0xFF6A35EE), fontSize = 16.sp)
        }
    }
}