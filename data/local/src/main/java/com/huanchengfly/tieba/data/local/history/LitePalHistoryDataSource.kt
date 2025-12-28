package com.huanchengfly.tieba.data.local.history

import com.huanchengfly.tieba.post.models.database.History
import com.huanchengfly.tieba.post.utils.HistoryUtil
import kotlinx.coroutines.flow.Flow
import org.litepal.LitePal
import org.litepal.extension.deleteAll
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LitePalHistoryDataSource @Inject constructor() : HistoryDataSource {
    override fun observe(type: Int, page: Int): Flow<List<History>> =
        HistoryUtil.getFlow(type, page)

    override fun deleteAll() {
        HistoryUtil.deleteAll()
    }

    override fun delete(id: Long): Boolean =
        LitePal.deleteAll<History>("id = ?", "$id") > 0

    override fun saveHistory(history: History, async: Boolean) {
        HistoryUtil.saveHistory(history, async)
    }
}
