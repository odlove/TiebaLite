package com.huanchengfly.tieba.post.ui.page.main.tabs.explore.concern.viewmodel

import com.huanchengfly.tieba.core.common.feed.ConcernMetadata
import com.huanchengfly.tieba.core.common.feed.FeedMetadata
import com.huanchengfly.tieba.core.common.feed.ThreadFeedPage
import com.huanchengfly.tieba.core.common.repository.ThreadCardRepository
import com.huanchengfly.tieba.core.common.repository.ThreadFeedFacade
import com.huanchengfly.tieba.core.common.repository.UserInteractionFacade
import com.huanchengfly.tieba.post.ui.BaseViewModelTest
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.concern.contract.ConcernUiIntent
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

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

    private lateinit var mockThreadFeedRepo: ThreadFeedFacade
    private lateinit var mockUserInteractionRepo: UserInteractionFacade
    private lateinit var mockThreadCardRepo: ThreadCardRepository

    @Before
    override fun setup() {
        super.setup()
        mockThreadFeedRepo = mockk(relaxed = true)
        mockUserInteractionRepo = mockk(relaxed = true)
        mockThreadCardRepo = mockk(relaxed = true) {
            every { threadCardFlow(any()) } returns MutableStateFlow(null)
        }
    }

    @After
    override fun tearDown() {
        super.tearDown()
        clearMocks(mockThreadFeedRepo, mockUserInteractionRepo, mockThreadCardRepo)
    }

    // ========== Agree Tests ==========

    @Test
    fun `Agree should call userInteractionRepository opAgree with correct parameters`() =
        runTest(testDispatcher) {
            // Given: Mock repository returns success
            every {
                mockUserInteractionRepo.opAgree("123", "456", 0, objType = 3)
            } returns flowOf(Unit)

            // When: Create ViewModel and send Agree intent
            val viewModel = ConcernViewModel(
                mockThreadFeedRepo,
                mockUserInteractionRepo,
                mockThreadCardRepo,
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

    @Test
    fun `Refresh success should update state with latest threadIds and metadata`() = runTest(testDispatcher) {
        val metadata1 = ConcernMetadata(recommendType = 2)
        val metadata2 = ConcernMetadata(recommendType = 5)
        val feedPage = ThreadFeedPage(
            threadIds = persistentListOf(11L, 22L),
            metadata = persistentMapOf<Long, FeedMetadata>(
                11L to metadata1,
                22L to metadata2
            )
        )
        every { mockThreadFeedRepo.userLikeThreads(any(), any()) } returns flowOf(feedPage)

        val viewModel = createViewModel()
        val job = collectUiState(viewModel)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.send(ConcernUiIntent.Refresh)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertEquals(feedPage.threadIds, state.threadIds)
        assertEquals(
            persistentMapOf(
                11L to metadata1,
                22L to metadata2
            ),
            state.metadata
        )
        job.cancelAndJoin()
    }

    @Test
    fun `LoadMore merges unique threadIds and replaces metadata for duplicates`() = runTest(testDispatcher) {
        val initialMeta1 = ConcernMetadata(recommendType = 1)
        val initialMeta2 = ConcernMetadata(recommendType = 2)
        val refreshPage = ThreadFeedPage(
            threadIds = persistentListOf(10L, 20L),
            metadata = persistentMapOf<Long, FeedMetadata>(
                10L to initialMeta1,
                20L to initialMeta2
            )
        )
        every { mockThreadFeedRepo.userLikeThreads(any(), any()) } returns flowOf(refreshPage)

        val loadMetaOverlap = ConcernMetadata(recommendType = 99)
        val loadMetaNew = ConcernMetadata(recommendType = 3)
        val loadMorePage = ThreadFeedPage(
            threadIds = persistentListOf(20L, 30L),
            metadata = persistentMapOf<Long, FeedMetadata>(
                20L to loadMetaOverlap,
                30L to loadMetaNew
            )
        )
        every { mockThreadFeedRepo.concernThreads("next_tag", 2) } returns flowOf(loadMorePage)

        val viewModel = createViewModel()
        val job = collectUiState(viewModel)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.send(ConcernUiIntent.Refresh)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.send(ConcernUiIntent.LoadMore(pageTag = "next_tag"))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingMore)
        assertEquals(persistentListOf(10L, 20L, 30L), state.threadIds)
        assertEquals(
            persistentMapOf(
                10L to initialMeta1,
                20L to loadMetaOverlap,
                30L to loadMetaNew
            ),
            state.metadata
        )
        job.cancelAndJoin()
    }

    private fun createViewModel(): ConcernViewModel =
        ConcernViewModel(
            mockThreadFeedRepo,
            mockUserInteractionRepo,
            mockThreadCardRepo,
            testDispatcherProvider
        )
}
