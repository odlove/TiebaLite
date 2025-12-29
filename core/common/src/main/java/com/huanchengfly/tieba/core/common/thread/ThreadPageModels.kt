package com.huanchengfly.tieba.core.common.thread

import com.huanchengfly.tieba.core.common.feed.OriginThreadCard

data class ThreadUser(
    val id: Long = 0L,
    val name: String = "",
    val nameShow: String? = null,
    val portrait: String? = null,
    val levelId: Int = 0,
    val bawuType: String? = null,
    val ipAddress: String? = null,
    val isLogin: Int = 0,
)

data class ThreadForum(
    val id: Long = 0L,
    val name: String = "",
    val avatar: String? = null,
)

data class ThreadAnti(
    val tbs: String? = null,
)

data class ThreadAgree(
    val hasAgree: Int = 0,
    val agreeNum: Long = 0L,
    val diffAgreeNum: Long = 0L,
)

data class ThreadContentItem(
    val type: Int = 0,
    val text: String = "",
    val link: String = "",
    val c: String = "",
    val bsize: String = "",
    val bigSrc: String = "",
    val bigCdnSrc: String = "",
    val originSrc: String = "",
    val showOriginalBtn: Int = 0,
    val originSize: Int = 0,
    val uid: Long = 0L,
    val dynamicUrl: String = "",
    val width: Int = 0,
    val height: Int = 0,
    val cdnSrcActive: String = "",
    val cdnSrc: String = "",
    val src: String = "",
    val voiceMd5: String = "",
    val duringTime: Int = 0,
)

data class ThreadSubPost(
    val id: Long = 0L,
    val time: Long = 0L,
    val authorId: Long = 0L,
    val author: ThreadUser? = null,
    val content: List<ThreadContentItem> = emptyList(),
    val agree: ThreadAgree? = null,
)

data class ThreadPost(
    val id: Long = 0L,
    val threadId: Long = 0L,
    val authorId: Long = 0L,
    val floor: Int = 0,
    val title: String = "",
    val isNTitle: Int = 0,
    val time: Long = 0L,
    val author: ThreadUser? = null,
    val content: List<ThreadContentItem> = emptyList(),
    val agree: ThreadAgree? = null,
    val subPostCount: Int = 0,
    val subPosts: List<ThreadSubPost> = emptyList(),
    val forum: ThreadForum? = null,
)

data class ThreadPostMeta(
    val hasAgree: Boolean = false,
    val agreeNum: Int = 0,
    val subPostCount: Int = 0,
)

data class ThreadDetail(
    val threadId: Long = 0L,
    val firstPostId: Long = 0L,
    val title: String = "",
    val replyNum: Int = 0,
    val forumId: Long = 0L,
    val forumName: String = "",
    val isShareThread: Boolean = false,
    val author: ThreadUser? = null,
    val agree: ThreadAgree? = null,
    val collectStatus: Int = 0,
    val collectMarkPid: Long = 0L,
    val postIds: List<Long> = emptyList(),
    val originThread: OriginThreadCard? = null,
)

data class ThreadPageInfo(
    val currentPage: Int = 0,
    val totalPage: Int = 0,
    val hasMore: Boolean = false,
    val hasPrev: Boolean = false,
)

data class ThreadPageData(
    val thread: ThreadDetail,
    val forum: ThreadForum? = null,
    val user: ThreadUser? = null,
    val anti: ThreadAnti? = null,
    val page: ThreadPageInfo? = null,
    val posts: List<ThreadPost> = emptyList(),
    val firstPost: ThreadPost? = null,
)

data class SubPostsPageInfo(
    val currentPage: Int = 0,
    val totalPage: Int = 0,
    val totalCount: Int = 0,
    val hasMore: Boolean = false,
    val hasPrev: Boolean = false,
)

data class SubPostsPageData(
    val thread: ThreadDetail,
    val forum: ThreadForum,
    val post: ThreadPost,
    val anti: ThreadAnti,
    val page: SubPostsPageInfo,
    val subPosts: List<ThreadSubPost> = emptyList(),
)
