package com.huanchengfly.tieba.core.common.search

data class SearchUser(
    val userId: String,
    val userName: String = "",
    val showNickname: String? = null,
    val portrait: String? = null,
)

data class SearchForum(
    val name: String,
    val avatar: String? = null,
)

data class SearchMedia(
    val type: String,
    val bigPic: String? = null,
    val smallPic: String? = null,
    val waterPic: String? = null,
)

data class SearchPost(
    val user: SearchUser,
    val content: String,
    val threadId: Long,
    val postId: Long,
)

data class SearchMainPost(
    val user: SearchUser,
    val title: String,
    val content: String,
    val threadId: Long,
)

data class SearchThreadItem(
    val threadId: String,
    val postId: String,
    val subPostId: String,
    val user: SearchUser,
    val time: String,
    val title: String,
    val content: String,
    val mainPost: SearchMainPost? = null,
    val quotePost: SearchPost? = null,
    val media: List<SearchMedia> = emptyList(),
    val forumName: String = "",
    val forumInfo: SearchForum? = null,
    val postNum: String = "",
    val likeNum: String = "",
    val shareNum: String = "",
)
