package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.models.CommonResponse
import kotlinx.coroutines.flow.Flow

/**
 * 帖子操作数据仓库接口
 *
 * 包含帖子收藏和删除相关操作
 */
interface ThreadOperationRepository {
    /**
     * 添加/更新收藏
     *
     * @param threadId 帖子 ID
     * @param postId 收藏到的回复 ID
     * @return 操作结果 Flow
     */
    fun addStore(threadId: Long, postId: Long): Flow<CommonResponse>

    /**
     * 移除收藏
     *
     * @param threadId 帖子 ID
     * @param forumId 吧 ID
     * @param tbs 防伪参数
     * @return 操作结果 Flow
     */
    fun removeStore(threadId: Long, forumId: Long, tbs: String?): Flow<CommonResponse>

    /**
     * 移除收藏(简化版)
     *
     * @param threadId 帖子 ID (String 格式)
     * @return 操作结果 Flow
     */
    fun removeStore(threadId: String): Flow<CommonResponse>

    /**
     * 删除帖子中的回复
     *
     * @param forumId 吧 ID
     * @param forumName 吧名
     * @param threadId 帖子 ID
     * @param postId 回复 ID
     * @param tbs 防伪参数
     * @param isFloor 是否为楼层
     * @param delMyPost 是否只删除我的回复
     * @return 操作结果 Flow
     */
    fun delPost(
        forumId: Long,
        forumName: String,
        threadId: Long,
        postId: Long,
        tbs: String?,
        isFloor: Boolean = false,
        delMyPost: Boolean = true
    ): Flow<CommonResponse>

    /**
     * 删除帖子
     *
     * @param forumId 吧 ID
     * @param forumName 吧名
     * @param threadId 帖子 ID
     * @param tbs 防伪参数
     * @param delMyThread 是否只删除我的帖子
     * @param isHide 是否隐藏
     * @return 操作结果 Flow
     */
    fun delThread(
        forumId: Long,
        forumName: String,
        threadId: Long,
        tbs: String?,
        delMyThread: Boolean,
        isHide: Boolean
    ): Flow<CommonResponse>
}
