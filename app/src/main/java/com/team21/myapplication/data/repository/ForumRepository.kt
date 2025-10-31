package com.team21.myapplication.data.repository

import android.content.Context
import android.util.LruCache
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
}
