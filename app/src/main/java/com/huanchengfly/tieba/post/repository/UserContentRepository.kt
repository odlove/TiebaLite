package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.protos.userPost.UserPostResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 用户内容数据仓库实现
 */
@Singleton
class UserContentRepositoryImpl @Inject constructor(
    private val api: ITiebaApi
) : UserContentRepository {
    override fun userPost(uid: Long, page: Int, isThread: Boolean): Flow<UserPostResponse> =
        api.userPostFlow(uid, page, isThread)
}
