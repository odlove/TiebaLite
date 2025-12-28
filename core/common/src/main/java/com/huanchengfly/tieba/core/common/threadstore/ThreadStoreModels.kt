package com.huanchengfly.tieba.core.common.threadstore

data class ThreadStoreAuthor(
    val id: String? = null,
    val name: String? = null,
    val nameShow: String? = null,
    val portrait: String? = null,
)

data class ThreadStoreMedia(
    val type: String? = null,
    val smallPic: String? = null,
    val bigPic: String? = null,
    val width: String? = null,
    val height: String? = null,
)

data class ThreadStoreItem(
    val threadId: String,
    val title: String,
    val forumName: String,
    val author: ThreadStoreAuthor,
    val media: List<ThreadStoreMedia> = emptyList(),
    val isDeleted: String,
    val lastTime: String,
    val type: String,
    val status: String,
    val maxPid: String,
    val minPid: String,
    val markPid: String,
    val markStatus: String,
    val postNo: String,
    val postNoMsg: String,
    val count: String,
)

data class ThreadStoreResult(
    val items: List<ThreadStoreItem>? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null,
)
