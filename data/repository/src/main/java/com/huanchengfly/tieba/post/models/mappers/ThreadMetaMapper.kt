package com.huanchengfly.tieba.post.models.mappers

import com.huanchengfly.tieba.core.common.feed.ThreadCard
import com.huanchengfly.tieba.core.common.thread.ThreadMeta
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo

fun ThreadInfo.resolveThreadId(): Long = threadId.takeIf { it != 0L } ?: id

fun ThreadInfo.toThreadMeta(): ThreadMeta =
    ThreadMeta(
        hasAgree = agree?.hasAgree == 1,
        agreeNum = agreeNum,
        collectStatus = collectStatus == 1,
        collectMarkPid = collectMarkPid.toLongOrNull() ?: 0L,
        replyNum = replyNum,
        shareNum = shareNum.toInt(),
        viewNum = viewNum,
    )

fun ThreadCard.toThreadMeta(): ThreadMeta =
    ThreadMeta(
        hasAgree = hasAgree == 1,
        agreeNum = agreeNum,
        collectStatus = collectStatus == 1,
        collectMarkPid = collectMarkPid,
        replyNum = replyNum,
        shareNum = shareNum,
        viewNum = 0,
    )

fun ThreadCard.withMeta(meta: ThreadMeta): ThreadCard =
    copy(
        agreeNum = meta.agreeNum,
        hasAgree = if (meta.hasAgree) 1 else 0,
        collectStatus = if (meta.collectStatus) 1 else 0,
        collectMarkPid = meta.collectMarkPid,
        replyNum = meta.replyNum,
        shareNum = meta.shareNum,
    )
