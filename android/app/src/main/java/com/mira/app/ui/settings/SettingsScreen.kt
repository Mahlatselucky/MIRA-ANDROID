package com.mira.app.ui.settings

/**
 * Lets the user change their preferred language and notification setting,
 * and log out of the app. Tapping a language chip switches the app's
 * display language immediately via LocaleHelper, and Save changes also
 * persists the choice to the server.
 */

import android.app.Activity
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mira.app.R
import com.mira.app.data.LocaleHelper
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

    var preferredLanguage by remember { mutableStateOf(LocaleHelper.getSavedLanguage(context)) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.apiService.getSettings()
            if (response.isSuccessful && response.body() != null) {
                // the device language (LocaleHelper) decides which chip is selected
                notificationsEnabled = response.body()!!.notificationsEnabled
            }
        } catch (e: Exception) {
            android.util.Log.e("MIRA", "Request failed in SettingsScreen", e)
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
                text = stringResource(R.string.settings_title),
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
                Text(text = stringResource(R.string.settings_language), fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    listOf(
                        "en" to stringResource(R.string.lang_english),
                        "zu" to stringResource(R.string.lang_zulu),
                        "af" to stringResource(R.string.lang_afrikaans)
                    ).forEach { (code, label) ->
                        FilterChip(
                            selected = preferredLanguage == code,
                            onClick = {
                                preferredLanguage = code
                                LocaleHelper.setLocale(context as Activity, code)
                            },
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
                Text(text = stringResource(R.string.settings_notifications), fontWeight = FontWeight.SemiBold)
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
                    color = if (it == stringResource(R.string.settings_saved)) Teal else MaterialTheme.colorScheme.error,
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
                            statusMessage = if (response.isSuccessful) context.getString(R.string.settings_saved) else "Could not save settings"
                        } catch (e: Exception) {
                            android.util.Log.e("MIRA", "Request failed in SettingsScreen", e)
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
                    if (isLoading) stringResource(R.string.settings_saving) else stringResource(R.string.settings_save),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    TokenManager.getInstance(context).clearToken()
                    com.mira.app.data.JournalDb(context).clearAll()
                    onLogout()
                },
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.5.dp, Terracotta),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Terracotta),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(stringResource(R.string.settings_logout), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
