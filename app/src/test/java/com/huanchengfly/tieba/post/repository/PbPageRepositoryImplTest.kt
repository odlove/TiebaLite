package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.protos.pbPage.PbPageResponse
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaUnknownException
import com.huanchengfly.tieba.post.ui.page.thread.ThreadPageFrom
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
 * Unit tests for PbPageRepositoryImpl
 *
 * Tests verify that the repository correctly delegates to ITiebaApi.pbPageFlow
 * and handles complex data validation and transformation logic for thread detail pages.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PbPageRepositoryImplTest {

    private lateinit var repository: PbPageRepositoryImpl
    private lateinit var mockApi: ITiebaApi

    @Before
    fun setup() {
        mockApi = mockk()
        repository = PbPageRepositoryImpl(mockApi)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ========== Helper Functions ==========

    private fun createMockPbPageResponse(
        hasData: Boolean = true,
        hasPostList: Boolean = true,
        hasRequiredFields: Boolean = true
    ): PbPageResponse {
        return mockk<PbPageResponse>(relaxed = true) {
            every { error } returns mockk(relaxed = true) {
                every { error_code } returns 0
                every { error_msg } returns "success"
            }

            if (!hasData) {
                every { data_ } returns null
            } else {
                every { data_ } returns mockk(relaxed = true) {
                    // post_list
                    if (!hasPostList) {
                        every { post_list } returns emptyList()
                    } else {
                        every { post_list } returns listOf(mockk(relaxed = true) {
                            every { author_id } returns 12345L
                            every { floor } returns 1
                            every { author } returns mockk(relaxed = true) {
                                every { id } returns 12345L
                            }
                            every { sub_post_list } returns null
                        })
                    }

                    // user_list
                    every { user_list } returns listOf(
                        mockk(relaxed = true) { every { id } returns 12345L },
                        mockk(relaxed = true) { every { id } returns 67890L }
                    )

                    // thread
                    every { thread } returns mockk(relaxed = true) {
                        every { id } returns 999L
                        if (hasRequiredFields) {
                            every { author } returns mockk(relaxed = true) {
                                every { id } returns 67890L
                            }
                        } else {
                            every { author } returns null
                        }
                    }

                    // forum, anti, page
                    every { forum } returns if (hasRequiredFields) mockk(relaxed = true) else null
                    every { anti } returns if (hasRequiredFields) mockk(relaxed = true) else null
                    every { page } returns if (hasRequiredFields) mockk(relaxed = true) else null
                    every { first_floor_post } returns null
                }
            }
        }
    }

    // ========== pbPage Basic Tests ==========

    @Test
    fun `pbPage should call api with correct parameters`() = runTest {
        // Given
        val threadId = 123456L
        val page = 1
        val postId = 789L
        val forumId = 111L
        val seeLz = true
        val sortType = 0
        val back = false
        val from = ""
        val lastPostId: Long? = null
        val expectedResponse = createMockPbPageResponse()

        every {
            mockApi.pbPageFlow(
                threadId, page, postId, seeLz, back, sortType, forumId, "", 0, lastPostId
            )
        } returns flowOf(expectedResponse)

        // When
        val result = repository.pbPage(threadId, page, postId, forumId, seeLz, sortType, back, from, lastPostId).first()

        // Then
        assertNotNull(result)
        assertEquals(0, result.error?.error_code)
        verify(exactly = 1) {
            mockApi.pbPageFlow(threadId, page, postId, seeLz, back, sortType, forumId, "", 0, lastPostId)
        }
    }

    @Test
    fun `pbPage should use default parameters correctly`() = runTest {
        // Given
        val threadId = 123456L
        val expectedResponse = createMockPbPageResponse()

        every {
            mockApi.pbPageFlow(threadId, 1, 0, false, false, 0, null, "", 0, null)
        } returns flowOf(expectedResponse)

        // When
        val result = repository.pbPage(threadId).first()

        // Then
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.pbPageFlow(threadId, 1, 0, false, false, 0, null, "", 0, null)
        }
    }

    @Test
    fun `pbPage should handle seeLz=true parameter`() = runTest {
        // Given
        val threadId = 123456L
        val seeLz = true
        val expectedResponse = createMockPbPageResponse()

        every {
            mockApi.pbPageFlow(threadId, 1, 0, true, false, 0, null, "", 0, null)
        } returns flowOf(expectedResponse)

        // When
        val result = repository.pbPage(threadId, seeLz = seeLz).first()

        // Then
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.pbPageFlow(threadId, 1, 0, true, false, 0, null, "", 0, null)
        }
    }

    @Test
    fun `pbPage should handle different sortType values`() = runTest {
        // Given
        val threadId = 123456L
        val expectedResponse = createMockPbPageResponse()

        // Test sortType = 1 (reverse)
        every {
            mockApi.pbPageFlow(threadId, 1, 0, false, false, 1, null, "", 0, null)
        } returns flowOf(expectedResponse)

        val result1 = repository.pbPage(threadId, sortType = 1).first()
        assertNotNull(result1)

        // Test sortType = 2 (hot)
        every {
            mockApi.pbPageFlow(threadId, 1, 0, false, false, 2, null, "", 0, null)
        } returns flowOf(expectedResponse)

        val result2 = repository.pbPage(threadId, sortType = 2).first()
        assertNotNull(result2)

        verify(exactly = 1) { mockApi.pbPageFlow(threadId, 1, 0, false, false, 1, null, "", 0, null) }
        verify(exactly = 1) { mockApi.pbPageFlow(threadId, 1, 0, false, false, 2, null, "", 0, null) }
    }

    @Test
    fun `pbPage should handle pagination correctly`() = runTest {
        // Given
        val threadId = 123456L
        val expectedResponse = createMockPbPageResponse()

        // Test page 1
        every {
            mockApi.pbPageFlow(threadId, 1, 0, false, false, 0, null, "", 0, null)
        } returns flowOf(expectedResponse)

        val result1 = repository.pbPage(threadId, page = 1).first()
        assertNotNull(result1)

        // Test page 2
        every {
            mockApi.pbPageFlow(threadId, 2, 0, false, false, 0, null, "", 0, null)
        } returns flowOf(expectedResponse)

        val result2 = repository.pbPage(threadId, page = 2).first()
        assertNotNull(result2)

        verify(exactly = 1) { mockApi.pbPageFlow(threadId, 1, 0, false, false, 0, null, "", 0, null) }
        verify(exactly = 1) { mockApi.pbPageFlow(threadId, 2, 0, false, false, 0, null, "", 0, null) }
    }

    @Test
    fun `pbPage should handle back=true for reverse pagination`() = runTest {
        // Given
        val threadId = 123456L
        val back = true
        val expectedResponse = createMockPbPageResponse()

        every {
            mockApi.pbPageFlow(threadId, 1, 0, false, true, 0, null, "", 0, null)
        } returns flowOf(expectedResponse)

        // When
        val result = repository.pbPage(threadId, back = back).first()

        // Then
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.pbPageFlow(threadId, 1, 0, false, true, 0, null, "", 0, null)
        }
    }

    // ========== from Parameter Tests ==========

    @Test
    fun `pbPage should handle from=mention parameter with stType`() = runTest {
        // Given
        val threadId = 123456L
        val from = "mention"
        val expectedResponse = createMockPbPageResponse()

        every {
            mockApi.pbPageFlow(threadId, 1, 0, false, false, 0, null, "mention", 0, null)
        } returns flowOf(expectedResponse)

        // When
        val result = repository.pbPage(threadId, from = from).first()

        // Then
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.pbPageFlow(threadId, 1, 0, false, false, 0, null, "mention", 0, null)
        }
    }

    @Test
    fun `pbPage should handle from=store_thread parameter with stType and mark`() = runTest {
        // Given
        val threadId = 123456L
        val from = "store_thread"  // Note: This equals ThreadPageFrom.FROM_STORE
        val expectedResponse = createMockPbPageResponse()

        // When from="store_thread", it matches both ST_TYPES and ThreadPageFrom.FROM_STORE
        // So stType="store_thread" and mark=1
        every {
            mockApi.pbPageFlow(threadId, 1, 0, false, false, 0, null, "store_thread", 1, null)
        } returns flowOf(expectedResponse)

        // When
        val result = repository.pbPage(threadId, from = from).first()

        // Then
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.pbPageFlow(threadId, 1, 0, false, false, 0, null, "store_thread", 1, null)
        }
    }

    @Test
    fun `pbPage should handle from=FROM_STORE constant with mark=1`() = runTest {
        // Given
        val threadId = 123456L
        val from = ThreadPageFrom.FROM_STORE  // This is "store_thread"
        val expectedResponse = createMockPbPageResponse()

        // ThreadPageFrom.FROM_STORE equals "store_thread", so stType="store_thread" and mark=1
        every {
            mockApi.pbPageFlow(threadId, 1, 0, false, false, 0, null, "store_thread", 1, null)
        } returns flowOf(expectedResponse)

        // When
        val result = repository.pbPage(threadId, from = from).first()

        // Then
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.pbPageFlow(threadId, 1, 0, false, false, 0, null, "store_thread", 1, null)
        }
    }

    @Test
    fun `pbPage should handle unknown from parameter with empty stType`() = runTest {
        // Given
        val threadId = 123456L
        val from = "unknown_source"
        val expectedResponse = createMockPbPageResponse()

        every {
            mockApi.pbPageFlow(threadId, 1, 0, false, false, 0, null, "", 0, null)
        } returns flowOf(expectedResponse)

        // When
        val result = repository.pbPage(threadId, from = from).first()

        // Then
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.pbPageFlow(threadId, 1, 0, false, false, 0, null, "", 0, null)
        }
    }

    // ========== Data Validation Tests ==========

    @Test
    fun `pbPage should throw TiebaUnknownException when data is null`() = runTest {
        // Given
        val threadId = 123456L
        val responseWithNullData = createMockPbPageResponse(hasData = false)

        every {
            mockApi.pbPageFlow(threadId, 1, 0, false, false, 0, null, "", 0, null)
        } returns flowOf(responseWithNullData)

        // When & Then
        try {
            repository.pbPage(threadId).first()
            throw AssertionError("Expected TiebaUnknownException to be thrown")
        } catch (e: TiebaUnknownException) {
            // Expected
        }
    }

    @Test
    fun `pbPage should throw EmptyDataException when post_list is empty`() = runTest {
        // Given
        val threadId = 123456L
        val responseWithEmptyPostList = createMockPbPageResponse(hasPostList = false)

        every {
            mockApi.pbPageFlow(threadId, 1, 0, false, false, 0, null, "", 0, null)
        } returns flowOf(responseWithEmptyPostList)

        // When & Then
        try {
            repository.pbPage(threadId).first()
            throw AssertionError("Expected EmptyDataException to be thrown")
        } catch (e: EmptyDataException) {
            // Expected
        }
    }

    @Test
    fun `pbPage should throw TiebaUnknownException when required fields are missing`() = runTest {
        // Given
        val threadId = 123456L
        val responseWithMissingFields = createMockPbPageResponse(hasRequiredFields = false)

        every {
            mockApi.pbPageFlow(threadId, 1, 0, false, false, 0, null, "", 0, null)
        } returns flowOf(responseWithMissingFields)

        // When & Then
        try {
            repository.pbPage(threadId).first()
            throw AssertionError("Expected TiebaUnknownException to be thrown")
        } catch (e: TiebaUnknownException) {
            // Expected
        }
    }

    // ========== Error Propagation Tests ==========

    @Test
    fun `pbPage should propagate error when API call fails`() = runTest {
        // Given
        val threadId = 123456L
        val expectedException = RuntimeException("Network error")

        every {
            mockApi.pbPageFlow(threadId, 1, 0, false, false, 0, null, "", 0, null)
        } returns flow { throw expectedException }

        // When & Then
        try {
            repository.pbPage(threadId).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Network error", e.message)
        }
    }

    @Test
    fun `pbPage should handle lastPostId parameter for incremental loading`() = runTest {
        // Given
        val threadId = 123456L
        val lastPostId = 999L
        val expectedResponse = createMockPbPageResponse()

        every {
            mockApi.pbPageFlow(threadId, 1, 0, false, false, 0, null, "", 0, lastPostId)
        } returns flowOf(expectedResponse)

        // When
        val result = repository.pbPage(threadId, lastPostId = lastPostId).first()

        // Then
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.pbPageFlow(threadId, 1, 0, false, false, 0, null, "", 0, lastPostId)
        }
    }

    @Test
    fun `pbPage should handle postId for jumping to specific post`() = runTest {
        // Given
        val threadId = 123456L
        val postId = 789L
        val expectedResponse = createMockPbPageResponse()

        every {
            mockApi.pbPageFlow(threadId, 1, postId, false, false, 0, null, "", 0, null)
        } returns flowOf(expectedResponse)

        // When
        val result = repository.pbPage(threadId, postId = postId).first()

        // Then
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.pbPageFlow(threadId, 1, postId, false, false, 0, null, "", 0, null)
        }
    }
}
