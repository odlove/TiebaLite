package com.huanchengfly.tieba.post.ui.page.main.tabs.notifications.list.viewmodel

import androidx.compose.runtime.Stable
import androidx.compose.ui.util.fastMap
import com.huanchengfly.tieba.core.network.error.defaultErrorMessage
import com.huanchengfly.tieba.core.mvi.BaseViewModel
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.DispatcherProvider
import com.huanchengfly.tieba.core.mvi.PartialChangeProducer
import com.huanchengfly.tieba.core.mvi.UiEvent
import com.huanchengfly.tieba.core.common.notification.NotificationMessage
import com.huanchengfly.tieba.core.common.notification.NotificationPage
import com.huanchengfly.tieba.core.common.repository.NotificationFeedRepository
import com.huanchengfly.tieba.post.ui.page.main.tabs.notifications.list.contract.MessageItemData
import com.huanchengfly.tieba.post.ui.page.main.tabs.notifications.list.contract.NotificationsListPartialChange
import com.huanchengfly.tieba.post.ui.page.main.tabs.notifications.list.contract.NotificationsListUiEvent
import com.huanchengfly.tieba.post.ui.page.main.tabs.notifications.list.contract.NotificationsListUiIntent
import com.huanchengfly.tieba.post.ui.page.main.tabs.notifications.list.contract.NotificationsListUiState
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

abstract class NotificationsListViewModel(
    dispatcherProvider: DispatcherProvider,
    protected val notificationFeedRepository: NotificationFeedRepository
) :
    BaseViewModel<NotificationsListUiIntent, NotificationsListPartialChange, NotificationsListUiState, NotificationsListUiEvent>(dispatcherProvider) {

    override fun createInitialState(): NotificationsListUiState = NotificationsListUiState()

    override fun dispatchEvent(partialChange: NotificationsListPartialChange): UiEvent? =
        when (partialChange) {
            is NotificationsListPartialChange.Refresh.Failure -> CommonUiEvent.Toast(partialChange.error.defaultErrorMessage())
            is NotificationsListPartialChange.LoadMore.Failure -> CommonUiEvent.Toast(partialChange.error.defaultErrorMessage())
            else -> null
        }
}

@Stable
@HiltViewModel
class ReplyMeListViewModel @Inject constructor(
    notificationFeedRepository: NotificationFeedRepository,
    dispatcherProvider: DispatcherProvider
) : NotificationsListViewModel(dispatcherProvider, notificationFeedRepository) {
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
            notificationFeedRepository.replyMe()
                .map<NotificationPage, NotificationsListPartialChange.Refresh> { page ->
                    val data = page.items.fastMap {
                        MessageItemData(it, it.blocked)
                    }
                    NotificationsListPartialChange.Refresh.Success(
                        data = data,
                        hasMore = page.hasMore
                    )
                }
                .onStart { emit(NotificationsListPartialChange.Refresh.Start) }
                .catch { emit(NotificationsListPartialChange.Refresh.Failure(it)) }

        private fun NotificationsListUiIntent.LoadMore.produceLoadMorePartialChange() =
            notificationFeedRepository.replyMe(page = page)
                .map<NotificationPage, NotificationsListPartialChange.LoadMore> { pageData ->
                    val data = pageData.items.fastMap {
                        MessageItemData(it, it.blocked)
                    }
                    NotificationsListPartialChange.LoadMore.Success(
                        currentPage = page,
                        data = data,
                        hasMore = pageData.hasMore
                    )
                }
                .onStart { emit(NotificationsListPartialChange.LoadMore.Start) }
                .catch { emit(NotificationsListPartialChange.LoadMore.Failure(currentPage = page, error = it)) }
    }
}

@Stable
@HiltViewModel
class AtMeListViewModel @Inject constructor(
    notificationFeedRepository: NotificationFeedRepository,
    dispatcherProvider: DispatcherProvider
) : NotificationsListViewModel(dispatcherProvider, notificationFeedRepository) {
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
            notificationFeedRepository.atMe()
                .map<NotificationPage, NotificationsListPartialChange.Refresh> { page ->
                    val data = page.items.fastMap {
                        MessageItemData(it, it.blocked)
                    }
                    NotificationsListPartialChange.Refresh.Success(
                        data = data,
                        hasMore = page.hasMore
                    )
                }
                .onStart { emit(NotificationsListPartialChange.Refresh.Start) }
                .catch { emit(NotificationsListPartialChange.Refresh.Failure(it)) }

        private fun NotificationsListUiIntent.LoadMore.produceLoadMorePartialChange() =
            notificationFeedRepository.atMe(page = page)
                .map<NotificationPage, NotificationsListPartialChange.LoadMore> { pageData ->
                    val data = pageData.items.fastMap {
                        MessageItemData(it, it.blocked)
                    }
                    NotificationsListPartialChange.LoadMore.Success(
                        currentPage = page,
                        data = data,
                        hasMore = pageData.hasMore
                    )
                }
                .onStart { emit(NotificationsListPartialChange.LoadMore.Start) }
                .catch { emit(NotificationsListPartialChange.LoadMore.Failure(currentPage = page, error = it)) }
    }
}
