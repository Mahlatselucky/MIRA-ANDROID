package com.mira.app.ui.journal

/**
 * Shows the user's private journal entries and lets them add a new one.
 * Journal entries are only visible to the user who wrote them.
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mira.app.model.CreateJournalRequest
import com.mira.app.model.JournalEntry
import com.mira.app.network.RetrofitClient
import com.mira.app.ui.theme.Cream
import com.mira.app.ui.theme.Terracotta
import com.mira.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun JournalScreen() {
    var entries by remember { mutableStateOf<List<JournalEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showNewEntryDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    suspend fun loadEntries() {
        try {
            val response = RetrofitClient.apiService.getJournalEntries()
            if (response.isSuccessful && response.body() != null) {
                entries = response.body()!!
                errorMessage = null
            } else {
                errorMessage = "Could not load journal entries"
            }
        } catch (e: Exception) {
            android.util.Log.e("MIRA", "Request failed in JournalScreen", e)
            errorMessage = "Could not reach the server. Check your connection."
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadEntries()
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
                text = "My journal",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Private. Only you can see this.",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Terracotta)
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage!!,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            entries.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nothing written yet.",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(entries) { entry ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .padding(18.dp)
                        ) {
                            Text(
                                text = entry.createdAt.take(10),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Terracotta
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = entry.content, fontSize = 15.sp)
                        }
                    }
                }
            }
        }

        Button(
            onClick = { showNewEntryDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .height(52.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Terracotta,
                contentColor = Color.White
            )
        ) {
            Text("+ New journal entry", fontWeight = FontWeight.SemiBold)
        }
    }

    if (showNewEntryDialog) {
        NewJournalEntryDialog(
            onDismiss = { showNewEntryDialog = false },
            onSave = { text ->
                scope.launch {
                    try {
                        val response = RetrofitClient.apiService.createJournalEntry(
                            CreateJournalRequest(text)
                        )
                        if (response.isSuccessful && response.body() != null) {
                            entries = listOf(response.body()!!) + entries
                        }
                    } catch (e: Exception) {
            android.util.Log.e("MIRA", "Request failed in JournalScreen", e)
                        // silently ignore for now
                    } finally {
                        showNewEntryDialog = false
                    }
                }
            }
        )
    }
}

@Composable
private fun NewJournalEntryDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Cream)
                .padding(24.dp)
        ) {
            Text(
                text = "New entry",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Terracotta
            )
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("What's on your mind today?", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Terracotta,
                    unfocusedBorderColor = Terracotta.copy(alpha = 0.4f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextSecondary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { if (text.isNotBlank()) onSave(text.trim()) },
                    enabled = text.isNotBlank(),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Terracotta,
                        contentColor = Color.White
                    )
                ) {
                    Text("Save", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
