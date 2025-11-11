package com.huanchengfly.tieba.post.ui.page.main

import com.huanchengfly.tieba.core.mvi.UiEvent
import javax.inject.Inject

class MainEffectMapper @Inject constructor() {
    fun map(partialChange: MainPartialChange): UiEvent? = when (partialChange) {
        is MainPartialChange.NewMessage -> null
    }
}
