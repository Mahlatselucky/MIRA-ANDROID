package com.mira.app.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mira.app.data.TokenManager
import com.mira.app.model.UpdateSettingsRequest
import com.mira.app.network.RetrofitClient
import com.mira.app.ui.theme.Cream
import com.mira.app.ui.theme.Teal
import com.mira.app.ui.theme.Terracotta
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
            .background(Cream)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Terracotta)
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Text(
                text = "Settings",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(18.dp)
            ) {
                Text(text = "Language", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    listOf("en" to "English", "zu" to "isiZulu", "af" to "Afrikaans").forEach { (code, label) ->
                        FilterChip(
                            selected = preferredLanguage == code,
                            onClick = { preferredLanguage = code },
                            label = { Text(label) },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Terracotta,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Notifications", fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Terracotta
                    )
                )
            }

            statusMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = it,
                    color = if (it == "Settings saved") Teal else MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
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
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Terracotta,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    if (isLoading) "Saving..." else "Save changes",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    TokenManager.getInstance(context).clearToken()
                    onLogout()
                },
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.5.dp, Terracotta),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Terracotta),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Log out", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
