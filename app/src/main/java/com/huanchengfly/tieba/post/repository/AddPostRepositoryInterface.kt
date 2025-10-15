package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.models.protos.addPost.AddPostResponse
import kotlinx.coroutines.flow.Flow

/**
 * 发帖/回帖数据仓库接口
 */
interface AddPostRepository {
    /**
     * 发布帖子或回复
     *
     * @param content 帖子内容
     * @param forumId 贴吧ID
     * @param forumName 贴吧名称
     * @param threadId 主题ID
     * @param tbs 防伪参数
     * @param nameShow 显示名称
     * @param postId 回复的帖子ID（用于回帖）
     * @param subPostId 回复的楼中楼ID（用于楼中楼回复）
     * @param replyUserId 被回复用户的ID
     * @return 发帖响应数据流
     */
    fun addPost(
        content: String,
        forumId: Long,
        forumName: String,
        threadId: Long,
        tbs: String? = null,
        nameShow: String? = null,
        postId: Long? = null,
        subPostId: Long? = null,
        replyUserId: Long? = null
    ): Flow<AddPostResponse>
}
