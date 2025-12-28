package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.models.protos.frsPage.FrsPageResponse
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaUnknownException

internal fun FrsPageResponse.filterThreadList(
    forumPreferences: ForumPreferences
): FrsPageResponse {
    val data = data_ ?: throw TiebaUnknownException
    val userList = data.user_list
    val threadList = data.thread_list
        .map { threadInfo ->
            threadInfo.copy(author = userList.find { it.id == threadInfo.authorId })
        }
        .filter { !forumPreferences.blockVideo || it.videoInfo == null }
        .filter { it.ala_info == null } // 去他妈的直播
    return copy(data_ = data.copy(thread_list = threadList))
}
