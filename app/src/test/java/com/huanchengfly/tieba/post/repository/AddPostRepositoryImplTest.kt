package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.protos.addPost.AddPostResponse
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
 * Unit tests for AddPostRepositoryImpl
 *
 * Tests verify that the repository correctly delegates to ITiebaApi.addPostFlow
 * and handles type conversions (Long to String) for post/reply operations.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AddPostRepositoryImplTest {

    private lateinit var repository: AddPostRepositoryImpl
    private lateinit var mockApi: ITiebaApi

    @Before
    fun setup() {
        mockApi = mockk()
        repository = AddPostRepositoryImpl(mockApi)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ========== Helper Functions ==========

    private fun createMockAddPostResponse(): AddPostResponse {
        return mockk<AddPostResponse>(relaxed = true) {
            every { error } returns mockk(relaxed = true) {
                every { error_code } returns 0
                every { error_msg } returns "success"
            }
        }
    }

    // ========== addPost Basic Tests ==========

    @Test
    fun `addPost should call api with correct parameters`() = runTest {
        // Given
        val content = "Test post content"
        val forumId = 123L
        val forumName = "testForum"
        val threadId = 456L
        val tbs = "test_tbs"
        val nameShow = "TestUser"
        val postId = 789L
        val subPostId = 111L
        val replyUserId = 222L
        val expectedResponse = createMockAddPostResponse()

        every {
            mockApi.addPostFlow(
                content,
                "123",
                forumName,
                "456",
                tbs,
                nameShow,
                "789",
                "111",
                "222"
            )
        } returns flowOf(expectedResponse)

        // When
        val result = repository.addPost(
            content, forumId, forumName, threadId, tbs, nameShow, postId, subPostId, replyUserId
        ).first()

        // Then
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.addPostFlow(
                content,
                "123",
                forumName,
                "456",
                tbs,
                nameShow,
                "789",
                "111",
                "222"
            )
        }
    }

    @Test
    fun `addPost should handle null optional parameters`() = runTest {
        // Given
        val content = "Test post content"
        val forumId = 123L
        val forumName = "testForum"
        val threadId = 456L
        val expectedResponse = createMockAddPostResponse()

        every {
            mockApi.addPostFlow(
                content,
                "123",
                forumName,
                "456",
                null,
                null,
                null,
                null,
                null
            )
        } returns flowOf(expectedResponse)

        // When
        val result = repository.addPost(
            content, forumId, forumName, threadId, null, null, null, null, null
        ).first()

        // Then
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.addPostFlow(
                content,
                "123",
                forumName,
                "456",
                null,
                null,
                null,
                null,
                null
            )
        }
    }

    @Test
    fun `addPost should convert Long parameters to String correctly`() = runTest {
        // Given
        val content = "Test post"
        val forumId = 999L
        val forumName = "forum"
        val threadId = 888L
        val postId = 777L
        val subPostId = 666L
        val replyUserId = 555L
        val expectedResponse = createMockAddPostResponse()

        every {
            mockApi.addPostFlow(
                content,
                "999",  // forumId converted
                forumName,
                "888",  // threadId converted
                null,
                null,
                "777",  // postId converted
                "666",  // subPostId converted
                "555"   // replyUserId converted
            )
        } returns flowOf(expectedResponse)

        // When
        val result = repository.addPost(
            content, forumId, forumName, threadId, null, null, postId, subPostId, replyUserId
        ).first()

        // Then
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.addPostFlow(
                content,
                "999",
                forumName,
                "888",
                null,
                null,
                "777",
                "666",
                "555"
            )
        }
    }

    @Test
    fun `addPost should handle different content values`() = runTest {
        // Given
        val forumId = 123L
        val forumName = "testForum"
        val threadId = 456L
        val expectedResponse = createMockAddPostResponse()

        // Test with short content
        every {
            mockApi.addPostFlow(
                "Short",
                "123",
                forumName,
                "456",
                null,
                null,
                null,
                null,
                null
            )
        } returns flowOf(expectedResponse)

        val result1 = repository.addPost(
            "Short", forumId, forumName, threadId, null, null, null, null, null
        ).first()
        assertNotNull(result1)

        // Test with long content
        val longContent = "This is a very long post content ".repeat(10)
        every {
            mockApi.addPostFlow(
                longContent,
                "123",
                forumName,
                "456",
                null,
                null,
                null,
                null,
                null
            )
        } returns flowOf(expectedResponse)

        val result2 = repository.addPost(
            longContent, forumId, forumName, threadId, null, null, null, null, null
        ).first()
        assertNotNull(result2)

        verify(exactly = 1) { mockApi.addPostFlow("Short", "123", forumName, "456", null, null, null, null, null) }
        verify(exactly = 1) { mockApi.addPostFlow(longContent, "123", forumName, "456", null, null, null, null, null) }
    }

    @Test
    fun `addPost should handle reply scenario with postId and replyUserId`() = runTest {
        // Given - Reply to a post
        val content = "Reply to user"
        val forumId = 123L
        val forumName = "testForum"
        val threadId = 456L
        val postId = 789L
        val replyUserId = 111L
        val expectedResponse = createMockAddPostResponse()

        every {
            mockApi.addPostFlow(
                content,
                "123",
                forumName,
                "456",
                null,
                null,
                "789",
                null,
                "111"
            )
        } returns flowOf(expectedResponse)

        // When
        val result = repository.addPost(
            content, forumId, forumName, threadId, null, null, postId, null, replyUserId
        ).first()

        // Then
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.addPostFlow(
                content,
                "123",
                forumName,
                "456",
                null,
                null,
                "789",
                null,
                "111"
            )
        }
    }

    @Test
    fun `addPost should handle sub-reply scenario with postId and subPostId`() = runTest {
        // Given - Reply to a sub-post
        val content = "Reply to sub-post"
        val forumId = 123L
        val forumName = "testForum"
        val threadId = 456L
        val postId = 789L
        val subPostId = 222L
        val replyUserId = 333L
        val expectedResponse = createMockAddPostResponse()

        every {
            mockApi.addPostFlow(
                content,
                "123",
                forumName,
                "456",
                null,
                null,
                "789",
                "222",
                "333"
            )
        } returns flowOf(expectedResponse)

        // When
        val result = repository.addPost(
            content, forumId, forumName, threadId, null, null, postId, subPostId, replyUserId
        ).first()

        // Then
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.addPostFlow(
                content,
                "123",
                forumName,
                "456",
                null,
                null,
                "789",
                "222",
                "333"
            )
        }
    }

    @Test
    fun `addPost should handle nameShow parameter for anonymous posting`() = runTest {
        // Given
        val content = "Anonymous post"
        val forumId = 123L
        val forumName = "testForum"
        val threadId = 456L
        val nameShow = "AnonymousUser"
        val expectedResponse = createMockAddPostResponse()

        every {
            mockApi.addPostFlow(
                content,
                "123",
                forumName,
                "456",
                null,
                nameShow,
                null,
                null,
                null
            )
        } returns flowOf(expectedResponse)

        // When
        val result = repository.addPost(
            content, forumId, forumName, threadId, null, nameShow, null, null, null
        ).first()

        // Then
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.addPostFlow(
                content,
                "123",
                forumName,
                "456",
                null,
                nameShow,
                null,
                null,
                null
            )
        }
    }

    @Test
    fun `addPost should handle tbs parameter for authentication`() = runTest {
        // Given
        val content = "Authenticated post"
        val forumId = 123L
        val forumName = "testForum"
        val threadId = 456L
        val tbs = "authentication_token_123"
        val expectedResponse = createMockAddPostResponse()

        every {
            mockApi.addPostFlow(
                content,
                "123",
                forumName,
                "456",
                tbs,
                null,
                null,
                null,
                null
            )
        } returns flowOf(expectedResponse)

        // When
        val result = repository.addPost(
            content, forumId, forumName, threadId, tbs, null, null, null, null
        ).first()

        // Then
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.addPostFlow(
                content,
                "123",
                forumName,
                "456",
                tbs,
                null,
                null,
                null,
                null
            )
        }
    }

    // ========== Error Propagation Tests ==========

    @Test
    fun `addPost should propagate error when API call fails`() = runTest {
        // Given
        val content = "Test post"
        val forumId = 123L
        val forumName = "testForum"
        val threadId = 456L
        val expectedException = RuntimeException("Network error")

        every {
            mockApi.addPostFlow(
                content,
                "123",
                forumName,
                "456",
                null,
                null,
                null,
                null,
                null
            )
        } returns flow { throw expectedException }

        // When & Then
        try {
            repository.addPost(
                content, forumId, forumName, threadId, null, null, null, null, null
            ).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Network error", e.message)
        }
    }

    @Test
    fun `addPost should handle empty content string`() = runTest {
        // Given
        val content = ""
        val forumId = 123L
        val forumName = "testForum"
        val threadId = 456L
        val expectedResponse = createMockAddPostResponse()

        every {
            mockApi.addPostFlow(
                content,
                "123",
                forumName,
                "456",
                null,
                null,
                null,
                null,
                null
            )
        } returns flowOf(expectedResponse)

        // When
        val result = repository.addPost(
            content, forumId, forumName, threadId, null, null, null, null, null
        ).first()

        // Then
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.addPostFlow(
                "",
                "123",
                forumName,
                "456",
                null,
                null,
                null,
                null,
                null
            )
        }
    }
}
