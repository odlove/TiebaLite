package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.forum.ForumLikeResult
import com.huanchengfly.tieba.core.common.forum.ForumSignResult
import com.huanchengfly.tieba.core.network.model.CommonResponse
import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.models.mappers.toForumLikeResult
import com.huanchengfly.tieba.post.models.mappers.toForumSignResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 贴吧操作数据仓库实现
 */
@Singleton
class ForumOperationRepositoryImpl @Inject constructor(
    private val api: ITiebaApi
) : ForumOperationRepository {
    override fun sign(
        forumId: String,
        forumName: String,
        tbs: String
    ): Flow<ForumSignResult> =
        api.signFlow(forumId, forumName, tbs)
            .map { it.toForumSignResult() }

    override fun likeForum(
        forumId: String,
        forumName: String,
        tbs: String
    ): Flow<ForumLikeResult> =
        api.likeForumFlow(forumId, forumName, tbs)
            .map { it.toForumLikeResult() }

    override fun unlikeForum(
        forumId: String,
        forumName: String,
        tbs: String
    ): Flow<CommonResponse> =
        api.unlikeForumFlow(forumId, forumName, tbs)

}
