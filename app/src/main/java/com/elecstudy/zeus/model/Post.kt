package com.elecstudy.zeus.model

import android.net.Uri

data class Post(
    val title: String,
    val content: String,
    val imageUri: Uri? = null
)
