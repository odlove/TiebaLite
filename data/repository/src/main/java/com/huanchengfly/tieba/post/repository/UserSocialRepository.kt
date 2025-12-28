package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.core.common.user.UserLikeForumResult
import com.huanchengfly.tieba.post.models.mappers.toUserLikeForumResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 用户社交数据仓库实现
 */
@Singleton
class UserSocialRepositoryImpl @Inject constructor(
    private val api: ITiebaApi
) : UserSocialRepository {
    override fun follow(portrait: String, tbs: String): Flow<Unit> =
        api.followFlow(portrait, tbs).map { }

    override fun unfollow(portrait: String, tbs: String): Flow<Unit> =
        api.unfollowFlow(portrait, tbs).map { }

    override fun userLikeForum(uid: String, page: Int): Flow<UserLikeForumResult> =
        api.userLikeForumFlow(uid, page).map { it.toUserLikeForumResult() }
}
