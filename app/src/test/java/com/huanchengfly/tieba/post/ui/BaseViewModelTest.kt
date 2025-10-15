package com.huanchengfly.tieba.post.ui

import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.DispatcherProvider
import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.arch.UiState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass

/**
 * Base class for ViewModel unit tests
 *
 * Provides common test infrastructure:
 * - StandardTestDispatcher and DispatcherProvider setup
 * - Android Log mocking (static, applied once for all tests)
 * - Dispatchers.Main setup/teardown
 * - Utility methods for collecting UI state
 *
 * Usage:
 * ```
 * class MyViewModelTest : BaseViewModelTest() {
 *     private lateinit var mockRepository: MyRepository
 *
 *     @Before
 *     override fun setup() {
 *         super.setup()
 *         mockRepository = mockk(relaxed = true)
 *     }
 *
 *     @After
 *     override fun tearDown() {
 *         super.tearDown()
 *         clearMocks(mockRepository)
 *     }
 *
 *     @Test
 *     fun myTest() = runTest(testDispatcher) {
 *         val viewModel = MyViewModel(mockRepository, testDispatcherProvider)
 *         val job = collectUiState(viewModel)
 *         testDispatcher.scheduler.advanceUntilIdle()
 *         // ... test logic
 *         job.cancelAndJoin()
 *     }
 * }
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseViewModelTest {

    companion object {
        @JvmStatic
        @BeforeClass
        fun setupClass() {
            // Mock Android Log globally to avoid "Method not mocked" errors
            // This needs to happen before any tests run, as ViewModels may log on background threads
            io.mockk.mockkStatic(android.util.Log::class)
            io.mockk.every { android.util.Log.i(any(), any()) } returns 0
            io.mockk.every { android.util.Log.d(any(), any()) } returns 0
            io.mockk.every { android.util.Log.e(any(), any()) } returns 0
            io.mockk.every { android.util.Log.v(any(), any()) } returns 0
            io.mockk.every { android.util.Log.w(any<String>(), any<String>()) } returns 0
        }
    }

    protected val testDispatcher = StandardTestDispatcher()
    protected lateinit var testDispatcherProvider: DispatcherProvider

    @Before
    open fun setup() {
        Dispatchers.setMain(testDispatcher)
        testDispatcherProvider = object : DispatcherProvider {
            override val main: CoroutineDispatcher = testDispatcher
            override val io: CoroutineDispatcher = testDispatcher
        }
    }

    @After
    open fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Collects UI state from a ViewModel in the background
     * Must be called within a TestScope (e.g., inside runTest)
     *
     * @param viewModel The ViewModel to collect state from
     * @return A Job that should be cancelled when the test completes
     */
    protected fun <I : UiIntent, PC : PartialChange<S>, S : UiState, E : UiEvent> TestScope.collectUiState(
        viewModel: BaseViewModel<I, PC, S, E>
    ) = launch { viewModel.uiState.collect { } }

    /**
     * Alternative for ViewModels that don't extend BaseViewModel
     */
    protected fun <T> TestScope.collectState(stateFlow: StateFlow<T>) =
        launch { stateFlow.collect { } }

    /**
     * Helper to mock AccountUtil.requireLoginInfo() for tests that need it
     *
     * Usage:
     * ```
     * @Before
     * override fun setup() {
     *     super.setup()
     *     mockAccountUtil(tbs = "test_tbs")
     * }
     * ```
     */
    protected fun mockAccountUtil(tbs: String = "test_tbs") {
        io.mockk.mockkStatic("com.huanchengfly.tieba.post.utils.AccountUtil")
        io.mockk.every {
            com.huanchengfly.tieba.post.utils.AccountUtil.requireLoginInfo()
        } returns mockk {
            every { this@mockk.tbs } returns tbs
        }
    }
}
