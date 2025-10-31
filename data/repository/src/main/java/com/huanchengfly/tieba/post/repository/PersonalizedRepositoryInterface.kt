package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.models.protos.personalized.PersonalizedResponse
import kotlinx.coroutines.flow.Flow

/**
 * 个性化推荐数据仓库接口
 */
interface PersonalizedRepository {
    /**
     * 获取个性化推荐内容
     *
     * @param loadType 加载类型
     * @param page 页码（默认为1）
     * @return 个性化推荐数据流
     */
    fun personalizedFlow(
        loadType: Int,
        page: Int = 1
    ): Flow<PersonalizedResponse>
}
