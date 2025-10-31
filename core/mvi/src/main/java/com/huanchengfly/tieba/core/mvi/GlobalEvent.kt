package com.huanchengfly.tieba.core.mvi
sealed interface GlobalEvent : UiEvent {
    data object AccountSwitched : GlobalEvent

    data class ReplySuccess(
        val threadId: Long,
        val newPostId: Long,
        val postId: Long? = null,
        val subPostId: Long? = null,
        val newSubPostId: Long? = null,
    ) : GlobalEvent
}
