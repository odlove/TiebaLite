package com.huanchengfly.tieba.post.models.mappers

import com.huanchengfly.tieba.core.common.forum.ForumRecommendItem
import com.huanchengfly.tieba.core.common.forum.ForumRecommendResult
import com.huanchengfly.tieba.post.api.models.protos.forumRecommend.ForumRecommendResponse

fun ForumRecommendResponse.toForumRecommendResult(): ForumRecommendResult {
    val forums = data_?.like_forum?.map {
        ForumRecommendItem(
            avatar = it.avatar,
            forumId = it.forum_id.toString(),
            forumName = it.forum_name,
            isSign = it.is_sign == 1,
            levelId = it.level_id.toString()
        )
    } ?: emptyList()
    return ForumRecommendResult(forums = forums)
}
