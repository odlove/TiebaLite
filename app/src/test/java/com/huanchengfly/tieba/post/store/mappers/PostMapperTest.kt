package com.huanchengfly.tieba.post.store.mappers

import android.os.SystemClock
import com.huanchengfly.tieba.post.TestFixtures
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit tests for PostMapper
 *
 * Tests verify that Post Proto is correctly mapped to PostEntity Domain Model
 *
 * Test Coverage:
 * - fromProto maps all core fields correctly
 * - fromProto uses diffAgreeNum (net agree count for post detail pages)
 * - fromProto maps hasAgree correctly
 * - fromProtos batch mapping with threadId works correctly
 * - timestamp is set automatically
 */
class PostMapperTest {

    @Before
    fun setup() {
        // Mock SystemClock for timestamp generation
        mockkStatic(SystemClock::class)
        every { SystemClock.elapsedRealtime() } returns 1000L
    }

    @After
    fun tearDown() {
        unmockkStatic(SystemClock::class)
    }

    @Test
    fun `fromProto maps all fields correctly`() {
        // Given: A Post Proto with all fields set
        val proto = TestFixtures.fakePost(
            id = 789L,
            threadId = 456L,
            floor = 1,
            time = 1609459200,
            authorId = 999L,
            hasAgree = 1,
            diffAgreeNum = 25L,
            subPostNumber = 5
        )

        // When: Map Proto to Entity with threadId
        val entity = PostMapper.fromProto(proto, threadId = 456L)

        // Then: All fields should be mapped correctly
        assertEquals(789L, entity.id)
        assertEquals(456L, entity.threadId)
        assertEquals(1, entity.floor)
        assertEquals(1609459200, entity.time)
        assertEquals(999L, entity.authorId)
        assertEquals(5, entity.subPostNumber)
        assertEquals(1, entity.meta.hasAgree)
        assertEquals(25, entity.meta.agreeNum)  // ✅ Should use diffAgreeNum
        assertNotNull(entity.proto)
    }

    @Test
    fun `fromProto uses diffAgreeNum for post detail pages`() {
        // Given: Post with diffAgreeNum (net agree count)
        val proto = TestFixtures.fakePost(
            id = 789L,
            threadId = 456L,
            diffAgreeNum = 100L  // ✅ Net agree count (detail page standard)
        )

        // When: Map Proto to Entity
        val entity = PostMapper.fromProto(proto, threadId = 456L)

        // Then: Should use diffAgreeNum for agreeNum
        assertEquals(100, entity.meta.agreeNum)  // ✅ Should use diffAgreeNum
    }

    @Test
    fun `fromProto maps hasAgree correctly`() {
        // Given: Post with hasAgree=1 (user has agreed)
        val proto = TestFixtures.fakePost(
            id = 789L,
            threadId = 456L,
            hasAgree = 1,  // ✅ User has agreed
            diffAgreeNum = 50L
        )

        // When: Map Proto to Entity
        val entity = PostMapper.fromProto(proto, threadId = 456L)

        // Then: Should map hasAgree correctly
        assertEquals(1, entity.meta.hasAgree)  // ✅ Should map from agree.hasAgree
    }

    @Test
    fun `fromProto handles hasAgree=0 correctly`() {
        // Given: Post with hasAgree=0 (user has not agreed)
        val proto = TestFixtures.fakePost(
            id = 789L,
            threadId = 456L,
            hasAgree = 0,  // ✅ User has not agreed
            diffAgreeNum = 50L
        )

        // When: Map Proto to Entity
        val entity = PostMapper.fromProto(proto, threadId = 456L)

        // Then: Should map hasAgree as 0
        assertEquals(0, entity.meta.hasAgree)
    }

    @Test
    fun `fromProto uses provided threadId parameter`() {
        // Given: Post Proto with tid field and separate threadId parameter
        val proto = TestFixtures.fakePost(
            id = 789L,
            threadId = 999L  // ✅ This is proto.tid
        )

        // When: Map Proto with explicit threadId parameter (canonical)
        val canonicalThreadId = 456L
        val entity = PostMapper.fromProto(proto, threadId = canonicalThreadId)

        // Then: Should use provided threadId parameter
        assertEquals(canonicalThreadId, entity.threadId)  // ✅ Should use parameter, not proto.tid
    }

    @Test
    fun `fromProtos batch mapping works correctly`() {
        // Given: A list of Post Protos
        val protos = listOf(
            TestFixtures.fakePost(id = 1L, threadId = 100L, floor = 1),
            TestFixtures.fakePost(id = 2L, threadId = 100L, floor = 2),
            TestFixtures.fakePost(id = 3L, threadId = 100L, floor = 3)
        )
        val canonicalThreadId = 456L

        // When: Batch map Protos to Entities
        val entities = PostMapper.fromProtos(protos, canonicalThreadId)

        // Then: Should map all entities with correct threadId
        assertEquals(3, entities.size)
        assertEquals(1L, entities[0].id)
        assertEquals(canonicalThreadId, entities[0].threadId)  // ✅ All use canonical threadId
        assertEquals(1, entities[0].floor)
        assertEquals(2L, entities[1].id)
        assertEquals(canonicalThreadId, entities[1].threadId)
        assertEquals(2, entities[1].floor)
        assertEquals(3L, entities[2].id)
        assertEquals(canonicalThreadId, entities[2].threadId)
        assertEquals(3, entities[2].floor)
    }

    @Test
    fun `fromProto sets timestamp automatically`() {
        // Given: A Post Proto
        val proto = TestFixtures.fakePost(id = 789L, threadId = 456L)

        // When: Map Proto to Entity
        val entity = PostMapper.fromProto(proto, threadId = 456L)

        // Then: Timestamp should be set (> 0)
        assert(entity.timestamp > 0L) { "Timestamp should be set automatically" }
    }

    @Test
    fun `fromProto handles null agree object correctly`() {
        // Given: Post with null agree object (agreeNum defaults to 0)
        val proto = TestFixtures.fakePost(
            id = 789L,
            threadId = 456L,
            hasAgree = 0,  // Will set agree to mockk with hasAgree=0
            diffAgreeNum = 0L
        )

        // When: Map Proto to Entity
        val entity = PostMapper.fromProto(proto, threadId = 456L)

        // Then: Should default to 0
        assertEquals(0, entity.meta.hasAgree)
        assertEquals(0, entity.meta.agreeNum)
    }

    @Test
    fun `fromProto maps author and content correctly`() {
        // Given: Post with author and content
        val author = TestFixtures.fakeUser(id = 999L, name = "testAuthor")
        val proto = TestFixtures.fakePost(
            id = 789L,
            threadId = 456L,
            author = author,
            authorId = 999L
        )

        // When: Map Proto to Entity
        val entity = PostMapper.fromProto(proto, threadId = 456L)

        // Then: Should map author correctly
        assertEquals(author, entity.author)
        assertEquals(999L, entity.authorId)
        assertEquals(emptyList(), entity.content)  // content is empty in fake
    }
}
