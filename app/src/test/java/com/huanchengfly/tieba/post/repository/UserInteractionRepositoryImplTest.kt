package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.AgreeBean
import com.huanchengfly.tieba.core.network.model.CommonResponse
import com.huanchengfly.tieba.post.models.DislikeBean
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
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for UserInteractionRepositoryImpl
 *
 * Tests verify that the repository correctly delegates to ITiebaApi and propagates
 * Flow emissions for both success and error scenarios.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserInteractionRepositoryImplTest {

    private lateinit var repository: UserInteractionRepositoryImpl
    private lateinit var mockApi: ITiebaApi

    @Before
    fun setup() {
        mockApi = mockk()
        repository = UserInteractionRepositoryImpl(mockApi)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ========== Helper Functions ==========

    private fun createMockAgreeBean(score: String = "1"): AgreeBean {
        return mockk<AgreeBean> {
            every { errorCode } returns "0"
            every { errorMsg } returns "success"
            every { data } returns mockk {
                every { agree } returns mockk {
                    every { this@mockk.score } returns score
                }
            }
        }
    }

    private fun createMockCommonResponse(errorCode: Int = 0, errorMsg: String = "success"): CommonResponse {
        return CommonResponse(
            errorCode = errorCode,
            errorMsg = errorMsg
        )
    }

    private fun createMockDislikeBean(): DislikeBean {
        return DislikeBean(
            threadId = "12345",
            dislikeIds = "1,2,3",
            forumId = "100",
            clickTime = System.currentTimeMillis(),
            extra = ""
        )
    }

    // ========== opAgree Tests ==========

    @Test
    fun `opAgree should return success flow when API call succeeds`() = runTest {
        // Given: Mock API returns successful AgreeBean
        val threadId = "123456"
        val postId = "789012"
        val hasAgree = 0
        val objType = 1
        val expectedBean = createMockAgreeBean(score = "1")

        every {
            mockApi.opAgreeFlow(threadId, postId, hasAgree, objType)
        } returns flowOf(expectedBean)

        // When: Call repository method
        val result = repository.opAgree(threadId, postId, hasAgree, objType).first()

        // Then: Verify the result matches expected data
        assertEquals("0", result.errorCode)
        assertNotNull(result.data)
        verify(exactly = 1) {
            mockApi.opAgreeFlow(threadId, postId, hasAgree, objType)
        }
    }

    @Test
    fun `opAgree should propagate error when API call fails`() = runTest {
        // Given: Mock API throws exception
        val threadId = "123456"
        val postId = "789012"
        val hasAgree = 0
        val objType = 1
        val expectedException = RuntimeException("Network error")

        every {
            mockApi.opAgreeFlow(threadId, postId, hasAgree, objType)
        } returns flow { throw expectedException }

        // When & Then: Verify exception is propagated
        try {
            repository.opAgree(threadId, postId, hasAgree, objType).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Network error", e.message)
        }
    }

    @Test
    fun `opAgree should handle empty threadId parameter`() = runTest {
        // Given: Empty threadId
        val threadId = ""
        val postId = "789012"
        val hasAgree = 0
        val objType = 1
        val expectedBean = createMockAgreeBean()

        every {
            mockApi.opAgreeFlow(threadId, postId, hasAgree, objType)
        } returns flowOf(expectedBean)

        // When: Call repository with empty threadId
        val result = repository.opAgree(threadId, postId, hasAgree, objType).first()

        // Then: Verify API is called with empty string (parameter validation is API's responsibility)
        verify(exactly = 1) {
            mockApi.opAgreeFlow("", postId, hasAgree, objType)
        }
    }

    @Test
    fun `opAgree should handle toggle between agree and disagree states`() = runTest {
        // Given: Mock API for agree (hasAgree=0) and disagree (hasAgree=1) scenarios
        val threadId = "123456"
        val postId = "789012"
        val objType = 1

        // Test agree action
        val agreeBean = createMockAgreeBean(score = "1")
        every {
            mockApi.opAgreeFlow(threadId, postId, 0, objType)
        } returns flowOf(agreeBean)

        val agreeResult = repository.opAgree(threadId, postId, 0, objType).first()
        assertEquals("0", agreeResult.errorCode)

        // Test disagree action
        val disagreeBean = createMockAgreeBean(score = "0")
        every {
            mockApi.opAgreeFlow(threadId, postId, 1, objType)
        } returns flowOf(disagreeBean)

        val disagreeResult = repository.opAgree(threadId, postId, 1, objType).first()
        assertEquals("0", disagreeResult.errorCode)
    }

    // ========== submitDislike Tests ==========

    @Test
    fun `submitDislike should return success flow when API call succeeds`() = runTest {
        // Given: Mock API returns successful CommonResponse
        val dislikeBean = createMockDislikeBean()
        val expectedResponse = createMockCommonResponse(errorCode = 0, errorMsg = "success")

        every {
            mockApi.submitDislikeFlow(dislikeBean)
        } returns flowOf(expectedResponse)

        // When: Call repository method
        val result = repository.submitDislike(dislikeBean).first()

        // Then: Verify the result matches expected response
        assertEquals(0, result.errorCode)
        assertEquals("success", result.errorMsg)
        verify(exactly = 1) {
            mockApi.submitDislikeFlow(dislikeBean)
        }
    }

    @Test
    fun `submitDislike should propagate error when API call fails`() = runTest {
        // Given: Mock API throws exception
        val dislikeBean = createMockDislikeBean()
        val expectedException = RuntimeException("Submit failed")

        every {
            mockApi.submitDislikeFlow(dislikeBean)
        } returns flow { throw expectedException }

        // When & Then: Verify exception is propagated
        try {
            repository.submitDislike(dislikeBean).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Submit failed", e.message)
        }
    }

    @Test
    fun `submitDislike should handle DislikeBean with multiple dislike IDs`() = runTest {
        // Given: DislikeBean with multiple dislike reasons
        val dislikeBean = DislikeBean(
            threadId = "12345",
            dislikeIds = "1,2,3,4,5", // Multiple dislike reasons
            forumId = "100",
            clickTime = System.currentTimeMillis(),
            extra = ""
        )
        val expectedResponse = createMockCommonResponse()

        every {
            mockApi.submitDislikeFlow(dislikeBean)
        } returns flowOf(expectedResponse)

        // When: Call repository method
        val result = repository.submitDislike(dislikeBean).first()

        // Then: Verify API receives the full dislike bean
        assertEquals(0, result.errorCode)
        verify(exactly = 1) {
            mockApi.submitDislikeFlow(match {
                it.dislikeIds == "1,2,3,4,5"
            })
        }
    }
}
