package com.huanchengfly.tieba.core.common.search

data class SearchHistoryItem(
    val id: Long,
    val content: String,
    val timestamp: Long,
)

data class SearchPostHistoryItem(
    val id: Long,
    val content: String,
    val forumName: String,
    val timestamp: Long,
)
