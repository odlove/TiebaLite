package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.moderation.ReportCheckResult
import com.huanchengfly.tieba.core.network.retrofit.ApiResult
import com.huanchengfly.tieba.core.network.retrofit.fetchIfSuccess
import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.models.mappers.toReportCheckResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentModerationRepositoryImpl @Inject constructor(
    private val api: ITiebaApi
) : ContentModerationRepository {
    override suspend fun checkReportPost(postId: String): ApiResult<ReportCheckResult> {
        return api.checkReportPostAsync(postId)
            .await()
            .fetchIfSuccess { it.toReportCheckResult() }
    }
}
