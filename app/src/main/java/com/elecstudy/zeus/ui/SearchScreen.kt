package com.elecstudy.zeus.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elecstudy.zeus.firebase.FirebasePostRepository
import com.elecstudy.zeus.firebase.Post
import com.elecstudy.zeus.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onPostClick: (Post) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf(listOf<Post>()) }
    var isLoading by remember { mutableStateOf(false) }
    var viewingPost by remember { mutableStateOf<Post?>(null) }
    val scope = rememberCoroutineScope()

    fun performSearch() {
        if (query.isBlank()) return
        isLoading = true
        scope.launch {
            searchResults = FirebasePostRepository.searchPosts(query)
            isLoading = false
        }
    }

    if (viewingPost != null) {
        ViewPostScreen(
            post = viewingPost!!,
            onBack = { viewingPost = null },
            onEdit = { /* Search screen is read-only for now or needs write logic */ },
            onDelete = { /* Search screen is read-only for now or needs delete logic */ }
        )
        return
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.elecstudy.zeus.R.drawable.thunder_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                alpha = 0.3f
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                // Header
                Text(
                    "검색",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ZeusElectric,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 20.dp, top = 10.dp)
                )

                // Search Bar
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                    },
                    placeholder = { Text("어떤 내용을 찾으시나요?", color = ZeusTextDim.copy(alpha = 0.5f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(24.dp))
                        .background(ZeusCard, RoundedCornerShape(24.dp)),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = ZeusElectric),
                    shape = RoundedCornerShape(24.dp),
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = "검색", tint = ZeusElectric)
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { performSearch() }) {
                                Icon(Icons.Filled.ArrowForward, contentDescription = "이동", tint = ZeusElectric)
                            }
                        }
                    },
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onDone = { performSearch() }
                    ),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Search
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Results or Empty State
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        CircularProgressIndicator(color = ZeusElectric)
                    }
                } else if (searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = ZeusTextDim.copy(alpha = 0.2f),
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                if (query.isEmpty()) "검색어를 입력해보세요" else "검색 결과가 없습니다",
                                style = MaterialTheme.typography.bodyLarge,
                                color = ZeusTextDim.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item {
                            Text(
                                "검색 결과 (${searchResults.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = ZeusTextDim,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(searchResults) { post ->
                            SearchPostItem(post = post, onClick = { viewingPost = post })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchPostItem(post: Post, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ZeusCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, ZeusTextDim.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = ZeusElectric.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = post.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = ZeusElectric,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                post.title,
                style = MaterialTheme.typography.titleMedium,
                color = ZeusTextLight,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = ZeusTextDim,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}
