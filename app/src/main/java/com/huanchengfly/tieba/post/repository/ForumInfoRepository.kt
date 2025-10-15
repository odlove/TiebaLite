package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.protos.forumRuleDetail.ForumRuleDetailResponse
import com.huanchengfly.tieba.post.api.models.protos.getForumDetail.GetForumDetailResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 论坛信息 Repository 实现
 */
@Singleton
class ForumInfoRepositoryImpl @Inject constructor(
    private val api: ITiebaApi
) : ForumInfoRepository {
    override fun getForumDetail(forumId: Long): Flow<GetForumDetailResponse> =
        api.getForumDetailFlow(forumId)

    override fun forumRuleDetail(forumId: Long): Flow<ForumRuleDetailResponse> =
        api.forumRuleDetailFlow(forumId)
}
