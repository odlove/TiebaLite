package com.huanchengfly.tieba.post.ui.page.main.tabs.home.contract

import androidx.compose.runtime.Immutable
import com.huanchengfly.tieba.core.common.history.HistoryItem
import com.huanchengfly.tieba.core.mvi.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class HomeUiState(
    val isLoading: Boolean = true,
    val forums: ImmutableList<Forum> = persistentListOf(),
    val topForums: ImmutableList<Forum> = persistentListOf(),
    val historyForums: ImmutableList<HistoryItem> = persistentListOf(),
    val expandHistoryForum: Boolean = true,
    val error: Throwable? = null,
) : UiState {
    @Immutable
    data class Forum(
        val avatar: String,
        val forumId: String,
        val forumName: String,
        val isSign: Boolean,
        val levelId: String,
    )
}
