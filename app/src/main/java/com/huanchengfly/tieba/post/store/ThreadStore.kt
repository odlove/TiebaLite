package com.huanchengfly.tieba.post.store

import com.huanchengfly.tieba.post.store.models.PostEntity
import com.huanchengfly.tieba.post.store.models.PostMeta
import com.huanchengfly.tieba.post.store.models.ThreadEntity
import com.huanchengfly.tieba.post.store.models.ThreadMeta
import kotlinx.coroutines.flow.Flow

/**
 * Meta 合并策略
 *
 * 解决乐观更新被网络返回覆盖的问题。
 *
 * **场景**：用户点赞 -> 乐观更新 UI -> 网络请求成功 -> 刷新列表
 * 如果直接用网络数据覆盖，会导致点赞状态被重置（因为网络数据可能是旧的）。
 */
enum class MergeStrategy {
    /**
     * 完全替换（刷新场景）
     *
     * 使用网络返回的数据完全覆盖本地数据，包括 meta。
     * 适用于下拉刷新、加载更多等场景。
     */
    REPLACE_ALL,

    /**
     * 优先保留本地 Meta（点赞后 2 秒内）
     *
     * 如果本地数据的 timestamp 距离现在不超过 2 秒，保留本地 meta，
     * 其他字段使用网络数据更新。
     *
     * 适用于点赞操作后立即刷新的场景，避免乐观更新被覆盖。
     */
    PREFER_LOCAL_META,

    /**
     * 按时间戳精细合并（高级场景）
     *
     * 对 meta 的每个字段按时间戳精细合并（需要 meta 包含字段级时间戳）。
     * 当前简化实现：优先使用新数据。
     *
     * 未来可扩展为字段级冲突解决。
     */
    MERGE_BY_TIMESTAMP
}

/**
 * Thread/Post Store 接口
 *
 * 提供帖子和楼层的统一数据存储、订阅和更新能力。
 * 所有订阅都是响应式的，数据变化会自动通知 UI。
 */
interface ThreadStore {
    // ===== 订阅 API =====

    /**
     * 订阅单个帖子
     *
     * @param threadId 帖子ID
     * @return 帖子的 Flow，数据变化时自动更新；帖子不存在时返回 null
     */
    fun threadFlow(threadId: Long): Flow<ThreadEntity?>

    /**
     * 批量订阅多个帖子（优化版）
     *
     * **性能优势**：避免 UI 层 `combine(threadIds.map { threadFlow(it) })`，
     * 减少 Flow 数量和 recomposition 次数。
     *
     * @param threadIds 帖子ID列表
     * @return 帖子列表的 Flow，只包含存在的帖子（过滤掉不存在的ID）
     */
    fun threadsFlow(threadIds: List<Long>): Flow<List<ThreadEntity>>

    /**
     * 订阅单个楼层
     *
     * @param threadId 所属帖子ID
     * @param postId 楼层ID
     * @return 楼层的 Flow；楼层不存在时返回 null
     */
    fun postFlow(threadId: Long, postId: Long): Flow<PostEntity?>

    /**
     * 批量订阅多个楼层（优化版）
     *
     * **性能优势**：避免 UI 层 combine N 个 Flow。
     *
     * @param threadId 所属帖子ID
     * @param postIds 楼层ID列表
     * @return 楼层列表的 Flow，只包含存在的楼层
     */
    fun postsFlow(threadId: Long, postIds: List<Long>): Flow<List<PostEntity>>

    // ===== 写入 API =====

    /**
     * 批量插入/更新帖子
     *
     * 如果帖子已存在，根据 mergeStrategy 决定如何合并；
     * 如果不存在，直接插入。
     *
     * @param entities 帖子实体列表
     * @param mergeStrategy 合并策略，默认 REPLACE_ALL
     */
    suspend fun upsertThreads(
        entities: Collection<ThreadEntity>,
        mergeStrategy: MergeStrategy = MergeStrategy.REPLACE_ALL
    )

    /**
     * 批量插入/更新楼层
     *
     * 如果楼层已存在，根据 mergeStrategy 决定如何合并；
     * 如果不存在，直接插入。
     *
     * @param threadId 所属帖子ID
     * @param posts 楼层实体列表
     * @param mergeStrategy 合并策略，默认 REPLACE_ALL
     */
    suspend fun upsertPosts(
        threadId: Long,
        posts: Collection<PostEntity>,
        mergeStrategy: MergeStrategy = MergeStrategy.REPLACE_ALL
    )

    // ===== Meta 更新 API（原子操作）=====

    /**
     * 原子更新帖子 Meta
     *
     * 使用 `MutableStateFlow.update {}` 确保并发安全。
     *
     * **用法**：
     * ```kotlin
     * threadStore.updateThreadMeta(threadId) { meta ->
     *     meta.copy(
     *         hasAgree = hasAgree xor 1,
     *         agreeNum = if (hasAgree == 0) meta.agreeNum + 1 else meta.agreeNum - 1
     *     )
     * }
     * ```
     *
     * @param threadId 帖子ID
     * @param block Meta 更新函数，接收旧 meta，返回新 meta
     */
    suspend fun updateThreadMeta(threadId: Long, block: (ThreadMeta) -> ThreadMeta)

    /**
     * 原子更新楼层 Meta
     *
     * @param threadId 所属帖子ID
     * @param postId 楼层ID
     * @param block Meta 更新函数
     */
    suspend fun updatePostMeta(threadId: Long, postId: Long, block: (PostMeta) -> PostMeta)

    // ===== 更新状态追踪 API =====

    /**
     * 订阅 Thread 是否正在更新中
     *
     * 当调用 updateThreadMeta 时，该 Thread 会被标记为更新中。
     * UI 可以订阅此状态来禁用按钮，防止并发操作。
     *
     * @param threadId 帖子ID
     * @return 是否正在更新的 Flow
     */
    fun isThreadUpdating(threadId: Long): Flow<Boolean>

    /**
     * 订阅 Post 是否正在更新中
     *
     * @param threadId 所属帖子ID
     * @param postId 楼层ID
     * @return 是否正在更新的 Flow
     */
    fun isPostUpdating(threadId: Long, postId: Long): Flow<Boolean>

    // ===== 内存管理 API =====

    /**
     * 手动触发 LRU 淘汰
     *
     * 当 Store 中帖子数超过 maxThreads 时，删除最久未访问的条目。
     *
     * @param maxThreads 最大帖子数，默认使用 StoreConfig.effectiveMaxThreads
     */
    suspend fun trimToSize(maxThreads: Int = StoreConfig.effectiveMaxThreads)

    /**
     * 启动自动清理任务
     *
     * 定期执行 TTL 清理和 LRU 淘汰。
     * 应在 Application 启动时调用一次（通过 Hilt 模块）。
     */
    fun startAutoCleanup()
}
