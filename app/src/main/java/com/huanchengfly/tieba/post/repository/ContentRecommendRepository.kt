package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.protos.topicList.TopicListResponse
import com.huanchengfly.tieba.post.api.models.protos.hotThreadList.HotThreadListResponse
import com.huanchengfly.tieba.post.api.models.protos.forumRecommend.ForumRecommendResponse
import kotlinx.coroutines.flow.Flow
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

    override fun forumRecommend(): Flow<ForumRecommendResponse> =
        api.forumRecommendNewFlow()

    override fun topicList(): Flow<TopicListResponse> =
        api.topicListFlow()
}
