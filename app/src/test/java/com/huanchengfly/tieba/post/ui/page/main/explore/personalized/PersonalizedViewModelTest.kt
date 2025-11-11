package com.huanchengfly.tieba.post.ui.page.main.explore.personalized

import com.huanchengfly.tieba.post.TestFixtures
import com.huanchengfly.tieba.core.mvi.wrapImmutable
import com.huanchengfly.tieba.post.models.DislikeBean
import com.huanchengfly.tieba.post.models.PersonalizedMetadata
import com.huanchengfly.tieba.post.models.ThreadFeedPage
import com.huanchengfly.tieba.post.repository.PbPageRepository
import com.huanchengfly.tieba.post.repository.ThreadFeedRepository
import com.huanchengfly.tieba.post.repository.UserInteractionRepository
import com.huanchengfly.tieba.post.ui.BaseViewModelTest
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Unit tests for PersonalizedViewModel
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
 * - Refresh: Verifies personalizedRepository.personalizedFlow(1, 1) is called
 * - LoadMore: Verifies personalizedRepository.personalizedFlow(2, page) is called
 * - Agree: Verifies userInteractionRepository.opAgree() is called
 * - Dislike: Verifies userInteractionRepository.submitDislike() is called
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PersonalizedViewModelTest : BaseViewModelTest() {

    private lateinit var mockThreadFeedRepo: ThreadFeedRepository
    private lateinit var mockUserInteractionRepo: UserInteractionRepository
    private lateinit var mockPbPageRepo: PbPageRepository

    @Before
    override fun setup() {
        super.setup()
        mockThreadFeedRepo = mockk(relaxed = true)
        mockUserInteractionRepo = mockk(relaxed = true)
        mockPbPageRepo = mockk(relaxed = true) {
            every { threadFlow(any()) } returns kotlinx.coroutines.flow.MutableStateFlow(null)
            every { upsertThreads(any()) } answers { }
        }
    }

    @After
    override fun tearDown() {
        super.tearDown()
        clearMocks(mockThreadFeedRepo, mockUserInteractionRepo, mockPbPageRepo)
    }

    // ========== Refresh Tests ==========

    @Test
    fun `Refresh should call personalizedRepository personalizedFlow with page 1`() =
        runTest(testDispatcher) {
            // Given: Mock repository returns success
            val response = ThreadFeedPage()
            every { mockThreadFeedRepo.personalizedThreads(1) } returns flowOf(response)

            // When: Create ViewModel and send Refresh intent
            val viewModel = PersonalizedViewModel(
                mockThreadFeedRepo,
                mockUserInteractionRepo,
                mockPbPageRepo,
                testDispatcherProvider
            )
            val job = collectUiState(viewModel)
            testDispatcher.scheduler.advanceUntilIdle() // Let initialization complete
            viewModel.send(PersonalizedUiIntent.Refresh)
            testDispatcher.scheduler.advanceUntilIdle() // Let coroutines execute

            // Then: Verify personalizedFlow() was called with correct parameters
            verify(atLeast = 1) {
                mockThreadFeedRepo.personalizedThreads(1)
            }
            job.cancelAndJoin()
        }

    // ========== LoadMore Tests ==========

    @Test
    fun `LoadMore should call personalizedRepository personalizedFlow with page 2`() =
        runTest(testDispatcher) {
            // Given: Mock repository returns success
            val response = ThreadFeedPage()
            every { mockThreadFeedRepo.personalizedThreads(2) } returns flowOf(response)

            // When: Create ViewModel and send LoadMore intent
            val viewModel = PersonalizedViewModel(
                mockThreadFeedRepo,
                mockUserInteractionRepo,
                mockPbPageRepo,
                testDispatcherProvider
            )
            val job = collectUiState(viewModel)
            testDispatcher.scheduler.advanceUntilIdle() // Let initialization complete
            viewModel.send(PersonalizedUiIntent.LoadMore(page = 2))
            testDispatcher.scheduler.advanceUntilIdle() // Let coroutines execute

            // Then: Verify personalizedFlow() was called with page 2
            verify(atLeast = 1) {
                mockThreadFeedRepo.personalizedThreads(2)
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
            val viewModel = PersonalizedViewModel(
                mockThreadFeedRepo,
                mockUserInteractionRepo,
                mockPbPageRepo,
                testDispatcherProvider
            )
            val job = collectUiState(viewModel)
            testDispatcher.scheduler.advanceUntilIdle() // Let initialization complete
            viewModel.send(PersonalizedUiIntent.Agree(threadId = 123L, postId = 456L, hasAgree = 0))
            testDispatcher.scheduler.advanceUntilIdle() // Let coroutines execute

            // Then: Verify opAgree() was called
            verify(atLeast = 1) {
                mockUserInteractionRepo.opAgree("123", "456", 0, objType = 3)
            }
            job.cancelAndJoin()
        }

    // ========== Dislike Tests ==========

    @Test
    fun `Dislike should call userInteractionRepository submitDislike with correct parameters`() =
        runTest(testDispatcher) {
            // Given: Mock repository returns success
            val response = TestFixtures.fakeCommonResponse()
            val dislikeBean = DislikeBean("123", "1,2", "789", 1000L, "extra1,extra2")
            every { mockUserInteractionRepo.submitDislike(dislikeBean) } returns flowOf(response)

            // When: Create ViewModel and send Dislike intent
            val viewModel = PersonalizedViewModel(
                mockThreadFeedRepo,
                mockUserInteractionRepo,
                mockPbPageRepo,
                testDispatcherProvider
            )
            val job = collectUiState(viewModel)
            testDispatcher.scheduler.advanceUntilIdle() // Let initialization complete

            val mockReason1 = mockk<com.huanchengfly.tieba.post.api.models.protos.personalized.DislikeReason>(relaxed = true) {
                every { dislikeId } returns 1
                every { extra } returns "extra1"
            }
            val mockReason2 = mockk<com.huanchengfly.tieba.post.api.models.protos.personalized.DislikeReason>(relaxed = true) {
                every { dislikeId } returns 2
                every { extra } returns "extra2"
            }

            viewModel.send(
                PersonalizedUiIntent.Dislike(
                    forumId = 789L,
                    threadId = 123L,
                    reasons = listOf(mockReason1.wrapImmutable(), mockReason2.wrapImmutable()),
                    clickTime = 1000L
                )
            )
            testDispatcher.scheduler.advanceUntilIdle() // Let coroutines execute

            // Then: Verify submitDislike() was called
            verify(atLeast = 1) {
                mockUserInteractionRepo.submitDislike(dislikeBean)
            }
            job.cancelAndJoin()
        }

    @Test
    fun `Refresh success should update threadIds metadata and reset flags`() = runTest(testDispatcher) {
        val metadataMap = persistentMapOf<Long, PersonalizedMetadata>(
            1L to PersonalizedMetadata(blocked = false),
            2L to PersonalizedMetadata(blocked = true)
        )
        val feedPage = ThreadFeedPage(
            threadIds = persistentListOf(1L, 2L),
            metadata = metadataMap
        )
        every { mockThreadFeedRepo.personalizedThreads(1) } returns flowOf(feedPage)

        val viewModel = createViewModel()
        val job = collectUiState(viewModel)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.send(PersonalizedUiIntent.Refresh)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertEquals(feedPage.threadIds, state.threadIds)
        assertEquals(metadataMap, state.metadata)
        assertEquals(0, state.refreshPosition)
        job.cancelAndJoin()
    }

    @Test
    fun `LoadMore success should merge ids and replace metadata for overlapping entries`() = runTest(testDispatcher) {
        val initialMeta1 = PersonalizedMetadata(blocked = false)
        val initialMeta2 = PersonalizedMetadata(blocked = true)
        val initialFeed = ThreadFeedPage(
            threadIds = persistentListOf(1L, 2L),
            metadata = persistentMapOf<Long, PersonalizedMetadata>(
                1L to initialMeta1,
                2L to initialMeta2
            )
        )
        every { mockThreadFeedRepo.personalizedThreads(1) } returns flowOf(initialFeed)

        val loadMetaOverlap = PersonalizedMetadata(blocked = false)
        val loadMetaNew = PersonalizedMetadata(blocked = true)
        val loadMoreFeed = ThreadFeedPage(
            threadIds = persistentListOf(2L, 3L),
            metadata = persistentMapOf<Long, PersonalizedMetadata>(
                2L to loadMetaOverlap,
                3L to loadMetaNew
            )
        )
        every { mockThreadFeedRepo.personalizedThreads(2) } returns flowOf(loadMoreFeed)

        val viewModel = createViewModel()
        val job = collectUiState(viewModel)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.send(PersonalizedUiIntent.Refresh)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.send(PersonalizedUiIntent.LoadMore(page = 2))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingMore)
        assertEquals(persistentListOf(1L, 2L, 3L), state.threadIds)
        assertEquals(
            persistentMapOf<Long, PersonalizedMetadata>(
                1L to initialMeta1,
                2L to loadMetaOverlap,
                3L to loadMetaNew
            ),
            state.metadata
        )
        assertEquals(2, state.currentPage)
        job.cancelAndJoin()
    }

    @Test
    fun `Dislike should optimistically hide thread id once`() = runTest(testDispatcher) {
        val dislikeResponse = TestFixtures.fakeCommonResponse()
        every { mockUserInteractionRepo.submitDislike(any()) } returns flowOf(dislikeResponse)

        val viewModel = createViewModel()
        val job = collectUiState(viewModel)
        testDispatcher.scheduler.advanceUntilIdle()

        val reason = mockk<com.huanchengfly.tieba.post.api.models.protos.personalized.DislikeReason>(relaxed = true) {
            every { dislikeId } returns 1
            every { extra } returns "extra"
        }.wrapImmutable()

        viewModel.send(
            PersonalizedUiIntent.Dislike(
                forumId = 123L,
                threadId = 999L,
                reasons = listOf(reason),
                clickTime = 1000L
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(persistentListOf(999L), state.hiddenThreadIds)
        job.cancelAndJoin()
    }

    private fun createViewModel(): PersonalizedViewModel =
        PersonalizedViewModel(
            mockThreadFeedRepo,
            mockUserInteractionRepo,
            mockPbPageRepo,
            testDispatcherProvider
        )
}
