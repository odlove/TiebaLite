package com.huanchengfly.tieba.post.ui.page.reply

import com.huanchengfly.tieba.post.TestFixtures
import com.huanchengfly.tieba.post.repository.AddPostRepository
import com.huanchengfly.tieba.post.ui.BaseViewModelTest
import com.huanchengfly.tieba.core.mvi.GlobalEventBus
import com.huanchengfly.tieba.core.common.ResourceProvider
import com.huanchengfly.tieba.post.utils.AppPreferencesUtils
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
 * Unit tests for ReplyViewModel
 *
 * Tests verify that dependency injection works correctly and AddPostRepository methods
 * are called with appropriate parameters.
 *
 * Testing Strategy:
 * - Only verify repository method calls with correct parameters (smoke test)
 * - No state/event assertions (due to SharingStarted.Eagerly complexity)
 * - Confirms dependency injection wiring is correct after DispatcherProvider refactoring
 *
 * Test Coverage:
 * - Send (new post reply): Verifies addPostRepository.addPost() is called with correct parameters
 * - Send (sub-post reply): Verifies addPostRepository.addPost() is called with postId/subPostId
 *
 * Skipped Tests:
 * - UploadImages: Requires mocking ImageUploader + FileUtil (complex, out of smoke test scope)
 * - State-only intents: SwitchPanel, AddImage, RemoveImage, ToggleIsOriginImage (low risk)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReplyViewModelTest : BaseViewModelTest() {

    private lateinit var mockAddPostRepo: AddPostRepository
    private lateinit var mockGlobalEventBus: GlobalEventBus
    private lateinit var mockResourceProvider: ResourceProvider
    private lateinit var mockContext: android.content.Context
    private lateinit var mockAppPreferences: AppPreferencesUtils

    @Before
    override fun setup() {
        super.setup()
        // Mock AccountUtil for tbs
        mockAccountUtil(tbs = "test_tbs")
        mockAddPostRepo = mockk(relaxed = true)
        mockGlobalEventBus = mockk(relaxed = true)
        mockResourceProvider = mockk(relaxed = true)
        mockContext = mockk(relaxed = true)
        mockAppPreferences = mockk(relaxed = true)
    }

    @After
    override fun tearDown() {
        super.tearDown()
        clearMocks(mockAddPostRepo, mockGlobalEventBus, mockResourceProvider, mockAppPreferences)
    }

    // ========== Send Tests ==========

    @Test
    fun `Send new post reply should call addPostRepository addPost with correct parameters`() =
        runTest(testDispatcher) {
            // Given: Mock repository returns success
            val response = TestFixtures.fakeAddPostResponse(tid = "123", pid = "456", expInc = "10")
            every {
                mockAddPostRepo.addPost(
                    content = "Test reply content",
                    forumId = 1L,
                    forumName = "TestForum",
                    threadId = 123L,
                    tbs = "test_tbs",
                    postId = null,
                    subPostId = null,
                    replyUserId = null
                )
            } returns flowOf(response)

            // When: Create ViewModel and send Send intent
            val viewModel = ReplyViewModel(
                mockAddPostRepo,
                mockGlobalEventBus,
                testDispatcherProvider,
                mockResourceProvider,
                mockContext,
                mockAppPreferences
            )
            val job = collectUiState(viewModel)
            testDispatcher.scheduler.advanceUntilIdle() // Let initialization complete
            viewModel.send(
                ReplyUiIntent.Send(
                    content = "Test reply content",
                    forumId = 1L,
                    forumName = "TestForum",
                    threadId = 123L,
                    tbs = "test_tbs",
                    postId = null,
                    subPostId = null,
                    replyUserId = null
                )
            )
            testDispatcher.scheduler.advanceUntilIdle() // Let coroutines execute

            // Then: Verify addPost() was called with correct parameters
            verify(atLeast = 1) {
                mockAddPostRepo.addPost(
                    content = "Test reply content",
                    forumId = 1L,
                    forumName = "TestForum",
                    threadId = 123L,
                    tbs = "test_tbs",
                    postId = null,
                    subPostId = null,
                    replyUserId = null
                )
            }
            job.cancelAndJoin()
        }

    @Test
    fun `Send sub-post reply should call addPostRepository addPost with postId and subPostId`() =
        runTest(testDispatcher) {
            // Given: Mock repository returns success
            val response = TestFixtures.fakeAddPostResponse(tid = "123", pid = "789", expInc = "5")
            every {
                mockAddPostRepo.addPost(
                    content = "Test sub-reply",
                    forumId = 2L,
                    forumName = "AnotherForum",
                    threadId = 456L,
                    tbs = "test_tbs",
                    postId = 999L,
                    subPostId = 888L,
                    replyUserId = 777L
                )
            } returns flowOf(response)

            // When: Create ViewModel and send Send intent with postId/subPostId
            val viewModel = ReplyViewModel(
                mockAddPostRepo,
                mockGlobalEventBus,
                testDispatcherProvider,
                mockResourceProvider,
                mockContext,
                mockAppPreferences
            )
            val job = collectUiState(viewModel)
            testDispatcher.scheduler.advanceUntilIdle() // Let initialization complete
            viewModel.send(
                ReplyUiIntent.Send(
                    content = "Test sub-reply",
                    forumId = 2L,
                    forumName = "AnotherForum",
                    threadId = 456L,
                    tbs = "test_tbs",
                    postId = 999L,
                    subPostId = 888L,
                    replyUserId = 777L
                )
            )
            testDispatcher.scheduler.advanceUntilIdle() // Let coroutines execute

            // Then: Verify addPost() was called with postId and subPostId
            verify(atLeast = 1) {
                mockAddPostRepo.addPost(
                    content = "Test sub-reply",
                    forumId = 2L,
                    forumName = "AnotherForum",
                    threadId = 456L,
                    tbs = "test_tbs",
                    postId = 999L,
                    subPostId = 888L,
                    replyUserId = 777L
                )
            }
            job.cancelAndJoin()
        }
}
