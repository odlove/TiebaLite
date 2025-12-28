package com.huanchengfly.tieba.post.models.mappers

import com.huanchengfly.tieba.core.common.feed.RichTextSegment
import com.huanchengfly.tieba.core.common.feed.ThreadMediaItem
import com.huanchengfly.tieba.core.common.feed.ThreadVideoInfo
import com.huanchengfly.tieba.core.common.user.UserPostContent
import com.huanchengfly.tieba.core.common.user.UserPostItem
import com.huanchengfly.tieba.core.common.user.UserPostPageResult
import com.huanchengfly.tieba.post.api.models.protos.Abstract
import com.huanchengfly.tieba.post.api.models.protos.Media
import com.huanchengfly.tieba.post.api.models.protos.PbContent
import com.huanchengfly.tieba.post.api.models.protos.PostInfoContent
import com.huanchengfly.tieba.post.api.models.protos.PostInfoList
import com.huanchengfly.tieba.post.api.models.protos.userPost.UserPostResponse

private val MULTI_SPACE_REGEX = Regex(" {2,}")

fun UserPostResponse.toUserPostPageResult(): UserPostPageResult {
    val data = checkNotNull(data_)
    return UserPostPageResult(
        posts = data.post_list.map { it.toUserPostItem() },
        hidePost = data.hide_post == 1,
    )
}

private fun PostInfoList.toUserPostItem(): UserPostItem {
    val abstractSegments = if (rich_abstract.isNotEmpty()) {
        rich_abstract.map { it.toRichTextSegment() }
    } else {
        abstract_thread.map { it.toRichTextSegment() }
    }
    val medias = media.map { it.toThreadMediaItem() }
    val videoInfo = video_info?.let { info ->
        ThreadVideoInfo(
            videoUrl = info.videoUrl,
            thumbnailUrl = info.thumbnailUrl,
            thumbnailWidth = info.thumbnailWidth,
            thumbnailHeight = info.thumbnailHeight,
        )
    }
    return UserPostItem(
        threadId = thread_id,
        postId = post_id,
        forumId = forum_id,
        forumName = forum_name,
        title = title,
        isThread = is_thread == 1,
        createTime = create_time.toLong(),
        userId = user_id,
        userName = user_name,
        nameShow = name_show,
        userPortrait = user_portrait,
        replyNum = reply_num,
        shareNum = share_num,
        agreeNum = agree_num,
        hasAgree = agree?.hasAgree ?: 0,
        isNoTitle = is_ntitle == 1,
        isGood = good_types > 0,
        isShareThread = is_share_thread == 1,
        abstractSegments = abstractSegments,
        medias = medias,
        videoInfo = videoInfo,
        originThread = origin_thread_info?.toOriginThreadCard(),
        contents = content.map { it.toUserPostContent() },
    )
}

private fun PostInfoContent.toUserPostContent(): UserPostContent =
    UserPostContent(
        contentText = post_content.toAbstractText(),
        createTime = create_time.toLong(),
        postId = post_id,
        isSubPost = post_type == 1L,
    )

private fun List<Abstract>.toAbstractText(): String =
    joinToString(separator = "") { item ->
        when (item.type) {
            0, 4 -> MULTI_SPACE_REGEX.replace(item.text, " ")
            else -> ""
        }
    }

private fun PbContent.toRichTextSegment(): RichTextSegment =
    RichTextSegment(
        type = type,
        text = text,
        c = c,
    )

private fun Abstract.toRichTextSegment(): RichTextSegment =
    RichTextSegment(
        type = type,
        text = text,
    )

private fun Media.toThreadMediaItem(): ThreadMediaItem =
    ThreadMediaItem(
        originPic = originPic,
        bigPic = bigPic,
        dynamicPic = dynamicPic,
        srcPic = srcPic,
        postId = postId,
        showOriginalBtn = showOriginalBtn,
        originSize = originSize,
    )
