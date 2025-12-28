package com.elecstudy.zeus.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(5000)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()

    ) {
        DotLottieAnimation(
            source = DotLottieSource.Url("https://lottie.host/33a7e57e-4ae6-4ad2-a4e5-38793f19b9f3/qIGvVWLBCV.lottie"),
            autoplay = true,
            loop = true,
            speed = 1f,
            useFrameInterpolation = false,
            modifier = Modifier.fillMaxSize(),

        )
    }
}