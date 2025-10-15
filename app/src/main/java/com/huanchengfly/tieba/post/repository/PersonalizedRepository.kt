package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.protos.personalized.PersonalizedResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 个性化推荐数据仓库实现
 */
@Singleton
class PersonalizedRepositoryImpl @Inject constructor(
    private val api: ITiebaApi
) : PersonalizedRepository {
    /**
     * 个性推荐
     *
     * @param loadType 加载类型（1 - 下拉刷新 2 - 加载更多）
     * @param page 分页页码
     */
    override fun personalizedFlow(loadType: Int, page: Int): Flow<PersonalizedResponse> =
        api.personalizedProtoFlow(loadType, page)
            .map { response ->
                // 过滤掉直播帖子
                val liveThreadIds =
                    response.data_?.thread_list?.filter { it.ala_info != null }?.map { it.id }
                        ?: emptyList()
                response.copy(
                    data_ = response.data_?.copy(
                        thread_list = response.data_.thread_list.filter { !liveThreadIds.contains(it.id) },
                        thread_personalized = response.data_.thread_personalized.filter {
                            !liveThreadIds.contains(
                                it.tid
                            )
                        }
                    )
                )
            }
}