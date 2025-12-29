package com.huanchengfly.tieba.core.common.repository

import com.huanchengfly.tieba.core.common.thread.ThreadMeta
import kotlinx.coroutines.flow.StateFlow

interface ThreadMetaStore {
    val metaState: StateFlow<Map<Long, ThreadMeta>>
    fun metaFlow(threadId: Long): StateFlow<ThreadMeta?>
    fun get(threadId: Long): ThreadMeta?
    fun updateFromServer(threadId: Long, meta: ThreadMeta)
    fun updateFromServer(metaMap: Map<Long, ThreadMeta>)
    fun clear(threadId: Long)
    fun clearAll()
}
