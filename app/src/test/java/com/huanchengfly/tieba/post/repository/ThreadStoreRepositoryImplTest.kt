package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.ThreadStoreBean
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
 * Unit tests for ThreadStoreRepositoryImpl
 *
 * Tests verify that the repository correctly delegates to ITiebaApi and propagates
 * Flow emissions for thread store operations (threadStore).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ThreadStoreRepositoryImplTest {

    private lateinit var repository: ThreadStoreRepositoryImpl
    private lateinit var mockApi: ITiebaApi

    @Before
    fun setup() {
        mockApi = mockk()
        repository = ThreadStoreRepositoryImpl(mockApi)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ========== Helper Functions ==========

    private fun createMockThreadStoreBean(): ThreadStoreBean {
        return mockk<ThreadStoreBean> {
            every { errorCode } returns "0"
            every { error } returns null
            every { storeThread } returns emptyList()
        }
    }

    // ========== threadStore Tests ==========

    @Test
    fun `threadStore should return success flow when API call succeeds`() = runTest {
        // Given: Mock API returns successful ThreadStoreBean
        val page = 1
        val expectedBean = createMockThreadStoreBean()

        every {
            mockApi.threadStoreFlow(page)
        } returns flowOf(expectedBean)

        // When: Call repository method
        val result = repository.threadStore(page).first()

        // Then: Verify the result matches expected data
        assertNotNull(result)
        assertEquals("0", result.errorCode)
        verify(exactly = 1) {
            mockApi.threadStoreFlow(page)
        }
    }

    @Test
    fun `threadStore should propagate error when API call fails`() = runTest {
        // Given: Mock API throws exception
        val page = 1
        val expectedException = RuntimeException("Network error")

        every {
            mockApi.threadStoreFlow(page)
        } returns flow { throw expectedException }

        // When & Then: Verify exception is propagated
        try {
            repository.threadStore(page).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Network error", e.message)
        }
    }

    @Test
    fun `threadStore should handle default parameters correctly`() = runTest {
        // Given: Use default parameters
        val expectedBean = createMockThreadStoreBean()

        every {
            mockApi.threadStoreFlow(1)
        } returns flowOf(expectedBean)

        // When: Call repository with default parameters
        val result = repository.threadStore().first()

        // Then: Verify API is called with default values
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.threadStoreFlow(1)
        }
    }

    @Test
    fun `threadStore should handle page pagination correctly`() = runTest {
        // Given: Different page numbers
        // Test page 1
        val page1Bean = createMockThreadStoreBean()
        every {
            mockApi.threadStoreFlow(1)
        } returns flowOf(page1Bean)

        val result1 = repository.threadStore(1).first()
        assertEquals("0", result1.errorCode)

        // Test page 2
        val page2Bean = createMockThreadStoreBean()
        every {
            mockApi.threadStoreFlow(2)
        } returns flowOf(page2Bean)

        val result2 = repository.threadStore(2).first()
        assertEquals("0", result2.errorCode)

        // Verify both pages were called
        verify(exactly = 1) { mockApi.threadStoreFlow(1) }
        verify(exactly = 1) { mockApi.threadStoreFlow(2) }
    }
}
