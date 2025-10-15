package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.models.SearchThreadBean
import com.huanchengfly.tieba.post.api.models.protos.searchSug.SearchSugResponse
import kotlinx.coroutines.flow.Flow

/**
 * 搜索数据仓库接口
 *
 * 负责处理搜索相关的数据获取
 */
interface SearchRepository {
    /**
     * 搜索帖子
     *
     * @param keyword 搜索关键词
     * @param page 页码
     * @param sortType 排序类型
     * @return 搜索结果数据流
     */
    fun searchThread(
        keyword: String,
        page: Int,
        sortType: Int
    ): Flow<SearchThreadBean>

    /**
     * 获取搜索建议
     *
     * @param keyword 搜索关键词
     * @return 搜索建议数据流
     */
    fun searchSuggestions(
        keyword: String
    ): Flow<SearchSugResponse>
}
