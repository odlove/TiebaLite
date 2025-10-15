package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.protos.forumRecommend.ForumRecommendResponse
import com.huanchengfly.tieba.post.api.models.protos.hotThreadList.HotThreadListResponse
import com.huanchengfly.tieba.post.api.models.protos.topicList.TopicListResponse
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ContentRecommendRepositoryImpl
 *
 * Tests verify that the repository correctly delegates to ITiebaApi and propagates
 * Flow emissions for content recommendation operations (hotThreadList, forumRecommend, topicList).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ContentRecommendRepositoryImplTest {

    private lateinit var repository: ContentRecommendRepositoryImpl
    private lateinit var mockApi: ITiebaApi

    @Before
    fun setup() {
        mockApi = mockk()
        repository = ContentRecommendRepositoryImpl(mockApi)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ========== Helper Functions ==========

    private fun createMockHotThreadListResponse(): HotThreadListResponse {
        return mockk<HotThreadListResponse> {
            every { error } returns mockk {
                every { error_code } returns 0
                every { error_msg } returns "success"
            }
            every { data_ } returns mockk()
        }
    }

    private fun createMockForumRecommendResponse(): ForumRecommendResponse {
        return mockk<ForumRecommendResponse> {
            every { error } returns mockk {
                every { error_code } returns 0
                every { error_msg } returns "success"
            }
            every { data_ } returns mockk()
        }
    }

    private fun createMockTopicListResponse(): TopicListResponse {
        return mockk<TopicListResponse> {
            every { error } returns mockk {
                every { error_code } returns 0
                every { error_msg } returns "success"
            }
            every { data_ } returns mockk()
        }
    }

    // ========== hotThreadList Tests ==========

    @Test
    fun `hotThreadList should return success flow when API call succeeds`() = runTest {
        // Given: Mock API returns successful HotThreadListResponse
        val tabCode = "home"
        val expectedResponse = createMockHotThreadListResponse()

        every {
            mockApi.hotThreadListFlow(tabCode)
        } returns flowOf(expectedResponse)

        // When: Call repository method
        val result = repository.hotThreadList(tabCode).first()

        // Then: Verify the result matches expected data
        assertNotNull(result)
        assertNotNull(result.error)
        verify(exactly = 1) {
            mockApi.hotThreadListFlow(tabCode)
        }
    }

    @Test
    fun `hotThreadList should propagate error when API call fails`() = runTest {
        // Given: Mock API throws exception
        val tabCode = "home"
        val expectedException = RuntimeException("HotThreadList failed")

        every {
            mockApi.hotThreadListFlow(tabCode)
        } returns flow { throw expectedException }

        // When & Then: Verify exception is propagated
        try {
            repository.hotThreadList(tabCode).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("HotThreadList failed", e.message)
        }
    }

    @Test
    fun `hotThreadList should handle empty tabCode parameter`() = runTest {
        // Given: Empty tabCode
        val tabCode = ""
        val expectedResponse = createMockHotThreadListResponse()

        every {
            mockApi.hotThreadListFlow(tabCode)
        } returns flowOf(expectedResponse)

        // When: Call repository with empty tabCode
        val result = repository.hotThreadList(tabCode).first()

        // Then: Verify API is called (parameter validation is API's responsibility)
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.hotThreadListFlow("")
        }
    }

    @Test
    fun `hotThreadList should handle different tabCodes`() = runTest {
        // Given: Different tab codes
        val homeResponse = createMockHotThreadListResponse()
        every {
            mockApi.hotThreadListFlow("home")
        } returns flowOf(homeResponse)

        val videoResponse = createMockHotThreadListResponse()
        every {
            mockApi.hotThreadListFlow("video")
        } returns flowOf(videoResponse)

        // When: Call repository with different tabCodes
        val homeResult = repository.hotThreadList("home").first()
        val videoResult = repository.hotThreadList("video").first()

        // Then: Verify both tabCodes were called
        assertNotNull(homeResult)
        assertNotNull(videoResult)
        verify(exactly = 1) { mockApi.hotThreadListFlow("home") }
        verify(exactly = 1) { mockApi.hotThreadListFlow("video") }
    }

    // ========== forumRecommend Tests ==========

    @Test
    fun `forumRecommend should return success flow when API call succeeds`() = runTest {
        // Given: Mock API returns successful ForumRecommendResponse
        val expectedResponse = createMockForumRecommendResponse()

        every {
            mockApi.forumRecommendNewFlow()
        } returns flowOf(expectedResponse)

        // When: Call repository method
        val result = repository.forumRecommend().first()

        // Then: Verify the result matches expected data
        assertNotNull(result)
        assertNotNull(result.error)
        verify(exactly = 1) {
            mockApi.forumRecommendNewFlow()
        }
    }

    @Test
    fun `forumRecommend should propagate error when API call fails`() = runTest {
        // Given: Mock API throws exception
        val expectedException = RuntimeException("ForumRecommend failed")

        every {
            mockApi.forumRecommendNewFlow()
        } returns flow { throw expectedException }

        // When & Then: Verify exception is propagated
        try {
            repository.forumRecommend().first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("ForumRecommend failed", e.message)
        }
    }

    @Test
    fun `forumRecommend should work without parameters`() = runTest {
        // Given: Mock API with no parameters
        val expectedResponse = createMockForumRecommendResponse()

        every {
            mockApi.forumRecommendNewFlow()
        } returns flowOf(expectedResponse)

        // When: Call repository method
        val result = repository.forumRecommend().first()

        // Then: Verify API is called correctly
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.forumRecommendNewFlow()
        }
    }

    // ========== topicList Tests ==========

    @Test
    fun `topicList should return success flow when API call succeeds`() = runTest {
        // Given: Mock API returns successful TopicListResponse
        val expectedResponse = createMockTopicListResponse()

        every {
            mockApi.topicListFlow()
        } returns flowOf(expectedResponse)

        // When: Call repository method
        val result = repository.topicList().first()

        // Then: Verify the result matches expected data
        assertNotNull(result)
        assertNotNull(result.error)
        verify(exactly = 1) {
            mockApi.topicListFlow()
        }
    }

    @Test
    fun `topicList should propagate error when API call fails`() = runTest {
        // Given: Mock API throws exception
        val expectedException = RuntimeException("TopicList failed")

        every {
            mockApi.topicListFlow()
        } returns flow { throw expectedException }

        // When & Then: Verify exception is propagated
        try {
            repository.topicList().first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("TopicList failed", e.message)
        }
    }

    @Test
    fun `topicList should work without parameters`() = runTest {
        // Given: Mock API with no parameters
        val expectedResponse = createMockTopicListResponse()

        every {
            mockApi.topicListFlow()
        } returns flowOf(expectedResponse)

        // When: Call repository method
        val result = repository.topicList().first()

        // Then: Verify API is called correctly
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.topicListFlow()
        }
    }

    @Test
    fun `all three methods should work independently`() = runTest {
        // Given: Mock all three API methods
        val hotThreadResponse = createMockHotThreadListResponse()
        every {
            mockApi.hotThreadListFlow("home")
        } returns flowOf(hotThreadResponse)

        val forumRecommendResponse = createMockForumRecommendResponse()
        every {
            mockApi.forumRecommendNewFlow()
        } returns flowOf(forumRecommendResponse)

        val topicListResponse = createMockTopicListResponse()
        every {
            mockApi.topicListFlow()
        } returns flowOf(topicListResponse)

        // When: Call all three methods
        val hotThreadResult = repository.hotThreadList("home").first()
        val forumRecommendResult = repository.forumRecommend().first()
        val topicListResult = repository.topicList().first()

        // Then: Verify all work independently
        assertNotNull(hotThreadResult)
        assertNotNull(forumRecommendResult)
        assertNotNull(topicListResult)
        verify(exactly = 1) { mockApi.hotThreadListFlow("home") }
        verify(exactly = 1) { mockApi.forumRecommendNewFlow() }
        verify(exactly = 1) { mockApi.topicListFlow() }
    }
}
