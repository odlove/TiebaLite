package com.huanchengfly.tieba.post.store

/**
 * Store 工具函数
 */

/**
 * 复制 LinkedHashMap 并保持 LRU 特性
 *
 * **问题**：使用 `LinkedHashMap(originalMap)` 拷贝构造函数会丢失 `accessOrder = true` 设置，
 * 导致 LRU 淘汰策略失效。
 *
 * **解决**：手动创建新的 LinkedHashMap 并指定 `accessOrder = true`，再复制所有条目。
 *
 * **用法**：
 * ```kotlin
 * MutableStateFlow.update { oldMap ->
 *     oldMap.copyWithLRU().apply {
 *         put(key, value)
 *     }
 * }
 * ```
 *
 * @receiver 原始 LinkedHashMap
 * @return 新的 LinkedHashMap，保持 LRU 特性（accessOrder = true）
 */
internal fun <K, V> LinkedHashMap<K, V>.copyWithLRU(): LinkedHashMap<K, V> {
    // 创建新的 LinkedHashMap，显式指定 accessOrder = true
    // initialCapacity = size * 2 减少扩容频率
    return LinkedHashMap<K, V>(size * 2, 0.75f, true).apply {
        // ✅ 使用 toMap() 创建快照，避免 ConcurrentModificationException
        // 当多个协程并发访问源 map 时（如快速多次点赞），accessOrder=true 的 LRU 特性
        // 会在访问时修改内部结构，导致正在遍历的 putAll() 抛出异常
        putAll(this@copyWithLRU.toMap())
    }
}
