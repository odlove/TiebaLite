package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.CommonResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 帖子操作数据仓库实现
 */
@Singleton
class ThreadOperationRepositoryImpl @Inject constructor(
    private val api: ITiebaApi
) : ThreadOperationRepository {
    override fun addStore(threadId: Long, postId: Long): Flow<CommonResponse> =
        api.addStoreFlow(threadId, postId)

    override fun removeStore(threadId: Long, forumId: Long, tbs: String?): Flow<CommonResponse> =
        api.removeStoreFlow(threadId, forumId, tbs)

    override fun delPost(
        forumId: Long,
        forumName: String,
        threadId: Long,
        postId: Long,
        tbs: String?,
        isFloor: Boolean,
        delMyPost: Boolean
    ): Flow<CommonResponse> =
        api.delPostFlow(forumId, forumName, threadId, postId, tbs, isFloor, delMyPost)

    override fun delThread(
        forumId: Long,
        forumName: String,
        threadId: Long,
        tbs: String?,
        delMyThread: Boolean,
        isHide: Boolean
    ): Flow<CommonResponse> =
        api.delThreadFlow(forumId, forumName, threadId, tbs, delMyThread, isHide)
}
