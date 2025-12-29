package com.huanchengfly.tieba.post.models.mappers

import com.huanchengfly.tieba.core.common.threadcollect.ThreadCollectAuthor
import com.huanchengfly.tieba.core.common.threadcollect.ThreadCollectItem
import com.huanchengfly.tieba.core.common.threadcollect.ThreadCollectMedia
import com.huanchengfly.tieba.core.common.threadcollect.ThreadCollectResult
import com.huanchengfly.tieba.post.api.models.ThreadCollectBean

fun ThreadCollectBean.toThreadCollectResult(): ThreadCollectResult =
    ThreadCollectResult(
        items = collectThread?.map { it.toThreadCollectItem() },
        errorCode = errorCode ?: error?.errorCode,
        errorMessage = error?.errorMsg,
    )

private fun ThreadCollectBean.ThreadCollectInfo.toThreadCollectItem(): ThreadCollectItem =
    ThreadCollectItem(
        threadId = threadId,
        title = title,
        forumName = forumName,
        author = author.toThreadCollectAuthor(),
        media = media.map { it.toThreadCollectMedia() },
        isDeleted = isDeleted,
        lastTime = lastTime,
        type = type,
        status = status,
        maxPid = maxPid,
        minPid = minPid,
        markPid = markPid,
        markStatus = markStatus,
        postNo = postNo,
        postNoMsg = postNoMsg,
        count = count,
    )

private fun ThreadCollectBean.AuthorInfo.toThreadCollectAuthor(): ThreadCollectAuthor =
    ThreadCollectAuthor(
        id = lzUid,
        name = name,
        nameShow = nameShow,
        portrait = userPortrait,
    )

private fun ThreadCollectBean.MediaInfo.toThreadCollectMedia(): ThreadCollectMedia =
    ThreadCollectMedia(
        type = type,
        smallPic = smallPic,
        bigPic = bigPic,
        width = width,
        height = height,
    )
