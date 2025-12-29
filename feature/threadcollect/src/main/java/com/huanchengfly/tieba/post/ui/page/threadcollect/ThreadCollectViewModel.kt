package com.huanchengfly.tieba.post.ui.page.threadcollect

import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.core.common.threadcollect.ThreadCollectItem
import com.huanchengfly.tieba.core.network.model.CommonResponse
import com.huanchengfly.tieba.core.network.error.getErrorCode
import com.huanchengfly.tieba.core.network.error.getErrorMessage
import com.huanchengfly.tieba.core.mvi.BaseViewModel
import com.huanchengfly.tieba.core.mvi.DispatcherProvider
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.core.mvi.PartialChange
import com.huanchengfly.tieba.core.mvi.PartialChangeProducer
import com.huanchengfly.tieba.core.mvi.UiEvent
import com.huanchengfly.tieba.core.mvi.UiIntent
import com.huanchengfly.tieba.core.mvi.UiState
import com.huanchengfly.tieba.core.mvi.wrapImmutable
import com.huanchengfly.tieba.post.repository.ThreadOperationRepository
import com.huanchengfly.tieba.post.repository.ThreadCollectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@Stable
@HiltViewModel
class ThreadCollectViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val threadCollectRepository: ThreadCollectRepository,
    private val threadOperationRepository: ThreadOperationRepository,
) :
    BaseViewModel<ThreadCollectUiIntent, ThreadCollectPartialChange, ThreadCollectUiState, ThreadCollectUiEvent>(dispatcherProvider) {
    override fun createInitialState(): ThreadCollectUiState = ThreadCollectUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<ThreadCollectUiIntent, ThreadCollectPartialChange, ThreadCollectUiState> =
        ThreadCollectPartialChangeProducer(
            threadCollectRepository,
            threadOperationRepository
        )

    override fun dispatchEvent(partialChange: ThreadCollectPartialChange): UiEvent? {
        return when (partialChange) {
            is ThreadCollectPartialChange.Delete.Success -> ThreadCollectUiEvent.Delete.Success
            is ThreadCollectPartialChange.Delete.Failure -> ThreadCollectUiEvent.Delete.Failure(
                partialChange.error.getErrorCode(),
                partialChange.error.getErrorMessage()
            )

            else -> null
        }
    }

    private class ThreadCollectPartialChangeProducer(
        private val threadCollectRepository: ThreadCollectRepository,
        private val threadOperationRepository: ThreadOperationRepository,
    ) :
        PartialChangeProducer<ThreadCollectUiIntent, ThreadCollectPartialChange, ThreadCollectUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<ThreadCollectUiIntent>): Flow<ThreadCollectPartialChange> =
            merge(
                intentFlow.filterIsInstance<ThreadCollectUiIntent.Refresh>()
                    .flatMapConcat { produceRefreshPartialChange() },
                intentFlow.filterIsInstance<ThreadCollectUiIntent.LoadMore>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ThreadCollectUiIntent.Delete>()
                    .flatMapConcat { it.producePartialChange() },
            )

        private fun produceRefreshPartialChange() =
            threadCollectRepository
                .threadCollect()
                .map {
                    val collectThread = it.items
                    if (collectThread != null) ThreadCollectPartialChange.Refresh.Success(
                        collectThread,
                        collectThread.isNotEmpty()
                    )
                    else ThreadCollectPartialChange.Refresh.Failure(NullPointerException("未知错误"))
                }
                .onStart { emit(ThreadCollectPartialChange.Refresh.Start) }
                .catch { emit(ThreadCollectPartialChange.Refresh.Failure(it)) }

        private fun ThreadCollectUiIntent.LoadMore.producePartialChange() =
            threadCollectRepository
                .threadCollect(page)
                .map {
                    val collectThread = it.items
                    if (collectThread != null) ThreadCollectPartialChange.LoadMore.Success(
                        collectThread,
                        collectThread.isNotEmpty(),
                        page
                    )
                    else ThreadCollectPartialChange.LoadMore.Failure(NullPointerException("未知错误"))
                }
                .onStart { emit(ThreadCollectPartialChange.LoadMore.Start) }
                .catch { emit(ThreadCollectPartialChange.LoadMore.Failure(it)) }

        private fun ThreadCollectUiIntent.Delete.producePartialChange() =
            threadOperationRepository
                .removeStore(threadId)
                .map<CommonResponse, ThreadCollectPartialChange.Delete> {
                    ThreadCollectPartialChange.Delete.Success(threadId)
                }
                .catch { emit(ThreadCollectPartialChange.Delete.Failure(it)) }
    }
}

sealed interface ThreadCollectUiIntent : UiIntent {
    object Refresh : ThreadCollectUiIntent

    data class LoadMore(val page: Int) : ThreadCollectUiIntent

    data class Delete(val threadId: String) : ThreadCollectUiIntent
}

sealed interface ThreadCollectPartialChange : PartialChange<ThreadCollectUiState> {
    sealed class Refresh : ThreadCollectPartialChange {
        override fun reduce(oldState: ThreadCollectUiState): ThreadCollectUiState = when (this) {
            is Failure -> oldState.copy(isRefreshing = false, error = wrapImmutable(error))
            Start -> oldState.copy(isRefreshing = true)
            is Success -> oldState.copy(
                isRefreshing = false,
                data = data,
                currentPage = 0,
                hasMore = hasMore,
                error = null
            )
        }

        object Start : Refresh()

        data class Success(
            val data: List<ThreadCollectItem>,
            val hasMore: Boolean
        ) : Refresh()

        data class Failure(
            val error: Throwable
        ) : Refresh()
    }

    sealed class LoadMore : ThreadCollectPartialChange {
        override fun reduce(oldState: ThreadCollectUiState): ThreadCollectUiState = when (this) {
            is Failure -> oldState.copy(isLoadingMore = false)
            Start -> oldState.copy(isLoadingMore = true)
            is Success -> oldState.copy(
                isLoadingMore = false,
                data = oldState.data + data,
                currentPage = currentPage,
                hasMore = hasMore
            )
        }

        object Start : LoadMore()

        data class Success(
            val data: List<ThreadCollectItem>,
            val hasMore: Boolean,
            val currentPage: Int
        ) : LoadMore()

        data class Failure(
            val error: Throwable
        ) : LoadMore()
    }

    sealed class Delete : ThreadCollectPartialChange {
        override fun reduce(oldState: ThreadCollectUiState): ThreadCollectUiState = when (this) {
            is Failure -> oldState
            is Success -> oldState.copy(data = oldState.data.filterNot { it.threadId == threadId })
        }

        data class Success(
            val threadId: String
        ) : Delete()

        data class Failure(
            val error: Throwable
        ) : Delete()
    }
}

data class ThreadCollectUiState(
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val currentPage: Int = 1,
    val data: List<ThreadCollectItem> = emptyList(),
    val error: ImmutableHolder<Throwable>? = null
) : UiState

sealed interface ThreadCollectUiEvent : UiEvent {
    sealed interface Delete : ThreadCollectUiEvent {
        object Success : Delete

        data class Failure(
            val errorCode: Int,
            val errorMsg: String
        ) : Delete
    }
}
