package com.huanchengfly.tieba.core.common.threadcollect

data class ThreadCollectAuthor(
    val id: String? = null,
    val name: String? = null,
    val nameShow: String? = null,
    val portrait: String? = null,
)

data class ThreadCollectMedia(
    val type: String? = null,
    val smallPic: String? = null,
    val bigPic: String? = null,
    val width: String? = null,
    val height: String? = null,
)

data class ThreadCollectItem(
    val threadId: String,
    val title: String,
    val forumName: String,
    val author: ThreadCollectAuthor,
    val media: List<ThreadCollectMedia> = emptyList(),
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

data class ThreadCollectResult(
    val items: List<ThreadCollectItem>? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null,
)
