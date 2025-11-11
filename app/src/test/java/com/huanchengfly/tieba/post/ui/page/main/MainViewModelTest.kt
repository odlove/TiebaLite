package com.huanchengfly.tieba.post.ui.page.main

import com.huanchengfly.tieba.post.ui.BaseViewModelTest
import com.huanchengfly.tieba.post.ui.page.main.usecase.ClearMessageUseCase
import com.huanchengfly.tieba.post.ui.page.main.usecase.ReceiveMessageUseCase
import javax.inject.Provider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Unit tests for MainViewModel
 *
 * Tests verify that the ViewModel correctly handles message count intents without errors.
 *
 * Testing Strategy:
 * - Since MainViewModel only performs state transitions without external dependencies,
 *   we verify intents are processed successfully (smoke test)
 * - No state assertions (StateFlow never completes, causing test hangs)
 * - No repository mocking needed
 *
 * Test Coverage:
 * - NewMessage.Receive: Verifies intent is processed without error
 * - NewMessage.Clear: Verifies intent is processed without error
 */
@OptIn(ExperimentalCoroutinesApi::class)
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

    // ========== NewMessage.Receive Tests ==========

    @Test
    fun `NewMessage Receive should update messageCount`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()

            val job = collectUiState(viewModel)
            testDispatcher.scheduler.advanceUntilIdle()
            viewModel.send(MainUiIntent.NewMessage.Receive(messageCount = 5))
            testDispatcher.scheduler.advanceUntilIdle()

            assert(viewModel.uiState.value.messageCount == 5)

            job.cancelAndJoin()
        }

    // ========== NewMessage.Clear Tests ==========

    @Test
    fun `NewMessage Clear should reset messageCount`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            val job = collectUiState(viewModel)
            testDispatcher.scheduler.advanceUntilIdle()
            viewModel.send(MainUiIntent.NewMessage.Receive(messageCount = 5))
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.send(MainUiIntent.NewMessage.Clear)
            testDispatcher.scheduler.advanceUntilIdle()

            assert(viewModel.uiState.value.messageCount == 0)

            job.cancelAndJoin()
        }
}
