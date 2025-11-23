package com.team21.myapplication.data.repository

import android.content.Context
import android.util.Log
import android.util.LruCache
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.team21.myapplication.data.local.AppDatabase
import com.team21.myapplication.data.mappers.toForumPost
import com.team21.myapplication.data.mappers.toForumPostEntity
import com.team21.myapplication.data.mappers.toThreadForum
import com.team21.myapplication.data.mappers.toThreadForumEntity
import com.team21.myapplication.data.model.ForumPost
import com.team21.myapplication.data.model.ThreadForum
import com.team21.myapplication.utils.NetworkMonitor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

class ForumRepository(context: Context) {

    private val networkMonitor = NetworkMonitor.get(context)
    private val db = AppDatabase.getDatabase(context)
    private val threadDao = db.threadForumDao()
    private val postDao = db.forumPostDao()
    private val firestore = FirebaseFirestore.getInstance()

    private val postCache = LruCache<String, List<ForumPost>>(10)
    private val postLimit = 10

    suspend fun getThreads(): List<ThreadForum> {
        val isOnline = networkMonitor.isOnline.first()
        return if (isOnline) {
            val remoteThreads = firestore.collection("ThreadForum").get().await()
                .mapNotNull { it.toObject(ThreadForum::class.java) }

            threadDao.clearAll()
            threadDao.insertAll(remoteThreads.map { it.toThreadForumEntity() })
            remoteThreads
        } else {
            threadDao.getAllThreads().map { it.toThreadForum() }
        }
    }

    suspend fun getThreadForumPosts(threadId: String): List<ForumPost> {
        val isOnline = networkMonitor.isOnline.first()

        if (isOnline) {
            val remotePosts = firestore.collection("ThreadForum").document(threadId)
                .collection("ForumPost").get().await()
                .mapNotNull { it.toObject(ForumPost::class.java) }
                .sortedByDescending { it.creationDate }

            postDao.insertAll(remotePosts.map { it.toForumPostEntity(threadId) })
            postDao.trimPostsForThread(threadId, postLimit)

            val finalPosts = remotePosts.take(postLimit)

            postCache.put(threadId, finalPosts)
            return finalPosts
        }

        // Offline logic
        val cachedPosts = postCache.get(threadId)
        if (cachedPosts != null) {
            return cachedPosts
        }

        val localPosts = postDao.getPostsForThread(threadId).map { it.toForumPost() }
        postCache.put(threadId, localPosts)
        return localPosts
    }

    /**
     * Creates a new thread with an initial post.
     *
     * @param title The title of the new thread.
     * @param description The description of the new thread.
     * @param initialPost The first post to be added to the thread.
     * @return The ID of the newly created thread.
     */
    suspend fun createThread(title: String, description: String, initialPost: ForumPost): String {
        val newThreadRef = firestore.collection("ThreadForum").document()
        val newPostRef = newThreadRef.collection("ForumPost").document()
        Log.d("ForumRepository", "Creating new thread and initial post")
        val thread = ThreadForum(
            id = newThreadRef.id,
            title = title,
            description = description,
            creationDate = Timestamp.now(),
            commentQuantity = 1,
            forumPost = emptyList() // Es una subcolección, no un campo del documento
        )

        val post = initialPost.copy(
            id = newPostRef.id,
            creationDate = Timestamp.now()
        )

        // CORRECCIÓN AQUÍ: Se nombra el parámetro de la lambda como 'transaction'
        firestore.runTransaction { transaction ->
            transaction.set(newThreadRef, thread)
            transaction.set(newPostRef, post)
        }.await()
        Log.d("ForumRepository", "Thread and initial post created successfully")
        return newThreadRef.id
    }

    /**
     * Adds a new post to an existing thread.
     *
     * @param threadId The ID of the thread to add the post to.
     * @param post The post to be added.
     */
    suspend fun createPost(threadId: String, post: ForumPost) {
        val threadRef = firestore.collection("ThreadForum").document(threadId)
        val newPostRef = threadRef.collection("ForumPost").document()

        val finalPost = post.copy(
            id = newPostRef.id,
            creationDate = Timestamp.now()
        )

        firestore.runTransaction { transaction ->
            transaction.set(newPostRef, finalPost)
            transaction.update(threadRef, "commentQuantity", FieldValue.increment(1))
        }.await()
    }
}
