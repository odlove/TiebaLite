package com.huanchengfly.tieba.post.ui.page.main.tabs.explore.hot.contract

import com.huanchengfly.tieba.core.common.feed.HotTab
import com.huanchengfly.tieba.core.common.feed.HotTopic
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.core.mvi.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class HotUiState(
    val isRefreshing: Boolean = true,
    val currentTabCode: String = "all",
    val isLoadingThreadList: Boolean = false,
    val topicList: ImmutableList<ImmutableHolder<HotTopic>> = persistentListOf(),
    val tabList: ImmutableList<ImmutableHolder<HotTab>> = persistentListOf(),
    val threadIds: ImmutableList<Long> = persistentListOf(),
) : UiState
