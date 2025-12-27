package com.huanchengfly.tieba.core.common.repository

import com.huanchengfly.tieba.core.common.forum.ForumRecommendResult
import kotlinx.coroutines.flow.Flow

interface ForumRecommendRepository {
    fun forumRecommend(): Flow<ForumRecommendResult>
}
