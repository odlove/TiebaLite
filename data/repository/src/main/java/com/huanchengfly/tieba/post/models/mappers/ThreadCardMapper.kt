package com.huanchengfly.tieba.post.models.mappers

import com.huanchengfly.tieba.core.common.feed.RichTextSegment
import com.huanchengfly.tieba.core.common.feed.ThreadAgree
import com.huanchengfly.tieba.core.common.feed.ThreadAuthor
import com.huanchengfly.tieba.core.common.feed.ThreadCard
import com.huanchengfly.tieba.core.common.feed.ThreadForumInfo
import com.huanchengfly.tieba.core.common.feed.ThreadMediaItem
import com.huanchengfly.tieba.core.common.feed.ThreadVideoInfo
import com.huanchengfly.tieba.post.models.ThreadEntity

object ThreadCardMapper {
    fun fromEntity(entity: ThreadEntity): ThreadCard {
        val proto = entity.proto
        val meta = entity.meta
        val author = proto.author?.let {
            ThreadAuthor(
                id = it.id,
                name = it.name,
                nameShow = it.nameShow,
                portrait = it.portrait
            )
        }
        val forumInfo = proto.forumInfo?.let {
            ThreadForumInfo(
                name = it.name,
                avatar = it.avatar
            )
        }
        val abstractSegments = if (proto.richAbstract.isNotEmpty()) {
            proto.richAbstract.map {
                RichTextSegment(
                    type = it.type,
                    text = it.text,
                    c = it.c
                )
            }
        } else {
            entity.abstract.map {
                RichTextSegment(
                    type = it.type,
                    text = it.text
                )
            }
        }
        val medias = proto.media.map {
            ThreadMediaItem(
                originPic = it.originPic,
                bigPic = it.bigPic,
                dynamicPic = it.dynamicPic,
                srcPic = it.srcPic,
                postId = it.postId,
                showOriginalBtn = it.showOriginalBtn,
                originSize = it.originSize
            )
        }
        val videoInfo = proto.videoInfo?.let {
            ThreadVideoInfo(
                videoUrl = it.videoUrl,
                thumbnailUrl = it.thumbnailUrl,
                thumbnailWidth = it.thumbnailWidth,
                thumbnailHeight = it.thumbnailHeight
            )
        }
        val agree = ThreadAgree(
            hasAgree = meta.hasAgree,
            agreeNum = meta.agreeNum
        )
        return ThreadCard(
            threadId = entity.threadId,
            firstPostId = entity.firstPostId,
            forumId = entity.forumId,
            forumName = entity.forumName,
            title = entity.title,
            tabName = proto.tabName,
            isNoTitle = proto.isNoTitle == 1,
            isGood = entity.isGood == 1,
            isShareThread = proto.is_share_thread == 1,
            lastTimeInt = entity.lastTimeInt,
            shareNum = proto.shareNum.toInt(),
            replyNum = entity.replyNum,
            hotNum = proto.hotNum.toInt(),
            agreeNum = meta.agreeNum,
            hasAgree = meta.hasAgree,
            collectStatus = meta.collectStatus,
            collectMarkPid = meta.collectMarkPid,
            author = author,
            forumInfo = forumInfo,
            abstractSegments = abstractSegments,
            medias = medias,
            videoInfo = videoInfo,
            hasOriginThreadInfo = proto.origin_thread_info != null,
            originThreadPayload = proto.origin_thread_info,
        )
    }
}
