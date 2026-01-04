package com.elecstudy.zeus.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elecstudy.zeus.ui.theme.ZeusDark
import com.elecstudy.zeus.ui.theme.ZeusElectric

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onLoginSuccess: () -> Unit, // Added callback
    isLoggedIn: Boolean
) {
    var currentScreen by remember { mutableStateOf(Screen.Home) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = ZeusDark,
                contentColor = ZeusElectric,
                modifier = Modifier.fillMaxWidth()
            ) {
                NavigationBarItem(
                    selected = currentScreen == Screen.Home,
                    onClick = { currentScreen = Screen.Home },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "홈") },
                    label = { Text("홈") },
                    alwaysShowLabel = false,
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
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ZeusDark,
                        selectedTextColor = ZeusElectric,
                        indicatorColor = ZeusElectric,
                        unselectedIconColor = ZeusElectric.copy(alpha = 0.6f),
                        unselectedTextColor = ZeusElectric.copy(alpha = 0.6f)
                    )
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.Search,
                    onClick = { currentScreen = Screen.Search },
                    icon = { Icon(Icons.Filled.Search, contentDescription = "검색") },
                    label = { Text("검색") },
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ZeusDark,
                        selectedTextColor = ZeusElectric,
                        indicatorColor = ZeusElectric,
                        unselectedIconColor = ZeusElectric.copy(alpha = 0.6f),
                        unselectedTextColor = ZeusElectric.copy(alpha = 0.6f)
                    )
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.Profile,
                    onClick = { currentScreen = Screen.Profile },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "내 정보") },
                    label = { Text(if (isLoggedIn) "로그아웃" else "로그인") },
                    alwaysShowLabel = false,
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
                Screen.Search -> SearchScreen(
                    onBack = { /* No back button in main nav */ },
                    onPostClick = { /* Handle post click inside SearchScreen internally or lift state later */ }
                )
                Screen.Profile -> {
                    if (isLoggedIn) {
                        ProfileScreen(onLogoutClick = onLogoutClick)
                    } else {
                        LoginScreen(onLoginSuccess = onLoginSuccess)
                    }
                }
            }
        }
    }
}

enum class Screen {
    Home, Board, Search, Profile
}

@Composable
fun ProfileScreen(onLogoutClick: () -> Unit) {
    androidx.compose.foundation.layout.Column(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text("로그인 상태입니다.", color = ZeusElectric, style = MaterialTheme.typography.titleMedium)
        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
        Button(
            onClick = onLogoutClick,
            colors = ButtonDefaults.buttonColors(containerColor = com.elecstudy.zeus.ui.theme.ZeusError)
        ) {
            Text("로그아웃", color = ZeusDark, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }
    }
}