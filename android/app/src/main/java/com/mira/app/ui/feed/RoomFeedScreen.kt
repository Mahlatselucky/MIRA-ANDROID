package com.mira.app.ui.feed

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mira.app.model.Post
import com.mira.app.model.Room
import com.mira.app.network.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun RoomFeedScreen(
    room: Room,
    onBack: () -> Unit,
    onCreatePost: () -> Unit,
    onPostClick: (Post) -> Unit
) {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun loadPosts() {
        try {
            val response = RetrofitClient.apiService.getRoomPosts(room.roomId)
            if (response.isSuccessful && response.body() != null) {
                posts = response.body()!!.posts
                errorMessage = null
            } else {
                errorMessage = "Could not load posts"
            }
        } catch (e: Exception) {
            errorMessage = "Could not reach the server. Check your connection."
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(room.roomId) {
        loadPosts()
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
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(text = room.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = "You're not the only one.", fontSize = 12.sp)
            }
        }

        if (room.isSensitive && !room.crisisResourceLink.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(12.dp)
            ) {
                Text(text = room.crisisResourceLink, fontSize = 12.sp)
            }
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = errorMessage!!, fontSize = 14.sp)
                }
            }
            posts.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No posts here yet. Be the first to share.", fontSize = 14.sp)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(posts) { post ->
                        PostCard(
                            post = post,
                            onClick = { onPostClick(post) },
                            onMeTooClick = {
                                scope.launch {
                                    try {
                                        val response = RetrofitClient.apiService.toggleMeToo(post.postId)
                                        if (response.isSuccessful && response.body() != null) {
                                            val updated = response.body()!!
                                            posts = posts.map {
                                                if (it.postId == post.postId) {
                                                    it.copy(
                                                        meTooCount = updated.meTooCount,
                                                        userHasTappedMeToo = updated.userHasTappedMeToo
                                                    )
                                                } else it
                                            }
                                        }
                                    } catch (e: Exception) {
                                        // silently ignore for now, tap will just not update
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        Button(
            onClick = onCreatePost,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Share what's on your mind")
        }
    }
}

@Composable
private fun PostCard(post: Post, onClick: () -> Unit, onMeTooClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(text = post.authorSessionAlias, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = post.content, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMeTooClick, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = if (post.userHasTappedMeToo) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Me too"
                )
            }
            Text(text = "Me too ${post.meTooCount}", fontSize = 13.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "${post.commentCount} comments", fontSize = 13.sp)
        }
    }
}
