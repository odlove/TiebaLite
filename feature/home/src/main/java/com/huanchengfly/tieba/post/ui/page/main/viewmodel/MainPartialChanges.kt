package com.huanchengfly.tieba.post.ui.page.main

import com.huanchengfly.tieba.core.mvi.PartialChange

sealed interface MainPartialChange : PartialChange<MainUiState> {
    sealed class NewMessage : MainPartialChange {
        override fun reduce(oldState: MainUiState): MainUiState = when (this) {
            is Receive -> oldState.copy(messageCount = messageCount)
            Clear -> oldState.copy(messageCount = 0)
        }

        data class Receive(val messageCount: Int) : NewMessage()
        data object Clear : NewMessage()
    }
}
