package com.team21.myapplication.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.team21.myapplication.data.model.ForumPost
import com.team21.myapplication.data.model.ThreadForum
import kotlinx.coroutines.tasks.await

class ThreadForumRepository {
    private val db = FirebaseFirestore.getInstance()
    private val col = db.collection("ThreadForum")

    suspend fun getThreads(): List<ThreadForum> {
        val snapshot = col.get().await()
        return snapshot.documents.mapNotNull { d -> d.toObject(ThreadForum::class.java) }
    }

    suspend fun getThreadForumPosts(threadId: String): List<ForumPost> {
        val threadRef = col.document(threadId)
        val postsSnapshot = threadRef.collection("ForumPost").get().await()

        return postsSnapshot.documents.mapNotNull { d ->
            d.toObject(ForumPost::class.java)
        }
    }

}