package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.SearchThreadBean
import com.huanchengfly.tieba.post.api.models.protos.searchSug.SearchSugResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 搜索数据仓库实现
 */
@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val api: ITiebaApi
) : SearchRepository {
    override fun searchThread(
        keyword: String,
        page: Int,
        sortType: Int
    ): Flow<SearchThreadBean> =
        api.searchThreadFlow(keyword, page, sortType)

    override fun searchSuggestions(
        keyword: String
    ): Flow<SearchSugResponse> =
        api.searchSuggestionsFlow(keyword)
}
