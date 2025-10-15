package com.huanchengfly.tieba.post.ui.page.search.thread

import com.huanchengfly.tieba.post.TestFixtures
import com.huanchengfly.tieba.post.repository.SearchRepository
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
 * Unit tests for SearchThreadViewModel
 *
 * Tests verify that dependency injection works correctly and repository methods
 * are called with appropriate parameters.
 *
 * Testing Strategy:
 * - Only verify repository method calls with correct parameters (smoke test)
 * - No state/event assertions (due to SharingStarted.Eagerly complexity)
 *
 * Test Coverage:
 * - Refresh: Verifies searchRepository.searchThread() is called with page = 1
 * - LoadMore: Verifies searchRepository.searchThread() is called with page + 1
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchThreadViewModelTest : BaseViewModelTest() {

    private lateinit var mockSearchRepo: SearchRepository

    @Before
    override fun setup() {
        super.setup()
        mockSearchRepo = mockk(relaxed = true)
    }

    @After
    override fun tearDown() {
        super.tearDown()
        clearMocks(mockSearchRepo)
    }

    // ========== Refresh Tests ==========

    @Test
    fun `Refresh should call searchRepository searchThread with correct parameters`() =
        runTest(testDispatcher) {
            // Given: Mock repository returns search results
            val response = TestFixtures.fakeSearchThreadBean()
            every {
                mockSearchRepo.searchThread("test keyword", 1, SearchThreadSortType.SORT_TYPE_NEWEST)
            } returns flowOf(response)

            // When: Create ViewModel and send Refresh intent
            val viewModel = SearchThreadViewModel(mockSearchRepo, testDispatcherProvider)
            val job = collectUiState(viewModel)
            testDispatcher.scheduler.advanceUntilIdle() // Let initialization complete
            viewModel.send(SearchThreadUiIntent.Refresh("test keyword", SearchThreadSortType.SORT_TYPE_NEWEST))
            testDispatcher.scheduler.advanceUntilIdle() // Let coroutines execute

            // Then: Verify searchThread() was called with page = 1
            verify(atLeast = 1) {
                mockSearchRepo.searchThread("test keyword", 1, SearchThreadSortType.SORT_TYPE_NEWEST)
            }
            job.cancelAndJoin()
        }

    // ========== LoadMore Tests ==========

    @Test
    fun `LoadMore should call searchRepository searchThread with correct page number`() =
        runTest(testDispatcher) {
            // Given: Mock repository returns search results
            val response = TestFixtures.fakeSearchThreadBean()
            every {
                mockSearchRepo.searchThread("test keyword", 3, SearchThreadSortType.SORT_TYPE_RELATIVE)
            } returns flowOf(response)

            // When: Create ViewModel and send LoadMore intent
            val viewModel = SearchThreadViewModel(mockSearchRepo, testDispatcherProvider)
            val job = collectUiState(viewModel)
            testDispatcher.scheduler.advanceUntilIdle() // Let initialization complete
            viewModel.send(SearchThreadUiIntent.LoadMore("test keyword", 2, SearchThreadSortType.SORT_TYPE_RELATIVE))
            testDispatcher.scheduler.advanceUntilIdle() // Let coroutines execute

            // Then: Verify searchThread() was called with page = 3 (page + 1)
            verify(atLeast = 1) {
                mockSearchRepo.searchThread("test keyword", 3, SearchThreadSortType.SORT_TYPE_RELATIVE)
            }
            job.cancelAndJoin()
        }
}
