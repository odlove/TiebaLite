package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.network.model.CommonResponse
import com.huanchengfly.tieba.post.api.models.FollowBean
import com.huanchengfly.tieba.post.api.models.UserLikeForumBean
import kotlinx.coroutines.flow.Flow

/**
 * 用户社交数据仓库接口
 *
 * 负责处理用户关注/取关及喜欢的吧列表
 */
interface UserSocialRepository {
    /**
     * 关注用户
     *
     * @param portrait 用户头像标识
     * @param tbs 防伪参数
     * @return 关注结果数据流
     */
    fun follow(portrait: String, tbs: String): Flow<FollowBean>

    /**
     * 取关用户
     *
     * @param portrait 用户头像标识
     * @param tbs 防伪参数
     * @return 操作结果数据流
     */
    fun unfollow(portrait: String, tbs: String): Flow<CommonResponse>

    /**
     * 查看用户关注的吧列表
     *
     * @param uid 用户 ID
     * @param page 分页页码（从 1 开始）
     * @return 关注的吧列表数据流
     */
    fun userLikeForum(uid: String, page: Int = 1): Flow<UserLikeForumBean>
}
