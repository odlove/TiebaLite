package com.huanchengfly.tieba.post.ui.page.main.tabs.notifications.list.contract

import com.huanchengfly.tieba.core.mvi.UiIntent

sealed interface NotificationsListUiIntent : UiIntent {
    data object Refresh : NotificationsListUiIntent

    data class LoadMore(val page: Int) : NotificationsListUiIntent
}
