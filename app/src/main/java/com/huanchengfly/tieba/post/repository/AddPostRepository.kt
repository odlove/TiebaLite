package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.protos.addPost.AddPostResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 发帖/回帖数据仓库实现
 */
@Singleton
class AddPostRepositoryImpl @Inject constructor(
    private val api: ITiebaApi
) : AddPostRepository {
    override fun addPost(
        content: String,
        forumId: Long,
        forumName: String,
        threadId: Long,
        tbs: String?,
        nameShow: String?,
        postId: Long?,
        subPostId: Long?,
        replyUserId: Long?
    ): Flow<AddPostResponse> =
        api.addPostFlow(
            content,
            forumId.toString(),
            forumName,
            threadId.toString(),
            tbs,
            nameShow,
            postId?.toString(),
            subPostId?.toString(),
            replyUserId?.toString()
        )
}