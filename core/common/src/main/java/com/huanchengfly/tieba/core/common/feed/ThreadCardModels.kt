package com.huanchengfly.tieba.core.common.feed

data class RichTextSegment(
    val type: Int = 0,
    val text: String = "",
    val c: String? = null,
)

data class ThreadAuthor(
    val id: Long = 0L,
    val name: String = "",
    val nameShow: String? = null,
    val portrait: String? = null,
)

data class ThreadAgree(
    val hasAgree: Int = 0,
    val agreeNum: Int = 0,
)

data class ThreadForumInfo(
    val name: String = "",
    val avatar: String? = null,
)

data class ThreadMediaItem(
    val originPic: String? = null,
    val bigPic: String? = null,
    val dynamicPic: String? = null,
    val srcPic: String? = null,
    val postId: Long = 0L,
    val showOriginalBtn: Int = 0,
    val originSize: Int = 0,
)

data class ThreadVideoInfo(
    val videoUrl: String? = null,
    val thumbnailUrl: String? = null,
    val thumbnailWidth: Int = 0,
    val thumbnailHeight: Int = 0,
)

data class ThreadCard(
    val threadId: Long,
    val firstPostId: Long,
    val forumId: Long,
    val forumName: String,
    val title: String,
    val tabName: String,
    val isNoTitle: Boolean,
    val isGood: Boolean,
    val isShareThread: Boolean,
    val lastTimeInt: Int,
    val shareNum: Int,
    val replyNum: Int,
    val hotNum: Int = 0,
    val agreeNum: Int,
    val hasAgree: Int,
    val collectStatus: Int,
    val collectMarkPid: Long,
    val author: ThreadAuthor? = null,
    val forumInfo: ThreadForumInfo? = null,
    val abstractSegments: List<RichTextSegment> = emptyList(),
    val medias: List<ThreadMediaItem> = emptyList(),
    val videoInfo: ThreadVideoInfo? = null,
    val originThreadPayload: Any? = null,
)
