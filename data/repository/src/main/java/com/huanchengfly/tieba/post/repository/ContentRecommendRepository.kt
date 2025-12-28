package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.forum.ForumRecommendResult
import com.huanchengfly.tieba.core.common.hottopic.HotTopicItem
import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.protos.hotThreadList.HotThreadListResponse
import com.huanchengfly.tieba.post.models.mappers.toForumRecommendResult
import com.huanchengfly.tieba.post.models.mappers.toHotTopicItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 内容推荐数据仓库实现
 */
@Singleton
class ContentRecommendRepositoryImpl @Inject constructor(
    private val api: ITiebaApi
) : ContentRecommendRepository {
    override fun hotThreadList(
        tabCode: String
    ): Flow<HotThreadListResponse> =
        api.hotThreadListFlow(tabCode)

    override fun forumRecommend(): Flow<ForumRecommendResult> =
        api.forumRecommendNewFlow()
            .map { it.toForumRecommendResult() }

    override fun topicList(): Flow<List<HotTopicItem>> =
        api.topicListFlow()
            .map { it.toHotTopicItems() }
}
