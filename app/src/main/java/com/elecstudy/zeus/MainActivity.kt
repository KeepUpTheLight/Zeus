package com.elecstudy.zeus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.elecstudy.zeus.ui.MainScreen
import com.elecstudy.zeus.ui.theme.ZeusTheme

import com.elecstudy.zeus.ui.LoginScreen
import com.elecstudy.zeus.ui.SplashScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            ZeusTheme {
                var showSplashScreen by remember { mutableStateOf(true) }
                var showLoginScreen by remember { mutableStateOf(false) }
                var isLoggedIn by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()

                // Check initial login state
                LaunchedEffect(Unit) {
                    isLoggedIn = com.elecstudy.zeus.firebase.FirebasePostRepository.isUserLoggedIn()
                }

                if (showSplashScreen) {
                    SplashScreen(onSplashFinished = { showSplashScreen = false })
                } else if (showLoginScreen) {
                    LoginScreen(onLoginSuccess = {
                        showLoginScreen = false
                        isLoggedIn = true
                    })
                } else {
                    MainScreen(
                        onLoginClick = { showLoginScreen = true },
                        onLogoutClick = {
                            scope.launch {
                                com.elecstudy.zeus.firebase.FirebasePostRepository.signOut()
                                isLoggedIn = false
                            }
                        },
                        onLoginSuccess = {
                            isLoggedIn = true
                        },
                        isLoggedIn = isLoggedIn
                    )
                }
            }
        }
    }
}