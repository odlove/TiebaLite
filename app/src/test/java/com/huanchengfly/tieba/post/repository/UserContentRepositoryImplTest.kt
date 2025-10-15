package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.protos.userPost.UserPostResponse
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
 * Unit tests for UserContentRepositoryImpl
 *
 * Tests verify that the repository correctly delegates to ITiebaApi and propagates
 * Flow emissions for user content operations (userPost).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserContentRepositoryImplTest {

    private lateinit var repository: UserContentRepositoryImpl
    private lateinit var mockApi: ITiebaApi

    @Before
    fun setup() {
        mockApi = mockk()
        repository = UserContentRepositoryImpl(mockApi)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ========== Helper Functions ==========

    private fun createMockUserPostResponse(): UserPostResponse {
        return mockk<UserPostResponse> {
            every { error } returns mockk {
                every { error_code } returns 0
                every { error_msg } returns "success"
            }
            every { data_ } returns mockk()
        }
    }

    // ========== userPost Tests ==========

    @Test
    fun `userPost should return success flow when API call succeeds`() = runTest {
        // Given: Mock API returns successful UserPostResponse
        val uid = 123456L
        val page = 1
        val isThread = true
        val expectedResponse = createMockUserPostResponse()

        every {
            mockApi.userPostFlow(uid, page, isThread)
        } returns flowOf(expectedResponse)

        // When: Call repository method
        val result = repository.userPost(uid, page, isThread).first()

        // Then: Verify the result matches expected data
        assertNotNull(result)
        assertEquals(0, result.error?.error_code ?: -1)
        verify(exactly = 1) {
            mockApi.userPostFlow(uid, page, isThread)
        }
    }

    @Test
    fun `userPost should propagate error when API call fails`() = runTest {
        // Given: Mock API throws exception
        val uid = 123456L
        val page = 1
        val isThread = true
        val expectedException = RuntimeException("Network error")

        every {
            mockApi.userPostFlow(uid, page, isThread)
        } returns flow { throw expectedException }

        // When & Then: Verify exception is propagated
        try {
            repository.userPost(uid, page, isThread).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Network error", e.message)
        }
    }

    @Test
    fun `userPost should handle default parameters correctly`() = runTest {
        // Given: Use default parameters
        val uid = 123456L
        val expectedResponse = createMockUserPostResponse()

        every {
            mockApi.userPostFlow(uid, 1, true)
        } returns flowOf(expectedResponse)

        // When: Call repository with default parameters
        val result = repository.userPost(uid).first()

        // Then: Verify API is called with default values
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.userPostFlow(uid, 1, true)
        }
    }

    @Test
    fun `userPost should handle pagination correctly`() = runTest {
        // Given: Different page numbers
        val uid = 123456L
        val isThread = true

        // Test page 1
        val page1Response = createMockUserPostResponse()
        every {
            mockApi.userPostFlow(uid, 1, isThread)
        } returns flowOf(page1Response)

        val result1 = repository.userPost(uid, 1, isThread).first()
        assertEquals(0, result1.error?.error_code ?: -1)

        // Test page 2
        val page2Response = createMockUserPostResponse()
        every {
            mockApi.userPostFlow(uid, 2, isThread)
        } returns flowOf(page2Response)

        val result2 = repository.userPost(uid, 2, isThread).first()
        assertEquals(0, result2.error?.error_code ?: -1)

        // Verify both pages were called
        verify(exactly = 1) { mockApi.userPostFlow(uid, 1, isThread) }
        verify(exactly = 1) { mockApi.userPostFlow(uid, 2, isThread) }
    }

    @Test
    fun `userPost should handle isThread parameter correctly`() = runTest {
        // Given: Different isThread values
        val uid = 123456L
        val page = 1

        // Test isThread = true (threads)
        val threadsResponse = createMockUserPostResponse()
        every {
            mockApi.userPostFlow(uid, page, true)
        } returns flowOf(threadsResponse)

        val result1 = repository.userPost(uid, page, true).first()
        assertNotNull(result1)

        // Test isThread = false (replies)
        val repliesResponse = createMockUserPostResponse()
        every {
            mockApi.userPostFlow(uid, page, false)
        } returns flowOf(repliesResponse)

        val result2 = repository.userPost(uid, page, false).first()
        assertNotNull(result2)

        // Verify both modes were called
        verify(exactly = 1) { mockApi.userPostFlow(uid, page, true) }
        verify(exactly = 1) { mockApi.userPostFlow(uid, page, false) }
    }
}
