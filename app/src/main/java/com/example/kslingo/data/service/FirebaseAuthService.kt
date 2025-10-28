package com.example.kslingo.data.service

import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class FirebaseAuthService {
    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore

    // Regular signup
    suspend fun signUpWithEmail(
        email: String,
        password: String,
        username: String
    ): Result<Boolean> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build()

            authResult.user?.updateProfile(profileUpdates)?.await()
            saveUserToFirestore(authResult.user?.uid, email, username)

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Login with optional 2FA check
    suspend fun signInWithEmail(email: String, password: String): Result<String> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()

            // Check if user has 2FA enabled
            val userDoc = db.collection("users").document(authResult.user?.uid ?: "").get().await()
            val is2FAEnabled = userDoc.getBoolean("is2FAEnabled") ?: false

            if (is2FAEnabled) {
                // Return user ID for 2FA verification
                Result.success(authResult.user?.uid ?: "")
            } else {
                // Direct login
                Result.success("SUCCESS")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Generate and send 2FA code
    suspend fun generate2FACode(userId: String): Result<String> {
        return try {
            val code = (100000..999999).random().toString() // 6-digit code
            val expiresAt = System.currentTimeMillis() + 10 * 60 * 1000 // 10 minutes

            val twoFAData = hashMapOf(
                "code" to code,
                "expiresAt" to expiresAt,
                "used" to false
            )

            db.collection("users").document(userId)
                .collection("twoFA")
                .document("current")
                .set(twoFAData)
                .await()

            // In a real app, you'd send this via SMS/Email
            // For now, we'll just return it for testing
            Result.success(code)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Verify 2FA code
    suspend fun verify2FACode(userId: String, code: String): Result<Boolean> {
        return try {
            val twoFADoc = db.collection("users").document(userId)
                .collection("twoFA")
                .document("current")
                .get()
                .await()

            if (twoFADoc.exists()) {
                val storedCode = twoFADoc.getString("code")
                val expiresAt = twoFADoc.getLong("expiresAt") ?: 0
                val used = twoFADoc.getBoolean("used") ?: true

                val isValid = !used &&
                        storedCode == code &&
                        System.currentTimeMillis() < expiresAt

                if (isValid) {
                    // Mark code as used
                    twoFADoc.reference.update("used", true).await()
                    Result.success(true)
                } else {
                    Result.failure(Exception("Invalid or expired code"))
                }
            } else {
                Result.failure(Exception("2FA code not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Enable/Disable 2FA for user
    suspend fun toggle2FA(userId: String, enable: Boolean): Result<Boolean> {
        return try {
            db.collection("users").document(userId)
                .update("is2FAEnabled", enable)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Enhanced password reset
    suspend fun sendPasswordResetEmail(email: String): Result<Boolean> {
        return try {
            auth.sendPasswordResetEmail(email).await()

            // Log the reset request for security
            val user = auth.currentUser
            if (user != null) {
                db.collection("security_logs").document().set(
                    hashMapOf(
                        "userId" to user.uid,
                        "action" to "password_reset_requested",
                        "timestamp" to Timestamp.now(),
                        "email" to email
                    )
                ).await()
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Check if 2FA is enabled for user
    suspend fun is2FAEnabled(userId: String): Boolean {
        return try {
            val userDoc = db.collection("users").document(userId).get().await()
            userDoc.getBoolean("is2FAEnabled") ?: false
        } catch (e: Exception) {
            false
        }
    }

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
                    "createdAt" to Timestamp.now(),
                    "is2FAEnabled" to false, // Default to disabled
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

    fun getCurrentUser() = auth.currentUser
    fun signOut() = auth.signOut()
    fun isUserLoggedIn(): Boolean = auth.currentUser != null
}