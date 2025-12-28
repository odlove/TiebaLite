package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.core.network.model.CommonResponse
import com.huanchengfly.tieba.post.api.models.protos.User
import com.huanchengfly.tieba.post.api.models.protos.profile.ProfileResponse
import com.huanchengfly.tieba.post.api.models.protos.profile.ProfileResponseData
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
import java.io.File

/**
 * Unit tests for UserProfileRepositoryImpl
 *
 * Tests verify that the repository correctly delegates to ITiebaApi and propagates
 * Flow emissions for user profile operations (userProfile, profileModify, imgPortrait).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserProfileRepositoryImplTest {

    private lateinit var repository: UserProfileRepositoryImpl
    private lateinit var mockApi: ITiebaApi

    @Before
    fun setup() {
        mockApi = mockk()
        repository = UserProfileRepositoryImpl(mockApi)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ========== Helper Functions ==========

    private fun createMockProfileResponse(userId: Long = 123456L): ProfileResponse {
        val mockUser = mockk<User>(relaxed = true) {
            every { id } returns userId
            every { name } returns "TestUser"
            every { nameShow } returns "TestUserShow"
            every { portrait } returns "portrait"
            every { intro } returns "intro"
            every { sex } returns 1
            every { fans_num } returns 10
            every { post_num } returns 20
            every { thread_num } returns 30
            every { concern_num } returns 40
            every { my_like_num } returns 5
            every { total_agree_num } returns 99
            every { has_concerned } returns 1
            every { tb_age } returns "3.5"
            every { tieba_uid } returns "tieba_uid"
            every { ip_address } returns "IP"
        }
        val mockData = mockk<ProfileResponseData>(relaxed = true) {
            every { user } returns mockUser
        }
        return mockk(relaxed = true) {
            every { data_ } returns mockData
        }
    }

    private fun createMockCommonResponse(): CommonResponse {
        return mockk<CommonResponse> {
            every { errorCode } returns 0
            every { errorMsg } returns "success"
        }
    }

    // ========== userProfile Tests ==========

    @Test
    fun `userProfile should return success flow when API call succeeds`() = runTest {
        // Given: Mock API returns successful ProfileResponse
        val uid = 123456L
        val expectedResponse = createMockProfileResponse()

        every {
            mockApi.userProfileFlow(uid)
        } returns flowOf(expectedResponse)

        // When: Call repository method
        val result = repository.userProfile(uid).first()

        // Then: Verify the result matches expected data
        assertNotNull(result)
        assertEquals(uid, result.id)
        verify(exactly = 1) {
            mockApi.userProfileFlow(uid)
        }
    }

    @Test
    fun `userProfile should propagate error when API call fails`() = runTest {
        // Given: Mock API throws exception
        val uid = 123456L
        val expectedException = RuntimeException("Network error")

        every {
            mockApi.userProfileFlow(uid)
        } returns flow { throw expectedException }

        // When & Then: Verify exception is propagated
        try {
            repository.userProfile(uid).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Network error", e.message)
        }
    }

    @Test
    fun `userProfile should handle different uid values`() = runTest {
        // Given: Different user IDs
        // Test uid 1
        val uid1Response = createMockProfileResponse(111L)
        every {
            mockApi.userProfileFlow(111L)
        } returns flowOf(uid1Response)

        val result1 = repository.userProfile(111L).first()
        assertNotNull(result1)
        assertEquals(111L, result1.id)

        // Test uid 2
        val uid2Response = createMockProfileResponse(222L)
        every {
            mockApi.userProfileFlow(222L)
        } returns flowOf(uid2Response)

        val result2 = repository.userProfile(222L).first()
        assertNotNull(result2)
        assertEquals(222L, result2.id)

        // Verify both uids were called
        verify(exactly = 1) { mockApi.userProfileFlow(111L) }
        verify(exactly = 1) { mockApi.userProfileFlow(222L) }
    }

    // ========== profileModify Tests ==========

    @Test
    fun `profileModify should return success flow when API call succeeds`() = runTest {
        // Given: Mock API returns successful CommonResponse
        val birthdayShowStatus = true
        val birthdayTime = "1234567890"
        val intro = "Test intro"
        val sex = "1"
        val nickName = "TestUser"
        val expectedResponse = createMockCommonResponse()

        every {
            mockApi.profileModifyFlow(birthdayShowStatus, birthdayTime, intro, sex, nickName)
        } returns flowOf(expectedResponse)

        // When: Call repository method
        val result = repository.profileModify(
            birthdayShowStatus,
            birthdayTime,
            intro,
            sex,
            nickName
        ).first()

        // Then: Verify the result matches expected data
        assertNotNull(result)
        assertEquals(0, result.errorCode)
        verify(exactly = 1) {
            mockApi.profileModifyFlow(birthdayShowStatus, birthdayTime, intro, sex, nickName)
        }
    }

    @Test
    fun `profileModify should propagate error when API call fails`() = runTest {
        // Given: Mock API throws exception
        val birthdayShowStatus = true
        val birthdayTime = "1234567890"
        val intro = "Test intro"
        val sex = "1"
        val nickName = "TestUser"
        val expectedException = RuntimeException("Network error")

        every {
            mockApi.profileModifyFlow(birthdayShowStatus, birthdayTime, intro, sex, nickName)
        } returns flow { throw expectedException }

        // When & Then: Verify exception is propagated
        try {
            repository.profileModify(
                birthdayShowStatus,
                birthdayTime,
                intro,
                sex,
                nickName
            ).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Network error", e.message)
        }
    }

    @Test
    fun `profileModify should handle different parameter values`() = runTest {
        // Given: Different parameter combinations
        // Test male profile
        val maleResponse = createMockCommonResponse()
        every {
            mockApi.profileModifyFlow(false, "111", "Male intro", "1", "MaleUser")
        } returns flowOf(maleResponse)

        val result1 = repository.profileModify(false, "111", "Male intro", "1", "MaleUser").first()
        assertEquals(0, result1.errorCode)

        // Test female profile
        val femaleResponse = createMockCommonResponse()
        every {
            mockApi.profileModifyFlow(true, "222", "Female intro", "2", "FemaleUser")
        } returns flowOf(femaleResponse)

        val result2 = repository.profileModify(true, "222", "Female intro", "2", "FemaleUser").first()
        assertEquals(0, result2.errorCode)

        // Verify both profiles were modified
        verify(exactly = 1) { mockApi.profileModifyFlow(false, "111", "Male intro", "1", "MaleUser") }
        verify(exactly = 1) { mockApi.profileModifyFlow(true, "222", "Female intro", "2", "FemaleUser") }
    }

    // ========== imgPortrait Tests ==========

    @Test
    fun `imgPortrait should return success flow when API call succeeds`() = runTest {
        // Given: Mock API returns successful CommonResponse
        val mockFile = mockk<File>()
        val expectedResponse = createMockCommonResponse()

        every {
            mockApi.imgPortrait(mockFile)
        } returns flowOf(expectedResponse)

        // When: Call repository method
        val result = repository.imgPortrait(mockFile).first()

        // Then: Verify the result matches expected data
        assertNotNull(result)
        assertEquals(0, result.errorCode)
        verify(exactly = 1) {
            mockApi.imgPortrait(mockFile)
        }
    }

    @Test
    fun `imgPortrait should propagate error when API call fails`() = runTest {
        // Given: Mock API throws exception
        val mockFile = mockk<File>()
        val expectedException = RuntimeException("Network error")

        every {
            mockApi.imgPortrait(mockFile)
        } returns flow { throw expectedException }

        // When & Then: Verify exception is propagated
        try {
            repository.imgPortrait(mockFile).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Network error", e.message)
        }
    }
}
