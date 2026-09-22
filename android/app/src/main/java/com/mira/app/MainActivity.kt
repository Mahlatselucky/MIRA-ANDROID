package com.mira.app

/**
 * Entry point of the app. Sets up the theme and hosts the navigation
 * between the login, register, rooms, feed, post, journal and settings screens.
 */

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.mira.app.data.TokenManager
import com.mira.app.model.Post
import com.mira.app.model.Room
import com.mira.app.network.RetrofitClient
import com.mira.app.ui.auth.LoginScreen
import com.mira.app.ui.auth.RegisterScreen
import com.mira.app.ui.feed.RoomFeedScreen
import com.mira.app.ui.journal.JournalScreen
import com.mira.app.ui.post.CreatePostScreen
import com.mira.app.ui.post.PostDetailScreen
import com.mira.app.ui.rooms.RoomListScreen
import com.mira.app.ui.settings.SettingsScreen
import com.mira.app.ui.theme.MIRATheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        RetrofitClient.init(applicationContext)
        setContent {
            MIRATheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        var loggedInToken by remember {
                            mutableStateOf(TokenManager.getInstance(applicationContext).getToken())
                        }
                        var showRegister by remember { mutableStateOf(false) }

                        when {
                            loggedInToken != null -> {
                                MiraHome(onLogout = { loggedInToken = null })
                            }
                            showRegister -> {
                                RegisterScreen(
                                    onRegisterSuccess = { token -> loggedInToken = token },
                                    onNavigateToLogin = { showRegister = false }
                                )
                            }
                            else -> {
                                LoginScreen(
                                    onLoginSuccess = { token -> loggedInToken = token },
                                    onNavigateToRegister = { showRegister = true }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class HomeScreenState { LIST, FEED, CREATE_POST, POST_DETAIL }

@Composable
fun MiraHome(onLogout: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedRoom by remember { mutableStateOf<Room?>(null) }
    var selectedPost by remember { mutableStateOf<Post?>(null) }
    var screenState by remember { mutableStateOf(HomeScreenState.LIST) }

    if (selectedTab == 0) {
        when (screenState) {
            HomeScreenState.FEED -> {
                RoomFeedScreen(
                    room = selectedRoom!!,
                    onBack = {
                        selectedRoom = null
                        screenState = HomeScreenState.LIST
                    },
                    onCreatePost = { screenState = HomeScreenState.CREATE_POST },
                    onPostClick = { post ->
                        selectedPost = post
                        screenState = HomeScreenState.POST_DETAIL
                    }
                )
                return
            }
            HomeScreenState.CREATE_POST -> {
                CreatePostScreen(
                    room = selectedRoom!!,
                    onBack = { screenState = HomeScreenState.FEED },
                    onPostCreated = { screenState = HomeScreenState.FEED }
                )
                return
            }
            HomeScreenState.POST_DETAIL -> {
                PostDetailScreen(
                    post = selectedPost!!,
                    onBack = {
                        selectedPost = null
                        screenState = HomeScreenState.FEED
                    }
                )
                return
            }
            HomeScreenState.LIST -> Unit
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Journal") },
                    label = { Text("Journal") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> RoomListScreen(
                    onRoomClick = { room ->
                        selectedRoom = room
                        screenState = HomeScreenState.FEED
                    }
                )
                1 -> JournalScreen()
                2 -> SettingsScreen(onLogout = onLogout)
            }
        }
    }
}
