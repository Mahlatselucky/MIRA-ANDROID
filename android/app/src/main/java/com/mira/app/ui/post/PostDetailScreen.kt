package com.mira.app.ui.post

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mira.app.model.Comment
import com.mira.app.model.CreateCommentRequest
import com.mira.app.model.Post
import com.mira.app.network.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun PostDetailScreen(
    post: Post,
    onBack: () -> Unit
) {
    var currentPost by remember { mutableStateOf(post) }
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var commentText by remember { mutableStateOf("") }
    var isSendingComment by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(post.postId) {
        try {
            val response = RetrofitClient.apiService.getPostDetail(post.postId)
            if (response.isSuccessful && response.body() != null) {
                val detail = response.body()!!
                comments = detail.comments
                currentPost = currentPost.copy(
                    meTooCount = detail.meTooCount,
                    commentCount = detail.commentCount,
                    userHasTappedMeToo = detail.userHasTappedMeToo
                )
            } else {
                errorMessage = "Could not load this post"
            }
        } catch (e: Exception) {
            errorMessage = "Could not reach the server. Check your connection."
        } finally {
            isLoading = false
        }
    }

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
            Text(text = "Post", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        errorMessage?.let {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(text = it, fontSize = 14.sp)
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Text(text = currentPost.authorSessionAlias, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = currentPost.content, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        modifier = Modifier.size(28.dp),
                        onClick = {
                            scope.launch {
                                try {
                                    val response = RetrofitClient.apiService.toggleMeToo(currentPost.postId)
                                    if (response.isSuccessful && response.body() != null) {
                                        val updated = response.body()!!
                                        currentPost = currentPost.copy(
                                            meTooCount = updated.meTooCount,
                                            userHasTappedMeToo = updated.userHasTappedMeToo
                                        )
                                    }
                                } catch (e: Exception) {
                                    // silently ignore, tap just won't update
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (currentPost.userHasTappedMeToo) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Me too"
                        )
                    }
                    Text(text = "Me too ${currentPost.meTooCount}", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (comments.isEmpty()) {
                Text(text = "No comments yet.", fontSize = 13.sp, modifier = Modifier.padding(vertical = 12.dp))
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(comments) { comment ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(12.dp)
                        ) {
                            Text(text = comment.authorSessionAlias, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = comment.content, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                placeholder = { Text("Add an anonymous comment...") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                enabled = !isSendingComment && commentText.isNotBlank(),
                onClick = {
                    val textToSend = commentText.trim()
                    isSendingComment = true
                    scope.launch {
                        try {
                            val response = RetrofitClient.apiService.addComment(
                                currentPost.postId,
                                CreateCommentRequest(textToSend)
                            )
                            if (response.isSuccessful && response.body() != null) {
                                comments = comments + response.body()!!
                                currentPost = currentPost.copy(commentCount = currentPost.commentCount + 1)
                                commentText = ""
                            }
                        } catch (e: Exception) {
                            // silently ignore for now
                        } finally {
                            isSendingComment = false
                        }
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}
