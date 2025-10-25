package com.huanchengfly.tieba.post.store

import com.huanchengfly.tieba.post.models.ThreadEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow

/**
 * 虚拟 ThreadStore 实现
 *
 * 用于兼容 UI 层对 Store 的订阅。
 * 当前不提供实际的数据，返回空 StateFlow/Flow。
 *
 * TODO: 完全移除，改为 UI 层直接订阅 Repository 的 StateFlow
 */
class DummyThreadStore : ThreadStore {
    // 使用 MutableStateFlow 来创建正确的 StateFlow（而非强制类型转换）
    // 注意：每个方法都返回相同的实例，避免类型转换问题
    private val _emptyThreadEntity = MutableStateFlow<ThreadEntity?>(null)
    private val _emptyThreadList = MutableStateFlow<List<ThreadEntity>>(emptyList())

    private val emptyThreadEntityStateFlow: StateFlow<ThreadEntity?> =
        _emptyThreadEntity.asStateFlow()
    private val emptyThreadListStateFlow: StateFlow<List<ThreadEntity>> =
        _emptyThreadList.asStateFlow()

    override fun threadFlow(threadId: Long): StateFlow<ThreadEntity?> =
        emptyThreadEntityStateFlow

    override fun threadsFlow(threadIds: List<Long>): StateFlow<List<ThreadEntity>> =
        emptyThreadListStateFlow

    override fun isThreadUpdating(threadId: Long): Flow<Boolean> = emptyFlow()
}
