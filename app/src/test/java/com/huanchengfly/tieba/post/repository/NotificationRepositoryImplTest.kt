package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.MessageListBean
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
 * Unit tests for NotificationRepositoryImpl
 *
 * Tests verify that the repository correctly delegates to ITiebaApi and propagates
 * Flow emissions for notification operations (replyMe, atMe).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NotificationRepositoryImplTest {

    private lateinit var repository: NotificationRepositoryImpl
    private lateinit var mockApi: ITiebaApi

    @Before
    fun setup() {
        mockApi = mockk()
        repository = NotificationRepositoryImpl(mockApi)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ========== Helper Functions ==========

    private fun createMockMessageListBean(errorCode: String = "0"): MessageListBean {
        val mockPage = mockk<MessageListBean.PageInfoBean> {
            every { currentPage } returns "1"
            every { hasMore } returns "0"
        }

        return mockk<MessageListBean> {
            every { this@mockk.errorCode } returns errorCode
            every { time } returns System.currentTimeMillis()
            every { replyList } returns emptyList()
            every { atList } returns emptyList()
            every { page } returns mockPage
        }
    }

    // ========== replyMe Tests ==========

    @Test
    fun `replyMe should return success flow when API call succeeds`() = runTest {
        // Given: Mock API returns successful MessageListBean
        val page = 1
        val expectedBean = createMockMessageListBean("0")

        every {
            mockApi.replyMeFlow(page)
        } returns flowOf(expectedBean)

        // When: Call repository method
        val result = repository.replyMe(page).first()

        // Then: Verify the result matches expected data
        assertEquals("0", result.errorCode)
        assertNotNull(result.page)
        verify(exactly = 1) {
            mockApi.replyMeFlow(page)
        }
    }

    @Test
    fun `replyMe should propagate error when API call fails`() = runTest {
        // Given: Mock API throws exception
        val page = 1
        val expectedException = RuntimeException("ReplyMe failed")

        every {
            mockApi.replyMeFlow(page)
        } returns flow { throw expectedException }

        // When & Then: Verify exception is propagated
        try {
            repository.replyMe(page).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("ReplyMe failed", e.message)
        }
    }

    @Test
    fun `replyMe should use default page 1 when not specified`() = runTest {
        // Given: Mock API with default page 1
        val expectedBean = createMockMessageListBean()

        every {
            mockApi.replyMeFlow(1)
        } returns flowOf(expectedBean)

        // When: Call repository without specifying page
        val result = repository.replyMe().first()

        // Then: Verify API is called with default page 1
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.replyMeFlow(1)
        }
    }

    @Test
    fun `replyMe should handle pagination correctly`() = runTest {
        // Given: Different pages
        val page1Bean = createMockMessageListBean()
        every {
            mockApi.replyMeFlow(1)
        } returns flowOf(page1Bean)

        val page2Bean = createMockMessageListBean()
        every {
            mockApi.replyMeFlow(2)
        } returns flowOf(page2Bean)

        // When: Call repository with different pages
        val result1 = repository.replyMe(1).first()
        val result2 = repository.replyMe(2).first()

        // Then: Verify both pages were called correctly
        assertEquals("0", result1.errorCode)
        assertEquals("0", result2.errorCode)
        verify(exactly = 1) { mockApi.replyMeFlow(1) }
        verify(exactly = 1) { mockApi.replyMeFlow(2) }
    }

    // ========== atMe Tests ==========

    @Test
    fun `atMe should return success flow when API call succeeds`() = runTest {
        // Given: Mock API returns successful MessageListBean
        val page = 1
        val expectedBean = createMockMessageListBean("0")

        every {
            mockApi.atMeFlow(page)
        } returns flowOf(expectedBean)

        // When: Call repository method
        val result = repository.atMe(page).first()

        // Then: Verify the result matches expected data
        assertEquals("0", result.errorCode)
        assertNotNull(result.page)
        verify(exactly = 1) {
            mockApi.atMeFlow(page)
        }
    }

    @Test
    fun `atMe should propagate error when API call fails`() = runTest {
        // Given: Mock API throws exception
        val page = 1
        val expectedException = RuntimeException("AtMe failed")

        every {
            mockApi.atMeFlow(page)
        } returns flow { throw expectedException }

        // When & Then: Verify exception is propagated
        try {
            repository.atMe(page).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("AtMe failed", e.message)
        }
    }

    @Test
    fun `atMe should use default page 1 when not specified`() = runTest {
        // Given: Mock API with default page 1
        val expectedBean = createMockMessageListBean()

        every {
            mockApi.atMeFlow(1)
        } returns flowOf(expectedBean)

        // When: Call repository without specifying page
        val result = repository.atMe().first()

        // Then: Verify API is called with default page 1
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.atMeFlow(1)
        }
    }

    @Test
    fun `atMe should handle pagination correctly`() = runTest {
        // Given: Different pages
        val page1Bean = createMockMessageListBean()
        every {
            mockApi.atMeFlow(1)
        } returns flowOf(page1Bean)

        val page2Bean = createMockMessageListBean()
        every {
            mockApi.atMeFlow(2)
        } returns flowOf(page2Bean)

        // When: Call repository with different pages
        val result1 = repository.atMe(1).first()
        val result2 = repository.atMe(2).first()

        // Then: Verify both pages were called correctly
        assertEquals("0", result1.errorCode)
        assertEquals("0", result2.errorCode)
        verify(exactly = 1) { mockApi.atMeFlow(1) }
        verify(exactly = 1) { mockApi.atMeFlow(2) }
    }

    @Test
    fun `atMe and replyMe should be independent`() = runTest {
        // Given: Mock both API methods
        val replyMeBean = createMockMessageListBean()
        every {
            mockApi.replyMeFlow(1)
        } returns flowOf(replyMeBean)

        val atMeBean = createMockMessageListBean()
        every {
            mockApi.atMeFlow(1)
        } returns flowOf(atMeBean)

        // When: Call both methods
        val replyMeResult = repository.replyMe(1).first()
        val atMeResult = repository.atMe(1).first()

        // Then: Verify both work independently
        assertNotNull(replyMeResult)
        assertNotNull(atMeResult)
        verify(exactly = 1) { mockApi.replyMeFlow(1) }
        verify(exactly = 1) { mockApi.atMeFlow(1) }
    }
}
