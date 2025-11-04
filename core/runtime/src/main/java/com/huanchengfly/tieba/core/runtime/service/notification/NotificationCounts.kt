package com.huanchengfly.tieba.core.runtime.service.notification

data class NotificationCounts(
    val replies: Int,
    val mentions: Int
) {
    val total: Int
        get() = replies + mentions

    fun isEmpty(): Boolean = replies <= 0 && mentions <= 0
}
