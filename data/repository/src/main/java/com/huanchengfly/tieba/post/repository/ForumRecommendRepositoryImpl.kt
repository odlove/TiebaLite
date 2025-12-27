package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.forum.ForumRecommendItem
import com.huanchengfly.tieba.core.common.forum.ForumRecommendResult
import com.huanchengfly.tieba.core.common.repository.ForumRecommendRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ForumRecommendRepositoryImpl @Inject constructor(
    private val contentRecommendRepository: ContentRecommendRepository
) : ForumRecommendRepository {
    override fun forumRecommend(): Flow<ForumRecommendResult> =
        contentRecommendRepository.forumRecommend().map { response ->
            val forums = response.data_?.like_forum?.map {
                ForumRecommendItem(
                    avatar = it.avatar,
                    forumId = it.forum_id.toString(),
                    forumName = it.forum_name,
                    isSign = it.is_sign == 1,
                    levelId = it.level_id.toString()
                )
            } ?: emptyList()
            ForumRecommendResult(forums = forums)
        }
}
