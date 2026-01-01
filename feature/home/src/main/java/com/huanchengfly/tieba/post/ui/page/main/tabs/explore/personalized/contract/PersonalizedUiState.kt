package com.huanchengfly.tieba.post.ui.page.main.tabs.explore.personalized.contract

import com.huanchengfly.tieba.core.common.feed.PersonalizedMetadata
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.core.mvi.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

data class PersonalizedUiState(
    val isRefreshing: Boolean = true,
    val isLoadingMore: Boolean = false,
    val error: ImmutableHolder<Throwable>? = null,
    val currentPage: Int = 1,
    val threadIds: ImmutableList<Long> = persistentListOf(),
    val metadata: PersistentMap<Long, PersonalizedMetadata> = persistentMapOf(),
    val hiddenThreadIds: ImmutableList<Long> = persistentListOf(),
    val refreshPosition: Int = 0,
) : UiState
