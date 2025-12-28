package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.forum.ForumDetailInfo
import com.huanchengfly.tieba.core.common.forum.ForumRuleDetail
import kotlinx.coroutines.flow.Flow

/**
 * 论坛信息 Repository 接口
 *
 * 提供论坛详情、规则等信息获取功能
 */
interface ForumInfoRepository {
    /**
     * 获取论坛详细信息
     *
     * @param forumId 论坛ID
     * @return Flow<ForumDetailInfo>
     */
    fun getForumDetail(forumId: Long): Flow<ForumDetailInfo>

    /**
     * 获取论坛规则详情
     *
     * @param forumId 论坛ID
     * @return Flow<ForumRuleDetail>
     */
    fun forumRuleDetail(forumId: Long): Flow<ForumRuleDetail>
}
