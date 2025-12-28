package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.core.network.model.CommonResponse
import com.huanchengfly.tieba.post.models.DislikeBean
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 用户交互操作数据仓库实现
 */
@Singleton
class UserInteractionRepositoryImpl @Inject constructor(
    private val api: ITiebaApi
) : UserInteractionRepository {
    override fun opAgree(
        threadId: String,
        postId: String,
        hasAgree: Int,
        objType: Int
    ): Flow<Unit> =
        api.opAgreeFlow(threadId, postId, hasAgree, objType).map { }

    override fun submitDislike(
        dislikeBean: DislikeBean
    ): Flow<CommonResponse> =
        api.submitDislikeFlow(dislikeBean)
}
