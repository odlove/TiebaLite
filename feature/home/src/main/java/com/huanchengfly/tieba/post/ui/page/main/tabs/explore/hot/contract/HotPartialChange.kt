package com.huanchengfly.tieba.post.ui.page.main.tabs.explore.hot.contract

import com.huanchengfly.tieba.core.common.feed.HotTab
import com.huanchengfly.tieba.core.common.feed.HotTopic
import com.huanchengfly.tieba.core.mvi.PartialChange
import com.huanchengfly.tieba.core.mvi.wrapImmutable
import kotlinx.collections.immutable.ImmutableList

sealed interface HotPartialChange : PartialChange<HotUiState> {
    sealed class Load : HotPartialChange {
        override fun reduce(oldState: HotUiState): HotUiState =
            when (this) {
                Start -> oldState.copy(isRefreshing = true)
                is Success -> oldState.copy(
                    isRefreshing = false,
                    currentTabCode = "all",
                    topicList = topicList.wrapImmutable(),
                    tabList = tabList.wrapImmutable(),
                    threadIds = threadIds
                )

                is Failure -> oldState.copy(isRefreshing = false)
            }

        data object Start : Load()

        data class Success(
            val topicList: List<HotTopic>,
            val tabList: List<HotTab>,
            val threadIds: ImmutableList<Long>,
        ) : Load()

        data class Failure(
            val error: Throwable
        ) : Load()
    }

    sealed class RefreshThreadList : HotPartialChange {
        override fun reduce(oldState: HotUiState): HotUiState =
            when (this) {
                is Start -> oldState.copy(isLoadingThreadList = true, currentTabCode = tabCode)
                is Success -> oldState.copy(
                    isLoadingThreadList = false,
                    currentTabCode = tabCode,
                    threadIds = threadIds
                )

                is Failure -> oldState.copy(isLoadingThreadList = false)
            }

        data class Start(val tabCode: String) : RefreshThreadList()

        data class Success(
            val tabCode: String,
            val threadIds: ImmutableList<Long>,
        ) : RefreshThreadList()

        data class Failure(
            val tabCode: String,
            val error: Throwable
        ) : RefreshThreadList()
    }

    sealed class Agree private constructor() : HotPartialChange {
        override fun reduce(oldState: HotUiState): HotUiState =
            when (this) {
                is Start, is Success, is Failure -> oldState
            }

        data class Start(
            val threadId: Long,
            val hasAgree: Int
        ) : Agree()

        data class Success(
            val threadId: Long,
            val hasAgree: Int
        ) : Agree()

        data class Failure(
            val threadId: Long,
            val hasAgree: Int,
            val error: Throwable
        ) : Agree()
    }
}
