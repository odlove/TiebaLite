package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.feed.PersonalizedFeedPage
import com.huanchengfly.tieba.core.common.repository.ThreadMetaStore
import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.models.mappers.toPersonalizedMapped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 个性化推荐数据仓库实现
 */
@Singleton
class PersonalizedRepositoryImpl @Inject constructor(
    private val api: ITiebaApi,
    private val pbPageRepository: PbPageRepository,
    private val threadMetaStore: ThreadMetaStore,
) : PersonalizedRepository {
    /**
     * 个性推荐
     *
     * @param loadType 加载类型（1 - 下拉刷新 2 - 加载更多）
     * @param page 分页页码
     */
    override fun personalizedFlow(loadType: Int, page: Int): Flow<PersonalizedFeedPage> =
        api.personalizedProtoFlow(loadType, page)
            .map { response ->
                val mapped = response.toPersonalizedMapped()
                if (mapped.threadCards.isNotEmpty()) {
                    pbPageRepository.upsertThreads(mapped.threadCards)
                }
                if (mapped.metaMap.isNotEmpty()) {
                    threadMetaStore.updateFromServer(mapped.metaMap)
                }
                mapped.feedPage
            }
}
