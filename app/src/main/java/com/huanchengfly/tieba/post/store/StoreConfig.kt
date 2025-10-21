package com.huanchengfly.tieba.post.store

import androidx.annotation.VisibleForTesting

/**
 * Store 配置常量
 *
 * 控制 ThreadStore 的内存管理、TTL 和自动清理行为
 */
object StoreConfig {
    /**
     * 最大帖子数量（LRU 淘汰阈值）
     */
    const val MAX_THREADS = 20

    /**
     * 最大楼层数量（LRU 淘汰阈值）
     */
    const val MAX_POSTS = 50

    /**
     * 数据过期时间（TTL，单位：毫秒）
     * 30 分钟未访问的数据将被清理
     */
    const val TTL_MILLIS = 5 * 60 * 1000L  // 5 分钟

    /**
     * 自动清理任务间隔（单位：毫秒）
     * 每 5 分钟执行一次 TTL 检查和 LRU 淘汰
     */
    const val CLEANUP_INTERVAL_MILLIS = 60 * 1000L  // 1 分钟

    /**
     * 测试模式标志
     * 启用后使用更小的阈值和更短的 TTL，便于单元测试
     */
    @VisibleForTesting
    var isTestMode = false

    /**
     * 生效的最大帖子数
     * 测试模式下使用较小值（10），生产环境使用 MAX_THREADS
     */
    val effectiveMaxThreads: Int
        get() = if (isTestMode) 10 else MAX_THREADS

    /**
     * 生效的最大楼层数
     */
    val effectiveMaxPosts: Int
        get() = if (isTestMode) 20 else MAX_POSTS

    /**
     * 生效的 TTL
     * 测试模式下使用 5 秒，生产环境使用 30 分钟
     */
    val effectiveTTL: Long
        get() = if (isTestMode) 5000L else TTL_MILLIS

    /**
     * 生效的清理间隔
     * 测试模式下使用 1 秒，生产环境使用 5 分钟
     */
    val effectiveCleanupInterval: Long
        get() = if (isTestMode) 1000L else CLEANUP_INTERVAL_MILLIS
}
