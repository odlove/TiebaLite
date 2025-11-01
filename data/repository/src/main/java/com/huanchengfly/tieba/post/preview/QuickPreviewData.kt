package com.huanchengfly.tieba.post.preview

data class ThreadPreviewData(
    val title: String?,
    val forumName: String?,
    val replyNum: Long?,
    val authorPortrait: String?,
)

data class ForumPreviewData(
    val name: String?,
    val slogan: String?,
    val avatar: String?,
)
