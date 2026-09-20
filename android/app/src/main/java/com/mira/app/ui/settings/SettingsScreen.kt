package com.mira.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mira.app.data.TokenManager
import com.mira.app.model.UpdateSettingsRequest
import com.mira.app.network.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var preferredLanguage by remember { mutableStateOf("en") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.apiService.getSettings()
            if (response.isSuccessful && response.body() != null) {
                preferredLanguage = response.body()!!.preferredLanguage
                notificationsEnabled = response.body()!!.notificationsEnabled
            }
        } catch (e: Exception) {
            statusMessage = "Could not load settings"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Settings",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Language", fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            listOf("en" to "English", "zu" to "isiZulu", "af" to "Afrikaans").forEach { (code, label) ->
                FilterChip(
                    selected = preferredLanguage == code,
                    onClick = { preferredLanguage = code },
                    label = { Text(label) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Notifications", fontWeight = FontWeight.Medium)
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
        }

        statusMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                isLoading = true
                statusMessage = null
                scope.launch {
                    try {
                        val response = RetrofitClient.apiService.updateSettings(
                            UpdateSettingsRequest(
                                preferredLanguage = preferredLanguage,
                                notificationsEnabled = notificationsEnabled
                            )
                        )
                        statusMessage = if (response.isSuccessful) "Settings saved" else "Could not save settings"
                    } catch (e: Exception) {
                        statusMessage = "Could not reach the server"
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "Saving..." else "Save changes")
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = {
                TokenManager.getInstance(context).clearToken()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log out")
        }
    }
}
