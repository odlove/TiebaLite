package com.huanchengfly.tieba.core.mvi

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable

sealed interface CommonUiEvent : UiEvent {
    data object ScrollToTop : CommonUiEvent

    data object NavigateUp : CommonUiEvent

    data class Toast(
        val message: CharSequence,
        val length: Int = android.widget.Toast.LENGTH_SHORT
    ) : CommonUiEvent
}

@Composable
fun BaseViewModel<*, *, *, *>.bindScrollToTopEvent(lazyListState: LazyListState) {
    onCommonEvent { event ->
        if (event is CommonUiEvent.ScrollToTop) {
            lazyListState.scrollToItem(0, 0)
        }
    }
}
