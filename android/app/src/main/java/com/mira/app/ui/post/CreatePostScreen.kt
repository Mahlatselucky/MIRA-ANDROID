package com.mira.app.ui.post

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mira.app.model.CreatePostRequest
import com.mira.app.model.Post
import com.mira.app.model.Room
import com.mira.app.network.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun CreatePostScreen(
    room: Room,
    onBack: () -> Unit,
    onPostCreated: (Post) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(text = "New post", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = "Posting in ${room.name}", fontSize = 13.sp)
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = {
                    Text("What's on your mind? You can be as honest as you need to be. Nobody will know it's you.")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                maxLines = 12
            )

            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Button(
                onClick = {
                    if (content.isBlank()) {
                        errorMessage = "Post content cannot be empty"
                        return@Button
                    }
                    errorMessage = null
                    isLoading = true
                    scope.launch {
                        try {
                            val response = RetrofitClient.apiService.createPost(
                                room.roomId,
                                CreatePostRequest(content.trim())
                            )
                            if (response.isSuccessful && response.body() != null) {
                                onPostCreated(response.body()!!)
                            } else {
                                errorMessage = "Could not create post, please try again"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Could not reach the server. Check your connection."
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoading) "Posting..." else "Post anonymously")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Save as draft if you're offline, it'll send once you're back online.",
                fontSize = 11.sp
            )
        }
    }
}
