package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.ThreadStoreBean
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 帖子收藏列表数据仓库实现
 */
@Singleton
class ThreadStoreRepositoryImpl @Inject constructor(
    private val api: ITiebaApi
) : ThreadStoreRepository {
    override fun threadStore(page: Int): Flow<ThreadStoreBean> =
        api.threadStoreFlow(page)
}
