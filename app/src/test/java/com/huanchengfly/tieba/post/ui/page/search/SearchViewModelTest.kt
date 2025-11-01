package com.huanchengfly.tieba.post.ui.page.search

import com.huanchengfly.tieba.post.TestFixtures
import com.huanchengfly.tieba.post.repository.SearchRepository
import com.huanchengfly.tieba.post.ui.BaseViewModelTest
import com.huanchengfly.tieba.core.common.ResourceProvider
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
 * Unit tests for SearchViewModel
 *
 * Tests verify that dependency injection works correctly and repository methods
 * are called with appropriate parameters.
 *
 * Testing Strategy:
 * - Only verify repository method calls with correct parameters (smoke test)
 * - No state/event assertions (due to SharingStarted.Eagerly complexity)
 *
 * Test Coverage:
 * - KeywordInputChanged: Verifies searchRepository.searchSuggestions() is called
 *
 * Skipped Tests:
 * - Init, ClearSearchHistory, DeleteSearchHistory, SubmitKeyword: These intents use LitePal
 *   database directly (global state) which is not initialized in test environment. Mocking
 *   LitePal is out of scope for smoke tests.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest : BaseViewModelTest() {

    private lateinit var mockSearchRepo: SearchRepository
    private lateinit var mockResourceProvider: ResourceProvider

    @Before
    override fun setup() {
        super.setup()
        mockSearchRepo = mockk(relaxed = true)
        mockResourceProvider = mockk(relaxed = true)
    }

    @After
    override fun tearDown() {
        super.tearDown()
        clearMocks(mockSearchRepo)
    }

    // ========== KeywordInputChanged Tests ==========

    @Test
    fun `KeywordInputChanged should call searchRepository searchSuggestions with correct keyword`() =
        runTest(testDispatcher) {
            // Given: Mock repository returns search suggestions
            val response = TestFixtures.fakeSearchSugResponse()
            every {
                mockSearchRepo.searchSuggestions("test keyword")
            } returns flowOf(response)

            // When: Create ViewModel and send KeywordInputChanged intent
            val viewModel = SearchViewModel(mockSearchRepo, testDispatcherProvider, mockResourceProvider)
            val job = collectUiState(viewModel)
            testDispatcher.scheduler.advanceUntilIdle() // Let initialization complete
            viewModel.send(SearchUiIntent.KeywordInputChanged("test keyword"))
            testDispatcher.scheduler.advanceUntilIdle() // Let coroutines execute

            // Then: Verify searchSuggestions() was called
            verify(atLeast = 1) {
                mockSearchRepo.searchSuggestions("test keyword")
            }
            job.cancelAndJoin()
        }
}
