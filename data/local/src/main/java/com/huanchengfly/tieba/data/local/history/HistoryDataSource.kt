package com.huanchengfly.tieba.data.local.history

import com.huanchengfly.tieba.post.models.database.History
import kotlinx.coroutines.flow.Flow

interface HistoryDataSource {
    fun observe(type: Int, page: Int): Flow<List<History>>
    fun deleteAll()
    fun delete(id: Long): Boolean
    fun saveHistory(history: History, async: Boolean = true)

    companion object {
        const val TYPE_FORUM = 1
        const val TYPE_THREAD = 2
    }
}
