package com.elecstudy.zeus.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseTest {

    private val TAG = "FirebaseTest"
    private val db = FirebaseFirestore.getInstance()

    fun addSamplePost() {
        val post = hashMapOf(
            "title" to "테스트 제목",
            "content" to "테스트 내용"
        )

        db.collection("posts")
            .add(post)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding document", e)
            }
    }

    fun fetchPosts() {
        db.collection("posts")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching documents", e)
            }
    }
}
