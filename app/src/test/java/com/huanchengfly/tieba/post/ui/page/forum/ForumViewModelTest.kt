package com.huanchengfly.tieba.post.ui.page.forum

import com.huanchengfly.tieba.post.TestFixtures
import com.huanchengfly.tieba.post.repository.ForumOperationRepository
import com.huanchengfly.tieba.post.repository.FrsPageRepository
import com.huanchengfly.tieba.post.ui.BaseViewModelTest
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ForumViewModel
 *
 * Tests verify that dependency injection works correctly and ForumOperationRepository methods
 * are called with appropriate parameters. This is a smoke test to prevent injection failures
 * similar to the ForumThreadListViewModel incident.
 *
 * Testing Strategy:
 * - Only verify repository method calls with correct parameters
 * - No state/event assertions (due to SharingStarted.Eagerly complexity)
 * - Confirms dependency injection wiring is correct after refactoring
 *
 * Test Coverage:
 * - SignIn: Verifies sign() is called with correct forumId, forumName, tbs
 * - Like: Verifies likeForum() is called
 * - Unlike: Verifies unlikeForum() is called
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ForumViewModelTest : BaseViewModelTest() {

    private lateinit var mockFrsPageRepo: FrsPageRepository
    private lateinit var mockForumOperationRepo: ForumOperationRepository

    @Before
    override fun setup() {
        super.setup()
        mockFrsPageRepo = mockk(relaxed = true)
        mockForumOperationRepo = mockk(relaxed = true)
    }

    @After
    override fun tearDown() {
        super.tearDown()
        clearMocks(mockFrsPageRepo, mockForumOperationRepo)
    }

    private fun createViewModel(): ForumViewModel {
        return ForumViewModel(
            mockFrsPageRepo,
            mockForumOperationRepo,
            testDispatcherProvider
        )
    }

    // ========== SignIn Tests ==========

    @Test
    fun `SignIn should call ForumOperationRepository sign with correct parameters`() = runTest {
        // Given: Mock repository returns success
        TestFixtures.mockSignSuccess(
            repo = mockForumOperationRepo,
            forumId = "123",
            forumName = "TestForum",
            tbs = "test_tbs_token"
        )

        // When: Create ViewModel and send SignIn intent
        val viewModel = createViewModel()
        val job = collectUiState(viewModel)
        testDispatcher.scheduler.advanceUntilIdle() // Let initialization complete
        viewModel.send(
            ForumUiIntent.SignIn(
                forumId = 123L,
                forumName = "TestForum",
                tbs = "test_tbs_token"
            )
        )
        testDispatcher.scheduler.advanceUntilIdle() // Let coroutines execute

        // Then: Verify sign() was called with correct parameters
        verify(atLeast = 1) {
            mockForumOperationRepo.sign(
                forumId = "123",
                forumName = "TestForum",
                tbs = "test_tbs_token"
            )
        }
        job.cancelAndJoin()
    }

    // ========== Like Tests ==========

    @Test
    fun `Like should call ForumOperationRepository likeForum with correct parameters`() = runTest {
        // Given: Mock repository returns success
        TestFixtures.mockLikeForumSuccess(
            repo = mockForumOperationRepo,
            forumId = "456",
            forumName = "TestForum2",
            tbs = "test_tbs_token_2"
        )

        // When: Create ViewModel and send Like intent
        val viewModel = createViewModel()
        val job = collectUiState(viewModel)
        testDispatcher.scheduler.advanceUntilIdle() // Let initialization complete
        viewModel.send(
            ForumUiIntent.Like(
                forumId = 456L,
                forumName = "TestForum2",
                tbs = "test_tbs_token_2"
            )
        )
        testDispatcher.scheduler.advanceUntilIdle() // Let coroutines execute

        // Then: Verify likeForum() was called with correct parameters
        verify(atLeast = 1) {
            mockForumOperationRepo.likeForum(
                forumId = "456",
                forumName = "TestForum2",
                tbs = "test_tbs_token_2"
            )
        }
        job.cancelAndJoin()
    }

    // ========== Unlike Tests ==========

    @Test
    fun `Unlike should call ForumOperationRepository unlikeForum with correct parameters`() = runTest {
        // Given: Mock repository returns success
        TestFixtures.mockUnlikeForumSuccess(
            repo = mockForumOperationRepo,
            forumId = "789",
            forumName = "TestForum3",
            tbs = "test_tbs_token_3"
        )

        // When: Create ViewModel and send Unlike intent
        val viewModel = createViewModel()
        val job = collectUiState(viewModel)
        testDispatcher.scheduler.advanceUntilIdle() // Let initialization complete
        viewModel.send(
            ForumUiIntent.Unlike(
                forumId = 789L,
                forumName = "TestForum3",
                tbs = "test_tbs_token_3"
            )
        )
        testDispatcher.scheduler.advanceUntilIdle() // Let coroutines execute

        // Then: Verify unlikeForum() was called with correct parameters
        verify(atLeast = 1) {
            mockForumOperationRepo.unlikeForum(
                forumId = "789",
                forumName = "TestForum3",
                tbs = "test_tbs_token_3"
            )
        }
        job.cancelAndJoin()
    }

    // ========== Load Tests ==========

    @Test
    fun `Load should call FrsPageRepository frsPage with correct parameters`() = runTest {
        // Given: Mock repository returns success
        val response = TestFixtures.fakeFrsPageResponse().apply {
            every { data_ } returns mockk(relaxed = true) {
                every { forum } returns mockk(relaxed = true)
                every { anti } returns mockk(relaxed = true) {
                    every { tbs } returns "tbs_from_response"
                }
            }
        }
        TestFixtures.mockFrsPageSuccess(mockFrsPageRepo, response)

        // When: Create ViewModel and send Load intent
        val viewModel = createViewModel()
        val job = collectUiState(viewModel)
        testDispatcher.scheduler.advanceUntilIdle() // Let initialization complete
        viewModel.send(ForumUiIntent.Load(forumName = "LoadTestForum", sortType = 1))
        testDispatcher.scheduler.advanceUntilIdle() // Let coroutines execute

        // Then: Verify frsPage() was called with correct parameters (forceNew=true for Load)
        verify(atLeast = 1) {
            mockFrsPageRepo.frsPage(
                forumName = "LoadTestForum",
                page = 1,
                loadType = 1,
                sortType = 1,
                goodClassifyId = null,
                forceNew = true
            )
        }
        job.cancelAndJoin()
    }
}
