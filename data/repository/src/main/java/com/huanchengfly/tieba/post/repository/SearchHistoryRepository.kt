package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.search.SearchHistoryItem
import com.huanchengfly.tieba.core.common.search.SearchPostHistoryItem

interface SearchHistoryRepository {
    suspend fun getSearchHistory(): List<SearchHistoryItem>

    suspend fun saveSearchHistory(keyword: String)

    suspend fun deleteSearchHistory(id: Long)

    suspend fun clearSearchHistory()

    suspend fun getSearchPostHistory(): List<SearchPostHistoryItem>

    suspend fun saveSearchPostHistory(keyword: String, forumName: String)

    suspend fun deleteSearchPostHistory(id: Long)

    suspend fun clearSearchPostHistory()
}
