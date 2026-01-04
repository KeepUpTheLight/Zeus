package com.elecstudy.zeus.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.SerialName

import kotlinx.serialization.Serializable
import java.io.File
import java.lang.Exception


private const val SUPABASE_URL = "https://djpduxvqoxtxfoilxhdu.supabase.co"
private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRqcGR1eHZxb3h0eGZvaWx4aGR1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQ0ODM0MjgsImV4cCI6MjA4MDA1OTQyOH0.rxpc9xRvZzaZIItcYBie30-Vl2XclK7HLIeMF7ks3vw"
private const val STORAGE_BUCKET_NAME = "Zeus"


@Serializable
data class Post(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("image_urls") val imageUrls: List<String>? = null,
    val category: String = "",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class Category(
    val id: String = "",
    val name: String
)

@Serializable
data class PostInsert(
    val title: String,
    val content: String,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("image_urls") val imageUrls: List<String> = emptyList(),
    val category: String
)



object FirebasePostRepository {

    private val supabaseClient = createSupabaseClient(
        SUPABASE_URL, SUPABASE_ANON_KEY
    ) {
        install(Storage)
        install(Auth)
        install(Postgrest)
    }

    suspend fun signIn(email: String, pass: String) {
        supabaseClient.auth.signInWith(Email) {
            this.email = email
            password = pass
        }
    }

    suspend fun signUp(email: String, pass: String) {
        supabaseClient.auth.signUpWith(Email) {
            this.email = email
            password = pass
        }
    }

    suspend fun signOut() {
        supabaseClient.auth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        // Simple check, potentially check currentSessionOrNull()
        return supabaseClient.auth.currentSessionOrNull() != null
    }

    // Expose flow for auth state if needed, or just check session
    val sessionStatus = supabaseClient.auth.sessionStatus


    private val TAG = "FirebasePostRepo"
    
    // Internal state flow for categories
    private val _categoriesFlow = MutableStateFlow<List<String>>(emptyList())
    // Public getter if needed, but we keep getCategoriesFlow() signature
    



    suspend fun uploadImageToSupabase(context: Context, imageUri: Uri): String {
        val fileName = "post_${System.currentTimeMillis()}.jpg"
        val tempFile = File(context.cacheDir, fileName)


        val bytes = context.contentResolver.openInputStream(imageUri)?.use { input ->
            input.readBytes()
        } ?: throw Error("Failed to read image bytes")


        try {
            val storagePath = "public/$fileName"
            val result = supabaseClient.storage.from(STORAGE_BUCKET_NAME).upload(storagePath, bytes)

            return "$SUPABASE_URL/storage/v1/object/public/$STORAGE_BUCKET_NAME/$storagePath"
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image to Supabase", e)
            throw e
        } finally {

            if (tempFile.exists()) {
                tempFile.delete()
            }
        }
    }


    suspend fun addPost(title: String, content: String, imageUris: List<Uri>, category: String, context: Context) {
        val uploadedImageUrls = mutableListOf<String>()

        try {
            for (uri in imageUris) {
                val url = uploadImageToSupabase(context, uri)
                uploadedImageUrls.add(url)
            }

            val mainImageUrl = uploadedImageUrls.firstOrNull()

            val post = Post(
                title = title,
                content = content,
                imageUrl = mainImageUrl,
                imageUrls = uploadedImageUrls,
                category = category
            )

            
            val postInsert = PostInsert(
                title = title,
                content = content,
                imageUrl = mainImageUrl,
                imageUrls = uploadedImageUrls,
                category = category
            )

            supabaseClient.from("posts").insert(postInsert)
            Log.d(TAG, "Post added successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding post", e)
            throw e
        }
    }


    suspend fun updatePost(post: Post) {
        try {
             // We can insert the object directly if we exclude ID from the update payload or if `upsert` handles it.
             // But for `update`, we should specify columns.
             // Best to use a Map or partial update.
             val updateData = PostInsert(
                 title = post.title,
                 content = post.content,
                 imageUrl = post.imageUrl,
                 imageUrls = post.imageUrls ?: emptyList(),
                 category = post.category
             )
             
            supabaseClient.from("posts").update(updateData) {
                filter {
                    eq("id", post.id)
                }
            }
            Log.d(TAG, "Post updated: ${post.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating post", e)
            throw e
        }
    }

    suspend fun deletePost(postId: String) {
        try {
            // 1. Fetch the post to get image URLs
            val post = supabaseClient.from("posts").select {
                filter { eq("id", postId) }
            }.decodeSingleOrNull<Post>()

            if (post != null) {
                // 2. Extract image paths
                val pathsToDelete = mutableListOf<String>()
                val allImages = (post.imageUrls ?: emptyList()) + listOfNotNull(post.imageUrl)
                
                allImages.forEach { url ->
                    // Expected URL format: .../storage/v1/object/public/Zeus/public/filename.jpg
                    // We need the path inside the bucket: public/filename.jpg
                    if (url.contains("/$STORAGE_BUCKET_NAME/")) {
                        val path = url.substringAfter("/$STORAGE_BUCKET_NAME/")
                        pathsToDelete.add(path)
                    }
                }

                // 3. Delete images from storage
                if (pathsToDelete.isNotEmpty()) {
                    try {
                        Log.d(TAG, "Attempting to delete image paths: $pathsToDelete")
                        supabaseClient.storage.from(STORAGE_BUCKET_NAME).delete(pathsToDelete)
                        Log.d(TAG, "Storage deletion request completed.")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to delete images from storage", e)
                        // Continue to delete post even if image delete fails
                    }
                }
            }

            // 4. Delete post from DB
            supabaseClient.from("posts").delete {
                filter {
                    eq("id", postId)
                }
            }
            Log.d(TAG, "Post deleted: $postId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting post", e)
            throw e
        }
    }

    suspend fun fetchPosts(): List<Post> {
        return try {
            val result = supabaseClient.from("posts").select().decodeList<Post>()
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching posts", e)
            emptyList()
        }
    }

    suspend fun searchPosts(query: String): List<Post> {
        return try {
            val result = supabaseClient.from("posts").select {
                filter {
                    or {
                        ilike("title", "%$query%")
                        ilike("content", "%$query%")
                    }
                }
            }.decodeList<Post>()
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error searching posts", e)
            emptyList()
        }
    }


    fun getCategoriesFlow(): Flow<List<String>> {
        // Trigger initial fetch if empty (optional, but good practice)
        if (_categoriesFlow.value.isEmpty()) {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                refreshCategories()
            }
        }
        return _categoriesFlow.asStateFlow()
    }

    private suspend fun refreshCategories() {
        try {
            val categories = fetchCategories()
            _categoriesFlow.emit(categories)
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing categories", e)
        }
    }

    suspend fun fetchCategories(): List<String> {
        return try {
            val result = supabaseClient.from("categories").select().decodeList<Category>()
            result.map { it.name }.sorted()
        } catch (e: Exception) {
             Log.e(TAG, "Error fetching categories", e)
             emptyList()
        }
    }

    suspend fun addCategory(name: String) {
        try {
            val category = Category(name = name)
            // exclude ID from insert if possible, or use map
             val catMap = mapOf("name" to name)
            supabaseClient.from("categories").insert(catMap)
            refreshCategories() // Refresh list after add
        } catch (e: Exception) {
            Log.e(TAG, "Error adding category", e)
            throw e
        }
    }

    suspend fun deleteCategory(name: String) {
        try {
            supabaseClient.from("categories").delete {
                filter {
                    eq("name", name)
                }
            }
            refreshCategories() // Refresh list after delete
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting category", e)
            throw e
        }
    }
}