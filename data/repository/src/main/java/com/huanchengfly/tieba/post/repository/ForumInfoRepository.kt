package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.core.common.forum.ForumDetailInfo
import com.huanchengfly.tieba.core.common.forum.ForumRuleDetail
import com.huanchengfly.tieba.post.models.mappers.toForumDetailInfo
import com.huanchengfly.tieba.post.models.mappers.toForumRuleDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 论坛信息 Repository 实现
 */
@Singleton
class ForumInfoRepositoryImpl @Inject constructor(
    private val api: ITiebaApi
) : ForumInfoRepository {
    override fun getForumDetail(forumId: Long): Flow<ForumDetailInfo> =
        api.getForumDetailFlow(forumId).map { response ->
            val data = checkNotNull(response.data_) { "forum detail data is null" }
            checkNotNull(data.forum_info) { "forum detail info is null" }.toForumDetailInfo()
        }

    override fun forumRuleDetail(forumId: Long): Flow<ForumRuleDetail> =
        api.forumRuleDetailFlow(forumId).map { it.toForumRuleDetail() }
}
