package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.models.AgreeBean
import com.huanchengfly.tieba.post.api.models.CommonResponse
import com.huanchengfly.tieba.post.models.DislikeBean
import kotlinx.coroutines.flow.Flow

/**
 * 用户交互操作数据仓库接口
 *
 * 负责处理用户与内容的交互操作，如点赞、不喜欢等
 */
interface UserInteractionRepository {
    /**
     * 点赞/取消点赞操作
     *
     * @param threadId 主题ID
     * @param postId 帖子ID
     * @param hasAgree 当前点赞状态（0-未点赞，1-已点赞）
     * @param objType 对象类型（1-回复，3-主题）
     * @return 点赞操作响应数据流
     */
    fun opAgree(
        threadId: String,
        postId: String,
        hasAgree: Int,
        objType: Int
    ): Flow<AgreeBean>

    /**
     * 提交不喜欢反馈
     *
     * @param dislikeBean 不喜欢反馈数据
     * @return 提交响应数据流
     */
    fun submitDislike(
        dislikeBean: DislikeBean
    ): Flow<CommonResponse>
}
