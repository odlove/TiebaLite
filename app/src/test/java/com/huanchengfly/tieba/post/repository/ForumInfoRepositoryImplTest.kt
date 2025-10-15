package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.protos.forumRuleDetail.ForumRuleDetailResponse
import com.huanchengfly.tieba.post.api.models.protos.getForumDetail.GetForumDetailResponse
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

    @Test
    fun `getForumDetail should call api getForumDetailFlow`() = runTest {
        // Given
        val forumId = 12345L
        val mockResponse = mockk<GetForumDetailResponse>(relaxed = true)
        every { api.getForumDetailFlow(forumId) } returns flowOf(mockResponse)

        // When
        val result = repository.getForumDetail(forumId).first()

        // Then
        verify(exactly = 1) { api.getForumDetailFlow(forumId) }
        assertEquals(mockResponse, result)
    }

    @Test
    fun `forumRuleDetail should call api forumRuleDetailFlow`() = runTest {
        // Given
        val forumId = 12345L
        val mockResponse = mockk<ForumRuleDetailResponse>(relaxed = true)
        every { api.forumRuleDetailFlow(forumId) } returns flowOf(mockResponse)

        // When
        val result = repository.forumRuleDetail(forumId).first()

        // Then
        verify(exactly = 1) { api.forumRuleDetailFlow(forumId) }
        assertEquals(mockResponse, result)
    }

    @Test
    fun `getForumDetail should propagate flow from api`() = runTest {
        // Given
        val forumId = 12345L
        val mockResponse = mockk<GetForumDetailResponse>(relaxed = true)
        every { api.getForumDetailFlow(forumId) } returns flowOf(mockResponse)

        // When
        val result = repository.getForumDetail(forumId).first()

        // Then
        assertEquals(mockResponse, result)
    }

    @Test
    fun `forumRuleDetail should propagate flow from api`() = runTest {
        // Given
        val forumId = 12345L
        val mockResponse = mockk<ForumRuleDetailResponse>(relaxed = true)
        every { api.forumRuleDetailFlow(forumId) } returns flowOf(mockResponse)

        // When
        val result = repository.forumRuleDetail(forumId).first()

        // Then
        assertEquals(mockResponse, result)
    }

    @Test
    fun `getForumDetail should handle multiple forumIds correctly`() = runTest {
        // Given
        val forumId1 = 111L
        val forumId2 = 222L
        val mockResponse1 = mockk<GetForumDetailResponse>(relaxed = true)
        val mockResponse2 = mockk<GetForumDetailResponse>(relaxed = true)
        every { api.getForumDetailFlow(forumId1) } returns flowOf(mockResponse1)
        every { api.getForumDetailFlow(forumId2) } returns flowOf(mockResponse2)

        // When
        val result1 = repository.getForumDetail(forumId1).first()
        val result2 = repository.getForumDetail(forumId2).first()

        // Then
        verify(exactly = 1) { api.getForumDetailFlow(forumId1) }
        verify(exactly = 1) { api.getForumDetailFlow(forumId2) }
        assertEquals(mockResponse1, result1)
        assertEquals(mockResponse2, result2)
    }

    @Test
    fun `forumRuleDetail should handle multiple forumIds correctly`() = runTest {
        // Given
        val forumId1 = 111L
        val forumId2 = 222L
        val mockResponse1 = mockk<ForumRuleDetailResponse>(relaxed = true)
        val mockResponse2 = mockk<ForumRuleDetailResponse>(relaxed = true)
        every { api.forumRuleDetailFlow(forumId1) } returns flowOf(mockResponse1)
        every { api.forumRuleDetailFlow(forumId2) } returns flowOf(mockResponse2)

        // When
        val result1 = repository.forumRuleDetail(forumId1).first()
        val result2 = repository.forumRuleDetail(forumId2).first()

        // Then
        verify(exactly = 1) { api.forumRuleDetailFlow(forumId1) }
        verify(exactly = 1) { api.forumRuleDetailFlow(forumId2) }
        assertEquals(mockResponse1, result1)
        assertEquals(mockResponse2, result2)
    }
}
