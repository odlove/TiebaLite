package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.CheckReportBean
import com.huanchengfly.tieba.core.network.retrofit.ApiResult
import kotlinx.coroutines.Deferred
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentModerationRepositoryImpl @Inject constructor(
    private val api: ITiebaApi
) : ContentModerationRepository {
    override fun checkReportPost(postId: String): Deferred<ApiResult<CheckReportBean>> {
        return api.checkReportPostAsync(postId)
    }
}
