package com.huanchengfly.tieba.post.ui.page.main.tabs.home.contract

import com.huanchengfly.tieba.core.mvi.UiIntent

sealed interface HomeUiIntent : UiIntent {
    data object Refresh : HomeUiIntent

    data object RefreshHistory : HomeUiIntent

    data class Unfollow(val forumId: String, val forumName: String) : HomeUiIntent

    sealed interface TopForums : HomeUiIntent {
        data class Delete(val forumId: String) : TopForums

        data class Add(val forum: HomeUiState.Forum) : TopForums
    }

    data class ToggleHistory(val currentExpand: Boolean) : HomeUiIntent
}
