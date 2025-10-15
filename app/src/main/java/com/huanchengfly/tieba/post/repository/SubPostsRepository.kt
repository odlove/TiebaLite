package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.protos.pbFloor.PbFloorResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 楼中楼数据仓库实现
 */
@Singleton
class SubPostsRepositoryImpl @Inject constructor(
    private val api: ITiebaApi
) : SubPostsRepository {
    override fun pbFloor(
        threadId: Long,
        postId: Long,
        forumId: Long,
        page: Int,
        subPostId: Long
    ): Flow<PbFloorResponse> =
        api.pbFloorFlow(threadId, postId, forumId, page, subPostId)
}
