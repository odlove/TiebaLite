package com.huanchengfly.tieba.post.ui.page.main.notifications.list

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.util.fastMap
import com.huanchengfly.tieba.post.api.models.MessageListBean
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.core.mvi.BaseViewModel
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.DispatcherProvider
import com.huanchengfly.tieba.core.mvi.PartialChange
import com.huanchengfly.tieba.core.mvi.PartialChangeProducer
import com.huanchengfly.tieba.core.mvi.UiEvent
import com.huanchengfly.tieba.core.mvi.UiIntent
import com.huanchengfly.tieba.core.mvi.UiState
import com.huanchengfly.tieba.post.repository.NotificationRepository
import com.huanchengfly.tieba.post.utils.BlockManager.shouldBlock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

abstract class NotificationsListViewModel(
    dispatcherProvider: DispatcherProvider
) :
    BaseViewModel<NotificationsListUiIntent, NotificationsListPartialChange, NotificationsListUiState, NotificationsListUiEvent>(dispatcherProvider) {

    override fun createInitialState(): NotificationsListUiState = NotificationsListUiState()

    override fun dispatchEvent(partialChange: NotificationsListPartialChange): UiEvent? =
        when (partialChange) {
            is NotificationsListPartialChange.Refresh.Failure -> CommonUiEvent.Toast(partialChange.error.getErrorMessage())
            is NotificationsListPartialChange.LoadMore.Failure -> CommonUiEvent.Toast(partialChange.error.getErrorMessage())
            else -> null
        }
}

@Stable
@HiltViewModel
class ReplyMeListViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    dispatcherProvider: DispatcherProvider
) : NotificationsListViewModel(dispatcherProvider) {
    override fun createPartialChangeProducer():
            PartialChangeProducer<NotificationsListUiIntent, NotificationsListPartialChange, NotificationsListUiState> =
        ReplyMePartialChangeProducer()

    private inner class ReplyMePartialChangeProducer :
        PartialChangeProducer<NotificationsListUiIntent, NotificationsListPartialChange, NotificationsListUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<NotificationsListUiIntent>): Flow<NotificationsListPartialChange> =
            merge(
                intentFlow.filterIsInstance<NotificationsListUiIntent.Refresh>().flatMapConcat { produceRefreshPartialChange() },
                intentFlow.filterIsInstance<NotificationsListUiIntent.LoadMore>().flatMapConcat { it.produceLoadMorePartialChange() },
            )

        private fun produceRefreshPartialChange(): Flow<NotificationsListPartialChange.Refresh> =
            notificationRepository.replyMe()
                .map<MessageListBean, NotificationsListPartialChange.Refresh> { messageListBean ->
                    val data = (messageListBean.replyList ?: emptyList()).fastMap {
                        MessageItemData(it)
                    }
                    NotificationsListPartialChange.Refresh.Success(
                        data = data,
                        hasMore = messageListBean.page?.hasMore == "1"
                    )
                }
                .onStart { emit(NotificationsListPartialChange.Refresh.Start) }
                .catch { emit(NotificationsListPartialChange.Refresh.Failure(it)) }

        private fun NotificationsListUiIntent.LoadMore.produceLoadMorePartialChange() =
            notificationRepository.replyMe(page = page)
                .map<MessageListBean, NotificationsListPartialChange.LoadMore> { messageListBean ->
                    val data = (messageListBean.replyList ?: emptyList()).fastMap {
                        MessageItemData(it)
                    }
                    NotificationsListPartialChange.LoadMore.Success(
                        currentPage = page,
                        data = data,
                        hasMore = messageListBean.page?.hasMore == "1"
                    )
                }
                .onStart { emit(NotificationsListPartialChange.LoadMore.Start) }
                .catch { emit(NotificationsListPartialChange.LoadMore.Failure(currentPage = page, error = it)) }
    }
}

@Stable
@HiltViewModel
class AtMeListViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    dispatcherProvider: DispatcherProvider
) : NotificationsListViewModel(dispatcherProvider) {
    override fun createPartialChangeProducer():
            PartialChangeProducer<NotificationsListUiIntent, NotificationsListPartialChange, NotificationsListUiState> =
        AtMePartialChangeProducer()

    private inner class AtMePartialChangeProducer :
        PartialChangeProducer<NotificationsListUiIntent, NotificationsListPartialChange, NotificationsListUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<NotificationsListUiIntent>): Flow<NotificationsListPartialChange> =
            merge(
                intentFlow.filterIsInstance<NotificationsListUiIntent.Refresh>().flatMapConcat { produceRefreshPartialChange() },
                intentFlow.filterIsInstance<NotificationsListUiIntent.LoadMore>().flatMapConcat { it.produceLoadMorePartialChange() },
            )

        private fun produceRefreshPartialChange(): Flow<NotificationsListPartialChange.Refresh> =
            notificationRepository.atMe()
                .map<MessageListBean, NotificationsListPartialChange.Refresh> { messageListBean ->
                    val data = (messageListBean.atList ?: emptyList()).fastMap {
                        MessageItemData(it)
                    }
                    NotificationsListPartialChange.Refresh.Success(
                        data = data,
                        hasMore = messageListBean.page?.hasMore == "1"
                    )
                }
                .onStart { emit(NotificationsListPartialChange.Refresh.Start) }
                .catch { emit(NotificationsListPartialChange.Refresh.Failure(it)) }

        private fun NotificationsListUiIntent.LoadMore.produceLoadMorePartialChange() =
            notificationRepository.atMe(page = page)
                .map<MessageListBean, NotificationsListPartialChange.LoadMore> { messageListBean ->
                    val data = (messageListBean.atList ?: emptyList()).fastMap {
                        MessageItemData(it)
                    }
                    NotificationsListPartialChange.LoadMore.Success(
                        currentPage = page,
                        data = data,
                        hasMore = messageListBean.page?.hasMore == "1"
                    )
                }
                .onStart { emit(NotificationsListPartialChange.LoadMore.Start) }
                .catch { emit(NotificationsListPartialChange.LoadMore.Failure(currentPage = page, error = it)) }
    }
}

enum class NotificationsType {
    ReplyMe, AtMe
}

sealed interface NotificationsListUiIntent : UiIntent {
    data object Refresh : NotificationsListUiIntent

    data class LoadMore(val page: Int) : NotificationsListUiIntent
}

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

@Immutable
data class MessageItemData(
    val info: MessageListBean.MessageInfoBean,
    val blocked: Boolean = info.shouldBlock(),
)

data class NotificationsListUiState(
    val isRefreshing: Boolean = true,
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val hasMore: Boolean = true,
    val data: ImmutableList<MessageItemData> = persistentListOf(),
) : UiState

sealed interface NotificationsListUiEvent : UiEvent