package com.huanchengfly.tieba.post.models.mappers

import com.huanchengfly.tieba.core.common.threadstore.ThreadStoreAuthor
import com.huanchengfly.tieba.core.common.threadstore.ThreadStoreItem
import com.huanchengfly.tieba.core.common.threadstore.ThreadStoreMedia
import com.huanchengfly.tieba.core.common.threadstore.ThreadStoreResult
import com.huanchengfly.tieba.post.api.models.ThreadStoreBean

fun ThreadStoreBean.toThreadStoreResult(): ThreadStoreResult =
    ThreadStoreResult(
        items = storeThread?.map { it.toThreadStoreItem() },
        errorCode = errorCode ?: error?.errorCode,
        errorMessage = error?.errorMsg,
    )

private fun ThreadStoreBean.ThreadStoreInfo.toThreadStoreItem(): ThreadStoreItem =
    ThreadStoreItem(
        threadId = threadId,
        title = title,
        forumName = forumName,
        author = author.toThreadStoreAuthor(),
        media = media.map { it.toThreadStoreMedia() },
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

private fun ThreadStoreBean.AuthorInfo.toThreadStoreAuthor(): ThreadStoreAuthor =
    ThreadStoreAuthor(
        id = lzUid,
        name = name,
        nameShow = nameShow,
        portrait = userPortrait,
    )

private fun ThreadStoreBean.MediaInfo.toThreadStoreMedia(): ThreadStoreMedia =
    ThreadStoreMedia(
        type = type,
        smallPic = smallPic,
        bigPic = bigPic,
        width = width,
        height = height,
    )
