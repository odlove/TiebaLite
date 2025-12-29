package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.repository.ThreadMetaStore
import com.huanchengfly.tieba.core.common.thread.ThreadMeta
import com.huanchengfly.tieba.core.runtime.di.ApplicationScope
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Singleton
class ThreadMetaStoreImpl @Inject constructor(
    @ApplicationScope private val applicationScope: CoroutineScope
) : ThreadMetaStore {
    private val _metaState = MutableStateFlow<Map<Long, ThreadMeta>>(emptyMap())

    override val metaState: StateFlow<Map<Long, ThreadMeta>> = _metaState.asStateFlow()

    override fun metaFlow(threadId: Long): StateFlow<ThreadMeta?> =
        metaState.map { it[threadId] }.stateIn(
            scope = applicationScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = metaState.value[threadId]
        )

    override fun get(threadId: Long): ThreadMeta? = metaState.value[threadId]

    override fun updateFromServer(threadId: Long, meta: ThreadMeta) {
        val current = _metaState.value.toMutableMap()
        current[threadId] = meta
        _metaState.value = current
    }

    override fun updateFromServer(metaMap: Map<Long, ThreadMeta>) {
        if (metaMap.isEmpty()) return
        val current = _metaState.value.toMutableMap()
        current.putAll(metaMap)
        _metaState.value = current
    }

    override fun clear(threadId: Long) {
        val current = _metaState.value.toMutableMap()
        if (current.remove(threadId) != null) {
            _metaState.value = current
        }
    }

    override fun clearAll() {
        _metaState.value = emptyMap()
    }
}
