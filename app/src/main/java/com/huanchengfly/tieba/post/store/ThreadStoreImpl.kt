package com.huanchengfly.tieba.post.store

import android.os.SystemClock
import androidx.annotation.VisibleForTesting
import com.huanchengfly.tieba.post.arch.DispatcherProvider
import com.huanchengfly.tieba.post.di.CoroutineModule.ApplicationScope
import com.huanchengfly.tieba.post.store.models.PostEntity
import com.huanchengfly.tieba.post.store.models.PostMeta
import com.huanchengfly.tieba.post.store.models.ThreadEntity
import com.huanchengfly.tieba.post.store.models.ThreadMeta
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ThreadStore 实现类
 *
 * 基于 LinkedHashMap + MutableStateFlow 实现线程和楼层的统一存储。
 *
 * **核心特性**：
 * - LRU 淘汰：使用 LinkedHashMap(accessOrder = true)
 * - TTL 过期：基于 SystemClock.elapsedRealtime()
 * - 原子更新：使用 MutableStateFlow.update {}
 * - 批量订阅：避免 UI 层 combine N 个 Flow
 * - 自动清理：定期执行 TTL 检查和 LRU 淘汰
 *
 * @param dispatcherProvider 协程调度器提供者
 * @param appScope 应用级协程作用域（绑定应用生命周期）
 */
@Singleton
class ThreadStoreImpl @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    @ApplicationScope private val appScope: CoroutineScope
) : ThreadStore {

    /**
     * 线程存储
     *
     * 使用 LinkedHashMap(accessOrder = true) 实现 LRU。
     * 每次访问（get）会将条目移到末尾，最久未访问的在最前面。
     */
    private val threads = MutableStateFlow<LinkedHashMap<Long, ThreadEntity>>(
        LinkedHashMap(StoreConfig.MAX_THREADS, 0.75f, true)
    )

    /**
     * 楼层存储
     *
     * Key: Pair(threadId, postId)
     */
    private val posts = MutableStateFlow<LinkedHashMap<Pair<Long, Long>, PostEntity>>(
        LinkedHashMap(StoreConfig.MAX_POSTS, 0.75f, true)
    )

    /**
     * 正在更新中的 Thread ID 集合
     */
    private val updatingThreadIds = MutableStateFlow<Set<Long>>(emptySet())

    /**
     * 正在更新中的 Post Key 集合
     */
    private val updatingPostKeys = MutableStateFlow<Set<Pair<Long, Long>>>(emptySet())

    /**
     * 自动清理任务
     */
    private var cleanupJob: Job? = null

    // ===== 订阅 API 实现 =====

    override fun threadFlow(threadId: Long): Flow<ThreadEntity?> = threads
        .map { it[threadId] }  // 访问时触发 LRU 更新
        .distinctUntilChanged()

    override fun threadsFlow(threadIds: List<Long>): Flow<List<ThreadEntity>> = threads
        .map { map -> threadIds.mapNotNull { map[it] } }
        .distinctUntilChanged()

    override fun postFlow(threadId: Long, postId: Long): Flow<PostEntity?> = posts
        .map { it[threadId to postId] }
        .distinctUntilChanged()

    override fun postsFlow(threadId: Long, postIds: List<Long>): Flow<List<PostEntity>> = posts
        .map { map -> postIds.mapNotNull { map[threadId to it] } }
        .distinctUntilChanged()

    // ===== 写入 API 实现 =====

    override suspend fun upsertThreads(
        entities: Collection<ThreadEntity>,
        mergeStrategy: MergeStrategy
    ) {
        threads.update { map ->
            map.copyWithLRU().apply {
                entities.forEach { newEntity ->
                    val existing = map[newEntity.threadId]

                    // ✅ 步骤 1: 智能合并 Proto（防止时间/图片丢失）
                    val mergedProto = mergeThreadProto(existing?.proto, newEntity.proto)

                    // ✅ 步骤 2: 根据策略合并 Meta
                    val merged = when (mergeStrategy) {
                        MergeStrategy.REPLACE_ALL -> {
                            // 完全替换（但 Proto 已经智能合并）
                            newEntity.copy(proto = mergedProto)
                        }

                        MergeStrategy.PREFER_LOCAL_META -> {
                            // 保护 2 秒内的乐观更新
                            if (existing != null &&
                                SystemClock.elapsedRealtime() - existing.timestamp < 2000
                            ) {
                                // 保留本地 meta，Proto 使用合并后的
                                newEntity.copy(meta = existing.meta, proto = mergedProto)
                            } else {
                                newEntity.copy(proto = mergedProto)
                            }
                        }

                        MergeStrategy.MERGE_BY_TIMESTAMP -> {
                            // 按时间戳精细合并（Proto 使用合并后的）
                            existing?.let { mergeByTimestamp(it, newEntity.copy(proto = mergedProto)) } ?: newEntity.copy(proto = mergedProto)
                        }
                    }

                    put(merged.threadId, merged)
                }
            }
        }
    }

    override suspend fun upsertPosts(
        threadId: Long,
        posts: Collection<PostEntity>,
        mergeStrategy: MergeStrategy
    ) {
        this.posts.update { map ->
            map.copyWithLRU().apply {
                posts.forEach { newEntity ->
                    val existing = map[threadId to newEntity.id]
                    val merged = when (mergeStrategy) {
                        MergeStrategy.REPLACE_ALL -> {
                            // 完全替换
                            newEntity
                        }

                        MergeStrategy.PREFER_LOCAL_META -> {
                            // 保护 2 秒内的乐观更新
                            if (existing != null &&
                                SystemClock.elapsedRealtime() - existing.timestamp < 2000
                            ) {
                                // 保留本地 meta，更新其他字段
                                newEntity.copy(meta = existing.meta)
                            } else {
                                newEntity
                            }
                        }

                        MergeStrategy.MERGE_BY_TIMESTAMP -> {
                            // 按时间戳精细合并（当前简化实现：优先使用新数据）
                            existing?.let { mergePostsByTimestamp(it, newEntity) } ?: newEntity
                        }
                    }
                    put(threadId to merged.id, merged)
                }
            }
        }
    }

    // ===== Meta 更新 API 实现 =====

    override suspend fun updateThreadMeta(
        threadId: Long,
        block: (ThreadMeta) -> ThreadMeta
    ) {
        // ✅ 标记为更新中
        updatingThreadIds.update { it + threadId }
        try {
            threads.update { map ->
                val entity = map[threadId] ?: return@update map
                val oldMeta = entity.meta
                val newMeta = block(oldMeta)

                map.copyWithLRU().apply {
                    put(
                        threadId, entity.copy(
                            meta = newMeta,
                            timestamp = SystemClock.elapsedRealtime()  // ✅ 刷新时间戳，避免被 TTL 误删
                        )
                    )
                }
            }
        } finally {
            // ✅ 无论成功失败，都移除标记
            updatingThreadIds.update { it - threadId }
        }
    }

    override suspend fun updatePostMeta(
        threadId: Long,
        postId: Long,
        block: (PostMeta) -> PostMeta
    ) {
        val key = threadId to postId
        // ✅ 标记为更新中
        updatingPostKeys.update { it + key }
        try {
            posts.update { map ->
                val entity = map[key] ?: return@update map
                map.copyWithLRU().apply {
                    put(
                        key, entity.copy(
                            meta = block(entity.meta),
                            timestamp = SystemClock.elapsedRealtime()  // ✅ 刷新时间戳
                        )
                    )
                }
            }
        } finally {
            // ✅ 无论成功失败，都移除标记
            updatingPostKeys.update { it - key }
        }
    }

    // ===== 更新状态追踪 API 实现 =====

    override fun isThreadUpdating(threadId: Long): Flow<Boolean> =
        updatingThreadIds.map { threadId in it }.distinctUntilChanged()

    override fun isPostUpdating(threadId: Long, postId: Long): Flow<Boolean> =
        updatingPostKeys.map { (threadId to postId) in it }.distinctUntilChanged()

    // ===== 内存管理 API 实现 =====

    override suspend fun trimToSize(maxThreads: Int) {
        threads.update { map ->
            if (map.size <= maxThreads) return@update map
            map.copyWithLRU().apply {
                val iterator = entries.iterator()
                // LinkedHashMap(accessOrder=true) 的 iterator 按访问顺序遍历
                // 最前面的是最久未访问的
                while (size > maxThreads && iterator.hasNext()) {
                    iterator.next()
                    iterator.remove()
                }
            }
        }

        // 同时清理楼层
        posts.update { map ->
            val maxPosts = StoreConfig.effectiveMaxPosts
            if (map.size <= maxPosts) return@update map
            map.copyWithLRU().apply {
                val iterator = entries.iterator()
                while (size > maxPosts && iterator.hasNext()) {
                    iterator.next()
                    iterator.remove()
                }
            }
        }
    }

    override fun startAutoCleanup() {
        cleanupJob?.cancel()  // ✅ Cancel existing job to prevent leaks
        cleanupJob = appScope.launch(dispatcherProvider.io) {
            while (isActive) {
                delay(StoreConfig.effectiveCleanupInterval)
                clearExpired()
                trimToSize()
            }
        }
    }

    // ===== 内部方法 =====

    /**
     * 智能合并 ThreadInfo Proto 字段
     *
     * **问题**: 详情页返回的 ThreadInfo 经常缺少列表页所需字段（createTime, media, _abstract 等），
     * 直接替换会导致时间显示为 0、图片丢失。
     *
     * **解决方案**: 字段级智能合并 - 有值的字段用新数据，无值的字段沿用旧数据。
     *
     * @param oldProto 旧 ThreadInfo（可能为 null）
     * @param newProto 新 ThreadInfo
     * @return 合并后的 ThreadInfo
     */
    private fun mergeThreadProto(oldProto: com.huanchengfly.tieba.post.api.models.protos.ThreadInfo?, newProto: com.huanchengfly.tieba.post.api.models.protos.ThreadInfo): com.huanchengfly.tieba.post.api.models.protos.ThreadInfo {
        if (oldProto == null) return newProto

        return newProto.copy(
            // === 核心标识（优先使用新数据，防止 0 值） ===
            threadId = newProto.id.takeIf { it != 0L } ?: oldProto.id,  // 直接使用 id（与 ThreadMapper 一致）
            id = newProto.id.takeIf { it != 0L } ?: oldProto.id,
            firstPostId = newProto.firstPostId.takeIf { it != 0L } ?: oldProto.firstPostId,

            // === 基本信息（优先使用新数据，防止空/0 值） ===
            title = newProto.title.ifBlank { oldProto.title },
            replyNum = newProto.replyNum.takeIf { it != 0 } ?: oldProto.replyNum,
            viewNum = newProto.viewNum.takeIf { it != 0 } ?: oldProto.viewNum,

            // === 时间字段（✅ 关键修复：防止时间重置为 0） ===
            createTime = newProto.createTime.takeIf { it != 0 } ?: oldProto.createTime,
            lastTimeInt = newProto.lastTimeInt.takeIf { it != 0 } ?: oldProto.lastTimeInt,

            // === 论坛信息（优先使用新数据） ===
            forumId = newProto.forumId.takeIf { it != 0L } ?: oldProto.forumId,
            forumName = newProto.forumName.ifBlank { oldProto.forumName },

            // === 作者信息（优先使用新数据） ===
            author = newProto.author ?: oldProto.author,
            authorId = newProto.authorId.takeIf { it != 0L } ?: oldProto.authorId,

            // === 内容预览（✅ 关键修复：防止摘要和图片丢失） ===
            _abstract = newProto._abstract.takeIf { it.isNotEmpty() } ?: oldProto._abstract,
            media = newProto.media.takeIf { it.isNotEmpty() } ?: oldProto.media,

            // === 视频信息（✅ 关键修复：防止视频信息丢失） ===
            videoInfo = newProto.videoInfo ?: oldProto.videoInfo,

            // === 分类标记（优先使用新数据，默认为 0） ===
            isTop = if (newProto.isTop != 0) newProto.isTop else oldProto.isTop,
            isGood = if (newProto.isGood != 0) newProto.isGood else oldProto.isGood,
            isDeleted = if (newProto.isDeleted != 0) newProto.isDeleted else oldProto.isDeleted,

            // === 论坛详细信息（✅ 关键修复：防止推荐页论坛头像丢失） ===
            forumInfo = newProto.forumInfo ?: oldProto.forumInfo,

            // === 转发帖信息（✅ 关键修复：防止转发帖原帖信息丢失） ===
            origin_thread_info = newProto.origin_thread_info ?: oldProto.origin_thread_info,

            // === 首楼内容（✅ 关键修复：防止首楼内容丢失） ===
            firstPostContent = newProto.firstPostContent.takeIf { it.isNotEmpty() } ?: oldProto.firstPostContent,

            // === 富文本内容（✅ 关键修复：防止富文本标题和摘要丢失） ===
            richTitle = newProto.richTitle.takeIf { it.isNotEmpty() } ?: oldProto.richTitle,
            richAbstract = newProto.richAbstract.takeIf { it.isNotEmpty() } ?: oldProto.richAbstract,

            // === 最后回复信息（✅ 关键修复：防止最后回复者信息丢失） ===
            lastReplyer = newProto.lastReplyer ?: oldProto.lastReplyer,
            lastTime = newProto.lastTime.ifBlank { oldProto.lastTime },

            // === 其他字段（保留 newProto 的值，这些字段通常详情页会更新） ===
            // agreeNum, agree, collectStatus 等由 Meta 管理，不在此合并
        )
    }

    /**
     * 清理过期数据（基于 TTL）
     */
    private suspend fun clearExpired() {
        val now = SystemClock.elapsedRealtime()
        val ttl = StoreConfig.effectiveTTL

        threads.update { map ->
            map.copyWithLRU().apply {
                clear()
                map.entries.forEach { (k, v) ->
                    if (now - v.timestamp <= ttl) {
                        put(k, v)
                    }
                }
            }
        }

        posts.update { map ->
            map.copyWithLRU().apply {
                clear()
                map.entries.forEach { (k, v) ->
                    if (now - v.timestamp <= ttl) {
                        put(k, v)
                    }
                }
            }
        }
    }

    /**
     * 按时间戳合并两个 ThreadEntity
     *
     * 当前简化实现：优先使用新数据。
     * 未来可扩展为字段级冲突解决（需要 meta 包含字段级时间戳）。
     *
     * @param old 旧实体
     * @param new 新实体
     * @return 合并后的实体
     */
    private fun mergeByTimestamp(old: ThreadEntity, new: ThreadEntity): ThreadEntity {
        // 简化实现：优先使用新数据
        return new
    }

    /**
     * 按时间戳合并两个 PostEntity
     *
     * 当前简化实现：优先使用新数据。
     * 未来可扩展为字段级冲突解决（需要 meta 包含字段级时间戳）。
     *
     * @param old 旧实体
     * @param new 新实体
     * @return 合并后的实体
     */
    private fun mergePostsByTimestamp(old: PostEntity, new: PostEntity): PostEntity {
        // 简化实现：优先使用新数据
        return new
    }

    /**
     * 停止自动清理任务
     *
     * 仅用于测试，生产环境不应调用。
     */
    @VisibleForTesting
    fun shutdown() {
        cleanupJob?.cancel()
    }
}
