package com.elecstudy.zeus.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.elecstudy.zeus.firebase.Post
import com.elecstudy.zeus.ui.theme.*
import androidx.compose.ui.graphics.Color


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewPostScreen(
    post: Post,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showImageViewer by remember { mutableStateOf(false) }
    var initialPage by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "글 보기", 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = ZeusElectric
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로", tint = ZeusElectric)
                    }
                },
                actions = {

                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "수정", tint = ZeusTextDim)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "삭제", tint = ZeusError)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = ZeusDark)
            )
        },
        containerColor = ZeusDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
        ) {
            // Hero Image (Multiple)
            if (post.imageUrls.isNotEmpty()) {
                post.imageUrls.forEachIndexed { index, url ->
                    Image(
                        painter = rememberAsyncImagePainter(url),
                        contentDescription = "이미지",
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(bottom = 16.dp)
                            .clickable {
                                initialPage = index
                                showImageViewer = true
                            },
                        contentScale = androidx.compose.ui.layout.ContentScale.FillWidth
                    )
                }
            } else if (post.imageUrl != null) {
                // Backward compatibility
                Image(
                    painter = rememberAsyncImagePainter(post.imageUrl),
                    contentDescription = "이미지",
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(bottom = 16.dp)
                        .clickable {
                            initialPage = 0
                            showImageViewer = true
                        },
                    contentScale = androidx.compose.ui.layout.ContentScale.FillWidth
                )
            }

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category Chip
                Surface(
                    color = ZeusElectric.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = post.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = ZeusElectric,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }

                Text(
                    post.title, 
                    style = MaterialTheme.typography.headlineMedium, 
                    color = ZeusTextLight,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                
                HorizontalDivider(color = ZeusTextDim.copy(alpha = 0.2f))

                Text(
                    post.content, 
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp), 
                    color = ZeusTextLight.copy(alpha = 0.9f)
                )
            }
        }

        if (showImageViewer) {
            val images = if (post.imageUrls.isNotEmpty()) post.imageUrls else listOfNotNull(post.imageUrl)
            FullScreenImageViewer(
                imageUrls = images,
                initialPage = initialPage,
                onDismiss = { showImageViewer = false }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullScreenImageViewer(
    imageUrls: List<String>,
    initialPage: Int,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        val pagerState = rememberPagerState(
            initialPage = initialPage,
            pageCount = { imageUrls.size }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                var scale by remember { mutableFloatStateOf(1f) }
                var offset by remember { mutableStateOf(Offset.Zero) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RectangleShape)
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                awaitFirstDown()
                                do {
                                    val event = awaitPointerEvent()
                                    val zoom = event.calculateZoom()
                                    val pan = event.calculatePan()
                                    
                                    // If we are zoomed in, or if we are zooming (2+ fingers), we handle it.
                                    if (scale > 1f || zoom != 1f) {
                                        scale = (scale * zoom).coerceIn(1f, 3f)
                                        val maxOffset = (scale - 1) * size.width / 2
                                        val newOffset = offset + pan
                                        offset = Offset(
                                            x = newOffset.x.coerceIn(-maxOffset, maxOffset),
                                            y = newOffset.y.coerceIn(-maxOffset, maxOffset)
                                        )
                                        
                                        // Consume events to prevent Pager from scrolling
                                        event.changes.forEach { 
                                            if (it.positionChanged()) {
                                                it.consume() 
                                            }
                                        }
                                    }
                                } while (event.changes.any { it.pressed })
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUrls[page]),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                            },
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                }
            }

            // Close Button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }

            // Page Indicator
            if (imageUrls.size > 1) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${imageUrls.size}",
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
