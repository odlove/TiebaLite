package com.huanchengfly.tieba.post.ui.page.main.explore.personalized

import com.huanchengfly.tieba.post.TestFixtures
import com.huanchengfly.tieba.post.arch.wrapImmutable
import com.huanchengfly.tieba.post.models.DislikeBean
import com.huanchengfly.tieba.post.repository.PersonalizedRepository
import com.huanchengfly.tieba.post.repository.UserInteractionRepository
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

    private lateinit var mockThreadStore: com.huanchengfly.tieba.post.store.ThreadStore
    private lateinit var mockPersonalizedRepo: PersonalizedRepository
    private lateinit var mockUserInteractionRepo: UserInteractionRepository

    @Before
    override fun setup() {
        super.setup()
        mockThreadStore = mockk(relaxed = true)
        mockPersonalizedRepo = mockk(relaxed = true)
        mockUserInteractionRepo = mockk(relaxed = true)
    }

    @After
    override fun tearDown() {
        super.tearDown()
        clearMocks(mockThreadStore, mockPersonalizedRepo, mockUserInteractionRepo)
    }

    // ========== Refresh Tests ==========

    @Test
    fun `Refresh should call personalizedRepository personalizedFlow with page 1`() =
        runTest(testDispatcher) {
            // Given: Mock repository returns success
            val response = TestFixtures.fakePersonalizedResponse()
            every { mockPersonalizedRepo.personalizedFlow(loadType = 1, page = 1) } returns flowOf(response)

            // When: Create ViewModel and send Refresh intent
            val viewModel = PersonalizedViewModel(
                mockPersonalizedRepo,
                mockUserInteractionRepo,
                mockThreadStore,
                testDispatcherProvider
            )
            val job = collectUiState(viewModel)
            testDispatcher.scheduler.advanceUntilIdle() // Let initialization complete
            viewModel.send(PersonalizedUiIntent.Refresh)
            testDispatcher.scheduler.advanceUntilIdle() // Let coroutines execute

            // Then: Verify personalizedFlow() was called with correct parameters
            verify(atLeast = 1) {
                mockPersonalizedRepo.personalizedFlow(loadType = 1, page = 1)
            }
            job.cancelAndJoin()
        }

    // ========== LoadMore Tests ==========

    @Test
    fun `LoadMore should call personalizedRepository personalizedFlow with page 2`() =
        runTest(testDispatcher) {
            // Given: Mock repository returns success
            val response = TestFixtures.fakePersonalizedResponse()
            every { mockPersonalizedRepo.personalizedFlow(loadType = 2, page = 2) } returns flowOf(response)

            // When: Create ViewModel and send LoadMore intent
            val viewModel = PersonalizedViewModel(
                mockPersonalizedRepo,
                mockUserInteractionRepo,
                mockThreadStore,
                testDispatcherProvider
            )
            val job = collectUiState(viewModel)
            testDispatcher.scheduler.advanceUntilIdle() // Let initialization complete
            viewModel.send(PersonalizedUiIntent.LoadMore(page = 2))
            testDispatcher.scheduler.advanceUntilIdle() // Let coroutines execute

            // Then: Verify personalizedFlow() was called with page 2
            verify(atLeast = 1) {
                mockPersonalizedRepo.personalizedFlow(loadType = 2, page = 2)
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
                mockPersonalizedRepo,
                mockUserInteractionRepo,
                mockThreadStore,
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
                mockPersonalizedRepo,
                mockUserInteractionRepo,
                mockThreadStore,
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
}
