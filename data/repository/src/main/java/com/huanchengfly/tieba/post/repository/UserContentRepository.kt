package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.core.common.user.UserPostPageResult
import com.huanchengfly.tieba.post.models.mappers.toUserPostPageResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 用户内容数据仓库实现
 */
@Singleton
class UserContentRepositoryImpl @Inject constructor(
    private val api: ITiebaApi
) : UserContentRepository {
    override fun userPost(uid: Long, page: Int, isThread: Boolean): Flow<UserPostPageResult> =
        api.userPostFlow(uid, page, isThread).map { it.toUserPostPageResult() }
}
