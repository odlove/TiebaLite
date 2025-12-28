package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.core.common.reply.AddPostResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import com.huanchengfly.tieba.post.models.mappers.toAddPostResult

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
    ): Flow<AddPostResult> =
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
            .map { it.toAddPostResult() }
}
