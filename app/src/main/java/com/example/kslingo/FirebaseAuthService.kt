package com.example.kslingo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore


class FirebaseAuthService {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Sign up with email and password
    suspend fun signUpWithEmail(
        email: String,
        password: String,
        username: String
    ): Result<Boolean> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()

            // Update user profile with username
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build()

            authResult.user?.updateProfile(profileUpdates)?.await()

            // Save additional user details to Firestore
            saveUserToFirestore(authResult.user?.uid, email, username)

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sign in with email and password
    suspend fun signInWithEmail(email: String, password: String): Result<Boolean> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Send password reset email
    suspend fun sendPasswordResetEmail(email: String): Result<Boolean> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Save user details to Firestore
    private suspend fun saveUserToFirestore(
        userId: String?,
        email: String,
        username: String
    ): Result<Boolean> {
        return try {
            if (userId != null) {
                val user = hashMapOf(
                    "email" to email,
                    "username" to username,
                    "createdAt" to com.google.firebase.Timestamp.now(),
                    "progress" to hashMapOf(
                        "lessonsCompleted" to 0,
                        "totalPracticeTime" to 0,
                        "accuracy" to 0.0
                    )
                )

                db.collection("users").document(userId).set(user).await()
                Result.success(true)
            } else {
                Result.failure(Exception("User ID is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get current user
    fun getCurrentUser() = auth.currentUser

    // Sign out
    fun signOut() = auth.signOut()
}