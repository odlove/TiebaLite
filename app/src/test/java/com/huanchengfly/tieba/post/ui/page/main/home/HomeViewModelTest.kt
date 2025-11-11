package com.huanchengfly.tieba.post.ui.page.main.home

import com.huanchengfly.tieba.post.TestFixtures
import com.huanchengfly.tieba.post.repository.ContentRecommendRepository
import com.huanchengfly.tieba.post.repository.ForumOperationRepository
import com.huanchengfly.tieba.post.ui.BaseViewModelTest
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.toImmutableList
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
 * Unit tests for HomeViewModel
 *
 * Tests verify that dependency injection works correctly and repository methods
 * are called with appropriate parameters.
 *
 * Testing Strategy:
 * - Only verify repository method calls with correct parameters (smoke test)
 * - No state/event assertions (due to SharingStarted.Eagerly complexity)
 * - Confirms dependency injection wiring is correct after DispatcherProvider refactoring
 * - Skips tests requiring LitePal/HistoryUtil mocking (too complex for smoke test)
 *
 * Test Coverage:
 * - Unfollow: Verifies forumOperationRepository.unlikeForum() is called
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest : BaseViewModelTest() {

    private lateinit var mockContentRecommendRepo: ContentRecommendRepository
    private lateinit var mockForumOperationRepo: ForumOperationRepository
    private lateinit var mockHistoryDataSource: com.huanchengfly.tieba.data.local.history.HistoryDataSource

    @Before
    override fun setup() {
        super.setup()
        // Mock AccountUtil for tbs
        mockAccountUtil(tbs = "test_tbs")

        mockContentRecommendRepo = mockk(relaxed = true)
        mockForumOperationRepo = mockk(relaxed = true)
        mockHistoryDataSource = mockk(relaxed = true)
    }

    @After
    override fun tearDown() {
        super.tearDown()
        clearMocks(mockContentRecommendRepo, mockForumOperationRepo)
    }

    // ========== Unfollow Tests ==========

    @Test
    fun `Unfollow should call forumOperationRepository unlikeForum with correct parameters`() =
        runTest(testDispatcher) {
            // Given: Mock repository returns success
            val response = TestFixtures.fakeCommonResponse()
            every {
                mockForumOperationRepo.unlikeForum("123", "TestForum", "test_tbs")
            } returns flowOf(response)

            // When: Create ViewModel and send Unfollow intent
            val viewModel = HomeViewModel(
                mockContentRecommendRepo,
                mockForumOperationRepo,
                mockHistoryDataSource,
                testDispatcherProvider
            )
            val job = collectUiState(viewModel)
            testDispatcher.scheduler.advanceUntilIdle() // Let initialization complete
            viewModel.send(HomeUiIntent.Unfollow(forumId = "123", forumName = "TestForum"))
            testDispatcher.scheduler.advanceUntilIdle() // Let coroutines execute

            // Then: Verify unlikeForum() was called with correct parameters
            verify(atLeast = 1) {
                mockForumOperationRepo.unlikeForum("123", "TestForum", "test_tbs")
            }
            job.cancelAndJoin()
        }

    @Test
    fun `Refresh success partial change should replace forum lists and stop loading`() {
        val initialState = HomeUiState(isLoading = true)
        val forums = listOf(
            HomeUiState.Forum(avatar = "a", forumId = "1", forumName = "Tieba1", isSign = true, levelId = "7"),
            HomeUiState.Forum(avatar = "b", forumId = "2", forumName = "Tieba2", isSign = false, levelId = "5"),
        )
        val topForums = listOf(
            HomeUiState.Forum(avatar = "b", forumId = "2", forumName = "Tieba2", isSign = false, levelId = "5"),
        )
        val history = listOf(
            com.huanchengfly.tieba.post.models.database.History(title = "history", data = "2", type = 1)
        )

        val partial = HomePartialChange.Refresh.Success(forums, topForums, history)
        val newState = partial.reduce(initialState)

        assertFalse(newState.isLoading)
        assertEquals(forums.toImmutableList(), newState.forums)
        assertEquals(topForums.toImmutableList(), newState.topForums)
        assertEquals(history.toImmutableList(), newState.historyForums)
        assertEquals(null, newState.error)
    }

    @Test
    fun `RefreshHistory success should replace only history list`() {
        val initialState = HomeUiState(historyForums = listOf(
            com.huanchengfly.tieba.post.models.database.History(title = "old", data = "x")
        ).toImmutableList())
        val newHistory = listOf(
            com.huanchengfly.tieba.post.models.database.History(title = "new", data = "y")
        )

        val newState = HomePartialChange.RefreshHistory.Success(newHistory).reduce(initialState)

        assertEquals(newHistory.toImmutableList(), newState.historyForums)
        assertEquals(initialState.forums, newState.forums)
    }

    @Test
    fun `ToggleHistory should update expandHistory flag`() {
        val initialState = HomeUiState(expandHistoryForum = false)
        val newState = HomePartialChange.ToggleHistory(expand = true).reduce(initialState)
        assertEquals(true, newState.expandHistoryForum)
    }
}
