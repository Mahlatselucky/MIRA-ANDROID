package com.mira.app.ui.rooms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.mira.app.model.Room
import com.mira.app.network.RetrofitClient
import com.mira.app.ui.theme.TextSecondary

@Composable
fun RoomListScreen(
    onRoomClick: (Room) -> Unit
) {
    var rooms by remember { mutableStateOf<List<Room>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.apiService.getRooms()
            if (response.isSuccessful && response.body() != null) {
                rooms = response.body()!!.map { r ->
                    val count = try {
                        RetrofitClient.apiService.getRoomPosts(r.roomId).body()?.size ?: 0
                    } catch (e: Exception) {
                        0
                    }
                    r.copy(postCount = count)
                }
            } else {
                errorMessage = "Could not load rooms"
            }
        } catch (e: Exception) {
            errorMessage = "Could not reach the server. Check your connection."
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = "Rooms", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "Pick a space that fits what you're going through",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = errorMessage!!, fontSize = 14.sp, color = TextSecondary)
                }
            }
            rooms.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No rooms available yet", fontSize = 14.sp, color = TextSecondary)
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(rooms) { room ->
                        RoomCard(room = room, onClick = { onRoomClick(room) })
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomCard(room: Room, onClick: () -> Unit) {
    val tagColor = roomAccentColor(room)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(tagColor.copy(alpha = 0.16f))
            .clickable { onClick() }
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(tagColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = roomIcon(room),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = room.name, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (room.postCount == 1) "1 person here" else "${room.postCount} people here",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
    }
}
