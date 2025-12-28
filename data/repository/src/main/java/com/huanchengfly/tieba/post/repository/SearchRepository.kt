package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.core.common.search.SearchForumResult
import com.huanchengfly.tieba.core.common.search.SearchThreadResult
import com.huanchengfly.tieba.core.common.search.SearchUserResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.huanchengfly.tieba.post.models.mappers.toSearchForumResult
import com.huanchengfly.tieba.post.models.mappers.toSearchThreadResult
import com.huanchengfly.tieba.post.models.mappers.toSearchUserResult
import com.huanchengfly.tieba.post.models.mappers.toSuggestionList
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
    ): Flow<SearchThreadResult> =
        api.searchThreadFlow(keyword, page, sortType)
            .map { it.toSearchThreadResult() }

    override fun searchSuggestions(
        keyword: String
    ): Flow<List<String>> =
        api.searchSuggestionsFlow(keyword)
            .map { it.toSuggestionList() }

    override fun searchPost(
        keyword: String,
        forumName: String,
        forumId: Long,
        sortType: Int,
        filterType: Int,
        page: Int,
        pageSize: Int
    ): Flow<SearchThreadResult> =
        api.searchPostFlow(keyword, forumName, forumId, sortType, filterType, page, pageSize)
            .map { it.toSearchThreadResult() }

    override fun searchForum(
        keyword: String
    ): Flow<SearchForumResult> =
        api.searchForumFlow(keyword)
            .map { it.toSearchForumResult() }

    override fun searchUser(
        keyword: String
    ): Flow<SearchUserResult> =
        api.searchUserFlow(keyword)
            .map { it.toSearchUserResult() }
}
