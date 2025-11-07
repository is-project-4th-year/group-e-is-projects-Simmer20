package com.example.kslingo.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.kslingo.data.model.QuizResult
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson

class QuizStateRepository(private val context: Context) {

    // A modern JSON parser for reliable data conversion.
    private val gson = Gson()

    // This will hold the SharedPreferences instance for the CURRENTLY logged-in user.
    private var userPrefs: SharedPreferences? = null

    // This list will hold the quiz results for the current user, loaded into memory.
    private val quizResultsInMemory = mutableListOf<QuizResult>()

    init {
        // Add a listener to react instantly when the user logs in or out.
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                // USER LOGGED IN: Initialize prefs and load their specific results.
                Log.d("QuizStateRepo", "User ${firebaseUser.uid} logged in. Loading their quiz results.")
                val prefsName = "quiz_prefs_${firebaseUser.uid}"
                userPrefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
                loadQuizResultsFromPrefs()
            } else {
                // USER LOGGED OUT: Clear all local data.
                Log.d("QuizStateRepo", "User logged out. Clearing local quiz data.")
                quizResultsInMemory.clear()
                userPrefs?.edit()?.clear()?.apply() // Clear the prefs file of the user who just logged out
                userPrefs = null // Set to null
            }
        }
    }

    /**
     * Loads the set of quiz result JSON strings from the current user's SharedPreferences into memory.
     */
    private fun loadQuizResultsFromPrefs() {
        quizResultsInMemory.clear() // Clear any previous user's data
        val resultsJsonSet = userPrefs?.getStringSet("quiz_results", emptySet()) ?: emptySet()
        val results = resultsJsonSet.mapNotNull { json ->
            try {
                gson.fromJson(json, QuizResult::class.java)
            } catch (e: Exception) {
                Log.e("QuizStateRepo", "Failed to parse QuizResult from JSON", e)
                null
            }
        }
        quizResultsInMemory.addAll(results)
        Log.d("QuizStateRepo", "Loaded ${quizResultsInMemory.size} quiz results from prefs.")
    }

    /**
     * Saves a quiz result for the current user to memory and persists it to SharedPreferences.
     */
    fun saveQuizResult(result: QuizResult) {
        if (userPrefs == null) {
            Log.w("QuizStateRepo", "Cannot save quiz result, no user is logged in.")
            return
        }
        // Add to the in-memory list
        quizResultsInMemory.add(result)

        // Convert the entire list to a set of JSON strings for saving
        val resultsJsonSet = quizResultsInMemory.map { gson.toJson(it) }.toSet()

        userPrefs?.edit()
            ?.putStringSet("quiz_results", resultsJsonSet)
            ?.apply()
        Log.d("QuizStateRepo", "Quiz result for '${result.quizId}' saved.")
    }

    /**
     * Returns the full list of quiz results for the current user from memory.
     */
    fun getQuizResults(): List<QuizResult> {
        return quizResultsInMemory.toList()
    }

    fun getBestScore(quizId: String): Int {
        return getQuizResults()
            .filter { it.quizId == quizId }
            .maxOfOrNull { it.score } ?: 0
    }

    fun isQuizPassed(quizId: String): Boolean {
        // This is okay for now, but for better practice, you might pass QuizRepository as a parameter
        // or inject it via the constructor if this class were managed by a DI framework like Hilt.
        val quiz = QuizRepository(context).getQuizById(quizId)
        val bestScore = getBestScore(quizId)
        return bestScore >= (quiz?.passingScore ?: 70)
    }
}
