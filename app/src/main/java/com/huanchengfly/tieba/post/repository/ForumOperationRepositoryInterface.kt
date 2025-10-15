package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.models.CommonResponse
import com.huanchengfly.tieba.post.api.models.LikeForumResultBean
import com.huanchengfly.tieba.post.api.models.SignResultBean
import com.huanchengfly.tieba.post.api.models.protos.userLike.UserLikeResponse
import kotlinx.coroutines.flow.Flow

/**
 * 贴吧操作数据仓库接口
 *
 * 负责处理贴吧相关的操作，如签到、关注、取消关注等
 */
interface ForumOperationRepository {
    /**
     * 贴吧签到
     *
     * @param forumId 贴吧ID
     * @param forumName 贴吧名称
     * @param tbs 防伪参数
     * @return 签到响应数据流
     */
    fun sign(
        forumId: String,
        forumName: String,
        tbs: String
    ): Flow<SignResultBean>

    /**
     * 关注贴吧
     *
     * @param forumId 贴吧ID
     * @param forumName 贴吧名称
     * @param tbs 防伪参数
     * @return 关注响应数据流
     */
    fun likeForum(
        forumId: String,
        forumName: String,
        tbs: String
    ): Flow<LikeForumResultBean>

    /**
     * 取消关注贴吧
     *
     * @param forumId 贴吧ID
     * @param forumName 贴吧名称
     * @param tbs 防伪参数
     * @return 取消关注响应数据流
     */
    fun unlikeForum(
        forumId: String,
        forumName: String,
        tbs: String
    ): Flow<CommonResponse>

    /**
     * 获取用户关注的贴吧列表
     *
     * @param pageTag 分页标识（首次为空字符串）
     * @param lastRequestUnix 上次请求时间戳
     * @param loadType 加载类型（1-刷新，2-加载更多）
     * @return 用户关注列表数据流
     */
    fun userLike(
        pageTag: String,
        lastRequestUnix: Long,
        loadType: Int
    ): Flow<UserLikeResponse>
}
