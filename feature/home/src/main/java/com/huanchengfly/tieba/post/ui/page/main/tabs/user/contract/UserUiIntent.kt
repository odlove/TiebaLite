package com.huanchengfly.tieba.post.ui.page.main.tabs.user.contract

import com.huanchengfly.tieba.core.mvi.UiIntent

sealed interface UserUiIntent : UiIntent {
    data object Refresh : UserUiIntent
}
