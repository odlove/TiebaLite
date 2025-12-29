package com.huanchengfly.tieba.post.ui.page.forum.threadlist

import com.huanchengfly.tieba.core.common.preferences.AppPreferencesDataSource
import com.huanchengfly.tieba.core.common.repository.ThreadMetaStore
import com.huanchengfly.tieba.core.common.feed.ThreadCard
import com.huanchengfly.tieba.core.common.forum.ForumInfo
import com.huanchengfly.tieba.core.common.forum.ForumPageData
import com.huanchengfly.tieba.core.common.forum.ForumPageInfo
import com.huanchengfly.tieba.post.repository.FrsPageRepository
import com.huanchengfly.tieba.post.repository.PbPageRepository
import com.huanchengfly.tieba.post.repository.UserInteractionRepository
import com.huanchengfly.tieba.post.ui.BaseViewModelTest
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Minimal smoke tests for ForumThreadListViewModel.
 *
 * Focuses on verifying that the expected repository methods are invoked. This helps detect
 * dependency-injection regressions like the one that caused a NullPointerException earlier.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ForumThreadListViewModelTest : BaseViewModelTest() {

    private lateinit var mockFrsRepo: FrsPageRepository
    private lateinit var mockUserRepo: UserInteractionRepository
    private lateinit var mockPbPageRepo: PbPageRepository
    private lateinit var mockAppPreferences: AppPreferencesDataSource
    private lateinit var mockThreadMetaStore: ThreadMetaStore

    @Before
    override fun setup() {
        super.setup()
        mockFrsRepo = mockk(relaxed = true)
        mockUserRepo = mockk(relaxed = true)
        mockPbPageRepo = mockk(relaxed = true) {
            every { threadFlow(any()) } returns kotlinx.coroutines.flow.MutableStateFlow(null)
            every { upsertThreads(any()) } answers { }
        }
        mockAppPreferences = mockk(relaxed = true)
        mockThreadMetaStore = mockk(relaxed = true)
    }

    @After
    override fun tearDown() {
        super.tearDown()
        clearMocks(mockFrsRepo, mockUserRepo, mockPbPageRepo, mockAppPreferences, mockThreadMetaStore)
    }

    private fun createForumPageDataForList(): ForumPageData =
        ForumPageData(
            forum = ForumInfo(),
            page = ForumPageInfo(hasMore = true),
            threadList = emptyList(),
            threadIdList = emptyList()
        )

    // region LatestThreadListViewModel

    @Test
    fun latest_firstLoad_requestsFrsPage() = runTest(testDispatcher) {
        val response = createForumPageDataForList()
        var callCount = 0
        val callSignal = CompletableDeferred<Unit>()
        every {
            mockFrsRepo.frsPage(
                any(),
                any(),
                any(),
                any(),
                anyNullable(),
                any()
            )
        } answers {
            callCount++
            if (!callSignal.isCompleted) {
                callSignal.complete(Unit)
            }
            flowOf(response)
        }

        val viewModel = LatestThreadListViewModel(
            mockFrsRepo,
            mockUserRepo,
            mockPbPageRepo,
            testDispatcherProvider,
            mockAppPreferences,
            mockThreadMetaStore
        )
        val job = collectUiState(viewModel)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.send(ForumThreadListUiIntent.FirstLoad(forumName = "test_forum", sortType = 0))
        testDispatcher.scheduler.advanceUntilIdle()

        withTimeout(1_000) { callSignal.await() }
        assertEquals(1, callCount, "frsPage should be invoked exactly once")
        job.cancelAndJoin()
    }

    @Test
    fun latest_loadMore_withIds_requestsThreadList() = runTest(testDispatcher) {
        val threadListResponse = emptyList<ThreadCard>()
        var callCount = 0
        val callSignal = CompletableDeferred<Unit>()
        every { mockFrsRepo.threadList(any(), any(), any(), any(), any()) } answers {
            callCount++
            if (!callSignal.isCompleted) {
                callSignal.complete(Unit)
            }
            flowOf(threadListResponse)
        }

        val viewModel = LatestThreadListViewModel(
            mockFrsRepo,
            mockUserRepo,
            mockPbPageRepo,
            testDispatcherProvider,
            mockAppPreferences,
            mockThreadMetaStore
        )
        val job = collectUiState(viewModel)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.send(
            ForumThreadListUiIntent.LoadMore(
                forumId = 1L,
                forumName = "test_forum",
                currentPage = 1,
                threadListIds = listOf(1L, 2L, 3L),
                sortType = 0
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        withTimeout(1_000) { callSignal.await() }
        assertEquals(1, callCount, "threadList should be invoked exactly once when IDs provided")
        job.cancelAndJoin()
    }

    @Test
    fun latest_loadMore_withoutIds_requestsNextPage() = runTest(testDispatcher) {
        val response = createForumPageDataForList()
        var callCount = 0
        val callSignal = CompletableDeferred<Unit>()
        every {
            mockFrsRepo.frsPage(
                any(),
                any(),
                any(),
                any(),
                anyNullable(),
                any()
            )
        } answers {
            callCount++
            if (!callSignal.isCompleted) {
                callSignal.complete(Unit)
            }
            flowOf(response)
        }

        val viewModel = LatestThreadListViewModel(
            mockFrsRepo,
            mockUserRepo,
            mockPbPageRepo,
            testDispatcherProvider,
            mockAppPreferences,
            mockThreadMetaStore
        )
        val job = collectUiState(viewModel)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.send(
            ForumThreadListUiIntent.LoadMore(
                forumId = 1L,
                forumName = "test_forum",
                currentPage = 1,
                threadListIds = emptyList(),
                sortType = 0
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        withTimeout(1_000) { callSignal.await() }
        assertEquals(1, callCount, "frsPage should be invoked exactly once when no IDs provided")
        job.cancelAndJoin()
    }

    @Test
    fun latest_agree_requestsUserInteractionRepo() = runTest(testDispatcher) {
        every { mockUserRepo.opAgree(threadId = "1", postId = "2", hasAgree = 0, objType = 3) } returns flowOf(Unit)

        val viewModel = LatestThreadListViewModel(
            mockFrsRepo,
            mockUserRepo,
            mockPbPageRepo,
            testDispatcherProvider,
            mockAppPreferences,
            mockThreadMetaStore
        )
        val job = collectUiState(viewModel)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.send(ForumThreadListUiIntent.Agree(threadId = 1L, postId = 2L, hasAgree = 0))
        testDispatcher.scheduler.advanceUntilIdle()

        verify(exactly = 1) { mockUserRepo.opAgree("1", "2", 0, 3) }
        job.cancelAndJoin()
    }

    // endregion

    // region GoodThreadListViewModel

    @Test
    fun good_firstLoad_passesGoodClassifyId() = runTest(testDispatcher) {
        val response = createForumPageDataForList()
        var callCount = 0
        val capturedGoodClassifyId = CompletableDeferred<Int?>()
        every {
            mockFrsRepo.frsPage(
                any(),
                any(),
                any(),
                any(),
                anyNullable(),
                any()
            )
        } answers {
            callCount++
            if (!capturedGoodClassifyId.isCompleted) {
                capturedGoodClassifyId.complete(invocation.args[4] as Int?)
            }
            flowOf(response)
        }

        val viewModel = GoodThreadListViewModel(
            mockFrsRepo,
            mockUserRepo,
            mockPbPageRepo,
            testDispatcherProvider,
            mockAppPreferences,
            mockThreadMetaStore
        )
        val job = collectUiState(viewModel)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.send(
            ForumThreadListUiIntent.FirstLoad(
                forumName = "test_forum",
                goodClassifyId = 5
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val goodClassifyId = withTimeout(1_000) { capturedGoodClassifyId.await() }
        assertEquals(1, callCount, "frsPage should be called once for good first load")
        assertEquals(5, goodClassifyId, "goodClassifyId must be propagated")
        job.cancelAndJoin()
    }

    @Test
    fun good_loadMore_withoutIds_requestsNextGoodPage() = runTest(testDispatcher) {
        val response = createForumPageDataForList()
        var callCount = 0
        val capturedGoodClassifyId = CompletableDeferred<Int?>()
        every {
            mockFrsRepo.frsPage(
                forumName = any(),
                page = any(),
                loadType = any(),
                sortType = any(),
                goodClassifyId = anyNullable(),
                forceNew = any()
            )
        } answers {
            callCount++
            if (!capturedGoodClassifyId.isCompleted) {
                capturedGoodClassifyId.complete(invocation.args[4] as Int?)
            }
            flowOf(response)
        }

        val viewModel = GoodThreadListViewModel(
            mockFrsRepo,
            mockUserRepo,
            mockPbPageRepo,
            testDispatcherProvider,
            mockAppPreferences,
            mockThreadMetaStore
        )
        val job = collectUiState(viewModel)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.send(
            ForumThreadListUiIntent.LoadMore(
                forumId = 1L,
                forumName = "test_forum",
                currentPage = 1,
                threadListIds = emptyList(),
                goodClassifyId = 5
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val goodClassifyId = withTimeout(1_000) { capturedGoodClassifyId.await() }
        assertEquals(1, callCount, "frsPage should be called once for good load more without IDs")
        assertEquals(5, goodClassifyId, "goodClassifyId must remain 5 for load more")
        job.cancelAndJoin()
    }

    // endregion
}
