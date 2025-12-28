package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.protos.pbFloor.PbFloorResponse
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SubPostsRepositoryImpl
 *
 * Tests verify that the repository correctly delegates to ITiebaApi and propagates
 * Flow emissions for sub-posts operations (pbFloor).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SubPostsRepositoryImplTest {
    private lateinit var repository: SubPostsRepositoryImpl
    private lateinit var mockApi: ITiebaApi

    @Before
    fun setup() {
        mockApi = mockk()
        repository = SubPostsRepositoryImpl(mockApi)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ========== Helper Functions ==========

    private fun createMockPbFloorResponse(
        threadId: Long = 123L,
        postId: Long = 456L,
        forumId: Long = 789L,
        forumName: String = "Test Forum",
        currentPage: Int = 1,
        totalPage: Int = 1,
        totalCount: Int = 0
    ): PbFloorResponse =
        mockk<PbFloorResponse> {
            every { data_ } returns
                mockk {
                    every { post } returns
                        mockk(relaxed = true) {
                            every { id } returns postId
                            every { tid } returns threadId
                            every { author_id } returns 0L
                            every { floor } returns 1
                            every { title } returns ""
                            every { is_ntitle } returns 0
                            every { time } returns 0
                            every { content } returns emptyList()
                        }
                    every { page } returns
                        mockk(relaxed = true) {
                            every { current_page } returns currentPage
                            every { total_page } returns totalPage
                            every { total_count } returns totalCount
                            every { has_more } returns 0
                            every { has_prev } returns 0
                        }
                    every { forum } returns
                        mockk(relaxed = true) {
                            every { id } returns forumId
                            every { name } returns forumName
                            every { avatar } returns ""
                        }
                    every { thread } returns
                        mockk<ThreadInfo>(relaxed = true) {
                            every { id } returns threadId
                            every { firstPostId } returns 0L
                            every { title } returns ""
                            every { replyNum } returns 0
                            every { this@mockk.forumId } returns forumId
                            every { this@mockk.forumName } returns forumName
                            every { is_share_thread } returns 0
                            every { agreeNum } returns 0
                            every { pids } returns ""
                            every { collectStatus } returns 0
                            every { collectMarkPid } returns "0"
                        }
                    every { anti } returns mockk(relaxed = true)
                    every { subpost_list } returns emptyList()
                }
        }

    // ========== pbFloor Tests ==========

    @Test
    fun `pbFloor should return success flow when API call succeeds`() =
        runTest {
            // Given: Mock API returns successful PbFloorResponse
            val threadId = 123456L
            val postId = 789L
            val forumId = 1L
            val page = 1
            val subPostId = 0L
            val expectedResponse = createMockPbFloorResponse(
                threadId = threadId,
                postId = postId,
                forumId = forumId,
                currentPage = page
            )

            every {
                mockApi.pbFloorFlow(threadId, postId, forumId, page, subPostId)
            } returns flowOf(expectedResponse)

            // When: Call repository method
            val result = repository.pbFloor(threadId, postId, forumId, page, subPostId).first()

            // Then: Verify the result matches expected data
            assertNotNull(result)
            assertEquals(threadId, result.thread.threadId)
            assertEquals(postId, result.post.id)
            assertEquals(page, result.page.currentPage)
            verify(exactly = 1) {
                mockApi.pbFloorFlow(threadId, postId, forumId, page, subPostId)
            }
        }

    @Test
    fun `pbFloor should propagate error when API call fails`() =
        runTest {
            // Given: Mock API throws exception
            val threadId = 123456L
            val postId = 789L
            val forumId = 1L
            val page = 1
            val subPostId = 0L
            val expectedException = RuntimeException("Network error")

            every {
                mockApi.pbFloorFlow(threadId, postId, forumId, page, subPostId)
            } returns flow { throw expectedException }

            // When & Then: Verify exception is propagated
            try {
                repository.pbFloor(threadId, postId, forumId, page, subPostId).first()
                throw AssertionError("Expected RuntimeException to be thrown")
            } catch (e: RuntimeException) {
                assertEquals("Network error", e.message)
            }
        }

    @Test
    fun `pbFloor should handle default parameters correctly`() =
        runTest {
            // Given: Use default parameters
            val threadId = 123456L
            val postId = 789L
            val forumId = 1L
            val expectedResponse = createMockPbFloorResponse()

            every {
                mockApi.pbFloorFlow(threadId, postId, forumId, 1, 0L)
            } returns flowOf(expectedResponse)

            // When: Call repository with default parameters
            val result = repository.pbFloor(threadId, postId, forumId).first()

            // Then: Verify API is called with default values
            assertNotNull(result)
            verify(exactly = 1) {
                mockApi.pbFloorFlow(threadId, postId, forumId, 1, 0L)
            }
        }

    @Test
    fun `pbFloor should handle page pagination correctly`() =
        runTest {
            // Given: Different page numbers
            val threadId = 123456L
            val postId = 789L
            val forumId = 1L
            val subPostId = 0L

            // Test page 1
            val page1Response = createMockPbFloorResponse(currentPage = 1)
            every {
                mockApi.pbFloorFlow(threadId, postId, forumId, 1, subPostId)
            } returns flowOf(page1Response)

            val result1 = repository.pbFloor(threadId, postId, forumId, 1, subPostId).first()
            assertEquals(1, result1.page.currentPage)

            // Test page 2
            val page2Response = createMockPbFloorResponse(currentPage = 2)
            every {
                mockApi.pbFloorFlow(threadId, postId, forumId, 2, subPostId)
            } returns flowOf(page2Response)

            val result2 = repository.pbFloor(threadId, postId, forumId, 2, subPostId).first()
            assertEquals(2, result2.page.currentPage)

            // Verify both pages were called
            verify(exactly = 1) { mockApi.pbFloorFlow(threadId, postId, forumId, 1, subPostId) }
            verify(exactly = 1) { mockApi.pbFloorFlow(threadId, postId, forumId, 2, subPostId) }
        }

    @Test
    fun `pbFloor should handle subPostId parameter correctly`() =
        runTest {
            // Given: Specific subPostId
            val threadId = 123456L
            val postId = 789L
            val forumId = 1L
            val page = 1
            val subPostId = 999L
            val expectedResponse = createMockPbFloorResponse()

            every {
                mockApi.pbFloorFlow(threadId, postId, forumId, page, subPostId)
            } returns flowOf(expectedResponse)

            // When: Call repository with specific subPostId
            val result = repository.pbFloor(threadId, postId, forumId, page, subPostId).first()

            // Then: Verify API is called with correct subPostId
            assertNotNull(result)
            verify(exactly = 1) {
                mockApi.pbFloorFlow(threadId, postId, forumId, page, subPostId)
            }
        }

    @Test
    fun `pbFloor should handle different threadId values`() =
        runTest {
            // Given: Different thread IDs
            val postId = 789L
            val forumId = 1L
            val page = 1
            val subPostId = 0L

            // Test thread 1
            val thread1Response = createMockPbFloorResponse()
            every {
                mockApi.pbFloorFlow(111L, postId, forumId, page, subPostId)
            } returns flowOf(thread1Response)

            val result1 = repository.pbFloor(111L, postId, forumId, page, subPostId).first()
            assertNotNull(result1)

            // Test thread 2
            val thread2Response = createMockPbFloorResponse()
            every {
                mockApi.pbFloorFlow(222L, postId, forumId, page, subPostId)
            } returns flowOf(thread2Response)

            val result2 = repository.pbFloor(222L, postId, forumId, page, subPostId).first()
            assertNotNull(result2)

            // Verify both threads were called
            verify(exactly = 1) { mockApi.pbFloorFlow(111L, postId, forumId, page, subPostId) }
            verify(exactly = 1) { mockApi.pbFloorFlow(222L, postId, forumId, page, subPostId) }
        }
}
