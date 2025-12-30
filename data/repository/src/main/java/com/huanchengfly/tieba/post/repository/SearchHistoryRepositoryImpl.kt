package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.search.SearchHistoryItem
import com.huanchengfly.tieba.core.common.search.SearchPostHistoryItem
import com.huanchengfly.tieba.post.models.database.SearchHistory
import com.huanchengfly.tieba.post.models.database.SearchPostHistory
import com.huanchengfly.tieba.post.models.mappers.toSearchHistoryItems
import com.huanchengfly.tieba.post.models.mappers.toSearchPostHistoryItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.litepal.LitePal
import org.litepal.extension.delete
import org.litepal.extension.deleteAll
import org.litepal.extension.find
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchHistoryRepositoryImpl @Inject constructor() : SearchHistoryRepository {
    override suspend fun getSearchHistory(): List<SearchHistoryItem> =
        withContext(Dispatchers.IO) {
            LitePal.order("timestamp DESC")
                .find<SearchHistory>()
                .toSearchHistoryItems()
        }

    override suspend fun saveSearchHistory(keyword: String) {
        withContext(Dispatchers.IO) {
            SearchHistory(keyword).saveOrUpdate("content = ?", keyword)
        }
    }

    override suspend fun deleteSearchHistory(id: Long) {
        withContext(Dispatchers.IO) {
            LitePal.delete<SearchHistory>(id)
        }
    }

    override suspend fun clearSearchHistory() {
        withContext(Dispatchers.IO) {
            LitePal.deleteAll<SearchHistory>()
        }
    }

    override suspend fun getSearchPostHistory(): List<SearchPostHistoryItem> =
        withContext(Dispatchers.IO) {
            LitePal.order("timestamp DESC")
                .find<SearchPostHistory>()
                .toSearchPostHistoryItems()
        }

    override suspend fun saveSearchPostHistory(keyword: String, forumName: String) {
        withContext(Dispatchers.IO) {
            SearchPostHistory(keyword, forumName).saveOrUpdate("content = ?", keyword)
        }
    }

    override suspend fun deleteSearchPostHistory(id: Long) {
        withContext(Dispatchers.IO) {
            LitePal.delete<SearchPostHistory>(id)
        }
    }

    override suspend fun clearSearchPostHistory() {
        withContext(Dispatchers.IO) {
            LitePal.deleteAll<SearchPostHistory>()
        }
    }
}
