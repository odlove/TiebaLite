package com.huanchengfly.tieba.post.arch

import com.huanchengfly.tieba.core.mvi.UiEvent

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
