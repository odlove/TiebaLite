package com.huanchengfly.tieba.post.ui.page.main.tabs.notifications.list.contract

import androidx.compose.runtime.Immutable
import com.huanchengfly.tieba.core.common.notification.NotificationMessage
import com.huanchengfly.tieba.core.mvi.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class NotificationsListUiState(
    val isRefreshing: Boolean = true,
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val hasMore: Boolean = true,
    val data: ImmutableList<MessageItemData> = persistentListOf(),
) : UiState

@Immutable
data class MessageItemData(
    val info: NotificationMessage,
    val blocked: Boolean,
)
