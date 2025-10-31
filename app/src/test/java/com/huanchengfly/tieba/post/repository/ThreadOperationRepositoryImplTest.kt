package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.core.network.model.CommonResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ThreadOperationRepositoryImplTest {
    private lateinit var repository: ThreadOperationRepositoryImpl
    private lateinit var mockApi: ITiebaApi
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        mockApi = mockk(relaxed = true)
        repository = ThreadOperationRepositoryImpl(mockApi)
    }

    @After
    fun tearDown() {
        // Clean up if needed
    }

    // ========== addStore tests ==========

    @Test
    fun `addStore should return success flow when API call succeeds`() = runTest(testDispatcher) {
        // Given
        val threadId = 123456L
        val postId = 789L
        val expectedResponse = createMockCommonResponse(errorCode = 0)
        every {
            mockApi.addStoreFlow(threadId, postId)
        } returns flowOf(expectedResponse)

        // When
        val result = repository.addStore(threadId, postId).first()

        // Then
        assertEquals(0, result.errorCode)
        verify { mockApi.addStoreFlow(threadId, postId) }
    }

    @Test
    fun `addStore should propagate error when API call fails`() = runTest(testDispatcher) {
        // Given
        val threadId = 123456L
        val postId = 789L
        val expectedException = RuntimeException("Network error")
        every {
            mockApi.addStoreFlow(threadId, postId)
        } returns flow { throw expectedException }

        // When/Then
        try {
            repository.addStore(threadId, postId).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Network error", e.message)
        }
    }

    @Test
    fun `addStore should handle different postId values`() = runTest(testDispatcher) {
        // Given
        val threadId = 123456L
        val postIds = listOf(0L, 1L, 999999L)

        postIds.forEach { postId ->
            val expectedResponse = createMockCommonResponse(errorCode = 0)
            every {
                mockApi.addStoreFlow(threadId, postId)
            } returns flowOf(expectedResponse)

            // When
            val result = repository.addStore(threadId, postId).first()

            // Then
            assertEquals(0, result.errorCode)
        }
    }

    // ========== removeStore tests ==========

    @Test
    fun `removeStore should return success flow when API call succeeds`() = runTest(testDispatcher) {
        // Given
        val threadId = 123456L
        val forumId = 999L
        val tbs = "test_tbs"
        val expectedResponse = createMockCommonResponse(errorCode = 0)
        every {
            mockApi.removeStoreFlow(threadId, forumId, tbs)
        } returns flowOf(expectedResponse)

        // When
        val result = repository.removeStore(threadId, forumId, tbs).first()

        // Then
        assertEquals(0, result.errorCode)
        verify { mockApi.removeStoreFlow(threadId, forumId, tbs) }
    }

    @Test
    fun `removeStore should propagate error when API call fails`() = runTest(testDispatcher) {
        // Given
        val threadId = 123456L
        val forumId = 999L
        val tbs = "test_tbs"
        val expectedException = RuntimeException("Network error")
        every {
            mockApi.removeStoreFlow(threadId, forumId, tbs)
        } returns flow { throw expectedException }

        // When/Then
        try {
            repository.removeStore(threadId, forumId, tbs).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Network error", e.message)
        }
    }

    @Test
    fun `removeStore should handle null tbs parameter`() = runTest(testDispatcher) {
        // Given
        val threadId = 123456L
        val forumId = 999L
        val tbs: String? = null
        val expectedResponse = createMockCommonResponse(errorCode = 0)
        every {
            mockApi.removeStoreFlow(threadId, forumId, tbs)
        } returns flowOf(expectedResponse)

        // When
        val result = repository.removeStore(threadId, forumId, tbs).first()

        // Then
        assertEquals(0, result.errorCode)
        verify { mockApi.removeStoreFlow(threadId, forumId, tbs) }
    }

    // ========== removeStore (String overload) tests ==========

    @Test
    fun `removeStore with String threadId should return success flow when API call succeeds`() = runTest(testDispatcher) {
        // Given
        val threadId = "123456"
        val expectedResponse = createMockCommonResponse(errorCode = 0)
        every {
            mockApi.removeStoreFlow(threadId)
        } returns flowOf(expectedResponse)

        // When
        val result = repository.removeStore(threadId).first()

        // Then
        assertEquals(0, result.errorCode)
        verify { mockApi.removeStoreFlow(threadId) }
    }

    @Test
    fun `removeStore with String threadId should propagate error when API call fails`() = runTest(testDispatcher) {
        // Given
        val threadId = "123456"
        val expectedException = RuntimeException("Network error")
        every {
            mockApi.removeStoreFlow(threadId)
        } returns flow { throw expectedException }

        // When/Then
        try {
            repository.removeStore(threadId).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Network error", e.message)
        }
    }

    @Test
    fun `removeStore with String threadId should handle different string formats`() = runTest(testDispatcher) {
        // Given
        val testCases = listOf(
            "123456" to "numeric string",
            "999999999999" to "large number",
            "0" to "zero",
            "1" to "single digit"
        )

        testCases.forEach { (threadId, description) ->
            val expectedResponse = createMockCommonResponse(errorCode = 0)
            every {
                mockApi.removeStoreFlow(threadId)
            } returns flowOf(expectedResponse)

            // When
            val result = repository.removeStore(threadId).first()

            // Then
            assertEquals("Failed for $description", 0, result.errorCode)
            verify { mockApi.removeStoreFlow(threadId) }
        }
    }

    // ========== delPost tests ==========

    @Test
    fun `delPost should return success flow when API call succeeds`() = runTest(testDispatcher) {
        // Given
        val forumId = 111L
        val forumName = "test_forum"
        val threadId = 123456L
        val postId = 789L
        val tbs = "test_tbs"
        val isFloor = false
        val delMyPost = true
        val expectedResponse = createMockCommonResponse(errorCode = 0)
        every {
            mockApi.delPostFlow(forumId, forumName, threadId, postId, tbs, isFloor, delMyPost)
        } returns flowOf(expectedResponse)

        // When
        val result = repository.delPost(forumId, forumName, threadId, postId, tbs, isFloor, delMyPost).first()

        // Then
        assertEquals(0, result.errorCode)
        verify { mockApi.delPostFlow(forumId, forumName, threadId, postId, tbs, isFloor, delMyPost) }
    }

    @Test
    fun `delPost should propagate error when API call fails`() = runTest(testDispatcher) {
        // Given
        val forumId = 111L
        val forumName = "test_forum"
        val threadId = 123456L
        val postId = 789L
        val tbs = "test_tbs"
        val expectedException = RuntimeException("Delete failed")
        every {
            mockApi.delPostFlow(forumId, forumName, threadId, postId, tbs, false, true)
        } returns flow { throw expectedException }

        // When/Then
        try {
            repository.delPost(forumId, forumName, threadId, postId, tbs).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Delete failed", e.message)
        }
    }

    @Test
    fun `delPost should handle different parameter combinations`() = runTest(testDispatcher) {
        // Given
        val forumId = 111L
        val forumName = "test_forum"
        val threadId = 123456L
        val postId = 789L
        val tbs = "test_tbs"

        val testCases = listOf(
            Triple(true, true, "delMyPost=true, isFloor=true"),
            Triple(true, false, "delMyPost=true, isFloor=false"),
            Triple(false, true, "delMyPost=false, isFloor=true"),
            Triple(false, false, "delMyPost=false, isFloor=false")
        )

        testCases.forEach { (isFloor, delMyPost, _) ->
            val expectedResponse = createMockCommonResponse(errorCode = 0)
            every {
                mockApi.delPostFlow(forumId, forumName, threadId, postId, tbs, isFloor, delMyPost)
            } returns flowOf(expectedResponse)

            // When
            val result = repository.delPost(forumId, forumName, threadId, postId, tbs, isFloor, delMyPost).first()

            // Then
            assertEquals(0, result.errorCode)
        }
    }

    @Test
    fun `delPost should handle null tbs parameter`() = runTest(testDispatcher) {
        // Given
        val forumId = 111L
        val forumName = "test_forum"
        val threadId = 123456L
        val postId = 789L
        val tbs: String? = null
        val expectedResponse = createMockCommonResponse(errorCode = 0)
        every {
            mockApi.delPostFlow(forumId, forumName, threadId, postId, tbs, false, true)
        } returns flowOf(expectedResponse)

        // When
        val result = repository.delPost(forumId, forumName, threadId, postId, tbs).first()

        // Then
        assertEquals(0, result.errorCode)
        verify { mockApi.delPostFlow(forumId, forumName, threadId, postId, tbs, false, true) }
    }

    // ========== delThread tests ==========

    @Test
    fun `delThread should return success flow when API call succeeds`() = runTest(testDispatcher) {
        // Given
        val forumId = 111L
        val forumName = "test_forum"
        val threadId = 123456L
        val tbs = "test_tbs"
        val delMyThread = true
        val isHide = false
        val expectedResponse = createMockCommonResponse(errorCode = 0)
        every {
            mockApi.delThreadFlow(forumId, forumName, threadId, tbs, delMyThread, isHide)
        } returns flowOf(expectedResponse)

        // When
        val result = repository.delThread(forumId, forumName, threadId, tbs, delMyThread, isHide).first()

        // Then
        assertEquals(0, result.errorCode)
        verify { mockApi.delThreadFlow(forumId, forumName, threadId, tbs, delMyThread, isHide) }
    }

    @Test
    fun `delThread should propagate error when API call fails`() = runTest(testDispatcher) {
        // Given
        val forumId = 111L
        val forumName = "test_forum"
        val threadId = 123456L
        val tbs = "test_tbs"
        val delMyThread = true
        val isHide = false
        val expectedException = RuntimeException("Delete thread failed")
        every {
            mockApi.delThreadFlow(forumId, forumName, threadId, tbs, delMyThread, isHide)
        } returns flow { throw expectedException }

        // When/Then
        try {
            repository.delThread(forumId, forumName, threadId, tbs, delMyThread, isHide).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Delete thread failed", e.message)
        }
    }

    @Test
    fun `delThread should handle different parameter combinations`() = runTest(testDispatcher) {
        // Given
        val forumId = 111L
        val forumName = "test_forum"
        val threadId = 123456L
        val tbs = "test_tbs"

        val testCases = listOf(
            Pair(true, true),
            Pair(true, false),
            Pair(false, true),
            Pair(false, false)
        )

        testCases.forEach { (delMyThread, isHide) ->
            val expectedResponse = createMockCommonResponse(errorCode = 0)
            every {
                mockApi.delThreadFlow(forumId, forumName, threadId, tbs, delMyThread, isHide)
            } returns flowOf(expectedResponse)

            // When
            val result = repository.delThread(forumId, forumName, threadId, tbs, delMyThread, isHide).first()

            // Then
            assertEquals(0, result.errorCode)
        }
    }

    @Test
    fun `delThread should handle null tbs parameter`() = runTest(testDispatcher) {
        // Given
        val forumId = 111L
        val forumName = "test_forum"
        val threadId = 123456L
        val tbs: String? = null
        val delMyThread = true
        val isHide = false
        val expectedResponse = createMockCommonResponse(errorCode = 0)
        every {
            mockApi.delThreadFlow(forumId, forumName, threadId, tbs, delMyThread, isHide)
        } returns flowOf(expectedResponse)

        // When
        val result = repository.delThread(forumId, forumName, threadId, tbs, delMyThread, isHide).first()

        // Then
        assertEquals(0, result.errorCode)
        verify { mockApi.delThreadFlow(forumId, forumName, threadId, tbs, delMyThread, isHide) }
    }

    @Test
    fun `all methods should work independently`() = runTest(testDispatcher) {
        // Given
        val threadId = 123456L
        val postId = 789L
        val forumId = 111L
        val forumName = "test_forum"
        val tbs = "test_tbs"

        every { mockApi.addStoreFlow(any(), any()) } returns flowOf(createMockCommonResponse(0))
        every { mockApi.removeStoreFlow(any(), any(), any()) } returns flowOf(createMockCommonResponse(0))
        every { mockApi.delPostFlow(any(), any(), any(), any(), any(), any(), any()) } returns flowOf(createMockCommonResponse(0))
        every { mockApi.delThreadFlow(any(), any(), any(), any(), any(), any()) } returns flowOf(createMockCommonResponse(0))

        // When/Then - all should succeed
        assertEquals(0, repository.addStore(threadId, postId).first().errorCode)
        assertEquals(0, repository.removeStore(threadId, forumId, tbs).first().errorCode)
        assertEquals(0, repository.delPost(forumId, forumName, threadId, postId, tbs).first().errorCode)
        assertEquals(0, repository.delThread(forumId, forumName, threadId, tbs, true, false).first().errorCode)
    }

    // Helper function
    private fun createMockCommonResponse(errorCode: Int): CommonResponse {
        return mockk<CommonResponse> {
            every { this@mockk.errorCode } returns errorCode
            every { this@mockk.errorMsg } returns if (errorCode == 0) "success" else "error"
        }
    }
}
