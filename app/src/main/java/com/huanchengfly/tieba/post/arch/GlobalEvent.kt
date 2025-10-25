package com.huanchengfly.tieba.post.arch

import android.content.Intent
import android.net.Uri
import com.huanchengfly.tieba.core.mvi.MediaSelectorType
import com.huanchengfly.tieba.core.mvi.UiEvent

sealed interface GlobalEvent : UiEvent {
    data object AccountSwitched : GlobalEvent

    data object ScrollToTop : GlobalEvent

    data class Refresh(val key: String) : GlobalEvent

    data class StartSelectImages(
        val id: String,
        val maxCount: Int,
        val mediaType: MediaSelectorType
    ) : GlobalEvent

    data class SelectedImages(
        val id: String,
        val images: List<Uri>,
    ) : GlobalEvent

    data class ReplySuccess(
        val threadId: Long,
        val newPostId: Long,
        val postId: Long? = null,
        val subPostId: Long? = null,
        val newSubPostId: Long? = null,
    ) : GlobalEvent

    data class StartActivityForResult(
        val requesterId: String,
        val intent: Intent,
    ) : GlobalEvent

    data class ActivityResult(
        val requesterId: String,
        val resultCode: Int,
        val intent: Intent?,
    ) : GlobalEvent
}
