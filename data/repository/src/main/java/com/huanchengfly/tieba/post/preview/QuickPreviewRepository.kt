package com.huanchengfly.tieba.post.preview

import kotlinx.coroutines.flow.Flow

interface QuickPreviewRepository {
    fun observeThread(threadId: Long): Flow<ThreadPreviewData>
    fun observeForum(forumName: String, sortType: Int): Flow<ForumPreviewData>

    suspend fun fetchThread(threadId: Long): ThreadPreviewData
    suspend fun fetchForum(forumName: String): ForumPreviewData
}
