package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.ThreadCollectBean
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
 * Unit tests for ThreadCollectRepositoryImpl
 *
 * Tests verify that the repository correctly delegates to ITiebaApi and propagates
 * Flow emissions for thread collect operations (threadCollect).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ThreadCollectRepositoryImplTest {

    private lateinit var repository: ThreadCollectRepositoryImpl
    private lateinit var mockApi: ITiebaApi

    @Before
    fun setup() {
        mockApi = mockk()
        repository = ThreadCollectRepositoryImpl(mockApi)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ========== Helper Functions ==========

    private fun createMockThreadCollectBean(): ThreadCollectBean {
        return ThreadCollectBean(
            errorCode = "0",
            error = null,
            collectThread =
                listOf(
                    ThreadCollectBean.ThreadCollectInfo(
                        threadId = "123",
                        title = "Test Title",
                        forumName = "Test Forum",
                        author =
                            ThreadCollectBean.AuthorInfo(
                                lzUid = "42",
                                name = "Test User",
                                nameShow = "Test User Show",
                                userPortrait = "portrait"
                            ),
                        media = emptyList(),
                        isDeleted = "0",
                        lastTime = "0",
                        type = "0",
                        status = "0",
                        maxPid = "1",
                        minPid = "1",
                        markPid = "1",
                        markStatus = "0",
                        postNo = "1",
                        postNoMsg = "",
                        count = "0"
                    )
                )
        )
    }

    // ========== threadCollect Tests ==========

    @Test
    fun `threadCollect should return success flow when API call succeeds`() = runTest {
        // Given: Mock API returns successful ThreadCollectBean
        val page = 1
        val expectedBean = createMockThreadCollectBean()

        every {
            mockApi.threadCollectFlow(page)
        } returns flowOf(expectedBean)

        // When: Call repository method
        val result = repository.threadCollect(page).first()

        // Then: Verify the result matches expected data
        assertNotNull(result)
        assertEquals(1, result.items?.size)
        verify(exactly = 1) {
            mockApi.threadCollectFlow(page)
        }
    }

    @Test
    fun `threadCollect should propagate error when API call fails`() = runTest {
        // Given: Mock API throws exception
        val page = 1
        val expectedException = RuntimeException("Network error")

        every {
            mockApi.threadCollectFlow(page)
        } returns flow { throw expectedException }

        // When & Then: Verify exception is propagated
        try {
            repository.threadCollect(page).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Network error", e.message)
        }
    }

    @Test
    fun `threadCollect should handle default parameters correctly`() = runTest {
        // Given: Use default parameters
        val expectedBean = createMockThreadCollectBean()

        every {
            mockApi.threadCollectFlow(1)
        } returns flowOf(expectedBean)

        // When: Call repository with default parameters
        val result = repository.threadCollect().first()

        // Then: Verify API is called with default values
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.threadCollectFlow(1)
        }
    }

    @Test
    fun `threadCollect should handle page pagination correctly`() = runTest {
        // Given: Different page numbers
        // Test page 1
        val page1Bean = createMockThreadCollectBean()
        every {
            mockApi.threadCollectFlow(1)
        } returns flowOf(page1Bean)

        val result1 = repository.threadCollect(1).first()
        assertEquals(1, result1.items?.size)

        // Test page 2
        val page2Bean = createMockThreadCollectBean()
        every {
            mockApi.threadCollectFlow(2)
        } returns flowOf(page2Bean)

        val result2 = repository.threadCollect(2).first()
        assertEquals(1, result2.items?.size)

        // Verify both pages were called
        verify(exactly = 1) { mockApi.threadCollectFlow(1) }
        verify(exactly = 1) { mockApi.threadCollectFlow(2) }
    }
}
