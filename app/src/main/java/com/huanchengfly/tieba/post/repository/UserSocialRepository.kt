package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.CommonResponse
import com.huanchengfly.tieba.post.api.models.FollowBean
import com.huanchengfly.tieba.post.api.models.UserLikeForumBean
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 用户社交数据仓库实现
 */
@Singleton
class UserSocialRepositoryImpl @Inject constructor(
    private val api: ITiebaApi
) : UserSocialRepository {
    override fun follow(portrait: String, tbs: String): Flow<FollowBean> =
        api.followFlow(portrait, tbs)

    override fun unfollow(portrait: String, tbs: String): Flow<CommonResponse> =
        api.unfollowFlow(portrait, tbs)

    override fun userLikeForum(uid: String, page: Int): Flow<UserLikeForumBean> =
        api.userLikeForumFlow(uid, page)
}
