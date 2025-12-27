package com.huanchengfly.tieba.core.common.history

import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun observe(type: Int, page: Int): Flow<List<HistoryItem>>

    companion object {
        const val TYPE_FORUM = 1
        const val TYPE_THREAD = 2
    }
}
