package com.huanchengfly.tieba.post.ui.page.thread.mapper

import com.huanchengfly.tieba.core.common.thread.ThreadPreview
import com.huanchengfly.tieba.post.api.models.protos.Agree
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.User

fun ThreadPreview.toThreadInfo(): ThreadInfo {
    val authorName = authorName.orEmpty()
    val authorShow = authorNameShow ?: authorName
    val author = if (authorId != 0L || authorName.isNotBlank() || !authorPortrait.isNullOrBlank()) {
        User(
            id = authorId,
            name = authorName,
            nameShow = authorShow,
            portrait = authorPortrait.orEmpty()
        )
    } else {
        null
    }
    return ThreadInfo(
        id = threadId,
        threadId = threadId,
        title = title,
        replyNum = replyNum,
        forumId = forumId,
        forumName = forumName,
        author = author,
        agreeNum = agreeNum,
        agree = Agree(
            agreeNum = agreeNum.toLong(),
            hasAgree = hasAgree
        ),
        collectStatus = collectStatus,
        collectMarkPid = collectMarkPid.takeIf { it != 0L }?.toString().orEmpty()
    )
}
