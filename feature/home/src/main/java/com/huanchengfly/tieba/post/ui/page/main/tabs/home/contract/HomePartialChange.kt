package com.huanchengfly.tieba.post.ui.page.main.tabs.home.contract

import com.huanchengfly.tieba.core.common.history.HistoryItem
import com.huanchengfly.tieba.core.mvi.PartialChange
import kotlinx.collections.immutable.toImmutableList

sealed interface HomePartialChange : PartialChange<HomeUiState> {
    sealed class Unfollow : HomePartialChange {
        override fun reduce(oldState: HomeUiState): HomeUiState =
            when (this) {
                is Success -> {
                    oldState.copy(
                        forums = oldState.forums.filterNot { it.forumId == forumId }
                            .toImmutableList(),
                        topForums = oldState.topForums.filterNot { it.forumId == forumId }
                            .toImmutableList(),
                    )
                }

                is Failure -> oldState
            }

        data class Success(val forumId: String) : Unfollow()

        data class Failure(val errorMessage: String) : Unfollow()
    }

    sealed class Refresh : HomePartialChange {
        override fun reduce(oldState: HomeUiState): HomeUiState =
            when (this) {
                is Success -> oldState.copy(
                    isLoading = false,
                    forums = forums.toImmutableList(),
                    topForums = topForums.toImmutableList(),
                    historyForums = historyForums.toImmutableList(),
                    error = null
                )

                is Failure -> oldState.copy(isLoading = false, error = error)
                Start -> oldState.copy(isLoading = true)
            }

        data object Start : Refresh()

        data class Success(
            val forums: List<HomeUiState.Forum>,
            val topForums: List<HomeUiState.Forum>,
            val historyForums: List<HistoryItem>,
        ) : Refresh()

        data class Failure(
            val error: Throwable,
        ) : Refresh()
    }

    sealed class RefreshHistory : HomePartialChange {
        override fun reduce(oldState: HomeUiState): HomeUiState =
            when (this) {
                is Success -> oldState.copy(
                    historyForums = historyForums.toImmutableList(),
                )

                else -> oldState
            }

        data class Success(
            val historyForums: List<HistoryItem>,
        ) : RefreshHistory()

        data class Failure(
            val error: Throwable,
        ) : RefreshHistory()
    }

    sealed interface TopForums : HomePartialChange {
        sealed interface Delete : HomePartialChange {
            override fun reduce(oldState: HomeUiState): HomeUiState =
                when (this) {
                    is Success -> oldState.copy(
                        topForums = oldState.topForums.filterNot { it.forumId == forumId }
                            .toImmutableList()
                    )

                    is Failure -> oldState
                }

            data class Success(val forumId: String) : Delete

            data class Failure(val errorMessage: String) : Delete
        }

        sealed interface Add : HomePartialChange {
            override fun reduce(oldState: HomeUiState): HomeUiState =
                when (this) {
                    is Success -> {
                        val topForumsId = oldState.topForums.map { it.forumId }.toMutableList()
                        topForumsId.add(forum.forumId)
                        oldState.copy(
                            topForums = oldState.forums.filter { topForumsId.contains(it.forumId) }
                                .toImmutableList()
                        )
                    }

                    is Failure -> oldState
                }

            data class Success(val forum: HomeUiState.Forum) : Add

            data class Failure(val errorMessage: String) : Add
        }
    }

    data class ToggleHistory(val expand: Boolean) : HomePartialChange {
        override fun reduce(oldState: HomeUiState): HomeUiState =
            oldState.copy(expandHistoryForum = expand)
    }
}
