package com.elecstudy.zeus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.elecstudy.zeus.ui.MainScreen
import com.elecstudy.zeus.ui.theme.ZeusTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            ZeusTheme {
                 MainScreen()
            }
        }
    }
}
