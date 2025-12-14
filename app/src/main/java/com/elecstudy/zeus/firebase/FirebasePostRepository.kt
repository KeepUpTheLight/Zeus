package com.elecstudy.zeus.firebase

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import java.io.File
import java.lang.Exception


private const val SUPABASE_URL = "https://djpduxvqoxtxfoilxhdu.supabase.co"
private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRqcGR1eHZxb3h0eGZvaWx4aGR1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQ0ODM0MjgsImV4cCI6MjA4MDA1OTQyOH0.rxpc9xRvZzaZIItcYBie30-Vl2XclK7HLIeMF7ks3vw"
private const val STORAGE_BUCKET_NAME = "Zeus"


data class Post(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val imageUrl: String? = null,
    val imageUrls: List<String> = emptyList(),
    val category: String = ""
)

interface SupabaseStorageService {
    @Multipart
    @POST("storage/v1/s3/{bucketName}/{filePath}")
    suspend fun uploadFile(
        @Path("bucketName") bucketName: String,
        @Path("filePath") filePath: String,
        @Header("Authorization") authorization: String,
        @Header("apikey") apiKey: String,
        @Header("Content-Type") contentType: String = "image/jpeg",
        @Part file: MultipartBody.Part,
        @Header("x-upsert") upsert: String = "true"
    ): Response<Unit>
}


object FirebasePostRepository {

    private val db = FirebaseFirestore.getInstance()
    private val TAG = "FirebasePostRepo"


    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(SUPABASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val supabaseStorageService: SupabaseStorageService = retrofit.create(SupabaseStorageService::class.java)


    suspend fun uploadImageToSupabase(context: Context, imageUri: Uri): String {
        val fileName = "post_${System.currentTimeMillis()}.jpg"
        val tempFile = File(context.cacheDir, fileName)


        val bytes = context.contentResolver.openInputStream(imageUri)?.use { input ->
            input.readBytes()
        } ?: throw Error("Failed to read image bytes")

        try {
            val client = createSupabaseClient(
                SUPABASE_URL, SUPABASE_ANON_KEY
            ) {
                install (Storage)
            }

            val storagePath = "public/$fileName"
            val result = client.storage.from(STORAGE_BUCKET_NAME).upload(storagePath, bytes)


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

            // For backward compatibility, set the first image as imageUrl
            val mainImageUrl = uploadedImageUrls.firstOrNull()

            val post = Post(
                title = title,
                content = content,
                imageUrl = mainImageUrl,
                imageUrls = uploadedImageUrls,
                category = category
            )

            db.collection("posts")
                .add(post)
                .await()
            Log.d(TAG, "Post added successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding post", e)
            throw e
        }
    }


    suspend fun updatePost(post: Post) {
        db.collection("posts").document(post.id)
            .set(post)
            .addOnSuccessListener { Log.d(TAG, "Post updated: ${post.id}") }
            .addOnFailureListener { e -> Log.e(TAG, "Error updating post", e) }
    }

    suspend fun deletePost(postId: String) {
        db.collection("posts").document(postId)
            .delete()
            .addOnSuccessListener { Log.d(TAG, "Post deleted: $postId") }
            .addOnFailureListener { e -> Log.e(TAG, "Error deleting post", e) }
    }

    suspend fun fetchPosts(): List<Post> {
        val snapshot = db.collection("posts").get().await()
        return snapshot.documents.map { doc ->
            val imageUrl = doc.getString("imageUrl")
            val imageUrls = doc.get("imageUrls") as? List<String> ?: emptyList()

            // If imageUrls is empty but imageUrl exists (old posts), add it to the list
            val finalImageUrls = if (imageUrls.isEmpty() && imageUrl != null) {
                listOf(imageUrl)
            } else {
                imageUrls
            }

            Post(
                id = doc.id,
                title = doc.getString("title") ?: "",
                content = doc.getString("content") ?: "",
                imageUrl = imageUrl,
                imageUrls = finalImageUrls,
                category = doc.getString("category") ?: "기타"
            )
        }
    }


    fun getCategoriesFlow(): kotlinx.coroutines.flow.Flow<List<String>> = kotlinx.coroutines.flow.callbackFlow {
        val listener = db.collection("categories").orderBy("name")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val categories = snapshot.documents.mapNotNull { it.getString("name") }
                    trySend(categories)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun fetchCategories(): List<String> {
        val snapshot = db.collection("categories").orderBy("name").get().await()
        return snapshot.documents.mapNotNull { it.getString("name") }
    }

    suspend fun addCategory(name: String) {
        val categoryMap = hashMapOf("name" to name)
        db.collection("categories").add(categoryMap).await()
    }

    suspend fun deleteCategory(name: String) {
        val snapshot = db.collection("categories").whereEqualTo("name", name).get().await()
        for (doc in snapshot.documents) {
            db.collection("categories").document(doc.id).delete().await()
        }
    }
}