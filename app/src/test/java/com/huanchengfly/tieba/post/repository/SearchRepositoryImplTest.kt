package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.SearchForumBean
import com.huanchengfly.tieba.post.api.models.SearchThreadBean
import com.huanchengfly.tieba.post.api.models.SearchUserBean
import com.huanchengfly.tieba.post.api.models.protos.searchSug.SearchSugResponse
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
 * Unit tests for SearchRepositoryImpl
 *
 * Tests verify that the repository correctly delegates to ITiebaApi and propagates
 * Flow emissions for search operations (searchThread, searchSuggestions).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchRepositoryImplTest {

    private lateinit var repository: SearchRepositoryImpl
    private lateinit var mockApi: ITiebaApi

    @Before
    fun setup() {
        mockApi = mockk()
        repository = SearchRepositoryImpl(mockApi)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ========== Helper Functions ==========

    private fun createMockSearchThreadBean(errorCode: Int = 0): SearchThreadBean {
        return mockk<SearchThreadBean> {
            every { this@mockk.errorCode } returns errorCode
            every { errorMsg } returns "success"
            every { data } returns mockk {
                every { hasMore } returns 1
                every { currentPage } returns 1
                every { postList } returns emptyList()
            }
        }
    }

    private fun createMockSearchSugResponse(): SearchSugResponse {
        return mockk<SearchSugResponse> {
            every { error } returns mockk {
                every { error_code } returns 0
                every { error_msg } returns "success"
            }
            every { data_ } returns mockk()
        }
    }

    // ========== searchThread Tests ==========

    @Test
    fun `searchThread should return success flow when API call succeeds`() = runTest {
        // Given: Mock API returns successful SearchThreadBean
        val keyword = "test"
        val page = 1
        val sortType = 0
        val expectedBean = createMockSearchThreadBean(0)

        every {
            mockApi.searchThreadFlow(keyword, page, sortType)
        } returns flowOf(expectedBean)

        // When: Call repository method
        val result = repository.searchThread(keyword, page, sortType).first()

        // Then: Verify the result matches expected data
        assertEquals(0, result.errorCode)
        assertNotNull(result.data)
        verify(exactly = 1) {
            mockApi.searchThreadFlow(keyword, page, sortType)
        }
    }

    @Test
    fun `searchThread should propagate error when API call fails`() = runTest {
        // Given: Mock API throws exception
        val keyword = "test"
        val page = 1
        val sortType = 0
        val expectedException = RuntimeException("Search failed")

        every {
            mockApi.searchThreadFlow(keyword, page, sortType)
        } returns flow { throw expectedException }

        // When & Then: Verify exception is propagated
        try {
            repository.searchThread(keyword, page, sortType).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Search failed", e.message)
        }
    }

    @Test
    fun `searchThread should handle empty keyword parameter`() = runTest {
        // Given: Empty keyword
        val keyword = ""
        val page = 1
        val sortType = 0
        val expectedBean = createMockSearchThreadBean()

        every {
            mockApi.searchThreadFlow(keyword, page, sortType)
        } returns flowOf(expectedBean)

        // When: Call repository with empty keyword
        val result = repository.searchThread(keyword, page, sortType).first()

        // Then: Verify API is called (parameter validation is API's responsibility)
        verify(exactly = 1) {
            mockApi.searchThreadFlow("", page, sortType)
        }
    }

    @Test
    fun `searchThread should handle page pagination correctly`() = runTest {
        // Given: Different page numbers
        val keyword = "test"
        val sortType = 0

        // Test page 1
        val page1Bean = createMockSearchThreadBean()
        every {
            mockApi.searchThreadFlow(keyword, 1, sortType)
        } returns flowOf(page1Bean)

        val result1 = repository.searchThread(keyword, 1, sortType).first()
        assertEquals(0, result1.errorCode)

        // Test page 2
        val page2Bean = createMockSearchThreadBean()
        every {
            mockApi.searchThreadFlow(keyword, 2, sortType)
        } returns flowOf(page2Bean)

        val result2 = repository.searchThread(keyword, 2, sortType).first()
        assertEquals(0, result2.errorCode)

        // Verify both pages were called
        verify(exactly = 1) { mockApi.searchThreadFlow(keyword, 1, sortType) }
        verify(exactly = 1) { mockApi.searchThreadFlow(keyword, 2, sortType) }
    }

    @Test
    fun `searchThread should handle different sort types`() = runTest {
        // Given: Different sort types
        val keyword = "test"
        val page = 1

        // Test sortType 0 (relevance)
        val relevanceBean = createMockSearchThreadBean()
        every {
            mockApi.searchThreadFlow(keyword, page, 0)
        } returns flowOf(relevanceBean)

        val resultRelevance = repository.searchThread(keyword, page, 0).first()
        assertEquals(0, resultRelevance.errorCode)

        // Test sortType 1 (time)
        val timeBean = createMockSearchThreadBean()
        every {
            mockApi.searchThreadFlow(keyword, page, 1)
        } returns flowOf(timeBean)

        val resultTime = repository.searchThread(keyword, page, 1).first()
        assertEquals(0, resultTime.errorCode)

        // Verify both sort types were called
        verify(exactly = 1) { mockApi.searchThreadFlow(keyword, page, 0) }
        verify(exactly = 1) { mockApi.searchThreadFlow(keyword, page, 1) }
    }

    // ========== searchSuggestions Tests ==========

    @Test
    fun `searchSuggestions should return success flow when API call succeeds`() = runTest {
        // Given: Mock API returns successful SearchSugResponse
        val keyword = "test"
        val expectedResponse = createMockSearchSugResponse()

        every {
            mockApi.searchSuggestionsFlow(keyword)
        } returns flowOf(expectedResponse)

        // When: Call repository method
        val result = repository.searchSuggestions(keyword).first()

        // Then: Verify the result matches expected data
        assertNotNull(result)
        assertNotNull(result.error)
        verify(exactly = 1) {
            mockApi.searchSuggestionsFlow(keyword)
        }
    }

    @Test
    fun `searchSuggestions should propagate error when API call fails`() = runTest {
        // Given: Mock API throws exception
        val keyword = "test"
        val expectedException = RuntimeException("Suggestions failed")

        every {
            mockApi.searchSuggestionsFlow(keyword)
        } returns flow { throw expectedException }

        // When & Then: Verify exception is propagated
        try {
            repository.searchSuggestions(keyword).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Suggestions failed", e.message)
        }
    }

    @Test
    fun `searchSuggestions should handle empty keyword parameter`() = runTest {
        // Given: Empty keyword
        val keyword = ""
        val expectedResponse = createMockSearchSugResponse()

        every {
            mockApi.searchSuggestionsFlow(keyword)
        } returns flowOf(expectedResponse)

        // When: Call repository with empty keyword
        val result = repository.searchSuggestions(keyword).first()

        // Then: Verify API is called (parameter validation is API's responsibility)
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.searchSuggestionsFlow("")
        }
    }

    @Test
    fun `searchSuggestions should handle blank keyword parameter`() = runTest {
        // Given: Blank keyword (spaces)
        val keyword = "   "
        val expectedResponse = createMockSearchSugResponse()

        every {
            mockApi.searchSuggestionsFlow(keyword)
        } returns flowOf(expectedResponse)

        // When: Call repository with blank keyword
        val result = repository.searchSuggestions(keyword).first()

        // Then: Verify API is called with the blank string as-is
        assertNotNull(result)
        verify(exactly = 1) {
            mockApi.searchSuggestionsFlow("   ")
        }
    }

    // ========== searchPost Tests ==========

    @Test
    fun `searchPost should return success flow when API call succeeds`() = runTest {
        // Given
        val keyword = "test"
        val forumName = "testForum"
        val forumId = 123L
        val expectedBean = createMockSearchThreadBean(0)

        every {
            mockApi.searchPostFlow(keyword, forumName, forumId, 1, 1, 1, 20)
        } returns flowOf(expectedBean)

        // When
        val result = repository.searchPost(keyword, forumName, forumId).first()

        // Then
        assertEquals(0, result.errorCode)
        verify(exactly = 1) {
            mockApi.searchPostFlow(keyword, forumName, forumId, 1, 1, 1, 20)
        }
    }

    @Test
    fun `searchPost should handle custom parameters`() = runTest {
        // Given
        val keyword = "test"
        val forumName = "testForum"
        val forumId = 123L
        val sortType = 2
        val filterType = 2
        val page = 2
        val pageSize = 30
        val expectedBean = createMockSearchThreadBean(0)

        every {
            mockApi.searchPostFlow(keyword, forumName, forumId, sortType, filterType, page, pageSize)
        } returns flowOf(expectedBean)

        // When
        val result = repository.searchPost(keyword, forumName, forumId, sortType, filterType, page, pageSize).first()

        // Then
        assertEquals(0, result.errorCode)
        verify(exactly = 1) {
            mockApi.searchPostFlow(keyword, forumName, forumId, sortType, filterType, page, pageSize)
        }
    }

    // ========== searchForum Tests ==========

    @Test
    fun `searchForum should return success flow when API call succeeds`() = runTest {
        // Given
        val keyword = "test"
        val expectedBean = mockk<SearchForumBean>(relaxed = true)

        every {
            mockApi.searchForumFlow(keyword)
        } returns flowOf(expectedBean)

        // When
        val result = repository.searchForum(keyword).first()

        // Then
        assertEquals(expectedBean, result)
        verify(exactly = 1) {
            mockApi.searchForumFlow(keyword)
        }
    }

    @Test
    fun `searchForum should propagate error when API call fails`() = runTest {
        // Given
        val keyword = "test"
        val expectedException = RuntimeException("Forum search failed")

        every {
            mockApi.searchForumFlow(keyword)
        } returns flow { throw expectedException }

        // When & Then
        try {
            repository.searchForum(keyword).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Forum search failed", e.message)
        }
    }

    // ========== searchUser Tests ==========

    @Test
    fun `searchUser should return success flow when API call succeeds`() = runTest {
        // Given
        val keyword = "test"
        val expectedBean = mockk<SearchUserBean>(relaxed = true)

        every {
            mockApi.searchUserFlow(keyword)
        } returns flowOf(expectedBean)

        // When
        val result = repository.searchUser(keyword).first()

        // Then
        assertEquals(expectedBean, result)
        verify(exactly = 1) {
            mockApi.searchUserFlow(keyword)
        }
    }

    @Test
    fun `searchUser should propagate error when API call fails`() = runTest {
        // Given
        val keyword = "test"
        val expectedException = RuntimeException("User search failed")

        every {
            mockApi.searchUserFlow(keyword)
        } returns flow { throw expectedException }

        // When & Then
        try {
            repository.searchUser(keyword).first()
            throw AssertionError("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("User search failed", e.message)
        }
    }
}
