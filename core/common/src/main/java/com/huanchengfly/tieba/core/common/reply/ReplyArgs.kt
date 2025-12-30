package com.huanchengfly.tieba.core.common.reply

data class ReplyArgs(
    val forumId: Long,
    val forumName: String,
    val threadId: Long,
    val postId: Long? = null,
    val subPostId: Long? = null,
    val replyUserId: Long? = null,
    val replyUserName: String? = null,
    val replyUserPortrait: String? = null,
    val tbs: String? = null,
)
