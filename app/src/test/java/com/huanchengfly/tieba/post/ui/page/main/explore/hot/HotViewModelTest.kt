package com.huanchengfly.tieba.post.ui.page.main.explore.hot

import com.huanchengfly.tieba.post.TestFixtures
import com.huanchengfly.tieba.post.repository.ContentRecommendRepository
import com.huanchengfly.tieba.post.repository.UserInteractionRepository
import com.huanchengfly.tieba.post.ui.BaseViewModelTest
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for HotViewModel
 *
 * Tests verify that dependency injection works correctly and repository methods
 * are called with appropriate parameters.
 *
 * Testing Strategy:
 * - Only verify repository method calls with correct parameters (smoke test)
 * - No state/event assertions (due to SharingStarted.Eagerly complexity)
 * - Confirms dependency injection wiring is correct after DispatcherProvider refactoring
 *
 * Test Coverage:
 * - Load: Verifies contentRecommendRepository.hotThreadList("all") is called
 * - RefreshThreadList: Verifies contentRecommendRepository.hotThreadList(tabCode) is called
 * - Agree: Verifies userInteractionRepository.opAgree() is called
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HotViewModelTest : BaseViewModelTest() {

    private lateinit var mockThreadStore: com.huanchengfly.tieba.post.store.ThreadStore
    private lateinit var mockContentRecommendRepo: ContentRecommendRepository
    private lateinit var mockUserInteractionRepo: UserInteractionRepository

    @Before
    override fun setup() {
        super.setup()
        mockThreadStore = mockk(relaxed = true)
        mockContentRecommendRepo = mockk(relaxed = true)
        mockUserInteractionRepo = mockk(relaxed = true)
    }

    @After
    override fun tearDown() {
        super.tearDown()
        clearMocks(mockThreadStore, mockContentRecommendRepo, mockUserInteractionRepo)
    }

    // ========== Load Tests ==========

    @Test
    fun `Load should call contentRecommendRepository hotThreadList with all tabCode`() =
        runTest(testDispatcher) {
            // Given: Mock repository returns success
            val response = TestFixtures.fakeHotThreadListResponse()
            every { mockContentRecommendRepo.hotThreadList("all") } returns flowOf(response)

            // When: Create ViewModel and send Load intent
            val viewModel = HotViewModel(
                mockContentRecommendRepo,
                mockUserInteractionRepo,
                mockThreadStore,
                testDispatcherProvider
            )
            val job = collectUiState(viewModel)
            testDispatcher.scheduler.advanceUntilIdle() // Let initialization complete
            viewModel.send(HotUiIntent.Load)
            testDispatcher.scheduler.advanceUntilIdle() // Let coroutines execute

            // Then: Verify hotThreadList() was called with "all"
            verify(atLeast = 1) {
                mockContentRecommendRepo.hotThreadList("all")
            }
            job.cancelAndJoin()
        }

    // ========== RefreshThreadList Tests ==========

    @Test
    fun `RefreshThreadList should call contentRecommendRepository hotThreadList with specific tabCode`() =
        runTest(testDispatcher) {
            // Given: Mock repository returns success
            val response = TestFixtures.fakeHotThreadListResponse()
            every { mockContentRecommendRepo.hotThreadList("game") } returns flowOf(response)

            // When: Create ViewModel and send RefreshThreadList intent
            val viewModel = HotViewModel(
                mockContentRecommendRepo,
                mockUserInteractionRepo,
                mockThreadStore,
                testDispatcherProvider
            )
            val job = collectUiState(viewModel)
            testDispatcher.scheduler.advanceUntilIdle() // Let initialization complete
            viewModel.send(HotUiIntent.RefreshThreadList(tabCode = "game"))
            testDispatcher.scheduler.advanceUntilIdle() // Let coroutines execute

            // Then: Verify hotThreadList() was called with "game"
            verify(atLeast = 1) {
                mockContentRecommendRepo.hotThreadList("game")
            }
            job.cancelAndJoin()
        }

    // ========== Agree Tests ==========

    @Test
    fun `Agree should call userInteractionRepository opAgree with correct parameters`() =
        runTest(testDispatcher) {
            // Given: Mock repository returns success
            val agreeBean = TestFixtures.fakeAgreeBean()
            every {
                mockUserInteractionRepo.opAgree("123", "456", 0, objType = 3)
            } returns flowOf(agreeBean)

            // When: Create ViewModel and send Agree intent
            val viewModel = HotViewModel(
                mockContentRecommendRepo,
                mockUserInteractionRepo,
                mockThreadStore,
                testDispatcherProvider
            )
            val job = collectUiState(viewModel)
            testDispatcher.scheduler.advanceUntilIdle() // Let initialization complete
            viewModel.send(HotUiIntent.Agree(threadId = 123L, postId = 456L, hasAgree = 0))
            testDispatcher.scheduler.advanceUntilIdle() // Let coroutines execute

            // Then: Verify opAgree() was called
            verify(atLeast = 1) {
                mockUserInteractionRepo.opAgree("123", "456", 0, objType = 3)
            }
            job.cancelAndJoin()
        }
}
