package com.huanchengfly.tieba.core.common.user

data class UserLikeForumItem(
    val id: String = "",
    val name: String = "",
    val levelId: String? = null,
    val levelName: String? = null,
    val avatar: String? = null,
    val slogan: String? = null,
)

data class UserLikeForumResult(
    val hasMore: Boolean = false,
    val forums: List<UserLikeForumItem> = emptyList(),
)
