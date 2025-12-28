package com.huanchengfly.tieba.core.common.history

import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun observe(type: Int, page: Int): Flow<List<HistoryItem>>
    suspend fun deleteAll()
    suspend fun delete(id: Long): Boolean
    suspend fun saveHistory(history: HistoryItem, async: Boolean = true)

    companion object {
        const val PAGE_SIZE = 100
        const val TYPE_FORUM = 1
        const val TYPE_THREAD = 2
    }
}
