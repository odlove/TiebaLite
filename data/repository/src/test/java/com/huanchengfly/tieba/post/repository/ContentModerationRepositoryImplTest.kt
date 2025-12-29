package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.moderation.ReportCheckResult
import com.huanchengfly.tieba.core.network.retrofit.ApiResult
import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.CheckReportBean
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
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ContentModerationRepositoryImpl
 *
 * Tests verify that the repository maps CheckReportBean to common
 * ReportCheckResult and propagates failures.
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

    private fun createMockCheckReportBean(
        errorCode: Int = 0,
        errorMsg: String = "success",
        url: String = "https://example.com/report/12345"
    ): CheckReportBean {
        return mockk<CheckReportBean> {
            every { this@mockk.errorCode } returns errorCode
            every { this@mockk.errorMsg } returns errorMsg
            every { data } returns mockk {
                every { this@mockk.url } returns url
            }
        }
    }

    private fun createSuccessApiResult(bean: CheckReportBean): ApiResult<CheckReportBean> {
        return ApiResult.Success(bean)
    }

    private fun createErrorApiResult(error: Throwable = RuntimeException("API call failed")): ApiResult<CheckReportBean> {
        return ApiResult.Failure(error)
    }

    // ========== checkReportPost Tests ==========

    @Test
    fun `checkReportPost should map success result to common model`() = runTest {
        // Given: Mock API returns successful ApiResult
        val postId = "12345"
        val bean = createMockCheckReportBean(errorCode = 2, errorMsg = "ok", url = "https://example.com/report/12345")
        val expectedResult = createSuccessApiResult(bean)
        val deferred = CompletableDeferred(expectedResult)

        every {
            mockApi.checkReportPostAsync(postId)
        } returns deferred

        // When: Call repository method
        val result = repository.checkReportPost(postId)

        // Then: Verify the result matches expected data
        assertNotNull(result)
        assertEquals(
            ApiResult.Success(
                ReportCheckResult(
                    url = "https://example.com/report/12345",
                    errorCode = 2,
                    errorMsg = "ok",
                )
            ),
            result
        )
        verify(exactly = 1) {
            mockApi.checkReportPostAsync(postId)
        }
    }

    @Test
    fun `checkReportPost should propagate error when API call fails`() = runTest {
        // Given: Mock API returns error ApiResult
        val postId = "12345"
        val error = RuntimeException("API call failed")
        val expectedResult = createErrorApiResult(error)
        val deferred = CompletableDeferred(expectedResult)

        every {
            mockApi.checkReportPostAsync(postId)
        } returns deferred

        // When: Call repository method
        val result = repository.checkReportPost(postId)

        // Then: Verify error is propagated
        assertNotNull(result)
        assertTrue(result is ApiResult.Failure)
        assertSame(error, (result as ApiResult.Failure).error)
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

        val result1 = createSuccessApiResult(createMockCheckReportBean(url = "https://example.com/report/111"))
        val result2 = createSuccessApiResult(createMockCheckReportBean(url = "https://example.com/report/222"))
        val result3 = createSuccessApiResult(createMockCheckReportBean(url = "https://example.com/report/999999"))

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
        val resultDeferred1 = repository.checkReportPost(postId1)
        val resultDeferred2 = repository.checkReportPost(postId2)
        val resultDeferred3 = repository.checkReportPost(postId3)

        // Then: Verify all postIds were passed correctly
        assertNotNull(resultDeferred1)
        assertNotNull(resultDeferred2)
        assertNotNull(resultDeferred3)

        verify(exactly = 1) { mockApi.checkReportPostAsync("111") }
        verify(exactly = 1) { mockApi.checkReportPostAsync("222") }
        verify(exactly = 1) { mockApi.checkReportPostAsync("999999") }
    }
}
