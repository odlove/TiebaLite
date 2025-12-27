package com.huanchengfly.tieba.core.common.interaction

data class DislikeRequest(
    val threadId: String,
    val dislikeIds: String,
    val forumId: String? = null,
    val clickTime: Long,
    val extra: String,
)
