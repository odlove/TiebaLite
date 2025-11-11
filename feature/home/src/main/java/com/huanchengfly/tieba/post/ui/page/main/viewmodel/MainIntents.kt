package com.huanchengfly.tieba.post.ui.page.main

import com.huanchengfly.tieba.core.mvi.UiIntent

sealed interface MainUiIntent : UiIntent {
    sealed interface NewMessage : MainUiIntent {
        data class Receive(val messageCount: Int) : NewMessage
        data object Clear : NewMessage
    }
}
