package com.elecstudy.zeus.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.elecstudy.zeus.firebase.FirebasePostRepository
import com.elecstudy.zeus.firebase.Post
import com.elecstudy.zeus.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures



import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.Search

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PostListScreen(
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    isLoggedIn: Boolean
) {
    var posts by remember { mutableStateOf(listOf<Post>()) }
    var showWriteScreen by remember { mutableStateOf(false) }
    var editingPost by remember { mutableStateOf<Post?>(null) }
    var viewingPost by remember { mutableStateOf<Post?>(null) }


    var categories by remember { mutableStateOf(listOf<String>()) }
    var selectedCategory by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        FirebasePostRepository.getCategoriesFlow().collect { fetched ->
            val filtered = fetched.filter { it != "전체" && it != "기타" }
            categories = filtered
            if (selectedCategory.isEmpty() && categories.isNotEmpty()) {
                selectedCategory = categories.first()
            } else if (selectedCategory.isNotEmpty() && !categories.contains(selectedCategory)) {
                selectedCategory = if (categories.isNotEmpty()) categories.first() else ""
            }
        }
    }

    LaunchedEffect(Unit) {
        posts = try {
            FirebasePostRepository.fetchPosts()
        } catch (e: Exception) {
            emptyList()
        }
    }

    val filteredPosts = posts.filter { it.category == selectedCategory }

    val context = androidx.compose.ui.platform.LocalContext.current


    if (showWriteScreen) {
        WritePostScreen(
            post = editingPost ?: Post(category = selectedCategory),
            onPostSave = { title, content, imageUris, category ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        if (editingPost != null) {

                            val updatedPost = editingPost!!.copy(title = title, content = content, category = category)
                            FirebasePostRepository.updatePost(updatedPost)
                        } else {
                            FirebasePostRepository.addPost(title, content, imageUris, category, context)
                        }
                        val updatedPosts = FirebasePostRepository.fetchPosts()
                        launch(Dispatchers.Main) {
                            posts = updatedPosts
                            showWriteScreen = false
                            editingPost = null
                            android.widget.Toast.makeText(context, "저장되었습니다.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        launch(Dispatchers.Main) {
                            android.widget.Toast.makeText(context, "저장 실패: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                }
            },
            onCancel = {
                showWriteScreen = false
                editingPost = null
            }
        )
        return
    }

    if (viewingPost != null) {
        ViewPostScreen(
            post = viewingPost!!,
            onBack = { viewingPost = null },
            onEdit = { editingPost = viewingPost; showWriteScreen = true; viewingPost = null },
            onDelete = {
                CoroutineScope(Dispatchers.IO).launch {
                    FirebasePostRepository.deletePost(viewingPost!!.id)
                    posts = FirebasePostRepository.fetchPosts()
                    viewingPost = null
                }
            }
        )
        return
    }


    Scaffold(
        topBar = {
            Column {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp) // Tight layout height
                        .padding(top = 20.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(contentAlignment = Alignment.Center) {
                         androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.elecstudy.zeus.R.drawable.board_font),
                            contentDescription = "Zeus Board Logo",
                            modifier = Modifier.requiredHeight(150.dp), // Force visual height > layout height to crop internal padding
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    }
                }

                var showAddCategoryDialog by remember { mutableStateOf(false) }
                var newCategoryName by remember { mutableStateOf("") }

                var showDeleteCategoryDialog by remember { mutableStateOf(false) }
                var categoryToDelete by remember { mutableStateOf("") }

                if (showAddCategoryDialog) {
                    AlertDialog(
                        onDismissRequest = { showAddCategoryDialog = false },
                        title = { Text("카테고리 추가", color = ZeusElectric, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                        containerColor = ZeusCard,
                        text = {
                            Column {
                                Text("새 카테고리 이름을 입력하세요.", style = MaterialTheme.typography.bodyMedium, color = ZeusTextDim)
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = newCategoryName,
                                    onValueChange = { newCategoryName = it },
                                    placeholder = { Text("이름 입력") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = ZeusElectric,
                                        unfocusedTextColor = ZeusTextLight,
                                        cursorColor = ZeusElectric,
                                        focusedBorderColor = ZeusElectric,
                                        unfocusedBorderColor = ZeusTextDim,
                                        focusedLabelColor = ZeusElectric,
                                        unfocusedLabelColor = ZeusTextDim,
                                    )
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (newCategoryName.isNotBlank()) {
                                        val category = newCategoryName
                                        CoroutineScope(Dispatchers.IO).launch {
                                            FirebasePostRepository.addCategory(category)
                                        }
                                        newCategoryName = ""
                                        showAddCategoryDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ZeusElectric)
                            ) {
                                Text("추가", color = ZeusBlack, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAddCategoryDialog = false }) { Text("취소", color = ZeusTextDim) }
                        }
                    )
                }

                if (showDeleteCategoryDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteCategoryDialog = false },
                        title = { Text("카테고리 삭제", color = ZeusError, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                        containerColor = ZeusCard,
                        text = {
                            Text("'$categoryToDelete' 카테고리를 삭제하시겠습니까?", color = ZeusTextLight)
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        FirebasePostRepository.deleteCategory(categoryToDelete)
                                        launch(Dispatchers.Main) {
                                            if (selectedCategory == categoryToDelete) {
                                                selectedCategory = ""
                                            }
                                            showDeleteCategoryDialog = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ZeusError)
                            ) {
                                Text("삭제", color = ZeusTextLight, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteCategoryDialog = false }) { Text("취소", color = ZeusTextDim) }
                        }
                    )
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) ZeusElectric else ZeusCard,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, ZeusTextDim.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .height(36.dp)
                                .combinedClickable(
                                    onClick = { selectedCategory = category },
                                    onLongClick = {
                                        if (isLoggedIn) {
                                            categoryToDelete = category
                                            showDeleteCategoryDialog = true
                                        } else {
                                            android.widget.Toast.makeText(context, "로그인 해주세요", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = category,
                                    color = if (isSelected) ZeusBlack else ZeusTextLight,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                                )
                            }
                        }
                    }
                    item {
                        Surface(
                            onClick = {
                                if (isLoggedIn) {
                                    showAddCategoryDialog = true
                                } else {
                                    android.widget.Toast.makeText(context, "로그인 해주세요", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = CircleShape,
                            color = ZeusCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ZeusElectric.copy(alpha = 0.5f)),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Add, contentDescription = "카테고리 추가", tint = ZeusElectric, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isLoggedIn) {
                        showWriteScreen = true
                    } else {
                        android.widget.Toast.makeText(context, "로그인 해주세요", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                containerColor = ZeusElectric,
                contentColor = ZeusBlack,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "작성")
            }
        },
        containerColor = ZeusDark
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredPosts) { post ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .clickable {
                            if (isLoggedIn) {
                                viewingPost = post
                            } else {
                                android.widget.Toast.makeText(context, "로그인 해주세요", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ZeusCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ZeusTextDim.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(0.dp)) {

                        post.imageUrl?.let { url ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = url),
                                    contentDescription = "글 이미지",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, ZeusCard),
                                                startY = 100f
                                            )
                                        )
                                )
                            }
                        }

                        Column(modifier = Modifier.padding(16.dp)) {

                            Surface(
                                color = ZeusElectric.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Text(
                                    text = post.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ZeusElectric,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                            }


                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    post.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                                    color = ZeusTextLight,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )

                                Row {
                                    IconButton(
                                        onClick = {
                                            if (isLoggedIn) {
                                                editingPost = post
                                                showWriteScreen = true
                                            } else {
                                                android.widget.Toast.makeText(context, "로그인 해주세요", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Filled.Edit, contentDescription = "수정", tint = ZeusTextDim, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            if (isLoggedIn) {
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    FirebasePostRepository.deletePost(post.id)

                                                    val updatedPosts = FirebasePostRepository.fetchPosts()
                                                    launch(Dispatchers.Main) { posts = updatedPosts }
                                                }
                                            } else {
                                                android.widget.Toast.makeText(context, "로그인 해주세요", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "삭제", tint = ZeusError, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                post.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ZeusTextDim,
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                            Text(
                                sdf.format(Date()),
                                style = MaterialTheme.typography.labelSmall,
                                color = ZeusTextDim.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}