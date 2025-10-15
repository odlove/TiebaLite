package com.huanchengfly.tieba.post.ui.page.main.notifications.list

import com.huanchengfly.tieba.post.TestFixtures
import com.huanchengfly.tieba.post.repository.NotificationRepository
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
 * Unit tests for NotificationsListViewModel (ReplyMeListViewModel and AtMeListViewModel)
 *
 * Tests verify that dependency injection works correctly and NotificationRepository methods
 * are called with appropriate parameters.
 *
 * Testing Strategy:
 * - Only verify repository method calls with correct parameters
 * - No state/event assertions (due to SharingStarted.Eagerly complexity)
 * - Confirms dependency injection wiring is correct after DispatcherProvider refactoring
 *
 * Test Coverage:
 * - Refresh: Verifies repository.replyMe() / repository.atMe() is called
 * - LoadMore: Verifies repository is called with correct page parameter
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsListViewModelTest : BaseViewModelTest() {

    private lateinit var mockNotificationRepo: NotificationRepository

    @Before
    override fun setup() {
        super.setup()
        mockNotificationRepo = mockk(relaxed = true)
    }

    @After
    override fun tearDown() {
        super.tearDown()
        clearMocks(mockNotificationRepo)
    }

    // ========== ReplyMeListViewModel Tests ==========

    @Test
    fun `ReplyMe Refresh should call repository replyMe with default page`() = runTest(testDispatcher) {
        // Given: Mock repository returns success
        val response = TestFixtures.fakeMessageListBean()
        every { mockNotificationRepo.replyMe(page = 1) } returns flowOf(response)

        // When: Create ViewModel and send Refresh intent
        val viewModel = ReplyMeListViewModel(mockNotificationRepo, testDispatcherProvider)
        val job = collectUiState(viewModel)
        testDispatcher.scheduler.advanceUntilIdle() // Let initialization complete
        viewModel.send(NotificationsListUiIntent.Refresh)
        testDispatcher.scheduler.advanceUntilIdle() // Let coroutines execute

        // Then: Verify replyMe() was called with default page
        verify(atLeast = 1) { mockNotificationRepo.replyMe(page = 1) }
        job.cancelAndJoin()
    }

    @Test
    fun `ReplyMe LoadMore should call repository replyMe with correct page`() = runTest(testDispatcher) {
        // Given: Mock repository returns success
        val response = TestFixtures.fakeMessageListBean()
        every { mockNotificationRepo.replyMe(page = 2) } returns flowOf(response)

        // When: Create ViewModel and send LoadMore intent
        val viewModel = ReplyMeListViewModel(mockNotificationRepo, testDispatcherProvider)
        val job = collectUiState(viewModel)
        testDispatcher.scheduler.advanceUntilIdle() // Let initialization complete
        viewModel.send(NotificationsListUiIntent.LoadMore(page = 2))
        testDispatcher.scheduler.advanceUntilIdle() // Let coroutines execute

        // Then: Verify replyMe() was called with page 2
        verify(atLeast = 1) { mockNotificationRepo.replyMe(page = 2) }
        job.cancelAndJoin()
    }

    // ========== AtMeListViewModel Tests ==========

    @Test
    fun `AtMe Refresh should call repository atMe with default page`() = runTest(testDispatcher) {
        // Given: Mock repository returns success
        val response = TestFixtures.fakeMessageListBean()
        every { mockNotificationRepo.atMe(page = 1) } returns flowOf(response)

        // When: Create ViewModel and send Refresh intent
        val viewModel = AtMeListViewModel(mockNotificationRepo, testDispatcherProvider)
        val job = collectUiState(viewModel)
        testDispatcher.scheduler.advanceUntilIdle() // Let initialization complete
        viewModel.send(NotificationsListUiIntent.Refresh)
        testDispatcher.scheduler.advanceUntilIdle() // Let coroutines execute

        // Then: Verify atMe() was called with default page
        verify(atLeast = 1) { mockNotificationRepo.atMe(page = 1) }
        job.cancelAndJoin()
    }

    @Test
    fun `AtMe LoadMore should call repository atMe with correct page`() = runTest(testDispatcher) {
        // Given: Mock repository returns success
        val response = TestFixtures.fakeMessageListBean()
        every { mockNotificationRepo.atMe(page = 3) } returns flowOf(response)

        // When: Create ViewModel and send LoadMore intent
        val viewModel = AtMeListViewModel(mockNotificationRepo, testDispatcherProvider)
        val job = collectUiState(viewModel)
        testDispatcher.scheduler.advanceUntilIdle() // Let initialization complete
        viewModel.send(NotificationsListUiIntent.LoadMore(page = 3))
        testDispatcher.scheduler.advanceUntilIdle() // Let coroutines execute

        // Then: Verify atMe() was called with page 3
        verify(atLeast = 1) { mockNotificationRepo.atMe(page = 3) }
        job.cancelAndJoin()
    }
}
