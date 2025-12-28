package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.forum.ForumRecommendResult
import com.huanchengfly.tieba.core.common.repository.ForumRecommendRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class ForumRecommendRepositoryImpl @Inject constructor(
    private val contentRecommendRepository: ContentRecommendRepository
) : ForumRecommendRepository {
    override fun forumRecommend(): Flow<ForumRecommendResult> =
        contentRecommendRepository.forumRecommend()
}
