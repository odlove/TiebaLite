package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.protos.pbFloor.PbFloorResponse
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

    private fun createMockPbFloorResponse(): PbFloorResponse =
        mockk<PbFloorResponse> {
            every { error } returns
                mockk {
                    every { error_code } returns 0
                    every { error_msg } returns "success"
                }
            every { data_ } returns
                mockk {
                    every { post } returns mockk()
                    every { page } returns mockk()
                    every { forum } returns mockk()
                    every { thread } returns mockk()
                    every { anti } returns mockk()
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
            val expectedResponse = createMockPbFloorResponse()

            every {
                mockApi.pbFloorFlow(threadId, postId, forumId, page, subPostId)
            } returns flowOf(expectedResponse)

            // When: Call repository method
            val result = repository.pbFloor(threadId, postId, forumId, page, subPostId).first()

            // Then: Verify the result matches expected data
            assertNotNull(result)
            assertNotNull(result.data_)
            assertEquals(0, result.error?.error_code ?: -1)
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
            val page1Response = createMockPbFloorResponse()
            every {
                mockApi.pbFloorFlow(threadId, postId, forumId, 1, subPostId)
            } returns flowOf(page1Response)

            val result1 = repository.pbFloor(threadId, postId, forumId, 1, subPostId).first()
            assertEquals(0, result1.error?.error_code ?: -1)

            // Test page 2
            val page2Response = createMockPbFloorResponse()
            every {
                mockApi.pbFloorFlow(threadId, postId, forumId, 2, subPostId)
            } returns flowOf(page2Response)

            val result2 = repository.pbFloor(threadId, postId, forumId, 2, subPostId).first()
            assertEquals(0, result2.error?.error_code ?: -1)

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
