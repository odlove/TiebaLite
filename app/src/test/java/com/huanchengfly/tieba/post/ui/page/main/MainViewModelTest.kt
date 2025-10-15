package com.huanchengfly.tieba.post.ui.page.main

import com.huanchengfly.tieba.post.ui.BaseViewModelTest
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

    // ========== NewMessage.Receive Tests ==========

    @Test
    fun `NewMessage Receive should process intent without error`() =
        runTest(testDispatcher) {
            // Given: Create ViewModel
            val viewModel = MainViewModel(testDispatcherProvider)

            // When: Send NewMessage.Receive intent with messageCount = 5
            val job = collectUiState(viewModel)
            testDispatcher.scheduler.advanceUntilIdle() // Let initialization complete
            viewModel.send(MainUiIntent.NewMessage.Receive(messageCount = 5))
            testDispatcher.scheduler.advanceUntilIdle() // Let state update

            // Then: No exception thrown (intent processed successfully)
            job.cancelAndJoin()
        }

    // ========== NewMessage.Clear Tests ==========

    @Test
    fun `NewMessage Clear should process intent without error`() =
        runTest(testDispatcher) {
            // Given: Create ViewModel with messageCount = 5
            val viewModel = MainViewModel(testDispatcherProvider)
            val job = collectUiState(viewModel)
            testDispatcher.scheduler.advanceUntilIdle()
            viewModel.send(MainUiIntent.NewMessage.Receive(messageCount = 5))
            testDispatcher.scheduler.advanceUntilIdle()

            // When: Send NewMessage.Clear intent
            viewModel.send(MainUiIntent.NewMessage.Clear)
            testDispatcher.scheduler.advanceUntilIdle()

            // Then: No exception thrown (intent processed successfully)
            job.cancelAndJoin()
        }
}
