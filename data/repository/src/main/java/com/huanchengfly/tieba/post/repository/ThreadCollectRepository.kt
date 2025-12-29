package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.core.common.threadcollect.ThreadCollectResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import com.huanchengfly.tieba.post.models.mappers.toThreadCollectResult

/**
 * 帖子收藏列表数据仓库实现
 */
@Singleton
class ThreadCollectRepositoryImpl @Inject constructor(
    private val api: ITiebaApi
) : ThreadCollectRepository {
    override fun threadCollect(page: Int): Flow<ThreadCollectResult> =
        api.threadCollectFlow(page)
            .map { it.toThreadCollectResult() }
}
