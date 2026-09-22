package com.mira.app.ui.post

/**
 * Lets the user write and submit a new anonymous post to a room.
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mira.app.model.CreatePostRequest
import com.mira.app.model.Post
import com.mira.app.model.Room
import com.mira.app.network.RetrofitClient
import com.mira.app.ui.theme.Cream
import com.mira.app.ui.theme.Terracotta
import com.mira.app.ui.theme.TextSecondary
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Terracotta)
                .padding(horizontal = 12.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = "New post",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Posting in ${room.name}",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = {
                    Text(
                        "What's on your mind? You can be as honest as you need to be. Nobody will know it's you.",
                        color = TextSecondary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                maxLines = 12,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Terracotta,
                    unfocusedBorderColor = Terracotta.copy(alpha = 0.4f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
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
                            val response = RetrofitClient.apiService.createPost(CreatePostRequest(room.roomId, content.trim()))
                            if (response.isSuccessful && response.body() != null) {
                                onPostCreated(response.body()!!)
                            } else {
                                errorMessage = "Could not create post, please try again"
                            }
                        } catch (e: Exception) {
            android.util.Log.e("MIRA", "Request failed in CreatePostScreen", e)
                            errorMessage = "Could not reach the server. Check your connection."
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Terracotta,
                    contentColor = Color.White
                )
            ) {
                Text(
                    if (isLoading) "Posting..." else "Post anonymously",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
