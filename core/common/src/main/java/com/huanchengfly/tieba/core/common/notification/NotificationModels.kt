package com.huanchengfly.tieba.core.common.notification

data class NotificationReplyUser(
    val id: String? = null,
    val name: String? = null,
    val nameShow: String? = null,
    val portrait: String? = null,
)

data class NotificationMessage(
    val threadId: String? = null,
    val postId: String? = null,
    val quotePid: String? = null,
    val isFloor: Boolean = false,
    val time: String? = null,
    val content: String? = null,
    val title: String? = null,
    val quoteContent: String? = null,
    val replyer: NotificationReplyUser? = null,
    val blocked: Boolean = false,
)

data class NotificationPage(
    val items: List<NotificationMessage> = emptyList(),
    val hasMore: Boolean = false,
)
