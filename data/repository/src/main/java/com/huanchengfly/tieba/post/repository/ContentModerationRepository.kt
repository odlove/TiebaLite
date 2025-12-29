package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.moderation.ReportCheckResult
import com.huanchengfly.tieba.core.network.retrofit.ApiResult

interface ContentModerationRepository {
    /**
     * 检查帖子是否可以举报
     * @param postId 帖子 ID
     * @return 举报检查结果
     */
    suspend fun checkReportPost(postId: String): ApiResult<ReportCheckResult>
}
