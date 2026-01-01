package com.huanchengfly.tieba.post.ui.page.main.tabs.explore.concern.contract

import com.huanchengfly.tieba.core.common.feed.ConcernMetadata
import com.huanchengfly.tieba.core.mvi.PartialChange
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentMap

sealed interface ConcernPartialChange : PartialChange<ConcernUiState> {
    sealed class Agree private constructor() : ConcernPartialChange {
        override fun reduce(oldState: ConcernUiState): ConcernUiState =
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

    sealed class Refresh private constructor() : ConcernPartialChange {
        override fun reduce(oldState: ConcernUiState): ConcernUiState =
            when (this) {
                Start -> oldState.copy(isRefreshing = true)
                is Success -> oldState.copy(
                    isRefreshing = false,
                    threadIds = threadIds,
                    metadata = metadata,
                    hasMore = hasMore,
                    nextPageTag = nextPageTag
                )
                is Failure -> oldState.copy(isRefreshing = false)
            }

        data object Start : Refresh()

        data class Success(
            val threadIds: ImmutableList<Long>,
            val metadata: PersistentMap<Long, ConcernMetadata>,
            val hasMore: Boolean,
            val nextPageTag: String,
        ) : Refresh()

        data class Failure(
            val error: Throwable,
        ) : Refresh()
    }

    sealed class LoadMore private constructor() : ConcernPartialChange {
        override fun reduce(oldState: ConcernUiState): ConcernUiState =
            when (this) {
                Start -> oldState.copy(isLoadingMore = true)
                is Success -> {
                    val newThreadIds = (oldState.threadIds + threadIds).distinct().toImmutableList()
                    val mergedMetadata = (oldState.metadata + metadata).filterKeys { it in newThreadIds }
                    oldState.copy(
                        isLoadingMore = false,
                        threadIds = newThreadIds,
                        metadata = mergedMetadata.toPersistentMap(),
                        hasMore = hasMore,
                        nextPageTag = nextPageTag
                    )
                }
                is Failure -> oldState.copy(isLoadingMore = false)
            }

        data object Start : LoadMore()

        data class Success(
            val threadIds: ImmutableList<Long>,
            val metadata: PersistentMap<Long, ConcernMetadata>,
            val hasMore: Boolean,
            val nextPageTag: String,
        ) : LoadMore()

        data class Failure(
            val error: Throwable,
        ) : LoadMore()
    }
}
