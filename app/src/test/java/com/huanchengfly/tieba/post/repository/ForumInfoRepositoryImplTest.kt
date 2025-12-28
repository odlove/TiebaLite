package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.protos.RecommendForumInfo
import com.huanchengfly.tieba.post.api.models.protos.forumRuleDetail.ForumRuleDetailResponseData
import com.huanchengfly.tieba.post.api.models.protos.forumRuleDetail.ForumRuleDetailResponse
import com.huanchengfly.tieba.post.api.models.protos.getForumDetail.GetForumDetailResponse
import com.huanchengfly.tieba.post.api.models.protos.getForumDetail.GetForumDetailResponseData
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ForumInfoRepositoryImplTest {
    private lateinit var api: ITiebaApi
    private lateinit var repository: ForumInfoRepository

    @Before
    fun setUp() {
        api = mockk()
        repository = ForumInfoRepositoryImpl(api)
    }

    private fun createMockForumDetailResponse(
        forumId: Long,
        forumName: String = "testForum",
    ): GetForumDetailResponse {
        val forumInfo = mockk<RecommendForumInfo>(relaxed = true) {
            every { forum_id } returns forumId
            every { forum_name } returns forumName
        }
        val data = mockk<GetForumDetailResponseData>(relaxed = true) {
            every { forum_info } returns forumInfo
        }
        return mockk(relaxed = true) {
            every { data_ } returns data
        }
    }

    private fun createMockForumRuleDetailResponse(
        title: String = "ruleTitle",
        publishTime: String = "2024-01-01",
        preface: String = "preface",
    ): ForumRuleDetailResponse {
        val data = mockk<ForumRuleDetailResponseData>(relaxed = true) {
            every { this@mockk.title } returns title
            every { publish_time } returns publishTime
            every { this@mockk.preface } returns preface
            every { rules } returns emptyList()
            every { bazhu } returns null
        }
        return mockk(relaxed = true) {
            every { data_ } returns data
        }
    }

    @Test
    fun `getForumDetail should call api getForumDetailFlow`() = runTest {
        // Given
        val forumId = 12345L
        val mockResponse = createMockForumDetailResponse(forumId)
        every { api.getForumDetailFlow(forumId) } returns flowOf(mockResponse)

        // When
        val result = repository.getForumDetail(forumId).first()

        // Then
        verify(exactly = 1) { api.getForumDetailFlow(forumId) }
        assertEquals(forumId, result.forumId)
    }

    @Test
    fun `forumRuleDetail should call api forumRuleDetailFlow`() = runTest {
        // Given
        val forumId = 12345L
        val mockResponse = createMockForumRuleDetailResponse()
        every { api.forumRuleDetailFlow(forumId) } returns flowOf(mockResponse)

        // When
        val result = repository.forumRuleDetail(forumId).first()

        // Then
        verify(exactly = 1) { api.forumRuleDetailFlow(forumId) }
        assertEquals("ruleTitle", result.title)
    }

    @Test
    fun `getForumDetail should propagate flow from api`() = runTest {
        // Given
        val forumId = 12345L
        val mockResponse = createMockForumDetailResponse(forumId, "forumA")
        every { api.getForumDetailFlow(forumId) } returns flowOf(mockResponse)

        // When
        val result = repository.getForumDetail(forumId).first()

        // Then
        assertEquals(forumId, result.forumId)
        assertEquals("forumA", result.forumName)
    }

    @Test
    fun `forumRuleDetail should propagate flow from api`() = runTest {
        // Given
        val forumId = 12345L
        val mockResponse = createMockForumRuleDetailResponse(title = "titleA")
        every { api.forumRuleDetailFlow(forumId) } returns flowOf(mockResponse)

        // When
        val result = repository.forumRuleDetail(forumId).first()

        // Then
        assertEquals("titleA", result.title)
    }

    @Test
    fun `getForumDetail should handle multiple forumIds correctly`() = runTest {
        // Given
        val forumId1 = 111L
        val forumId2 = 222L
        val mockResponse1 = createMockForumDetailResponse(forumId1, "forum1")
        val mockResponse2 = createMockForumDetailResponse(forumId2, "forum2")
        every { api.getForumDetailFlow(forumId1) } returns flowOf(mockResponse1)
        every { api.getForumDetailFlow(forumId2) } returns flowOf(mockResponse2)

        // When
        val result1 = repository.getForumDetail(forumId1).first()
        val result2 = repository.getForumDetail(forumId2).first()

        // Then
        verify(exactly = 1) { api.getForumDetailFlow(forumId1) }
        verify(exactly = 1) { api.getForumDetailFlow(forumId2) }
        assertEquals("forum1", result1.forumName)
        assertEquals("forum2", result2.forumName)
    }

    @Test
    fun `forumRuleDetail should handle multiple forumIds correctly`() = runTest {
        // Given
        val forumId1 = 111L
        val forumId2 = 222L
        val mockResponse1 = createMockForumRuleDetailResponse(title = "title1")
        val mockResponse2 = createMockForumRuleDetailResponse(title = "title2")
        every { api.forumRuleDetailFlow(forumId1) } returns flowOf(mockResponse1)
        every { api.forumRuleDetailFlow(forumId2) } returns flowOf(mockResponse2)

        // When
        val result1 = repository.forumRuleDetail(forumId1).first()
        val result2 = repository.forumRuleDetail(forumId2).first()

        // Then
        verify(exactly = 1) { api.forumRuleDetailFlow(forumId1) }
        verify(exactly = 1) { api.forumRuleDetailFlow(forumId2) }
        assertEquals("title1", result1.title)
        assertEquals("title2", result2.title)
    }
}
