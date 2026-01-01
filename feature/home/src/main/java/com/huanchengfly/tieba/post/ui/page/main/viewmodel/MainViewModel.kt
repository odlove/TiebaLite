package com.huanchengfly.tieba.post.ui.page.main.viewmodel

import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.core.mvi.BaseViewModel
import com.huanchengfly.tieba.core.mvi.DispatcherProvider
import com.huanchengfly.tieba.core.mvi.PartialChangeProducer
import com.huanchengfly.tieba.core.mvi.UiEvent
import com.huanchengfly.tieba.core.mvi.UiState
import com.huanchengfly.tieba.post.ui.page.main.contract.MainPartialChange
import com.huanchengfly.tieba.post.ui.page.main.contract.MainUiEvent
import com.huanchengfly.tieba.post.ui.page.main.contract.MainUiIntent
import com.huanchengfly.tieba.post.ui.page.main.contract.MainUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat

@Stable
@HiltViewModel
class MainViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val effectMapper: MainEffectMapper,
    private val useCaseRegistry: MainUseCaseRegistry
) : BaseViewModel<MainUiIntent, MainPartialChange, MainUiState, MainUiEvent>(dispatcherProvider) {
    override fun createInitialState(): MainUiState = MainUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<MainUiIntent, MainPartialChange, MainUiState> =
        MainPartialChangeProducer()

    override fun dispatchEvent(partialChange: MainPartialChange): UiEvent? =
        effectMapper.map(partialChange)

    private inner class MainPartialChangeProducer :
        PartialChangeProducer<MainUiIntent, MainPartialChange, MainUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<MainUiIntent>): Flow<MainPartialChange> =
            intentFlow.flatMapConcat { intent ->
                useCaseRegistry.execute(intent)
            }
    }
}
