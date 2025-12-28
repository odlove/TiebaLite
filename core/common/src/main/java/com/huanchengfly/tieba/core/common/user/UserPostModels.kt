package com.huanchengfly.tieba.core.common.user

import com.huanchengfly.tieba.core.common.feed.OriginThreadCard
import com.huanchengfly.tieba.core.common.feed.RichTextSegment
import com.huanchengfly.tieba.core.common.feed.ThreadMediaItem
import com.huanchengfly.tieba.core.common.feed.ThreadVideoInfo

data class UserPostContent(
    val contentText: String = "",
    val createTime: Long = 0L,
    val postId: Long = 0L,
    val isSubPost: Boolean = false,
)

data class UserPostItem(
    val threadId: Long = 0L,
    val postId: Long = 0L,
    val forumId: Long = 0L,
    val forumName: String = "",
    val title: String = "",
    val isThread: Boolean = false,
    val createTime: Long = 0L,
    val userId: Long = 0L,
    val userName: String = "",
    val nameShow: String? = null,
    val userPortrait: String? = null,
    val replyNum: Int = 0,
    val shareNum: Int = 0,
    val agreeNum: Int = 0,
    val hasAgree: Int = 0,
    val isNoTitle: Boolean = false,
    val isGood: Boolean = false,
    val isShareThread: Boolean = false,
    val abstractSegments: List<RichTextSegment> = emptyList(),
    val medias: List<ThreadMediaItem> = emptyList(),
    val videoInfo: ThreadVideoInfo? = null,
    val originThread: OriginThreadCard? = null,
    val contents: List<UserPostContent> = emptyList(),
)

data class UserPostPageResult(
    val posts: List<UserPostItem> = emptyList(),
    val hidePost: Boolean = false,
)
