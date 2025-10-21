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
 * Unit tests for ThreadMapper
 *
 * Tests verify that ThreadInfo Proto is correctly mapped to ThreadEntity Domain Model
 *
 * Test Coverage:
 * - fromProto maps all core fields correctly
 * - fromProto uses fallback threadId when threadId=0
 * - fromProto maps agreeNum from top-level field (list page standard)
 * - fromProto maps hasAgree from agree object
 * - fromProtos batch mapping works correctly
 * - timestamp is set automatically
 */
class ThreadMapperTest {

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
        // Given: A ThreadInfo Proto with all fields set
        val proto = TestFixtures.fakeThreadInfo(
            id = 123L,
            threadId = 456L,
            firstPostId = 789L,
            title = "Test Title",
            replyNum = 100,
            viewNum = 1000,
            createTime = 1609459200,
            lastTimeInt = 1609545600,
            isTop = 1,
            isGood = 0,
            isDeleted = 0,
            authorId = 999L,
            forumId = 888L,
            forumName = "Test Forum",
            agreeNum = 50,
            hasAgree = 1,
            collectStatus = 1,
            collectMarkPid = "12345"
        )

        // When: Map Proto to Entity
        val entity = ThreadMapper.fromProto(proto)

        // Then: All fields should be mapped correctly
        assertEquals(123L, entity.threadId)
        assertEquals(789L, entity.firstPostId)
        assertEquals("Test Title", entity.title)
        assertEquals(100, entity.replyNum)
        assertEquals(1000, entity.viewNum)
        assertEquals(1609459200, entity.createTime)
        assertEquals(1609545600, entity.lastTimeInt)
        assertEquals(1, entity.isTop)
        assertEquals(0, entity.isGood)
        assertEquals(0, entity.isDeleted)
        assertEquals(999L, entity.authorId)
        assertEquals(888L, entity.forumId)
        assertEquals("Test Forum", entity.forumName)
        assertEquals(1, entity.meta.hasAgree)
        assertEquals(50, entity.meta.agreeNum)
        assertEquals(1, entity.meta.collectStatus)
        assertEquals(12345L, entity.meta.collectMarkPid)
        assertNotNull(entity.proto)
    }

    @Test
    fun `fromProto always uses proto id as threadId`() {
        // Given: ThreadInfo with id field (standard case)
        val proto = TestFixtures.fakeThreadInfo(
            id = 123L,
            threadId = 0L
        )

        // When: Map Proto to Entity
        val entity = ThreadMapper.fromProto(proto)

        // Then: Should use proto.id as threadId
        assertEquals(123L, entity.threadId)
    }

    @Test
    fun `fromProto ignores proto threadId field`() {
        // Given: ThreadInfo with both id and threadId fields
        val proto = TestFixtures.fakeThreadInfo(
            id = 123L,
            threadId = 456L  // This field is ignored
        )

        // When: Map Proto to Entity
        val entity = ThreadMapper.fromProto(proto)

        // Then: Should always use proto.id, not proto.threadId
        assertEquals(123L, entity.threadId)  // ✅ Uses proto.id, not proto.threadId
    }

    @Test
    fun `fromProto maps agreeNum from top-level field`() {
        // Given: ThreadInfo with top-level agreeNum (list page standard)
        val proto = TestFixtures.fakeThreadInfo(
            agreeNum = 100,  // ✅ Top-level field used in list pages
            hasAgree = 1
        )

        // When: Map Proto to Entity
        val entity = ThreadMapper.fromProto(proto)

        // Then: Should use top-level agreeNum
        assertEquals(100, entity.meta.agreeNum)  // ✅ Should use top-level agreeNum
    }

    @Test
    fun `fromProto maps hasAgree from agree object`() {
        // Given: ThreadInfo with agree object
        val proto = TestFixtures.fakeThreadInfo(
            hasAgree = 1,  // ✅ User has agreed
            agreeNum = 50
        )

        // When: Map Proto to Entity
        val entity = ThreadMapper.fromProto(proto)

        // Then: Should map hasAgree correctly
        assertEquals(1, entity.meta.hasAgree)  // ✅ Should map from agree.hasAgree
    }

    @Test
    fun `fromProto maps collectStatus and collectMarkPid correctly`() {
        // Given: ThreadInfo with collect fields
        val proto = TestFixtures.fakeThreadInfo(
            collectStatus = 1,
            collectMarkPid = "67890"
        )

        // When: Map Proto to Entity
        val entity = ThreadMapper.fromProto(proto)

        // Then: Should map collect fields correctly
        assertEquals(1, entity.meta.collectStatus)
        assertEquals(67890L, entity.meta.collectMarkPid)
    }

    @Test
    fun `fromProto handles empty collectMarkPid`() {
        // Given: ThreadInfo with empty collectMarkPid
        val proto = TestFixtures.fakeThreadInfo(
            collectStatus = 0,
            collectMarkPid = ""  // ✅ Empty string
        )

        // When: Map Proto to Entity
        val entity = ThreadMapper.fromProto(proto)

        // Then: Should default to 0
        assertEquals(0, entity.meta.collectStatus)
        assertEquals(0L, entity.meta.collectMarkPid)  // ✅ Should parse as 0
    }

    @Test
    fun `fromProtos batch mapping works correctly`() {
        // Given: A list of ThreadInfo Protos
        val protos = listOf(
            TestFixtures.fakeThreadInfo(id = 1L, threadId = 1L, title = "Thread 1"),
            TestFixtures.fakeThreadInfo(id = 2L, threadId = 2L, title = "Thread 2"),
            TestFixtures.fakeThreadInfo(id = 3L, threadId = 3L, title = "Thread 3")
        )

        // When: Batch map Protos to Entities
        val entities = ThreadMapper.fromProtos(protos)

        // Then: Should map all entities correctly
        assertEquals(3, entities.size)
        assertEquals(1L, entities[0].threadId)
        assertEquals("Thread 1", entities[0].title)
        assertEquals(2L, entities[1].threadId)
        assertEquals("Thread 2", entities[1].title)
        assertEquals(3L, entities[2].threadId)
        assertEquals("Thread 3", entities[2].title)
    }

    @Test
    fun `fromProto sets timestamp automatically`() {
        // Given: A ThreadInfo Proto
        val proto = TestFixtures.fakeThreadInfo(id = 123L, threadId = 456L)

        // When: Map Proto to Entity
        val entity = ThreadMapper.fromProto(proto)

        // Then: Timestamp should be set (> 0)
        assert(entity.timestamp > 0L) { "Timestamp should be set automatically" }
    }
}
