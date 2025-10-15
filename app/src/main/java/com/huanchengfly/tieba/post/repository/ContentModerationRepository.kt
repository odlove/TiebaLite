package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.models.CheckReportBean
import com.huanchengfly.tieba.post.api.retrofit.ApiResult
import kotlinx.coroutines.Deferred

interface ContentModerationRepository {
    /**
     * 检查帖子是否可以举报
     * @param postId 帖子 ID
     * @return 举报检查结果 Deferred
     */
    fun checkReportPost(postId: String): Deferred<ApiResult<CheckReportBean>>
}
