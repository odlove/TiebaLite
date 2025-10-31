package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.core.network.model.CommonResponse
import com.huanchengfly.tieba.post.api.models.LikeForumResultBean
import com.huanchengfly.tieba.post.api.models.SignResultBean
import com.huanchengfly.tieba.post.api.models.protos.userLike.UserLikeResponse
import kotlinx.coroutines.flow.Flow
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
    ): Flow<SignResultBean> =
        api.signFlow(forumId, forumName, tbs)

    override fun likeForum(
        forumId: String,
        forumName: String,
        tbs: String
    ): Flow<LikeForumResultBean> =
        api.likeForumFlow(forumId, forumName, tbs)

    override fun unlikeForum(
        forumId: String,
        forumName: String,
        tbs: String
    ): Flow<CommonResponse> =
        api.unlikeForumFlow(forumId, forumName, tbs)

    override fun userLike(
        pageTag: String,
        lastRequestUnix: Long,
        loadType: Int
    ): Flow<UserLikeResponse> =
        api.userLikeFlow(pageTag, lastRequestUnix, loadType)
}
