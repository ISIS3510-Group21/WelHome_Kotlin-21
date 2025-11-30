
package com.team21.myapplication.data.repository

import android.content.Context
import android.util.Log
import android.util.LruCache
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.team21.myapplication.data.local.AppDatabase
import com.team21.myapplication.data.local.entity.OfflineForumPostEntity
import com.team21.myapplication.data.mappers.toForumPost
import com.team21.myapplication.data.mappers.toForumPostEntity
import com.team21.myapplication.data.mappers.toThreadForum
import com.team21.myapplication.data.mappers.toThreadForumEntity
import com.team21.myapplication.data.model.ForumPost
import com.team21.myapplication.data.model.ThreadForum
import com.team21.myapplication.utils.NetworkMonitor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ForumRepository(context: Context) {

    private val networkMonitor = NetworkMonitor.get(context)
    private val db = AppDatabase.getDatabase(context)
    private val threadDao = db.threadForumDao()
    private val postDao = db.forumPostDao()
    private val offlineForumPostDao = db.offlineForumPostDao()
    private val firestore = FirebaseFirestore.getInstance()

    private val postCache = LruCache<String, List<ForumPost>>(10)
    private val postLimit = 10

    suspend fun getThreads(): List<ThreadForum> {
        val isOnline = networkMonitor.isOnline.first()
        if (isOnline) {
            syncOfflinePosts()
            val remoteThreads = firestore.collection("ThreadForum").get().await()
                .mapNotNull { it.toObject(ThreadForum::class.java) }

            threadDao.clearAll()
            threadDao.insertAll(remoteThreads.map { it.toThreadForumEntity() })
            return remoteThreads
        } else {
            return threadDao.getAllThreads().map { it.toThreadForum() }
        }
    }

    suspend fun getThreadForumPosts(threadId: String): List<ForumPost> {
        val isOnline = networkMonitor.isOnline.first()

        if (isOnline) {
            syncOfflinePosts()
            val stillOfflinePosts = offlineForumPostDao.getOfflinePosts()
                .filter { it.threadId == threadId }
                .map { it.toForumPost() }

            val remotePosts = firestore.collection("ThreadForum").document(threadId)
                .collection("ForumPost").get().await()
                .mapNotNull { it.toObject(ForumPost::class.java) }
                .sortedByDescending { it.creationDate }

            postDao.insertAll(remotePosts.map { it.toForumPostEntity(threadId) })
            postDao.trimPostsForThread(threadId, postLimit)

            val finalPosts = remotePosts.take(postLimit)
            postCache.put(threadId, finalPosts)
            return stillOfflinePosts + finalPosts
        }

        // --- OFFLINE LOGIC ---
        val cachedPosts = postCache.get(threadId)
        if (cachedPosts != null) {
            return cachedPosts
        }

        val offlinePosts = offlineForumPostDao.getOfflinePosts()
            .filter { it.threadId == threadId || (it.threadId == null && it.newThreadTitle != null) }
            .map { it.toForumPost() }

        val localPosts = postDao.getPostsForThread(threadId).map { it.toForumPost() }

        val allPosts = (offlinePosts + localPosts).sortedByDescending { it.creationDate?.seconds }
        postCache.put(threadId, allPosts)
        return allPosts
    }

    private suspend fun _createThread(title: String, description: String, initialPost: ForumPost): String {
        val newThreadRef = firestore.collection("ThreadForum").document()
        val newPostRef = newThreadRef.collection("ForumPost").document()
        Log.d("ForumRepository", "Creating new thread and initial post")
        val thread = ThreadForum(
            id = newThreadRef.id,
            title = title,
            description = description,
            creationDate = Timestamp.now(),
            commentQuantity = 1,
            forumPost = emptyList()
        )

        val post = initialPost.copy(
            id = newPostRef.id,
            creationDate = Timestamp.now()
        )

        firestore.runTransaction { transaction ->
            transaction.set(newThreadRef, thread)
            transaction.set(newPostRef, post)
        }.await()
        Log.d("ForumRepository", "Thread and initial post created successfully")
        return newThreadRef.id
    }

    suspend fun createThread(title: String, description: String, initialPost: ForumPost): String {
        val isOnline = networkMonitor.isOnline.first()
        return if (isOnline) {
            syncOfflinePosts()
            postCache.evictAll()
            _createThread(title, description, initialPost)
        } else {
            val offlinePost = OfflineForumPostEntity(
                id = UUID.randomUUID().toString(),
                threadId = null,
                newThreadTitle = title,
                newThreadDescription = description,
                content = initialPost.content,
                user = initialPost.user,
                userName = initialPost.userName,
                userPhoto = initialPost.userPhoto
            )
            offlineForumPostDao.insertOfflinePost(offlinePost)
            postCache.evictAll()
            "offline_thread"
        }
    }

    private suspend fun _createPost(threadId: String, post: ForumPost) {
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

    suspend fun createPost(threadId: String, post: ForumPost) {
        val isOnline = networkMonitor.isOnline.first()
        if (isOnline) {
            syncOfflinePosts()
            _createPost(threadId, post)
            postCache.remove(threadId)
        } else {
            val offlinePost = OfflineForumPostEntity(
                id = UUID.randomUUID().toString(),
                threadId = threadId,
                newThreadTitle = null,
                newThreadDescription = null,
                content = post.content,
                user = post.user,
                userName = post.userName,
                userPhoto = post.userPhoto
            )
            offlineForumPostDao.insertOfflinePost(offlinePost)
            postCache.remove(threadId)
        }
    }

    suspend fun syncOfflinePosts() {
        val offlinePosts = offlineForumPostDao.getOfflinePosts()
        if (offlinePosts.isNotEmpty()) {
            Log.d("ForumRepository", "Syncing ${offlinePosts.size} offline posts")
            var wasNewThreadSynced = false
            for (offlinePost in offlinePosts) {
                try {
                    if (offlinePost.threadId == null && !offlinePost.newThreadTitle.isNullOrBlank()) {
                        val post = ForumPost(
                            content = offlinePost.content,
                            user = offlinePost.user,
                            userName = offlinePost.userName,
                            userPhoto = offlinePost.userPhoto
                        )
                        _createThread(offlinePost.newThreadTitle, offlinePost.newThreadDescription ?: "", post)
                        wasNewThreadSynced = true
                    } else if (offlinePost.threadId != null) {
                        val post = ForumPost(
                            content = offlinePost.content,
                            user = offlinePost.user,
                            userName = offlinePost.userName,
                            userPhoto = offlinePost.userPhoto
                        )
                        _createPost(offlinePost.threadId, post)
                        postCache.remove(offlinePost.threadId)
                    }
                    offlineForumPostDao.deleteOfflinePost(offlinePost.id)
                } catch (e: Exception) {
                    Log.e("ForumRepository", "Error syncing offline post: ${offlinePost.id}", e)
                }
            }
            if (wasNewThreadSynced) {
                postCache.evictAll()
            }
        }
    }

    private fun OfflineForumPostEntity.toForumPost(): ForumPost {
        return ForumPost(
            id = this.id,
            content = this.content,
            user = this.user,
            userName = this.userName,
            userPhoto = this.userPhoto,
            creationDate = Timestamp(this.timestamp / 1000, (this.timestamp % 1000).toInt() * 1_000_000),
            isOffline = true
        )
    }
}
