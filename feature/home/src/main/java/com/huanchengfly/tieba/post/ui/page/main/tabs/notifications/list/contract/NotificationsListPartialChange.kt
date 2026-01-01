package com.huanchengfly.tieba.post.ui.page.main.tabs.notifications.list.contract

import com.huanchengfly.tieba.core.mvi.PartialChange
import kotlinx.collections.immutable.toImmutableList

sealed interface NotificationsListPartialChange : PartialChange<NotificationsListUiState> {
    sealed class Refresh private constructor(): NotificationsListPartialChange {
        override fun reduce(oldState: NotificationsListUiState): NotificationsListUiState =
            when (this) {
                Start -> oldState.copy(isRefreshing = true)
                is Success -> oldState.copy(
                    isRefreshing = false,
                    currentPage = 1,
                    data = data.toImmutableList(),
                    hasMore = hasMore
                )

                is Failure -> oldState.copy(isRefreshing = false)
            }

        data object Start : Refresh()

        data class Success(
            val data: List<MessageItemData>,
            val hasMore: Boolean,
        ) : Refresh()

        data class Failure(
            val error: Throwable,
        ) : Refresh()
    }

    sealed class LoadMore private constructor(): NotificationsListPartialChange {
        override fun reduce(oldState: NotificationsListUiState): NotificationsListUiState =
            when (this) {
                Start -> oldState.copy(isLoadingMore = true)
                is Success -> {
                    val uniqueData = data.filter { item ->
                        oldState.data.none { it.info == item.info }
                    }
                    oldState.copy(
                        isLoadingMore = false,
                        currentPage = currentPage,
                        data = (oldState.data + uniqueData).toImmutableList(),
                        hasMore = hasMore
                    )
                }

                is Failure -> oldState.copy(isLoadingMore = false)
            }

        data object Start : LoadMore()

        data class Success(
            val currentPage: Int,
            val data: List<MessageItemData>,
            val hasMore: Boolean,
        ) : LoadMore()

        data class Failure(
            val currentPage: Int,
            val error: Throwable,
        ) : LoadMore()
    }
}
