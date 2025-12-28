package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.core.common.threadstore.ThreadStoreResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import com.huanchengfly.tieba.post.models.mappers.toThreadStoreResult

/**
 * 帖子收藏列表数据仓库实现
 */
@Singleton
class ThreadStoreRepositoryImpl @Inject constructor(
    private val api: ITiebaApi
) : ThreadStoreRepository {
    override fun threadStore(page: Int): Flow<ThreadStoreResult> =
        api.threadStoreFlow(page)
            .map { it.toThreadStoreResult() }
}
