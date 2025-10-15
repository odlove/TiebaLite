package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.CheckReportBean
import com.huanchengfly.tieba.post.api.retrofit.ApiResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ContentModerationRepositoryImpl
 *
 * Tests verify that the repository correctly delegates to ITiebaApi
 * for content moderation operations (checkReportPost).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ContentModerationRepositoryImplTest {

    private lateinit var repository: ContentModerationRepositoryImpl
    private lateinit var mockApi: ITiebaApi

    @Before
    fun setup() {
        mockApi = mockk()
        repository = ContentModerationRepositoryImpl(mockApi)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ========== Helper Functions ==========

    private fun createMockCheckReportBean(errorCode: Int = 0, errorMsg: String = "success"): CheckReportBean {
        return mockk<CheckReportBean> {
            every { this@mockk.errorCode } returns errorCode
            every { this@mockk.errorMsg } returns errorMsg
            every { data } returns mockk {
                every { url } returns "https://example.com/report/12345"
            }
        }
    }

    private fun createSuccessApiResult(): ApiResult<CheckReportBean> {
        val bean = createMockCheckReportBean()
        return ApiResult.Success(bean)
    }

    private fun createErrorApiResult(): ApiResult<CheckReportBean> {
        return ApiResult.Failure(RuntimeException("API call failed"))
    }

    // ========== checkReportPost Tests ==========

    @Test
    fun `checkReportPost should return success when API call succeeds`() = runTest {
        // Given: Mock API returns successful ApiResult
        val postId = "12345"
        val expectedResult = createSuccessApiResult()
        val deferred = CompletableDeferred(expectedResult)

        every {
            mockApi.checkReportPostAsync(postId)
        } returns deferred

        // When: Call repository method
        val result = repository.checkReportPost(postId)

        // Then: Verify the result matches expected data
        assertNotNull(result)
        assertEquals(expectedResult, result.await())
        verify(exactly = 1) {
            mockApi.checkReportPostAsync(postId)
        }
    }

    @Test
    fun `checkReportPost should propagate error when API call fails`() = runTest {
        // Given: Mock API returns error ApiResult
        val postId = "12345"
        val expectedResult = createErrorApiResult()
        val deferred = CompletableDeferred(expectedResult)

        every {
            mockApi.checkReportPostAsync(postId)
        } returns deferred

        // When: Call repository method
        val result = repository.checkReportPost(postId)

        // Then: Verify error is propagated
        assertNotNull(result)
        assertEquals(expectedResult, result.await())
        verify(exactly = 1) {
            mockApi.checkReportPostAsync(postId)
        }
    }

    @Test
    fun `checkReportPost should handle different postIds`() = runTest {
        // Given: Different post IDs
        val postId1 = "111"
        val postId2 = "222"
        val postId3 = "999999"

        val result1 = createSuccessApiResult()
        val result2 = createSuccessApiResult()
        val result3 = createSuccessApiResult()

        every {
            mockApi.checkReportPostAsync("111")
        } returns CompletableDeferred(result1)

        every {
            mockApi.checkReportPostAsync("222")
        } returns CompletableDeferred(result2)

        every {
            mockApi.checkReportPostAsync("999999")
        } returns CompletableDeferred(result3)

        // When: Call repository with different postIds
        val deferred1 = repository.checkReportPost(postId1)
        val deferred2 = repository.checkReportPost(postId2)
        val deferred3 = repository.checkReportPost(postId3)

        // Then: Verify all postIds were passed correctly
        assertNotNull(deferred1.await())
        assertNotNull(deferred2.await())
        assertNotNull(deferred3.await())

        verify(exactly = 1) { mockApi.checkReportPostAsync("111") }
        verify(exactly = 1) { mockApi.checkReportPostAsync("222") }
        verify(exactly = 1) { mockApi.checkReportPostAsync("999999") }
    }

    @Test
    fun `checkReportPost should handle empty postId parameter`() = runTest {
        // Given: Empty postId
        val postId = ""
        val expectedResult = createSuccessApiResult()
        val deferred = CompletableDeferred(expectedResult)

        every {
            mockApi.checkReportPostAsync("")
        } returns deferred

        // When: Call repository with empty postId
        val result = repository.checkReportPost(postId)

        // Then: Verify API is called (parameter validation is API's responsibility)
        assertNotNull(result.await())
        verify(exactly = 1) {
            mockApi.checkReportPostAsync("")
        }
    }

    @Test
    fun `checkReportPost should return Deferred that can be awaited multiple times`() = runTest {
        // Given: Mock API returns successful ApiResult
        val postId = "12345"
        val expectedResult = createSuccessApiResult()
        val deferred = CompletableDeferred(expectedResult)

        every {
            mockApi.checkReportPostAsync(postId)
        } returns deferred

        // When: Call repository and await result multiple times
        val result = repository.checkReportPost(postId)
        val firstAwait = result.await()
        val secondAwait = result.await()

        // Then: Both awaits should return the same result
        assertEquals(expectedResult, firstAwait)
        assertEquals(expectedResult, secondAwait)
        assertEquals(firstAwait, secondAwait)
        verify(exactly = 1) {
            mockApi.checkReportPostAsync(postId)
        }
    }

    @Test
    fun `checkReportPost should delegate directly to API without transformation`() = runTest {
        // Given: Mock API with specific response
        val postId = "789"
        val expectedBean = createMockCheckReportBean(errorCode = 0, errorMsg = "success")
        val expectedResult = ApiResult.Success(expectedBean)
        val deferred = CompletableDeferred(expectedResult)

        every {
            mockApi.checkReportPostAsync(postId)
        } returns deferred

        // When: Call repository
        val actualResult = repository.checkReportPost(postId).await()

        // Then: Verify result is not transformed (direct delegation)
        assertEquals(expectedResult, actualResult)
        when (actualResult) {
            is ApiResult.Success -> {
                assertEquals(0, actualResult.data.errorCode)
                assertEquals("success", actualResult.data.errorMsg)
                assertEquals("https://example.com/report/12345", actualResult.data.data.url)
            }
            else -> throw AssertionError("Expected Success result")
        }
    }
}
