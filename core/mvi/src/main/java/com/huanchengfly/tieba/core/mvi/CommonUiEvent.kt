package com.huanchengfly.tieba.core.mvi

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable

sealed interface CommonUiEvent : UiEvent {
    data object ScrollToTop : CommonUiEvent

    data object NavigateUp : CommonUiEvent

    data class Toast(
        val message: CharSequence,
        val length: Int = android.widget.Toast.LENGTH_SHORT
    ) : CommonUiEvent

    data class Refresh(val key: String) : CommonUiEvent

    data class StartSelectImages(
        val id: String,
        val maxCount: Int,
        val mediaType: MediaSelectorType,
    ) : CommonUiEvent

    data class SelectedImages(
        val id: String,
        val images: List<Uri>,
    ) : CommonUiEvent

    data class StartActivityForResult(
        val requesterId: String,
        val intent: Intent,
    ) : CommonUiEvent

    data class ActivityResult(
        val requesterId: String,
        val resultCode: Int,
        val intent: Intent?,
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
