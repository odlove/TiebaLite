package com.huanchengfly.tieba.post.ui.page.main

import com.huanchengfly.tieba.post.ui.BaseViewModelTest
import com.huanchengfly.tieba.post.ui.page.main.contract.MainUiIntent
import com.huanchengfly.tieba.post.ui.page.main.usecase.ClearMessageUseCase
import com.huanchengfly.tieba.post.ui.page.main.usecase.ReceiveMessageUseCase
import com.huanchengfly.tieba.post.ui.page.main.viewmodel.MainEffectMapper
import com.huanchengfly.tieba.post.ui.page.main.viewmodel.MainIntentUseCase
import com.huanchengfly.tieba.post.ui.page.main.viewmodel.MainUseCaseRegistry
import com.huanchengfly.tieba.post.ui.page.main.viewmodel.MainViewModel
import javax.inject.Provider
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Unit tests for MainViewModel
 *
 * - Since MainViewModel only performs state transitions without external dependencies,
 *   we can run it without using Hilt for dependency injection.
 */
class MainViewModelTest : BaseViewModelTest() {
    private fun createViewModel(): MainViewModel {
        val providers = mapOf<Class<out MainUiIntent>, Provider<MainIntentUseCase<out MainUiIntent>>>(
            MainUiIntent.NewMessage.Receive::class.java to Provider { ReceiveMessageUseCase() },
            MainUiIntent.NewMessage.Clear::class.java to Provider { ClearMessageUseCase() }
        )
        val registry = MainUseCaseRegistry(providers)
        return MainViewModel(
            dispatcherProvider = testDispatcherProvider,
            effectMapper = MainEffectMapper(),
            useCaseRegistry = registry
        )
    }

    @Test
    fun `receive message should update state`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        val job = collectUiState(viewModel)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.send(MainUiIntent.NewMessage.Receive(messageCount = 5))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assert(state.messageCount == 5)
        job.cancelAndJoin()
    }

    @Test
    fun `clear message should reset state`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        val job = collectUiState(viewModel)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.send(MainUiIntent.NewMessage.Receive(messageCount = 5))
        viewModel.send(MainUiIntent.NewMessage.Clear)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assert(state.messageCount == 0)
        job.cancelAndJoin()
    }
}
