package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.models.AgreeBean
import com.huanchengfly.tieba.core.network.model.CommonResponse
import com.huanchengfly.tieba.post.models.DislikeBean
import kotlinx.coroutines.flow.Flow

/**
 * 用户交互操作数据仓库接口
 *
 * 负责处理用户与内容的交互操作，如点赞、不喜欢等
 */
interface UserInteractionRepository {
    /**
     * 点赞/取消点赞操作（切换点赞状态）
     *
     * 该操作会切换当前的点赞状态：
     * - 如果当前未点赞（hasAgree=0），操作后变为已点赞（1）
     * - 如果当前已点赞（hasAgree=1），操作后变为未点赞（0）
     *
     * **使用规范：**
     * - UI 层使用 Boolean 表示状态，调用时需转换为 Int
     * - ViewModel 中使用 `hasAgree xor 1` 计算操作后的新状态
     * - 建议在 `.onStart {}` 中实现乐观更新，在 `.catch {}` 中实现回滚
     *
     * @param threadId 主题ID（String 格式）
     * @param postId 帖子ID（String 格式）
     * @param hasAgree 当前点赞状态（0=未点赞，1=已点赞）**注意：传入当前状态，API 会自动切换**
     * @param objType 对象类型：1=Post(楼层), 2=SubPost(楼中楼), 3=Thread(主题)
     * @return 点赞操作响应数据流
     *
     * @see com.huanchengfly.tieba.post.api.models.AgreeBean
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
