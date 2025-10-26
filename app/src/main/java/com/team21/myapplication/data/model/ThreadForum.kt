package com.team21.myapplication.data.model

import com.google.firebase.Timestamp

data class ThreadForum (
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val creationDate: Timestamp ?= null,
    val commentQuantity: Int = 0,
    val photoPath: String = "",
    val forumPost: List<ForumPost> = emptyList()
)

data class ForumPost (
    val id: String = "",
    val content: String = "",
    val positiveVotes: Int = 0,
    val negativeVotes: Int = 0,
    val creationDate: Timestamp ?= null,
    val user: String = "",
    val userNamer: String = "",
    val userPhoto: String = ""
)