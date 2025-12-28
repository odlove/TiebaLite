package com.huanchengfly.tieba.post.models.mappers

import com.huanchengfly.tieba.core.common.user.UserLikeForumItem
import com.huanchengfly.tieba.core.common.user.UserLikeForumResult
import com.huanchengfly.tieba.post.api.models.UserLikeForumBean

fun UserLikeForumBean.toUserLikeForumResult(): UserLikeForumResult =
    UserLikeForumResult(
        hasMore = hasMore == "1",
        forums = forumList.forumList.map { forum ->
            UserLikeForumItem(
                id = forum.id,
                name = forum.name.orEmpty(),
                levelId = forum.levelId,
                levelName = forum.levelName,
                avatar = forum.avatar,
                slogan = forum.slogan,
            )
        }
    )
