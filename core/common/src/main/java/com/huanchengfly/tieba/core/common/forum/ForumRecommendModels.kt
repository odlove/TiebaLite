package com.huanchengfly.tieba.core.common.forum

data class ForumRecommendItem(
    val avatar: String? = null,
    val forumId: String = "",
    val forumName: String = "",
    val isSign: Boolean = false,
    val levelId: String = "",
)

data class ForumRecommendResult(
    val forums: List<ForumRecommendItem> = emptyList(),
)
