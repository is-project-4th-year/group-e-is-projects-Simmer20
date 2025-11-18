package com.example.kslingo.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.size
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
import com.example.kslingo.data.service.FirebaseAuthService
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var currentStep by remember { mutableStateOf(1) } // 1: Email, 2: Success

    val authService = remember { FirebaseAuthService() }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        when (currentStep) {
            1 -> EmailInputStep(
                email = email,
                onEmailChange = { email = it; errorMessage = ""; successMessage = "" },
                isLoading = isLoading,
                errorMessage = errorMessage,
                onSendResetLink = {
                    if (email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        isLoading = true
                        errorMessage = ""
                        successMessage = ""

                        coroutineScope.launch {
                            val result = authService.sendPasswordResetEmail(email)

                            isLoading = false

                            if (result.isSuccess) {
                                currentStep = 2
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Failed to send reset email. Please try again."
                            }
                        }
                    } else {
                        errorMessage = "Please enter a valid email address"
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )

            2 -> SuccessStep(
                email = email,
                onBackToLogin = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun FirebaseAuthService() {
    TODO("Not yet implemented")
}

@Composable
fun EmailInputStep(
    email: String,
    onEmailChange: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String,
    onSendResetLink: () -> Unit,
    onBackToLogin: () -> Unit
) {
    Column {
        // Header with icon
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Security",
                tint = Color(0xFF6A35EE),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Reset Password",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = Color(0xFF6A35EE),
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Text(
            text = "Enter your email address and we'll send you a password reset link",
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 32.dp),
            fontSize = 16.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // Error message
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

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = "Email")
            },
            isError = errorMessage.isNotEmpty()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Send Reset Link Button
        Button(
            onClick = onSendResetLink,
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
            onClick = onBackToLogin
        ) {
            Text("Back to Login", color = Color(0xFF6A35EE), fontSize = 16.sp)
        }
    }
}

@Composable
fun SuccessStep(
    email: String,
    onBackToLogin: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Email,
            contentDescription = "Email Sent",
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Check Your Email",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = Color(0xFF6A35EE),
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "We've sent a password reset link to:",
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Text(
            text = email,
            color = Color(0xFF6A35EE),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = "Click the link in the email to reset your password. The link will expire in 1 hour for security.",
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 32.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Button(
            onClick = onBackToLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6A35EE),
                contentColor = Color.White
            )
        ) {
            Text("Back to Login", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { /* Could resend email here */ }
        ) {
            Text("Didn't receive email? Resend", color = Color(0xFF6A35EE))
        }
    }
}