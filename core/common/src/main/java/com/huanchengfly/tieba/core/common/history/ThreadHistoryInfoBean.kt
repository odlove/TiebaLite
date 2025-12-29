package com.huanchengfly.tieba.core.common.history

data class ThreadHistoryInfoBean(
    val isSeeLz: Boolean = false,
    val pid: String? = null,
    val forumName: String? = null,
    val floor: String? = null,
)
