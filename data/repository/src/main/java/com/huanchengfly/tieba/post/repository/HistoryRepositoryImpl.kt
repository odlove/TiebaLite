package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.history.HistoryItem
import com.huanchengfly.tieba.core.common.history.HistoryRepository
import com.huanchengfly.tieba.data.local.history.HistoryDataSource
import com.huanchengfly.tieba.post.models.database.History
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val historyDataSource: HistoryDataSource
) : HistoryRepository {
    override fun observe(type: Int, page: Int): Flow<List<HistoryItem>> {
        return historyDataSource.observe(type, page)
            .map { histories -> histories.map { it.toHistoryItem() } }
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            historyDataSource.deleteAll()
        }
    }

    override suspend fun delete(id: Long): Boolean =
        withContext(Dispatchers.IO) {
            historyDataSource.delete(id)
        }

    override suspend fun saveHistory(history: HistoryItem, async: Boolean) {
        withContext(Dispatchers.IO) {
            historyDataSource.saveHistory(history.toHistory(), async)
        }
    }

    private fun History.toHistoryItem(): HistoryItem =
        HistoryItem(
            title = title,
            data = data,
            type = type,
            timestamp = timestamp,
            count = count,
            extras = extras,
            avatar = avatar,
            username = username,
            id = id,
        )

    private fun HistoryItem.toHistory(): History =
        History(
            title = title,
            data = data,
            type = type,
            timestamp = timestamp,
            count = count,
            extras = extras,
            avatar = avatar,
            username = username,
        )
}
