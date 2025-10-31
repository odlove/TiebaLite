package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.core.network.model.CommonResponse
import com.huanchengfly.tieba.post.api.models.LikeForumResultBean
import com.huanchengfly.tieba.post.api.models.SignResultBean
import com.huanchengfly.tieba.post.api.models.protos.userLike.UserLikeResponse
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
 * Unit tests for ForumOperationRepositoryImpl
 *
 * Tests verify that the repository correctly delegates to ITiebaApi and propagates
 * Flow emissions for forum-related operations (sign, like, unlike, userLike).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ForumOperationRepositoryImplTest {

    private lateinit var repository: ForumOperationRepositoryImpl
    private lateinit var mockApi: ITiebaApi

    @Before
    fun setup() {
        mockApi = mockk()
        repository = ForumOperationRepositoryImpl(mockApi)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ========== Helper Functions ==========

    private fun createMockSignResultBean(errorCode: String = "0"): SignResultBean {
        return mockk<SignResultBean> {
            every { this@mockk.errorCode } returns errorCode
            every { errorMsg } returns "success"
            every { userInfo } returns mockk {
                every { isSignIn } returns "1"
                every { contSignNum } returns "7"
            }
        }
    }

    private fun createMockLikeForumResultBean(errorCode: String = "0"): LikeForumResultBean {
        return mockk<LikeForumResultBean> {
            every { this@mockk.errorCode } returns errorCode
            every { error } returns mockk {
                every { errno } returns "0"
                every { errmsg } returns "success"
            }
            every { info } returns mockk {
                every { memberSum } returns "12345"
                every { levelName } returns "Level 5"
            }
        }
    }

    private fun createMockCommonResponse(errorCode: Int = 0): CommonResponse {
        return CommonResponse(
            errorCode = errorCode,
            errorMsg = "success"
        )
    }

    private fun createMockUserLikeResponse(): UserLikeResponse {
        return mockk<UserLikeResponse> {
            every { error } returns mockk {
                every { error_code } returns 0
                every { error_msg } returns "success"
            }
        }
    }

    // ========== sign Tests ==========

    @Test
    fun `sign should return success flow when API call succeeds`() = runTest {
        // Given: Mock API returns successful SignResultBean
        val forumId = "12345"
        val forumName = "TestForum"
        val tbs = "test_tbs_token"
        val expectedBean = createMockSignResultBean("0")

        every {
            mockApi.signFlow(forumId, forumName, tbs)
        } returns flowOf(expectedBean)

        // When: Call repository method
        val result = repository.sign(forumId, forumName, tbs).first()

        // Then: Verify the result matches expected data
        assertEquals("0", result.errorCode)
        assertNotNull(result.userInfo)
        verify(exactly = 1) {
            mockApi.signFlow(forumId, forumName, tbs)
        }
    }

    @Test
    fun `sign should propagate error when API call fails`() = runTest {
        // Given: Mock API throws exception
        val forumId = "12345"
        val forumName = "TestForum"
        val tbs = "test_tbs_token"
        val expectedException = RuntimeException("Sign failed")

        every {
            mockApi.signFlow(forumId, forumName, tbs)
        } returns flow { throw expectedException }

        // When & Then: Verify exception is propagated
        try {
            repository.sign(forumId, forumName, tbs).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Sign failed", e.message)
        }
    }

    @Test
    fun `sign should handle empty tbs parameter`() = runTest {
        // Given: Empty tbs token
        val forumId = "12345"
        val forumName = "TestForum"
        val tbs = ""
        val expectedBean = createMockSignResultBean()

        every {
            mockApi.signFlow(forumId, forumName, tbs)
        } returns flowOf(expectedBean)

        // When: Call repository with empty tbs
        val result = repository.sign(forumId, forumName, tbs).first()

        // Then: Verify API is called (parameter validation is API's responsibility)
        verify(exactly = 1) {
            mockApi.signFlow(forumId, forumName, "")
        }
    }

    // ========== likeForum Tests ==========

    @Test
    fun `likeForum should return success flow when API call succeeds`() = runTest {
        // Given: Mock API returns successful LikeForumResultBean
        val forumId = "12345"
        val forumName = "TestForum"
        val tbs = "test_tbs_token"
        val expectedBean = createMockLikeForumResultBean("0")

        every {
            mockApi.likeForumFlow(forumId, forumName, tbs)
        } returns flowOf(expectedBean)

        // When: Call repository method
        val result = repository.likeForum(forumId, forumName, tbs).first()

        // Then: Verify the result matches expected data
        assertEquals("0", result.errorCode)
        assertNotNull(result.info)
        verify(exactly = 1) {
            mockApi.likeForumFlow(forumId, forumName, tbs)
        }
    }

    @Test
    fun `likeForum should propagate error when API call fails`() = runTest {
        // Given: Mock API throws exception
        val forumId = "12345"
        val forumName = "TestForum"
        val tbs = "test_tbs_token"
        val expectedException = RuntimeException("Like failed")

        every {
            mockApi.likeForumFlow(forumId, forumName, tbs)
        } returns flow { throw expectedException }

        // When & Then: Verify exception is propagated
        try {
            repository.likeForum(forumId, forumName, tbs).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Like failed", e.message)
        }
    }

    // ========== unlikeForum Tests ==========

    @Test
    fun `unlikeForum should return success flow when API call succeeds`() = runTest {
        // Given: Mock API returns successful CommonResponse
        val forumId = "12345"
        val forumName = "TestForum"
        val tbs = "test_tbs_token"
        val expectedResponse = createMockCommonResponse(0)

        every {
            mockApi.unlikeForumFlow(forumId, forumName, tbs)
        } returns flowOf(expectedResponse)

        // When: Call repository method
        val result = repository.unlikeForum(forumId, forumName, tbs).first()

        // Then: Verify the result matches expected data
        assertEquals(0, result.errorCode)
        verify(exactly = 1) {
            mockApi.unlikeForumFlow(forumId, forumName, tbs)
        }
    }

    @Test
    fun `unlikeForum should propagate error when API call fails`() = runTest {
        // Given: Mock API throws exception
        val forumId = "12345"
        val forumName = "TestForum"
        val tbs = "test_tbs_token"
        val expectedException = RuntimeException("Unlike failed")

        every {
            mockApi.unlikeForumFlow(forumId, forumName, tbs)
        } returns flow { throw expectedException }

        // When & Then: Verify exception is propagated
        try {
            repository.unlikeForum(forumId, forumName, tbs).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Unlike failed", e.message)
        }
    }

    // ========== userLike Tests ==========

    @Test
    fun `userLike should return success flow when API call succeeds`() = runTest {
        // Given: Mock API returns successful UserLikeResponse
        val pageTag = "tag_123"
        val lastRequestUnix = 1234567890L
        val loadType = 1
        val expectedResponse = createMockUserLikeResponse()

        every {
            mockApi.userLikeFlow(pageTag, lastRequestUnix, loadType)
        } returns flowOf(expectedResponse)

        // When: Call repository method
        val result = repository.userLike(pageTag, lastRequestUnix, loadType).first()

        // Then: Verify the result matches expected data
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.userLikeFlow(pageTag, lastRequestUnix, loadType)
        }
    }

    @Test
    fun `userLike should propagate error when API call fails`() = runTest {
        // Given: Mock API throws exception
        val pageTag = "tag_123"
        val lastRequestUnix = 1234567890L
        val loadType = 1
        val expectedException = RuntimeException("UserLike failed")

        every {
            mockApi.userLikeFlow(pageTag, lastRequestUnix, loadType)
        } returns flow { throw expectedException }

        // When & Then: Verify exception is propagated
        try {
            repository.userLike(pageTag, lastRequestUnix, loadType).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("UserLike failed", e.message)
        }
    }

    @Test
    fun `userLike should handle lastRequestUnix as Long not Int`() = runTest {
        // Given: lastRequestUnix is a Long value (not Int)
        val pageTag = "tag_123"
        val lastRequestUnix = 9999999999L // Large value that exceeds Int range
        val loadType = 1
        val expectedResponse = createMockUserLikeResponse()

        every {
            mockApi.userLikeFlow(pageTag, lastRequestUnix, loadType)
        } returns flowOf(expectedResponse)

        // When: Call repository method
        val result = repository.userLike(pageTag, lastRequestUnix, loadType).first()

        // Then: Verify API receives Long parameter correctly
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.userLikeFlow(pageTag, lastRequestUnix, loadType)
        }
    }

    @Test
    fun `userLike should handle empty pageTag parameter`() = runTest {
        // Given: Empty pageTag
        val pageTag = ""
        val lastRequestUnix = 1234567890L
        val loadType = 1
        val expectedResponse = createMockUserLikeResponse()

        every {
            mockApi.userLikeFlow(pageTag, lastRequestUnix, loadType)
        } returns flowOf(expectedResponse)

        // When: Call repository with empty pageTag
        val result = repository.userLike(pageTag, lastRequestUnix, loadType).first()

        // Then: Verify API is called (parameter validation is API's responsibility)
        verify(exactly = 1) {
            mockApi.userLikeFlow("", lastRequestUnix, loadType)
        }
    }
}
