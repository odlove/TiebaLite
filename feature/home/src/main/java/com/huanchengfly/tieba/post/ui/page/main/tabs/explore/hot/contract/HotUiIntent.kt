package com.huanchengfly.tieba.post.ui.page.main.tabs.explore.hot.contract

import com.huanchengfly.tieba.core.mvi.UiIntent

sealed interface HotUiIntent : UiIntent {
    data object Load : HotUiIntent

    data class RefreshThreadList(val tabCode: String) : HotUiIntent

    data class Agree(
        val threadId: Long,
        val postId: Long,
        val hasAgree: Int
    ) : HotUiIntent
}
