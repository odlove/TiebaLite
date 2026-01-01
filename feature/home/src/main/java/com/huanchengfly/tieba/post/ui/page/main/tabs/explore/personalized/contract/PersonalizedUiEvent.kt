package com.huanchengfly.tieba.post.ui.page.main.tabs.explore.personalized.contract

import com.huanchengfly.tieba.core.mvi.UiEvent

sealed interface PersonalizedUiEvent : UiEvent {
    data class RefreshSuccess(val count: Int) : PersonalizedUiEvent
}
