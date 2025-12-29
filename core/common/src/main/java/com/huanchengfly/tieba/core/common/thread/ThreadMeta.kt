package com.huanchengfly.tieba.core.common.thread

data class ThreadMeta(
    val hasAgree: Boolean = false,
    val agreeNum: Int = 0,
    val collectStatus: Boolean = false,
    val collectMarkPid: Long = 0L,
    val replyNum: Int = 0,
    val shareNum: Int = 0,
    val viewNum: Int = 0,
)
