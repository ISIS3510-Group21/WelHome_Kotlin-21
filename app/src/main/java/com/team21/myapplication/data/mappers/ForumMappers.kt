package com.team21.myapplication.data.mappers

import com.team21.myapplication.data.local.entity.ForumPostEntity
import com.team21.myapplication.data.local.entity.ThreadForumEntity
import com.team21.myapplication.data.model.ForumPost
import com.team21.myapplication.data.model.ThreadForum

fun ThreadForum.toThreadForumEntity() = ThreadForumEntity(
    id = this.id,
    title = this.title,
    description = this.description,
    creationDate = this.creationDate,
    commentQuantity = this.commentQuantity,
    photoPath = this.photoPath
)

fun ThreadForumEntity.toThreadForum() = ThreadForum(
    id = this.id,
    title = this.title,
    description = this.description,
    creationDate = this.creationDate,
    commentQuantity = this.commentQuantity,
    photoPath = this.photoPath,
    forumPost = emptyList() // Posts are loaded separately
)

fun ForumPost.toForumPostEntity(threadId: String) = ForumPostEntity(
    id = this.id,
    threadId = threadId,
    content = this.content,
    positiveVotes = this.positiveVotes,
    negativeVotes = this.negativeVotes,
    creationDate = this.creationDate,
    user = this.user,
    userName = this.userName,
    userPhoto = this.userPhoto
)

fun ForumPostEntity.toForumPost() = ForumPost(
    id = this.id,
    content = this.content,
    positiveVotes = this.positiveVotes,
    negativeVotes = this.negativeVotes,
    creationDate = this.creationDate,
    user = this.user,
    userName = this.userName,
    userPhoto = this.userPhoto
)
