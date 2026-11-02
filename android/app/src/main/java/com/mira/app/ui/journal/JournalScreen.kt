package com.mira.app.ui.journal

/**
 * Shows the user's private journal entries and lets them add a new one.
 * Journal entries are only visible to the user who wrote them.
 *
 * Offline mode: the screen always reads from the local SQLite copy (JournalDb),
 * so it works with no connection. New entries are saved on the phone first and
 * marked "Waiting to sync". When the network comes back (or the screen opens
 * while online) the waiting entries are sent to the server and the list is refreshed.
 */

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mira.app.R
import com.mira.app.data.JournalDb
import com.mira.app.model.CreateJournalRequest
import com.mira.app.model.JournalEntry
import com.mira.app.network.RetrofitClient
import com.mira.app.ui.theme.Cream
import com.mira.app.ui.theme.Terracotta
import com.mira.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun JournalScreen() {
    val context = LocalContext.current
    val db = remember { JournalDb(context) }
    var entries by remember { mutableStateOf(db.getAll()) }
    var isLoading by remember { mutableStateOf(true) }
    var offline by remember { mutableStateOf(false) }
    var showNewEntryDialog by remember { mutableStateOf(false) }
    val syncing = remember { AtomicBoolean(false) }
    val scope = rememberCoroutineScope()

    /** Sends waiting entries to the server, then pulls the latest list into the phone database. */
    suspend fun syncNow() {
        if (!syncing.compareAndSet(false, true)) return
        try {
            for ((localId, text) in db.getPending()) {
                val response = RetrofitClient.apiService.createJournalEntry(CreateJournalRequest(text))
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    db.markSynced(localId, body.entryId, body.createdAt)
                } else {
                    break
                }
            }
            val response = RetrofitClient.apiService.getJournalEntries()
            if (response.isSuccessful && response.body() != null) {
                db.replaceWithServer(response.body()!!)
            }
            offline = false
        } catch (e: Exception) {
            android.util.Log.e("MIRA", "Journal sync failed, staying offline", e)
            offline = true
        } finally {
            entries = db.getAll()
            isLoading = false
            syncing.set(false)
        }
    }

    LaunchedEffect(Unit) {
        syncNow()
    }

    // Sync automatically whenever the phone gets a network connection back.
    DisposableEffect(Unit) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                scope.launch { syncNow() }
            }
        }
        cm.registerDefaultNetworkCallback(callback)
        onDispose { cm.unregisterNetworkCallback(callback) }
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
                text = stringResource(R.string.journal_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.journal_subtitle),
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }

        if (offline) {
            Text(
                text = stringResource(R.string.journal_offline_banner),
                fontSize = 12.sp,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TextSecondary)
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }

        when {
            isLoading && entries.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Terracotta)
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
                        text = stringResource(R.string.journal_empty),
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
                            if (entry.entryId.startsWith("local-")) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.journal_pending),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                            }
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
            Text(stringResource(R.string.journal_new_entry), fontWeight = FontWeight.SemiBold)
        }
    }

    if (showNewEntryDialog) {
        NewJournalEntryDialog(
            onDismiss = { showNewEntryDialog = false },
            onSave = { text ->
                // Save on the phone first so nothing is lost, then try to sync straight away.
                db.addPending(text, java.time.Instant.now().toString())
                entries = db.getAll()
                showNewEntryDialog = false
                scope.launch { syncNow() }
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
                text = stringResource(R.string.journal_dialog_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Terracotta
            )
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text(stringResource(R.string.journal_dialog_hint), color = TextSecondary) },
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
                    Text(stringResource(R.string.journal_cancel), color = TextSecondary)
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
                    Text(stringResource(R.string.journal_save), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
