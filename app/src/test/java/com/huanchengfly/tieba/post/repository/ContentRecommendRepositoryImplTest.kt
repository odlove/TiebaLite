package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.repository.ThreadMetaStore
import com.huanchengfly.tieba.core.common.thread.ThreadMeta
import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.protos.FrsTabInfo
import com.huanchengfly.tieba.post.api.models.protos.RecommendTopicList
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.forumRecommend.ForumRecommendResponse
import com.huanchengfly.tieba.post.api.models.protos.forumRecommend.ForumRecommendResponseData
import com.huanchengfly.tieba.post.api.models.protos.forumRecommend.LikeForum
import com.huanchengfly.tieba.post.api.models.protos.hotThreadList.HotThreadListResponse
import com.huanchengfly.tieba.post.api.models.protos.hotThreadList.HotThreadListResponseData
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
    private lateinit var mockPbPageRepository: PbPageRepository
    private lateinit var mockThreadMetaStore: ThreadMetaStore

    @Before
    fun setup() {
        mockApi = mockk()
        mockPbPageRepository = mockk(relaxed = true)
        mockThreadMetaStore = mockk(relaxed = true)
        repository = ContentRecommendRepositoryImpl(mockApi, mockPbPageRepository, mockThreadMetaStore)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ========== Helper Functions ==========

    private fun createMockHotThreadListResponse(
        threadInfoList: List<ThreadInfo> = emptyList(),
        topicItems: List<RecommendTopicList> = emptyList(),
        tabItems: List<FrsTabInfo> = emptyList(),
    ): HotThreadListResponse {
        val data = mockk<HotThreadListResponseData>(relaxed = true) {
            every { threadInfo } returns threadInfoList
            every { topicList } returns topicItems
            every { hotThreadTabInfo } returns tabItems
        }
        return mockk<HotThreadListResponse> {
            every { error } returns mockk {
                every { error_code } returns 0
                every { error_msg } returns "success"
            }
            every { data_ } returns data
        }
    }

    private fun createMockForumRecommendResponse(): ForumRecommendResponse {
        val forum = mockk<LikeForum>(relaxed = true) {
            every { forum_id } returns 123L
            every { forum_name } returns "TestForum"
            every { avatar } returns "avatar"
            every { is_sign } returns 1
            every { level_id } returns 3
        }
        val data = mockk<ForumRecommendResponseData>(relaxed = true) {
            every { like_forum } returns listOf(forum)
        }
        return mockk<ForumRecommendResponse> {
            every { error } returns mockk {
                every { error_code } returns 0
                every { error_msg } returns "success"
            }
            every { data_ } returns data
        }
    }

    private fun createMockThreadInfo(
        idValue: Long = 123L,
        threadIdValue: Long = 0L,
    ): ThreadInfo =
        mockk(relaxed = true) {
            every { id } returns idValue
            every { threadId } returns threadIdValue
            every { richAbstract } returns emptyList()
            every { _abstract } returns emptyList()
            every { media } returns emptyList()
        }

    private fun createMockRecommendTopicList(): RecommendTopicList =
        mockk(relaxed = true) {
            every { topicId } returns 11L
            every { topicName } returns "Hot Topic"
            every { type } returns 2
            every { discussNum } returns 100L
            every { tag } returns 3
            every { topicDesc } returns "Hot Desc"
            every { topicPic } returns "https://example.com/topic.jpg"
        }

    private fun createMockFrsTabInfo(): FrsTabInfo =
        mockk(relaxed = true) {
            every { tabId } returns 7
            every { tabType } returns 1
            every { tabName } returns "TabName"
            every { tabUrl } returns "https://example.com/tab"
            every { tabGid } returns "gid"
            every { tabTitle } returns "TabTitle"
            every { isGeneralTab } returns 1
            every { tabCode } returns "home"
            every { tabVersion } returns 2
            every { isDefault } returns 1
        }

    private fun createMockTopicListResponse(): TopicListResponse {
        return mockk<TopicListResponse> {
            every { error } returns mockk {
                every { error_code } returns 0
                every { error_msg } returns "success"
            }
            every { data_ } returns
                mockk(relaxed = true) {
                    every { topic_list } returns
                        listOf(
                            mockk(relaxed = true) {
                                every { topic_id } returns 1L
                                every { topic_name } returns "Hot Topic"
                                every { topic_desc } returns "Hot Desc"
                                every { discuss_num } returns 100L
                                every { topic_image } returns "https://example.com/topic.jpg"
                                every { topic_tag } returns 2
                            }
                        )
                }
        }
    }

    // ========== hotThreadList Tests ==========

    @Test
    fun `hotThreadList should return success flow when API call succeeds`() = runTest {
        // Given: Mock API returns successful HotThreadListResponse
        val tabCode = "home"
        val threadInfo = createMockThreadInfo(idValue = 123L)
        val topic = createMockRecommendTopicList()
        val tab = createMockFrsTabInfo()
        val expectedResponse = createMockHotThreadListResponse(
            threadInfoList = listOf(threadInfo),
            topicItems = listOf(topic),
            tabItems = listOf(tab),
        )

        every {
            mockApi.hotThreadListFlow(tabCode)
        } returns flowOf(expectedResponse)

        // When: Call repository method
        val result = repository.hotThreadList(tabCode).first()

        // Then: Verify the result matches expected data
        assertNotNull(result)
        assertEquals(listOf(123L), result.threadIds)
        assertEquals(1, result.topicList.size)
        assertEquals(1, result.tabList.size)
        val mappedTopic = result.topicList.first()
        assertEquals(11L, mappedTopic.topicId)
        assertEquals("Hot Topic", mappedTopic.topicName)
        assertEquals(2, mappedTopic.type)
        assertEquals(100L, mappedTopic.discussNum)
        assertEquals(3, mappedTopic.tag)
        assertEquals("Hot Desc", mappedTopic.topicDesc)
        assertEquals("https://example.com/topic.jpg", mappedTopic.topicPic)
        val mappedTab = result.tabList.first()
        assertEquals(7, mappedTab.tabId)
        assertEquals(1, mappedTab.tabType)
        assertEquals("TabName", mappedTab.tabName)
        assertEquals("https://example.com/tab", mappedTab.tabUrl)
        assertEquals("gid", mappedTab.tabGid)
        assertEquals("TabTitle", mappedTab.tabTitle)
        assertEquals(1, mappedTab.isGeneralTab)
        assertEquals("home", mappedTab.tabCode)
        assertEquals(2, mappedTab.tabVersion)
        assertEquals(1, mappedTab.isDefault)
        verify(exactly = 1) {
            mockApi.hotThreadListFlow(tabCode)
        }
        verify(exactly = 1) { mockPbPageRepository.upsertThreads(any()) }
        verify(exactly = 1) { mockThreadMetaStore.updateFromServer(any<Map<Long, ThreadMeta>>()) }
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
        assertEquals(0, result.threadIds.size)
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
        assertEquals(1, result.forums.size)
        assertEquals("123", result.forums.first().forumId)
        assertEquals("TestForum", result.forums.first().forumName)
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
        assertEquals(1, result.forums.size)
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
        assertEquals(1, result.size)
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
