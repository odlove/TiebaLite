package com.huanchengfly.tieba.core.common.thread

import android.os.Parcelable
import com.huanchengfly.tieba.core.common.feed.ThreadCard
import kotlinx.parcelize.Parcelize

@Parcelize
data class ThreadPreview(
    val threadId: Long,
    val forumId: Long = 0L,
    val forumName: String = "",
    val title: String = "",
    val replyNum: Int = 0,
    val agreeNum: Int = 0,
    val hasAgree: Int = 0,
    val collectStatus: Int = 0,
    val collectMarkPid: Long = 0L,
    val authorId: Long = 0L,
    val authorName: String? = null,
    val authorNameShow: String? = null,
    val authorPortrait: String? = null,
) : Parcelable

fun ThreadPreview.toThreadDetail(): ThreadDetail =
    ThreadDetail(
        threadId = threadId,
        firstPostId = 0L,
        title = title,
        replyNum = replyNum,
        forumId = forumId,
        forumName = forumName,
        isShareThread = false,
        author = ThreadUser(
            id = authorId,
            name = authorName.orEmpty(),
            nameShow = authorNameShow,
            portrait = authorPortrait,
        ),
        agree = ThreadAgree(
            hasAgree = hasAgree,
            agreeNum = agreeNum.toLong(),
            diffAgreeNum = 0L,
        ),
        collectStatus = collectStatus,
        collectMarkPid = collectMarkPid,
        postIds = emptyList(),
        originThread = null,
    )

fun ThreadCard.toThreadPreview(): ThreadPreview =
    ThreadPreview(
        threadId = threadId,
        forumId = forumId,
        forumName = forumName,
        title = title,
        replyNum = replyNum,
        agreeNum = agreeNum,
        hasAgree = hasAgree,
        collectStatus = collectStatus,
        collectMarkPid = collectMarkPid,
        authorId = author?.id ?: 0L,
        authorName = author?.name,
        authorNameShow = author?.nameShow,
        authorPortrait = author?.portrait,
    )
