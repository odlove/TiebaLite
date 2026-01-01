package com.huanchengfly.tieba.post.ui.page.main.tabs.explore.concern.contract

import com.huanchengfly.tieba.core.mvi.UiIntent

sealed interface ConcernUiIntent : UiIntent {
    data object Refresh : ConcernUiIntent

    data class LoadMore(val pageTag: String) : ConcernUiIntent

    data class Agree(
        val threadId: Long,
        val postId: Long,
        val hasAgree: Int,
    ) : ConcernUiIntent
}
