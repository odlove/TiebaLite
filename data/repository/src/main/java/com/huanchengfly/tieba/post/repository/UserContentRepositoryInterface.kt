package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.models.protos.userPost.UserPostResponse
import kotlinx.coroutines.flow.Flow

/**
 * 用户内容数据仓库接口
 *
 * 负责处理用户发布的主题贴和回复内容
 */
interface UserContentRepository {
    /**
     * 查看用户的所有主题贴/回复
     *
     * @param uid 用户 ID
     * @param page 分页页码（从 1 开始）
     * @param isThread 是否查看主题贴（true = 主题贴，false = 回复）
     * @return 用户帖子数据流
     */
    fun userPost(
        uid: Long,
        page: Int = 1,
        isThread: Boolean = true
    ): Flow<UserPostResponse>
}
