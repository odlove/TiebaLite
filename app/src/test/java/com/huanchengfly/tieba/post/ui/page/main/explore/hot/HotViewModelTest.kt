package com.huanchengfly.tieba.post.ui.page.main.explore.hot

import com.huanchengfly.tieba.post.TestFixtures
import com.huanchengfly.tieba.post.api.models.protos.FrsTabInfo
import com.huanchengfly.tieba.post.api.models.protos.RecommendTopicList
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

    // ========== Load Tests ==========

    @Test
    fun `Load should call contentRecommendRepository hotThreadList with all tabCode`() =
        runTest(testDispatcher) {
            // Given: Mock repository returns success
            val response = ThreadFeedPage()
            every { mockThreadFeedRepo.hotThreadList("all") } returns flowOf(response)

            // When: Create ViewModel and send Load intent
            val viewModel = HotViewModel(
                mockThreadFeedRepo,
                mockUserInteractionRepo,
                mockPbPageRepo,
                testDispatcherProvider
            )
            val job = collectUiState(viewModel)
            testDispatcher.scheduler.advanceUntilIdle() // Let initialization complete
            viewModel.send(HotUiIntent.Load)
            testDispatcher.scheduler.advanceUntilIdle() // Let coroutines execute

            // Then: Verify hotThreadList() was called with "all"
            verify(atLeast = 1) {
                mockThreadFeedRepo.hotThreadList("all")
            }
            job.cancelAndJoin()
        }

    // ========== RefreshThreadList Tests ==========

    @Test
    fun `RefreshThreadList should call contentRecommendRepository hotThreadList with specific tabCode`() =
        runTest(testDispatcher) {
            // Given: Mock repository returns success
            val response = ThreadFeedPage()
            every { mockThreadFeedRepo.hotThreadList("game") } returns flowOf(response)

            // When: Create ViewModel and send RefreshThreadList intent
            val viewModel = HotViewModel(
                mockThreadFeedRepo,
                mockUserInteractionRepo,
                mockPbPageRepo,
                testDispatcherProvider
            )
            val job = collectUiState(viewModel)
            testDispatcher.scheduler.advanceUntilIdle() // Let initialization complete
            viewModel.send(HotUiIntent.RefreshThreadList(tabCode = "game"))
            testDispatcher.scheduler.advanceUntilIdle() // Let coroutines execute

            // Then: Verify hotThreadList() was called with "game"
            verify(atLeast = 1) {
                mockThreadFeedRepo.hotThreadList("game")
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
                mockThreadFeedRepo,
                mockUserInteractionRepo,
                mockPbPageRepo,
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

    @Test
    fun `Load success should populate topics tabs and threadIds`() = runTest(testDispatcher) {
        val topic = mockk<RecommendTopicList>(relaxed = true)
        val tab = mockk<FrsTabInfo>(relaxed = true) {
            every { tabCode } returns "video"
        }
        val feedPage = ThreadFeedPage(
            threadIds = persistentListOf(1L, 2L),
            topicList = persistentListOf(topic),
            tabList = persistentListOf(tab)
        )
        every { mockThreadFeedRepo.hotThreadList("all") } returns flowOf(feedPage)

        val viewModel = createViewModel()
        val job = collectUiState(viewModel)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.send(HotUiIntent.Load)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertEquals(feedPage.threadIds, state.threadIds)
        assertEquals(1, state.topicList.size)
        assertEquals(topic, state.topicList.first().item)
        assertEquals(tab, state.tabList.first().item)
        job.cancelAndJoin()
    }

    @Test
    fun `RefreshThreadList success should replace threadIds and update tab code`() = runTest(testDispatcher) {
        val refreshPage = ThreadFeedPage(
            threadIds = persistentListOf(4L, 5L)
        )
        every { mockThreadFeedRepo.hotThreadList("video") } returns flowOf(refreshPage)

        val viewModel = createViewModel()
        val job = collectUiState(viewModel)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.send(HotUiIntent.RefreshThreadList(tabCode = "video"))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingThreadList)
        assertEquals("video", state.currentTabCode)
        assertEquals(refreshPage.threadIds, state.threadIds)
        job.cancelAndJoin()
    }

    private fun createViewModel(): HotViewModel =
        HotViewModel(
            mockThreadFeedRepo,
            mockUserInteractionRepo,
            mockPbPageRepo,
            testDispatcherProvider
        )
}
