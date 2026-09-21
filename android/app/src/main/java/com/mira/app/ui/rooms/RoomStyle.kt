package com.mira.app.ui.rooms

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.mira.app.model.Room
import com.mira.app.ui.theme.Gold
import com.mira.app.ui.theme.Lavender
import com.mira.app.ui.theme.Teal
import com.mira.app.ui.theme.Terracotta
import com.mira.app.ui.theme.TerracottaDark

fun roomAccentColor(room: Room): Color {
    val n = room.name.lowercase()
    return when {
        "anxiety" in n || "depress" in n -> Lavender
        "skin" in n || "acne" in n -> Terracotta
        "family" in n -> Teal
        "gambl" in n -> Gold
        "substance" in n -> TerracottaDark
        else -> try {
            Color(android.graphics.Color.parseColor(room.colorTag))
        } catch (e: Exception) {
            Terracotta
        }
    }
}

fun roomIcon(room: Room): ImageVector {
    val n = room.name.lowercase()
    return when {
        "anxiety" in n || "depress" in n -> Icons.Filled.Favorite
        "skin" in n || "acne" in n -> Icons.Filled.Face
        "family" in n -> Icons.Filled.Home
        "gambl" in n -> Icons.Filled.Star
        "substance" in n -> Icons.Filled.Refresh
        else -> Icons.Filled.Favorite
    }
}
