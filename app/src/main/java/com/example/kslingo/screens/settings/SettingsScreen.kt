package com.example.kslingo.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable;
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kslingo.data.service.FirebaseAuthService
import kotlinx.coroutines.launch

data class SettingItem(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val hasSwitch: Boolean = false,
    val switchState: Boolean = false,
    val onSwitchChange: ((Boolean) -> Unit)? = null,
    val onClick: (() -> Unit)? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var is2FAEnabled by remember { mutableStateOf(false) }
    var areNotificationsEnabled by remember { mutableStateOf(true) }
    var isSoundEnabled by remember { mutableStateOf(true) }
    var isOfflineModeEnabled by remember { mutableStateOf(false) }

    val authService = remember { FirebaseAuthService() }
    val currentUser = authService.getCurrentUser()
    val coroutineScope = rememberCoroutineScope()

    // Check current 2FA status when screen loads
    LaunchedEffect(currentUser?.uid) {
        if (currentUser?.uid != null) {
            is2FAEnabled = authService.is2FAEnabled(currentUser.uid)
        }
    }

    val securitySettings = listOf(
        SettingItem(
            title = "Two-Factor Authentication",
            description = "Add extra security to your account with 2FA",
            icon = Icons.Default.Security,
            hasSwitch = true,
            switchState = is2FAEnabled,
            onSwitchChange = { enabled ->
                if (currentUser?.uid != null) {
                    coroutineScope.launch {
                        val result = authService.toggle2FA(currentUser.uid, enabled)
                        if (result.isSuccess) {
                            is2FAEnabled = enabled
                        }
                    }
                }
            }
        ),
        SettingItem(
            title = "Privacy Settings",
            description = "Manage your data and privacy preferences",
            icon = Icons.Default.PrivacyTip,
            onClick = { /* Navigate to privacy screen */ }
        )
    )

    val preferenceSettings = listOf(
        SettingItem(
            title = "Notifications",
            description = "Enable or disable push notifications",
            icon = Icons.Default.Notifications,
            hasSwitch = true,
            switchState = areNotificationsEnabled,
            onSwitchChange = { enabled ->
                areNotificationsEnabled = enabled
            }
        ),
        SettingItem(
            title = "Sound Effects",
            description = "Toggle sound effects during lessons",
            icon = Icons.Default.VolumeUp,
            hasSwitch = true,
            switchState = isSoundEnabled,
            onSwitchChange = { enabled ->
                isSoundEnabled = enabled
            }
        ),
        SettingItem(
            title = "Language",
            description = "Change app language",
            icon = Icons.Default.Language,
            onClick = { /* Navigate to language settings */ }
        )
    )

    val dataSettings = listOf(
        SettingItem(
            title = "Offline Mode",
            description = "Download lessons for offline use",
            icon = Icons.Default.Storage,
            hasSwitch = true,
            switchState = isOfflineModeEnabled,
            onSwitchChange = { enabled ->
                isOfflineModeEnabled = enabled
            }
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6A35EE)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF6A35EE)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF8F8F8))
        ) {
            // User Info Card
            UserInfoCard(
                username = currentUser?.displayName ?: "KSL Learner",
                email = currentUser?.email ?: ""
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Security Settings Section
            SettingsSection(
                title = "Security",
                settings = securitySettings
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Preferences Section
            SettingsSection(
                title = "Preferences",
                settings = preferenceSettings
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Data Section
            SettingsSection(
                title = "Data & Storage",
                settings = dataSettings
            )

            Spacer(modifier = Modifier.height(32.dp))

            // App Info
            AppInfoSection()
        }
    }
}

@Composable
fun UserInfoCard(username: String, email: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Account",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = username,
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = email,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun SettingsSection(title: String, settings: List<SettingItem>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = Color.Gray,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            settings.forEachIndexed { index, setting ->
                SettingItemRow(setting = setting)
                if (index < settings.size - 1) {
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color.LightGray.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingItemRow(setting: SettingItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(enabled = setting.onClick != null) {
                setting.onClick?.invoke()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = setting.icon,
            contentDescription = setting.title,
            tint = Color(0xFF6A35EE),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.size(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = setting.title,
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = setting.description,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }

        if (setting.hasSwitch) {
            Switch(
                checked = setting.switchState,
                onCheckedChange = setting.onSwitchChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF6A35EE),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.Gray
                )
            )
        }
    }
}

@Composable
fun AppInfoSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "KSLingo v1.0.0",
            color = Color.Gray,
            fontSize = 14.sp
        )
        Text(
            text = "Kenyan Sign Language Learning App",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}