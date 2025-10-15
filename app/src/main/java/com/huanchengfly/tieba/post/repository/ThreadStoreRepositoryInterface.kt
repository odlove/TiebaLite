package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.models.ThreadStoreBean
import kotlinx.coroutines.flow.Flow

/**
 * 帖子收藏列表数据仓库接口
 *
 * 负责处理帖子收藏列表的数据获取
 */
interface ThreadStoreRepository {
    /**
     * 获取收藏列表
     *
     * @param page 页码（默认为1）
     * @return 收藏列表数据流
     */
    fun threadStore(
        page: Int = 1
    ): Flow<ThreadStoreBean>
}
