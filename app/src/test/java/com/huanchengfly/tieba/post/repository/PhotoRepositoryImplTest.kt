package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.PicPageBean
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
 * Unit tests for PhotoRepositoryImpl
 *
 * Tests verify that the repository correctly delegates to ITiebaApi.picPageFlow
 * and propagates Flow emissions for picture browsing operations.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PhotoRepositoryImplTest {

    private lateinit var repository: PhotoRepositoryImpl
    private lateinit var mockApi: ITiebaApi

    @Before
    fun setup() {
        mockApi = mockk()
        repository = PhotoRepositoryImpl(mockApi)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ========== Helper Functions ==========

    private fun createMockPicPageBean(
        picAmount: String = "10",
        picList: List<PicPageBean.PicBean> = emptyList()
    ): PicPageBean {
        return mockk<PicPageBean>(relaxed = true) {
            every { this@mockk.picAmount } returns picAmount
            every { this@mockk.picList } returns picList
        }
    }

    // ========== picPage Tests ==========

    @Test
    fun `picPage should call api picPageFlow with correct parameters`() = runTest {
        // Given
        val forumId = "123"
        val forumName = "testForum"
        val threadId = "456"
        val seeLz = true
        val picId = "789"
        val picIndex = "1"
        val objType = "thread"
        val prev = false
        val expectedBean = createMockPicPageBean()

        every {
            mockApi.picPageFlow(forumId, forumName, threadId, seeLz, picId, picIndex, objType, prev)
        } returns flowOf(expectedBean)

        // When
        val result = repository.picPage(forumId, forumName, threadId, seeLz, picId, picIndex, objType, prev).first()

        // Then
        assertEquals(expectedBean, result)
        verify(exactly = 1) {
            mockApi.picPageFlow(forumId, forumName, threadId, seeLz, picId, picIndex, objType, prev)
        }
    }

    @Test
    fun `picPage should handle prev=true parameter correctly`() = runTest {
        // Given
        val forumId = "123"
        val forumName = "testForum"
        val threadId = "456"
        val seeLz = false
        val picId = "789"
        val picIndex = "5"
        val objType = "thread"
        val prev = true
        val expectedBean = createMockPicPageBean()

        every {
            mockApi.picPageFlow(forumId, forumName, threadId, seeLz, picId, picIndex, objType, prev)
        } returns flowOf(expectedBean)

        // When
        val result = repository.picPage(forumId, forumName, threadId, seeLz, picId, picIndex, objType, prev).first()

        // Then
        assertEquals(expectedBean, result)
        verify(exactly = 1) {
            mockApi.picPageFlow(forumId, forumName, threadId, seeLz, picId, picIndex, objType, true)
        }
    }

    @Test
    fun `picPage should handle seeLz=true parameter correctly`() = runTest {
        // Given
        val forumId = "123"
        val forumName = "testForum"
        val threadId = "456"
        val seeLz = true
        val picId = "789"
        val picIndex = "1"
        val objType = "thread"
        val prev = false
        val expectedBean = createMockPicPageBean()

        every {
            mockApi.picPageFlow(forumId, forumName, threadId, seeLz, picId, picIndex, objType, prev)
        } returns flowOf(expectedBean)

        // When
        val result = repository.picPage(forumId, forumName, threadId, seeLz, picId, picIndex, objType, prev).first()

        // Then
        assertEquals(expectedBean, result)
        verify(exactly = 1) {
            mockApi.picPageFlow(forumId, forumName, threadId, true, picId, picIndex, objType, prev)
        }
    }

    @Test
    fun `picPage should handle seeLz=false parameter correctly`() = runTest {
        // Given
        val forumId = "123"
        val forumName = "testForum"
        val threadId = "456"
        val seeLz = false
        val picId = "789"
        val picIndex = "1"
        val objType = "thread"
        val prev = false
        val expectedBean = createMockPicPageBean()

        every {
            mockApi.picPageFlow(forumId, forumName, threadId, seeLz, picId, picIndex, objType, prev)
        } returns flowOf(expectedBean)

        // When
        val result = repository.picPage(forumId, forumName, threadId, seeLz, picId, picIndex, objType, prev).first()

        // Then
        assertEquals(expectedBean, result)
        verify(exactly = 1) {
            mockApi.picPageFlow(forumId, forumName, threadId, false, picId, picIndex, objType, prev)
        }
    }

    @Test
    fun `picPage should propagate api flow emissions correctly`() = runTest {
        // Given
        val forumId = "123"
        val forumName = "testForum"
        val threadId = "456"
        val seeLz = true
        val picId = "789"
        val picIndex = "1"
        val objType = "thread"
        val prev = false
        val expectedBean = createMockPicPageBean(picAmount = "20")

        every {
            mockApi.picPageFlow(forumId, forumName, threadId, seeLz, picId, picIndex, objType, prev)
        } returns flowOf(expectedBean)

        // When
        val result = repository.picPage(forumId, forumName, threadId, seeLz, picId, picIndex, objType, prev).first()

        // Then
        assertNotNull(result)
        assertEquals("20", result.picAmount)
        verify(exactly = 1) {
            mockApi.picPageFlow(forumId, forumName, threadId, seeLz, picId, picIndex, objType, prev)
        }
    }

    @Test
    fun `picPage should propagate error when API call fails`() = runTest {
        // Given
        val forumId = "123"
        val forumName = "testForum"
        val threadId = "456"
        val seeLz = true
        val picId = "789"
        val picIndex = "1"
        val objType = "thread"
        val prev = false
        val expectedException = RuntimeException("Failed to load pictures")

        every {
            mockApi.picPageFlow(forumId, forumName, threadId, seeLz, picId, picIndex, objType, prev)
        } returns flow { throw expectedException }

        // When & Then
        try {
            repository.picPage(forumId, forumName, threadId, seeLz, picId, picIndex, objType, prev).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Failed to load pictures", e.message)
        }
    }

    @Test
    fun `picPage should handle different objType values`() = runTest {
        // Given
        val forumId = "123"
        val forumName = "testForum"
        val threadId = "456"
        val seeLz = false
        val picId = "789"
        val picIndex = "1"
        val objType = "post"
        val prev = false
        val expectedBean = createMockPicPageBean()

        every {
            mockApi.picPageFlow(forumId, forumName, threadId, seeLz, picId, picIndex, objType, prev)
        } returns flowOf(expectedBean)

        // When
        val result = repository.picPage(forumId, forumName, threadId, seeLz, picId, picIndex, objType, prev).first()

        // Then
        assertEquals(expectedBean, result)
        verify(exactly = 1) {
            mockApi.picPageFlow(forumId, forumName, threadId, seeLz, picId, picIndex, "post", prev)
        }
    }

    @Test
    fun `picPage should handle empty forumName`() = runTest {
        // Given
        val forumId = "123"
        val forumName = ""
        val threadId = "456"
        val seeLz = true
        val picId = "789"
        val picIndex = "1"
        val objType = "thread"
        val prev = false
        val expectedBean = createMockPicPageBean()

        every {
            mockApi.picPageFlow(forumId, forumName, threadId, seeLz, picId, picIndex, objType, prev)
        } returns flowOf(expectedBean)

        // When
        val result = repository.picPage(forumId, forumName, threadId, seeLz, picId, picIndex, objType, prev).first()

        // Then
        assertEquals(expectedBean, result)
        verify(exactly = 1) {
            mockApi.picPageFlow(forumId, "", threadId, seeLz, picId, picIndex, objType, prev)
        }
    }
}
