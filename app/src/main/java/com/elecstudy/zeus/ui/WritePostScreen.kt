package com.elecstudy.zeus.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.elecstudy.zeus.firebase.Post
import com.elecstudy.zeus.ui.theme.ZeusBlack
import com.elecstudy.zeus.ui.theme.ZeusCard
import com.elecstudy.zeus.ui.theme.ZeusDark
import com.elecstudy.zeus.ui.theme.ZeusElectric
import com.elecstudy.zeus.ui.theme.ZeusTextDim
import com.elecstudy.zeus.ui.theme.ZeusTextLight


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WritePostScreen(
    post: Post? = null,
    onPostSave: (title: String, content: String, imageUris: List<Uri>, category: String) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(TextFieldValue(post?.title ?: "")) }
    var content by remember { mutableStateOf(TextFieldValue(post?.content ?: "")) }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf(post?.category ?: "") }
    var expanded by remember { mutableStateOf(false) }

    var categories by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(Unit) {
        com.elecstudy.zeus.firebase.FirebasePostRepository.getCategoriesFlow().collect { fetched: List<String> ->
            val filtered = fetched.filter { it != "전체" && it != "기타" }
            categories = filtered

            if (selectedCategory == "기타" && categories.isNotEmpty()) {
                selectedCategory = categories.first()
            } else if (selectedCategory.isEmpty() && categories.isNotEmpty()) {
                selectedCategory = categories.first()
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        selectedImageUris = uris
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (post == null || post.id.isEmpty()) "글 작성" else "글 수정",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = ZeusElectric
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onCancel() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로", tint = ZeusElectric)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ZeusElectric,
                        containerColor = ZeusCard
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ZeusElectric)
                ) {
                    Text("카테고리: $selectedCategory", style = MaterialTheme.typography.bodyLarge)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth().background(ZeusCard)
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category, color = ZeusTextLight) },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("제목") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ZeusTextLight,
                    unfocusedTextColor = ZeusTextLight,
                    cursorColor = ZeusElectric,
                    focusedBorderColor = ZeusElectric,
                    unfocusedBorderColor = ZeusTextDim,
                    focusedLabelColor = ZeusElectric,
                    unfocusedLabelColor = ZeusTextDim,
                ),
                singleLine = true
            )

            // Content Input
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("내용") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Fill available space
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ZeusTextLight,
                    unfocusedTextColor = ZeusTextLight,
                    cursorColor = ZeusElectric,
                    focusedBorderColor = ZeusElectric,
                    unfocusedBorderColor = ZeusTextDim,
                    focusedLabelColor = ZeusElectric,
                    unfocusedLabelColor = ZeusTextDim,
                ),
                textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Start)
            )

            val hasImages = selectedImageUris.isNotEmpty() || (post?.imageUrls?.isNotEmpty() == true)

            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ZeusCard)
                        .clickable { imagePickerLauncher.launch("image/*") }
                        .border(1.dp, ZeusTextDim, RoundedCornerShape(12.dp)),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(Icons.Filled.Add, contentDescription = "이미지 추가", tint = ZeusTextDim)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("이미지 추가 (여러 장 가능)", color = ZeusTextDim)
                    }
                }

                if (hasImages) {
                    Spacer(modifier = Modifier.height(12.dp))
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        items(selectedImageUris.size) { index ->
                            Image(
                                painter = rememberAsyncImagePainter(model = selectedImageUris[index]),
                                contentDescription = "선택된 이미지",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, ZeusElectric, RoundedCornerShape(8.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }

                        if (post != null && selectedImageUris.isEmpty()) {
                            val existingImages = post.imageUrls ?: emptyList()
                            items(existingImages.size) { index ->
                                Image(
                                    painter = rememberAsyncImagePainter(model = existingImages[index]),
                                    contentDescription = "기존 이미지",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, ZeusTextDim, RoundedCornerShape(8.dp)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    onPostSave(title.text, content.text, selectedImageUris, selectedCategory)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ZeusElectric),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "저장하기",
                    color = ZeusBlack,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        }
    }
}