package com.elecstudy.zeus.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.elecstudy.zeus.ui.theme.ZeusDark
import com.elecstudy.zeus.ui.theme.ZeusElectric

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    isLoggedIn: Boolean
) {
    var currentScreen by remember { mutableStateOf(Screen.Home) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = ZeusDark,
                contentColor = ZeusElectric
            ) {
                NavigationBarItem(
                    selected = currentScreen == Screen.Home,
                    onClick = { currentScreen = Screen.Home },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "홈") },
                    label = { Text("홈") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ZeusDark,
                        selectedTextColor = ZeusElectric,
                        indicatorColor = ZeusElectric,
                        unselectedIconColor = ZeusElectric.copy(alpha = 0.6f),
                        unselectedTextColor = ZeusElectric.copy(alpha = 0.6f)
                    )
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.Board,
                    onClick = { currentScreen = Screen.Board },
                    icon = { Icon(Icons.Filled.List, contentDescription = "게시판") },
                    label = { Text("게시판") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ZeusDark,
                        selectedTextColor = ZeusElectric,
                        indicatorColor = ZeusElectric,
                        unselectedIconColor = ZeusElectric.copy(alpha = 0.6f),
                        unselectedTextColor = ZeusElectric.copy(alpha = 0.6f)
                    )
                )
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier.padding(padding),
            color = ZeusDark
        ) {
            when (currentScreen) {
                Screen.Home -> HomeScreen(onPostClick = { /* Navigate to post details if needed */ })
                Screen.Board -> PostListScreen(
                    onLoginClick = onLoginClick,
                    onLogoutClick = onLogoutClick,
                    isLoggedIn = isLoggedIn
                )
            }
        }
    }
}

enum class Screen {
    Home, Board
}