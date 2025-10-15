package com.huanchengfly.tieba.post.ui.page.hottopic.list

import com.huanchengfly.tieba.post.TestFixtures
import com.huanchengfly.tieba.post.repository.ContentRecommendRepository
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
 * Unit tests for HotTopicListViewModel
 *
 * Tests verify that dependency injection works correctly and repository methods
 * are called with appropriate parameters.
 *
 * Testing Strategy:
 * - Only verify repository method calls with correct parameters (smoke test)
 * - No state/event assertions (due to SharingStarted.Eagerly complexity)
 *
 * Test Coverage:
 * - Load: Verifies contentRecommendRepository.topicList() is called
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HotTopicListViewModelTest : BaseViewModelTest() {

    private lateinit var mockContentRecommendRepo: ContentRecommendRepository

    @Before
    override fun setup() {
        super.setup()
        mockContentRecommendRepo = mockk(relaxed = true)
    }

    @After
    override fun tearDown() {
        super.tearDown()
        clearMocks(mockContentRecommendRepo)
    }

    // ========== Load Tests ==========

    @Test
    fun `Load should call contentRecommendRepository topicList`() =
        runTest(testDispatcher) {
            // Given: Mock repository returns topic list
            val response = TestFixtures.fakeTopicListResponse()
            every {
                mockContentRecommendRepo.topicList()
            } returns flowOf(response)

            // When: Create ViewModel and send Load intent
            val viewModel = HotTopicListViewModel(mockContentRecommendRepo, testDispatcherProvider)
            val job = collectUiState(viewModel)
            testDispatcher.scheduler.advanceUntilIdle() // Let initialization complete
            viewModel.send(HotTopicListUiIntent.Load)
            testDispatcher.scheduler.advanceUntilIdle() // Let coroutines execute

            // Then: Verify topicList() was called
            verify(atLeast = 1) {
                mockContentRecommendRepo.topicList()
            }
            job.cancelAndJoin()
        }
}
