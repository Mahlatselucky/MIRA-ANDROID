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
                rooms = response.body()!!
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
            Text(text = "Pick a space that fits what you're going through", fontSize = 14.sp)
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
            rooms.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No rooms available yet", fontSize = 14.sp)
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(rooms) { room ->
                        RoomRow(room = room, onClick = { onRoomClick(room) })
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomRow(room: Room, onClick: () -> Unit) {
    val tagColor = try {
        Color(android.graphics.Color.parseColor(room.colorTag))
    } catch (e: Exception) {
        Color.Gray
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(tagColor)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = room.name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(text = "${room.postCount} people here", fontSize = 13.sp)
        }
    }
}
