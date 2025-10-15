package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.CommonResponse
import com.huanchengfly.tieba.post.api.models.FollowBean
import com.huanchengfly.tieba.post.api.models.UserLikeForumBean
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
 * Unit tests for UserSocialRepositoryImpl
 *
 * Tests verify that the repository correctly delegates to ITiebaApi and propagates
 * Flow emissions for user social operations (follow, unfollow, userLikeForum).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserSocialRepositoryImplTest {

    private lateinit var repository: UserSocialRepositoryImpl
    private lateinit var mockApi: ITiebaApi

    @Before
    fun setup() {
        mockApi = mockk()
        repository = UserSocialRepositoryImpl(mockApi)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ========== Helper Functions ==========

    private fun createMockFollowBean(): FollowBean {
        return mockk<FollowBean> {
            every { errorCode } returns 0
            every { errorMsg } returns "success"
        }
    }

    private fun createMockCommonResponse(): CommonResponse {
        return mockk<CommonResponse> {
            every { errorCode } returns 0
            every { errorMsg } returns "success"
        }
    }

    private fun createMockUserLikeForumBean(): UserLikeForumBean {
        return mockk<UserLikeForumBean>(relaxed = true) {
            every { errorCode } returns "0"
            every { errorMsg } returns "success"
        }
    }

    // ========== follow Tests ==========

    @Test
    fun `follow should return success flow when API call succeeds`() = runTest {
        // Given: Mock API returns successful FollowBean
        val portrait = "test_portrait"
        val tbs = "test_tbs"
        val expectedResponse = createMockFollowBean()

        every {
            mockApi.followFlow(portrait, tbs)
        } returns flowOf(expectedResponse)

        // When: Call repository method
        val result = repository.follow(portrait, tbs).first()

        // Then: Verify the result matches expected data
        assertNotNull(result)
        assertEquals(0, result.errorCode)
        verify(exactly = 1) {
            mockApi.followFlow(portrait, tbs)
        }
    }

    @Test
    fun `follow should propagate error when API call fails`() = runTest {
        // Given: Mock API throws exception
        val portrait = "test_portrait"
        val tbs = "test_tbs"
        val expectedException = RuntimeException("Network error")

        every {
            mockApi.followFlow(portrait, tbs)
        } returns flow { throw expectedException }

        // When & Then: Verify exception is propagated
        try {
            repository.follow(portrait, tbs).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Network error", e.message)
        }
    }

    // ========== unfollow Tests ==========

    @Test
    fun `unfollow should return success flow when API call succeeds`() = runTest {
        // Given: Mock API returns successful CommonResponse
        val portrait = "test_portrait"
        val tbs = "test_tbs"
        val expectedResponse = createMockCommonResponse()

        every {
            mockApi.unfollowFlow(portrait, tbs)
        } returns flowOf(expectedResponse)

        // When: Call repository method
        val result = repository.unfollow(portrait, tbs).first()

        // Then: Verify the result matches expected data
        assertNotNull(result)
        assertEquals(0, result.errorCode)
        verify(exactly = 1) {
            mockApi.unfollowFlow(portrait, tbs)
        }
    }

    @Test
    fun `unfollow should propagate error when API call fails`() = runTest {
        // Given: Mock API throws exception
        val portrait = "test_portrait"
        val tbs = "test_tbs"
        val expectedException = RuntimeException("Network error")

        every {
            mockApi.unfollowFlow(portrait, tbs)
        } returns flow { throw expectedException }

        // When & Then: Verify exception is propagated
        try {
            repository.unfollow(portrait, tbs).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Network error", e.message)
        }
    }

    // ========== userLikeForum Tests ==========

    @Test
    fun `userLikeForum should return success flow when API call succeeds`() = runTest {
        // Given: Mock API returns successful UserLikeForumBean
        val uid = "123456"
        val page = 1
        val expectedBean = createMockUserLikeForumBean()

        every {
            mockApi.userLikeForumFlow(uid, page)
        } returns flowOf(expectedBean)

        // When: Call repository method
        val result = repository.userLikeForum(uid, page).first()

        // Then: Verify the result matches expected data
        assertNotNull(result)
        assertEquals("0", result.errorCode)
        verify(exactly = 1) {
            mockApi.userLikeForumFlow(uid, page)
        }
    }

    @Test
    fun `userLikeForum should propagate error when API call fails`() = runTest {
        // Given: Mock API throws exception
        val uid = "123456"
        val page = 1
        val expectedException = RuntimeException("Network error")

        every {
            mockApi.userLikeForumFlow(uid, page)
        } returns flow { throw expectedException }

        // When & Then: Verify exception is propagated
        try {
            repository.userLikeForum(uid, page).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Network error", e.message)
        }
    }

    @Test
    fun `userLikeForum should handle default parameters correctly`() = runTest {
        // Given: Use default parameters
        val uid = "123456"
        val expectedBean = createMockUserLikeForumBean()

        every {
            mockApi.userLikeForumFlow(uid, 1)
        } returns flowOf(expectedBean)

        // When: Call repository with default parameters
        val result = repository.userLikeForum(uid).first()

        // Then: Verify API is called with default values
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.userLikeForumFlow(uid, 1)
        }
    }

    @Test
    fun `userLikeForum should handle pagination correctly`() = runTest {
        // Given: Different page numbers
        val uid = "123456"

        // Test page 1
        val page1Bean = createMockUserLikeForumBean()
        every {
            mockApi.userLikeForumFlow(uid, 1)
        } returns flowOf(page1Bean)

        val result1 = repository.userLikeForum(uid, 1).first()
        assertEquals("0", result1.errorCode)

        // Test page 2
        val page2Bean = createMockUserLikeForumBean()
        every {
            mockApi.userLikeForumFlow(uid, 2)
        } returns flowOf(page2Bean)

        val result2 = repository.userLikeForum(uid, 2).first()
        assertEquals("0", result2.errorCode)

        // Verify both pages were called
        verify(exactly = 1) { mockApi.userLikeForumFlow(uid, 1) }
        verify(exactly = 1) { mockApi.userLikeForumFlow(uid, 2) }
    }
}
