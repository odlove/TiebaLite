package com.huanchengfly.tieba.post.ui.page.subposts

import com.huanchengfly.tieba.post.TestFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for SubPostsUtils.kt
 *
 * Tests the buildReplyArgs function which validates forum info and constructs ReplyArgs
 */
class SubPostsUtilsTest {

    /**
     * Test Case 1: buildReplyArgs should return null when forumId is 0
     *
     * Validates that the function returns null when both API and fallback forumId are 0
     */
    @Test
    fun `buildReplyArgs should return null when forumId is 0`() {
        // Given: forumId from API is null and fallback is 0
        val forumIdFromApi: Long? = null
        val fallbackForumId = 0L
        val forumNameFromApi = "test_forum"
        val threadId = 123456L
        val postIdFromApi = 789012L
        val fallbackPostId = 111111L

        // When: building reply args
        val result = buildReplyArgs(
            forumIdFromApi = forumIdFromApi,
            fallbackForumId = fallbackForumId,
            forumNameFromApi = forumNameFromApi,
            threadId = threadId,
            postIdFromApi = postIdFromApi,
            fallbackPostId = fallbackPostId
        )

        // Then: result should be null
        assertNull("buildReplyArgs should return null when forumId is 0", result)
    }

    /**
     * Test Case 2: buildReplyArgs should return null when forumName is null
     *
     * Validates that the function returns null when forumName is null
     */
    @Test
    fun `buildReplyArgs should return null when forumName is null`() {
        // Given: forumName is null
        val forumIdFromApi = 123L
        val fallbackForumId = 456L
        val forumNameFromApi: String? = null
        val threadId = 123456L
        val postIdFromApi = 789012L
        val fallbackPostId = 111111L

        // When: building reply args
        val result = buildReplyArgs(
            forumIdFromApi = forumIdFromApi,
            fallbackForumId = fallbackForumId,
            forumNameFromApi = forumNameFromApi,
            threadId = threadId,
            postIdFromApi = postIdFromApi,
            fallbackPostId = fallbackPostId
        )

        // Then: result should be null
        assertNull("buildReplyArgs should return null when forumName is null", result)
    }

    /**
     * Test Case 3: buildReplyArgs should return null when forumName is empty
     *
     * Validates that the function returns null when forumName is an empty string
     */
    @Test
    fun `buildReplyArgs should return null when forumName is empty`() {
        // Given: forumName is an empty string
        val forumIdFromApi = 123L
        val fallbackForumId = 456L
        val forumNameFromApi = ""
        val threadId = 123456L
        val postIdFromApi = 789012L
        val fallbackPostId = 111111L

        // When: building reply args
        val result = buildReplyArgs(
            forumIdFromApi = forumIdFromApi,
            fallbackForumId = fallbackForumId,
            forumNameFromApi = forumNameFromApi,
            threadId = threadId,
            postIdFromApi = postIdFromApi,
            fallbackPostId = fallbackPostId
        )

        // Then: result should be null
        assertNull("buildReplyArgs should return null when forumName is empty", result)
    }

    /**
     * Test Case 4: buildReplyArgs should prefer API data over fallback
     *
     * Validates that when both API and fallback data are available, API data is used
     */
    @Test
    fun `buildReplyArgs should prefer API data over fallback`() {
        // Given: Both API and fallback data are available
        val forumIdFromApi = 999L
        val fallbackForumId = 111L
        val forumNameFromApi = "api_forum"
        val threadId = 123456L
        val postIdFromApi = 888L
        val fallbackPostId = 222L

        // When: building reply args
        val result = buildReplyArgs(
            forumIdFromApi = forumIdFromApi,
            fallbackForumId = fallbackForumId,
            forumNameFromApi = forumNameFromApi,
            threadId = threadId,
            postIdFromApi = postIdFromApi,
            fallbackPostId = fallbackPostId
        )

        // Then: result should use API data
        assertNotNull("buildReplyArgs should return non-null result", result)
        assertEquals("forumId should be from API", forumIdFromApi, result?.forumId)
        assertEquals("forumName should be from API", forumNameFromApi, result?.forumName)
        assertEquals("postId should be from API", postIdFromApi, result?.postId)
        assertEquals("threadId should match", threadId, result?.threadId)
    }

    /**
     * Test Case 5: buildReplyArgs should use fallback when API data is null
     *
     * Validates that when API data is null, fallback data is used
     */
    @Test
    fun `buildReplyArgs should use fallback when API data is null`() {
        // Given: API data is null, fallback data is available
        val forumIdFromApi: Long? = null
        val fallbackForumId = 111L
        val forumNameFromApi = "test_forum"
        val threadId = 123456L
        val postIdFromApi: Long? = null
        val fallbackPostId = 222L

        // When: building reply args
        val result = buildReplyArgs(
            forumIdFromApi = forumIdFromApi,
            fallbackForumId = fallbackForumId,
            forumNameFromApi = forumNameFromApi,
            threadId = threadId,
            postIdFromApi = postIdFromApi,
            fallbackPostId = fallbackPostId
        )

        // Then: result should use fallback data
        assertNotNull("buildReplyArgs should return non-null result", result)
        assertEquals("forumId should be from fallback", fallbackForumId, result?.forumId)
        assertEquals("postId should be from fallback", fallbackPostId, result?.postId)
        assertEquals("threadId should match", threadId, result?.threadId)
    }

    /**
     * Test Case 6: buildReplyArgs should include subPost info when provided
     *
     * Validates that when a targetSubPost is provided, its information is included in ReplyArgs
     */
    @Test
    fun `buildReplyArgs should include subPost info when provided`() {
        // Given: A target sub-post with author info
        val subPostAuthor = TestFixtures.fakeUser(
            id = 555L,
            name = "subPostAuthor",
            nameShow = "Sub Post Author",
            portrait = "sub_portrait"
        )
        val targetSubPost = TestFixtures.fakeSubPostList(
            id = 777L,
            authorId = 555L,
            author = subPostAuthor
        )

        val forumIdFromApi = 123L
        val fallbackForumId = 456L
        val forumNameFromApi = "test_forum"
        val threadId = 123456L
        val postIdFromApi = 789012L
        val fallbackPostId = 111111L

        // When: building reply args with targetSubPost
        val result = buildReplyArgs(
            forumIdFromApi = forumIdFromApi,
            fallbackForumId = fallbackForumId,
            forumNameFromApi = forumNameFromApi,
            threadId = threadId,
            postIdFromApi = postIdFromApi,
            fallbackPostId = fallbackPostId,
            targetSubPost = targetSubPost
        )

        // Then: result should include subPost info
        assertNotNull("buildReplyArgs should return non-null result", result)
        assertEquals("subPostId should match", 777L, result?.subPostId)
        assertEquals("replyUserId should be from subPost author", 555L, result?.replyUserId)
        assertEquals("replyUserName should be from subPost author", "Sub Post Author", result?.replyUserName)
        assertEquals("replyUserPortrait should be from subPost author", "sub_portrait", result?.replyUserPortrait)
    }

    /**
     * Test Case 7: buildReplyArgs should use replyUser when both replyUser and targetSubPost are provided
     *
     * Validates that replyUser takes priority over targetSubPost author info
     */
    @Test
    fun `buildReplyArgs should use replyUser when both replyUser and targetSubPost are provided`() {
        // Given: Both replyUser and targetSubPost are provided
        val replyUser = TestFixtures.fakeUser(
            id = 999L,
            name = "replyUser",
            nameShow = "Reply User",
            portrait = "reply_portrait"
        )

        val subPostAuthor = TestFixtures.fakeUser(
            id = 555L,
            name = "subPostAuthor",
            nameShow = "Sub Post Author",
            portrait = "sub_portrait"
        )
        val targetSubPost = TestFixtures.fakeSubPostList(
            id = 777L,
            authorId = 555L,
            author = subPostAuthor
        )

        val forumIdFromApi = 123L
        val fallbackForumId = 456L
        val forumNameFromApi = "test_forum"
        val threadId = 123456L
        val postIdFromApi = 789012L
        val fallbackPostId = 111111L

        // When: building reply args with both replyUser and targetSubPost
        val result = buildReplyArgs(
            forumIdFromApi = forumIdFromApi,
            fallbackForumId = fallbackForumId,
            forumNameFromApi = forumNameFromApi,
            threadId = threadId,
            postIdFromApi = postIdFromApi,
            fallbackPostId = fallbackPostId,
            targetSubPost = targetSubPost,
            replyUser = replyUser
        )

        // Then: result should prefer replyUser info
        assertNotNull("buildReplyArgs should return non-null result", result)
        assertEquals("subPostId should still be from targetSubPost", 777L, result?.subPostId)
        assertEquals("replyUserId should be from replyUser", 999L, result?.replyUserId)
        assertEquals("replyUserName should be from replyUser", "Reply User", result?.replyUserName)
        assertEquals("replyUserPortrait should be from replyUser", "reply_portrait", result?.replyUserPortrait)
    }

    /**
     * Test Case 8: buildReplyArgs should use postAuthorIdFallback when replyUser is null
     *
     * This test validates the critical fallback scenario for anonymous/blocked users:
     * When replying to the main post where post.author is null (PbFloor frequently omits
     * the nested author object for anonymous/blocked users), the function should fall back
     * to post.author_id which is always populated.
     */
    @Test
    fun `buildReplyArgs should use postAuthorIdFallback when replyUser is null`() {
        // Given: replyUser is null (anonymous/blocked user) and postAuthorIdFallback present
        val forumIdFromApi = 123L
        val fallbackForumId = 456L
        val forumNameFromApi = "test_forum"
        val threadId = 123456L
        val postIdFromApi = 789012L
        val fallbackPostId = 111111L
        val postAuthorIdFallback = 888L  // This is post.author_id from PbFloor response

        // When: building reply args with replyUser = null but postAuthorIdFallback present
        val result = buildReplyArgs(
            forumIdFromApi = forumIdFromApi,
            fallbackForumId = fallbackForumId,
            forumNameFromApi = forumNameFromApi,
            threadId = threadId,
            postIdFromApi = postIdFromApi,
            fallbackPostId = fallbackPostId,
            replyUser = null,
            postAuthorIdFallback = postAuthorIdFallback
        )

        // Then: result should use postAuthorIdFallback for replyUserId
        assertNotNull("buildReplyArgs should return non-null result", result)
        assertEquals("replyUserId should fall back to postAuthorIdFallback", postAuthorIdFallback, result?.replyUserId)
        assertNull("replyUserName should be null when replyUser is null", result?.replyUserName)
        assertNull("replyUserPortrait should be null when replyUser is null", result?.replyUserPortrait)
    }

    /**
     * Test Case 9: buildReplyArgs should prefer replyUser.id over postAuthorIdFallback when both present
     *
     * Validates that postAuthorIdFallback is truly a fallback and doesn't override replyUser.id
     */
    @Test
    fun `buildReplyArgs should prefer replyUser id over postAuthorIdFallback when both present`() {
        // Given: replyUser with valid id and postAuthorIdFallback both present
        val replyUser = TestFixtures.fakeUser(
            id = 999L,
            name = "normalUser",
            nameShow = "Normal User",
            portrait = "normal_portrait"
        )

        val forumIdFromApi = 123L
        val fallbackForumId = 456L
        val forumNameFromApi = "test_forum"
        val threadId = 123456L
        val postIdFromApi = 789012L
        val fallbackPostId = 111111L
        val postAuthorIdFallback = 888L

        // When: building reply args with both replyUser.id and postAuthorIdFallback
        val result = buildReplyArgs(
            forumIdFromApi = forumIdFromApi,
            fallbackForumId = fallbackForumId,
            forumNameFromApi = forumNameFromApi,
            threadId = threadId,
            postIdFromApi = postIdFromApi,
            fallbackPostId = fallbackPostId,
            replyUser = replyUser,
            postAuthorIdFallback = postAuthorIdFallback
        )

        // Then: result should prefer replyUser.id
        assertNotNull("buildReplyArgs should return non-null result", result)
        assertEquals("replyUserId should be from replyUser, not fallback", 999L, result?.replyUserId)
        assertEquals("replyUserName should be from replyUser", "Normal User", result?.replyUserName)
    }

    /**
     * Test Case 10: buildReplyArgs should fall back to targetSubPost.author_id when replyUser and postAuthorIdFallback are null
     *
     * Validates the complete fallback chain: replyUser.id -> postAuthorIdFallback -> targetSubPost.author_id
     */
    @Test
    fun `buildReplyArgs should use targetSubPost author_id when replyUser and postAuthorIdFallback are null`() {
        // Given: targetSubPost with author_id but replyUser and postAuthorIdFallback are null
        val targetSubPost = TestFixtures.fakeSubPostList(
            id = 777L,
            authorId = 666L,  // author_id is present
            author = null  // But author object is null
        )

        val forumIdFromApi = 123L
        val fallbackForumId = 456L
        val forumNameFromApi = "test_forum"
        val threadId = 123456L
        val postIdFromApi = 789012L
        val fallbackPostId = 111111L

        // When: building reply args with targetSubPost but no replyUser or postAuthorIdFallback
        val result = buildReplyArgs(
            forumIdFromApi = forumIdFromApi,
            fallbackForumId = fallbackForumId,
            forumNameFromApi = forumNameFromApi,
            threadId = threadId,
            postIdFromApi = postIdFromApi,
            fallbackPostId = fallbackPostId,
            targetSubPost = targetSubPost,
            postAuthorIdFallback = null
        )

        // Then: result should use targetSubPost.author_id
        assertNotNull("buildReplyArgs should return non-null result", result)
        assertEquals("replyUserId should fall back to targetSubPost.author_id", 666L, result?.replyUserId)
    }
}
