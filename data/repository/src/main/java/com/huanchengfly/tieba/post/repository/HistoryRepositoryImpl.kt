package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.history.HistoryItem
import com.huanchengfly.tieba.core.common.history.HistoryRepository
import com.huanchengfly.tieba.data.local.history.HistoryDataSource
import com.huanchengfly.tieba.post.models.database.History
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val historyDataSource: HistoryDataSource
) : HistoryRepository {
    override fun observe(type: Int, page: Int): Flow<List<HistoryItem>> {
        return historyDataSource.observe(type, page)
            .map { histories -> histories.map { it.toHistoryItem() } }
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
        )
}
