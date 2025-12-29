package com.huanchengfly.tieba.post.models.mappers

import com.huanchengfly.tieba.core.common.moderation.ReportCheckResult
import com.huanchengfly.tieba.post.api.models.CheckReportBean

fun CheckReportBean.toReportCheckResult(): ReportCheckResult =
    ReportCheckResult(
        url = data.url,
        errorCode = errorCode ?: 0,
        errorMsg = errorMsg.orEmpty(),
    )
