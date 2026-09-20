package com.mira.app.ui.journal

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mira.app.model.CreateJournalRequest
import com.mira.app.model.JournalEntry
import com.mira.app.network.RetrofitClient
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
            errorMessage = "Could not reach the server. Check your connection."
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadEntries()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = "My journal", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(text = "Private. Only you can see this.", fontSize = 14.sp)
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = errorMessage!!, fontSize = 14.sp)
                }
            }
            entries.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Nothing written yet.", fontSize = 14.sp)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(entries) { entry ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(16.dp)
                        ) {
                            Text(text = entry.createdAt.take(10), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = entry.content, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        Button(
            onClick = { showNewEntryDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text("+ New journal entry")
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
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp)
        ) {
            Text(text = "New entry", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("What's on your mind today?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { if (text.isNotBlank()) onSave(text.trim()) },
                    enabled = text.isNotBlank()
                ) {
                    Text("Save")
                }
            }
        }
    }
}
