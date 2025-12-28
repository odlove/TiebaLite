package com.huanchengfly.tieba.core.common.history

data class HistoryItem(
    val title: String = "",
    val data: String = "",
    val type: Int = 0,
    val timestamp: Long = 0,
    val count: Int = 0,
    val extras: String? = null,
    val avatar: String? = null,
    val username: String? = null,
    val id: Long = 0L,
)
