package com.huanchengfly.tieba.post.ui.page.main.viewmodel

import com.huanchengfly.tieba.core.mvi.UiEvent
import com.huanchengfly.tieba.post.ui.page.main.contract.MainPartialChange
import javax.inject.Inject

class MainEffectMapper @Inject constructor() {
    fun map(partialChange: MainPartialChange): UiEvent? = when (partialChange) {
        is MainPartialChange.NewMessage -> null
    }
}
