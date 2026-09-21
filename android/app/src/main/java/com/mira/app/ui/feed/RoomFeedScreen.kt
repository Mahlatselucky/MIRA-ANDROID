package com.mira.app.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mira.app.model.Post
import com.mira.app.model.Room
import com.mira.app.network.RetrofitClient
import com.mira.app.ui.rooms.roomAccentColor
import com.mira.app.ui.theme.Cream
import com.mira.app.ui.theme.Terracotta
import com.mira.app.ui.theme.TextSecondary
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

    val roomColor = roomAccentColor(room)

    suspend fun loadPosts() {
        try {
            val response = RetrofitClient.apiService.getRoomPosts(room.roomId)
            if (response.isSuccessful && response.body() != null) {
                posts = response.body()!!
                errorMessage = null
            } else {
                errorMessage = "Could not load posts"
            }
        } catch (e: Exception) {
            android.util.Log.e("MIRA", "Request failed in RoomFeedScreen", e)
            errorMessage = "Could not reach the server. Check your connection."
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(room.roomId) {
        loadPosts()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(roomColor)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Cream)
                }
                Text(
                    text = room.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Cream,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            Text(
                text = "You're not the only one.",
                fontSize = 13.sp,
                color = Cream,
                modifier = Modifier.padding(start = 52.dp, top = 2.dp)
            )
        }

        if (room.isSensitive && !room.crisisResourceLink.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Terracotta.copy(alpha = 0.15f))
                    .padding(12.dp)
            ) {
                Text(text = room.crisisResourceLink, fontSize = 12.sp)
            }
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = roomColor)
                }
            }
            errorMessage != null -> {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text(text = errorMessage!!, fontSize = 14.sp, color = TextSecondary)
                }
            }
            posts.isEmpty() -> {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No posts here yet.\nBe the first to share.",
                        fontSize = 15.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(posts) { post ->
                        PostCard(
                            post = post,
                            roomColor = roomColor,
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
            android.util.Log.e("MIRA", "Request failed in RoomFeedScreen", e)
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
            colors = ButtonDefaults.buttonColors(containerColor = roomColor),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Share what's on your mind", color = Cream)
        }
    }
}

@Composable
private fun PostCard(post: Post, roomColor: Color, onClick: () -> Unit, onMeTooClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(roomColor)
        )
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = post.authorSessionAlias,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = roomColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = post.content, fontSize = 15.sp, lineHeight = 21.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (post.userHasTappedMeToo) roomColor.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.background
                        )
                        .clickable { onMeTooClick() }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Me too ${post.meTooCount}",
                        fontSize = 13.sp,
                        fontWeight = if (post.userHasTappedMeToo) FontWeight.Bold else FontWeight.Normal,
                        color = if (post.userHasTappedMeToo) roomColor else TextSecondary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${post.comments.size} comments",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
