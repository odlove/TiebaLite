package com.huanchengfly.tieba.post.components

import com.huanchengfly.tieba.core.runtime.preview.ForumLink
import com.huanchengfly.tieba.core.runtime.preview.QuickPreviewService
import com.huanchengfly.tieba.core.runtime.preview.SimpleLink
import com.huanchengfly.tieba.core.runtime.preview.ThreadLink
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClipBoardLinkDetectorTest {

    @Test
    fun `SimpleLink stores url`() {
        val url = "https://tieba.baidu.com/p/123456"
        val link = SimpleLink(url)
        assertEquals(url, link.url)
    }

    @Test
    fun `ForumLink stores forum name`() {
        val url = "https://tieba.baidu.com/f?kw=test"
        val forumName = "test"
        val link = ForumLink(url, forumName)
        assertEquals(url, link.url)
        assertEquals(forumName, link.forumName)
    }

    @Test
    fun `ThreadLink stores thread id`() {
        val url = "https://tieba.baidu.com/p/123456"
        val threadId = 123456L
        val link = ThreadLink(url, threadId)
        assertEquals(url, link.url)
        assertEquals(threadId, link.threadId)
    }

    @Test
    fun `Preview state flow starts null`() {
        val detector = ClipBoardLinkDetector(FakeQuickPreviewService())
        assertNull(detector.previewInfoStateFlow.value)
    }

    private class FakeQuickPreviewService : QuickPreviewService {
        override fun getPreviewFlow(context: android.content.Context, link: com.huanchengfly.tieba.core.runtime.preview.ClipBoardLink, lifecycle: androidx.lifecycle.Lifecycle?): Flow<com.huanchengfly.tieba.core.runtime.preview.PreviewInfo?> = emptyFlow()

        override fun fetchPreview(
            context: android.content.Context,
            link: com.huanchengfly.tieba.core.runtime.preview.ClipBoardLink,
            callback: (Result<com.huanchengfly.tieba.core.runtime.preview.PreviewInfo>) -> Unit
        ) {
            callback(Result.failure(UnsupportedOperationException("Not used")))
        }
    }
}
