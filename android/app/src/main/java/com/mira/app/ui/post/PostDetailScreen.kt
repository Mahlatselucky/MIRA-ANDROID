package com.mira.app.ui.post

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mira.app.model.Comment
import com.mira.app.model.CreateCommentRequest
import com.mira.app.model.Post
import com.mira.app.network.RetrofitClient
import com.mira.app.ui.theme.Cream
import com.mira.app.ui.theme.Terracotta
import com.mira.app.ui.theme.TextSecondary
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
        comments = post.comments
        isLoading = false
    }

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
            Text(
                text = "Post",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Terracotta)
            }
            return@Column
        }

        errorMessage?.let {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(text = it, fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(18.dp)
            ) {
                Text(
                    text = currentPost.authorSessionAlias,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = currentPost.content, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (currentPost.userHasTappedMeToo) Terracotta
                            else Terracotta.copy(alpha = 0.16f)
                        )
                        .clickable {
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
            android.util.Log.e("MIRA", "Request failed in PostDetailScreen", e)
                                    // silently ignore, tap just won't update
                                }
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (currentPost.userHasTappedMeToo) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Me too",
                        tint = if (currentPost.userHasTappedMeToo) Color.White else Terracotta,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Me too ${currentPost.meTooCount}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (currentPost.userHasTappedMeToo) Color.White else Terracotta
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (comments.isEmpty()) {
                Text(
                    text = "No comments yet.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(comments) { comment ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .padding(14.dp)
                        ) {
                            Text(
                                text = comment.authorSessionAlias,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                placeholder = { Text("Add an anonymous comment...", color = TextSecondary) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Terracotta,
                    unfocusedBorderColor = Terracotta.copy(alpha = 0.4f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
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
                                comments = response.body()!!.comments
                                currentPost = currentPost.copy(commentCount = comments.size)
                                commentText = ""
                            }
                        } catch (e: Exception) {
            android.util.Log.e("MIRA", "Request failed in PostDetailScreen", e)
                            // silently ignore for now
                        } finally {
                            isSendingComment = false
                        }
                    }
                }
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (!isSendingComment && commentText.isNotBlank()) Terracotta else TextSecondary
                )
            }
        }
    }
}
