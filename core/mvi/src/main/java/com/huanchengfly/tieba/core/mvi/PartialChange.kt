package com.huanchengfly.tieba.core.mvi

interface PartialChange<State : UiState> {
    fun reduce(oldState: State): State
}
