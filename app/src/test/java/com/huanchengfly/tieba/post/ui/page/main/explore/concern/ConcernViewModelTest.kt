package com.huanchengfly.tieba.post.ui.page.main.explore.concern

import com.huanchengfly.tieba.post.TestFixtures
import com.huanchengfly.tieba.post.repository.ForumOperationRepository
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
 * Unit tests for ConcernViewModel
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
 * - Agree: Verifies userInteractionRepository.opAgree() is called
 *
 * Skipped Tests:
 * - Refresh and LoadMore: These intents access App.INSTANCE.appPreferences.userLikeLastRequestUnix
 *   (global state) which is not initialized in test environment. Mocking global state is out of
 *   scope for smoke tests.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ConcernViewModelTest : BaseViewModelTest() {

    private lateinit var mockThreadStore: com.huanchengfly.tieba.post.store.ThreadStore
    private lateinit var mockForumOperationRepo: ForumOperationRepository
    private lateinit var mockUserInteractionRepo: UserInteractionRepository

    @Before
    override fun setup() {
        super.setup()
        mockThreadStore = mockk(relaxed = true)
        mockForumOperationRepo = mockk(relaxed = true)
        mockUserInteractionRepo = mockk(relaxed = true)
    }

    @After
    override fun tearDown() {
        super.tearDown()
        clearMocks(mockThreadStore, mockForumOperationRepo, mockUserInteractionRepo)
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
            val viewModel = ConcernViewModel(
                mockForumOperationRepo,
                mockUserInteractionRepo,
                mockThreadStore,
                testDispatcherProvider
            )
            val job = collectUiState(viewModel)
            testDispatcher.scheduler.advanceUntilIdle() // Let initialization complete
            viewModel.send(ConcernUiIntent.Agree(threadId = 123L, postId = 456L, hasAgree = 0))
            testDispatcher.scheduler.advanceUntilIdle() // Let coroutines execute

            // Then: Verify opAgree() was called
            verify(atLeast = 1) {
                mockUserInteractionRepo.opAgree("123", "456", 0, objType = 3)
            }
            job.cancelAndJoin()
        }
}
