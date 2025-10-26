package com.team21.myapplication.data.model

import com.google.firebase.Timestamp

data class ThreadForum (
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val creationDate: Timestamp ?= null,
    val commentQuantity: Int = 0,
    val photoPath: String = ""
)

data class ForumPost (
    val id: String = "",
    val content: String = "",
    val positiveVotes: Int = 0q,
    val negativeVotes: Int = 0,
    val creationDate: Timestamp ?= null,
    val user: String = ""
)