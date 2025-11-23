
package com.example.kslingo.screens.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kslingo.data.model.UserProfile
import com.example.kslingo.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProgressViewModel(application: Application) : AndroidViewModel(application) {
    private val userProfileRepository = UserProfileRepository(application.applicationContext)
    val userProfile: StateFlow<UserProfile?> = userProfileRepository.userProfile
}
