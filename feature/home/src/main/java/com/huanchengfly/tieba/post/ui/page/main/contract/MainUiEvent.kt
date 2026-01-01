package com.huanchengfly.tieba.post.ui.page.main.contract

import com.huanchengfly.tieba.core.mvi.UiEvent

sealed interface MainUiEvent : UiEvent {
    data object Refresh : MainUiEvent
}
