package com.huanchengfly.tieba.core.common.moderation

data class ReportCheckResult(
    val url: String = "",
    val errorCode: Int = 0,
    val errorMsg: String = "",
)
