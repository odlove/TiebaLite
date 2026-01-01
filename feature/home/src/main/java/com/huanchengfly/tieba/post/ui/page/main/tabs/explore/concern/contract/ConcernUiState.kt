package com.huanchengfly.tieba.post.ui.page.main.tabs.explore.concern.contract

import com.huanchengfly.tieba.core.common.feed.ConcernMetadata
import com.huanchengfly.tieba.core.mvi.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

data class ConcernUiState(
    val isRefreshing: Boolean = true,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val nextPageTag: String = "",
    val threadIds: ImmutableList<Long> = persistentListOf(),
    val metadata: PersistentMap<Long, ConcernMetadata> = persistentMapOf(),
) : UiState
