package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.protos.OriginThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.pbPage.PbPageResponse
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaUnknownException
import com.huanchengfly.tieba.post.di.CoroutineModule
import com.huanchengfly.tieba.post.models.PostEntity
import com.huanchengfly.tieba.post.models.ProtoFieldTags
import com.huanchengfly.tieba.post.models.ThreadEntity
import com.huanchengfly.tieba.post.models.mappers.PostMapper
import com.huanchengfly.tieba.post.models.mappers.ThreadMapper
import com.huanchengfly.tieba.post.ui.page.thread.ThreadPageFrom
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PbPage（帖子详情页）数据仓库实现
 *
 * 提供帖子详情页数据获取和缓存功能。包括：
 * - pbPage(): 获取帖子详情页数据（API 调用）
 * - threadFlow(): 订阅单个帖子的缓存数据
 * - threadsFlow(): 订阅多个帖子的缓存数据
 * - isThreadUpdating(): 订阅帖子是否正在更新
 *
 * 缓存策略：
 * - 内存中保留最近加载的帖子，容量约 100 个
 * - 当 pbPage() API 调用成功时，自动更新缓存
 * - 每个帖子都有一个更新标志，用于 UI 显示加载状态
 */
@Singleton
class PbPageRepositoryImpl @Inject constructor(
    private val api: ITiebaApi,
    @param:CoroutineModule.ApplicationScope
    private val applicationScope: CoroutineScope
) : PbPageRepository {
    companion object {
        const val ST_TYPE_MENTION = "mention"
        const val ST_TYPE_STORE_THREAD = "store_thread"
        private val ST_TYPES = persistentListOf(ST_TYPE_MENTION, ST_TYPE_STORE_THREAD)
        private const val MAX_THREAD_CACHE_SIZE = 1000  // 缓存最多少个帖子
        private const val MAX_POST_CACHE_SIZE = 5000    // 缓存最多少个回复
    }

    // ========== 内部缓存 MutableStateFlow ==========
    // 这些内部缓存用于接收 API 调用结果和管理更新状态
    // 不直接暴露给外部，通过 stateIn() 转换为公开 StateFlow

    // 帖子缓存：threadId -> ThreadEntity
    private val _threadsCacheInternal = MutableStateFlow<Map<Long, ThreadEntity>>(emptyMap())

    // 帖子更新状态：threadId -> 是否正在更新
    private val _updatingThreadsInternal = MutableStateFlow<Set<Long>>(emptySet())

    // 回复缓存：threadId -> (postId -> PostEntity)
    private val _postsCacheInternal = MutableStateFlow<Map<Long, Map<Long, PostEntity>>>(emptyMap())

    // 回复更新状态：(threadId, postId) 对集合
    private val _updatingPostsInternal = MutableStateFlow<Set<Pair<Long, Long>>>(emptySet())

    // ========== 公开 StateFlow（使用 WhileSubscribed() 自动管理生命周期） ==========
    // ✅ 当有订阅者时保留缓存，无订阅者时 5 秒后自动清理

    private val threadsCache: StateFlow<Map<Long, ThreadEntity>> =
        _threadsCacheInternal.stateIn(
            scope = applicationScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    private val updatingThreads: StateFlow<Set<Long>> =
        _updatingThreadsInternal.stateIn(
            scope = applicationScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    private val postsCache: StateFlow<Map<Long, Map<Long, PostEntity>>> =
        _postsCacheInternal.stateIn(
            scope = applicationScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    private val updatingPosts: StateFlow<Set<Pair<Long, Long>>> =
        _updatingPostsInternal.stateIn(
            scope = applicationScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    override fun pbPage(
        threadId: Long,
        page: Int,
        postId: Long,
        forumId: Long?,
        seeLz: Boolean,
        sortType: Int,
        back: Boolean,
        from: String,
        lastPostId: Long?,
    ): Flow<PbPageResponse> =
        api.pbPageFlow(
                threadId,
                page,
                postId = postId,
                seeLz = seeLz,
                sortType = sortType,
                back = back,
                forumId = forumId,
                stType = from.takeIf { ST_TYPES.contains(it) }.orEmpty(),
                mark = if (from == ThreadPageFrom.FROM_STORE) 1 else 0,
                lastPostId = lastPostId
            )
            .map { response ->
                if (response.data_ == null) {
                    throw TiebaUnknownException
                }
                if (response.data_.post_list.isEmpty()) {
                    throw EmptyDataException
                }
                if (
                    response.data_.page == null
                    || response.data_.thread?.author == null
                    || response.data_.forum == null
                    || response.data_.anti == null
                ) {
                    throw TiebaUnknownException
                }
                val userList = response.data_.user_list
                val postList = response.data_.post_list.map {
                    val author = it.author
                        ?: userList.first { user -> user.id == it.author_id }
                    it.copy(
                        author_id = author.id,
                        author = it.author
                            ?: userList.first { user -> user.id == it.author_id },
                        from_forum = response.data_.forum,
                        tid = response.data_.thread.id,
                        sub_post_list = it.sub_post_list?.copy(
                            sub_post_list = it.sub_post_list.sub_post_list.map { subPost ->
                                subPost.copy(
                                    author = subPost.author
                                        ?: userList.first { user -> user.id == subPost.author_id }
                                )
                            }
                        ),
                        origin_thread_info = OriginThreadInfo(
                            author = response.data_.thread.author
                        )
                    )
                }
                val firstPost = postList.firstOrNull { it.floor == 1 }
                    ?: response.data_.first_floor_post?.copy(
                        author_id = response.data_.thread.author.id,
                        author = response.data_.thread.author,
                        from_forum = response.data_.forum,
                        tid = response.data_.thread.id,
                        sub_post_list = response.data_.first_floor_post.sub_post_list?.copy(
                            sub_post_list = response.data_.first_floor_post.sub_post_list.sub_post_list.map { subPost ->
                                subPost.copy(
                                    author = subPost.author
                                        ?: userList.first { user -> user.id == subPost.author_id }
                                )
                            }
                        )
                    )

                val finalResponse = response.copy(
                    data_ = response.data_.copy(
                        post_list = postList,
                        first_floor_post = firstPost,
                    )
                )

                // ✅ API 调用成功时更新缓存
                response.data_.thread?.let { threadProto ->
                    // ✅ 传入正确的 threadId（来自方法参数，而不是 proto.id）
                    updateThreadCache(threadId, threadProto, null)
                    // 同时更新回复缓存
                    val postList = response.data_.post_list
                    val firstPost = response.data_.first_floor_post
                    val allPosts = listOfNotNull(firstPost) + postList.filterNot { it.floor == 1 }
                    if (allPosts.isNotEmpty()) {
                        updatePostsCache(threadId, allPosts)
                    }
                }

                finalResponse
            }

    override fun threadFlow(threadId: Long): StateFlow<ThreadEntity?> =
        threadsCache.map { cache ->
            cache[threadId]
        }.stateIn(
            scope = applicationScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = threadsCache.value[threadId]
        )

    override fun threadsFlow(threadIds: List<Long>): StateFlow<List<ThreadEntity>> =
        threadsCache.map { cache ->
            threadIds.mapNotNull { cache[it] }
        }.stateIn(
            scope = applicationScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = threadIds.mapNotNull { threadsCache.value[it] }
        )

    override fun isThreadUpdating(threadId: Long): Flow<Boolean> =
        updatingThreads.map { it.contains(threadId) }

    // ✅ 私有方法：更新帖子缓存（使用合并策略）
    // 当 pbPage API 调用时，调用 ThreadEntity.mergeWithDetail() 来合并数据
    // 这样只更新 pbPage 真正提供的字段，保留来自 list API 的完整数据（abstract、media 等）
    private fun updateThreadCache(
        threadId: Long,  // ✅ 显式传入正确的 threadId，而不是从 proto.id 获取
        threadProto: com.huanchengfly.tieba.post.api.models.protos.ThreadInfo,
        presence: com.huanchengfly.tieba.post.models.FieldPresence? = null
    ) {
        val currentCache = _threadsCacheInternal.value.toMutableMap()
        val existingEntity = currentCache[threadId]

        val entityToCache = if (existingEntity != null) {
            // ✅ 缓存中已存在：使用 Domain 层的合并逻辑
            val detailPresence = presence ?: com.huanchengfly.tieba.post.models.FieldPresence()
            val detailEntity = ThreadMapper.fromProtoWithPresence(
                com.huanchengfly.tieba.post.models.ThreadInfoWithPresence(threadProto, detailPresence)
            )
            // 清晰简洁：调用 Domain 层的合并方法
            existingEntity.mergeWithDetail(detailEntity)
        } else {
            // ✅ 缓存中不存在：首次加载，直接创建新 Entity
            // ⚠️ Proto3 默认值问题：threadProto.id 可能为 0，需要强制校正为参数中的正确 threadId
            val entity = if (presence != null) {
                ThreadMapper.fromProtoWithPresence(
                    com.huanchengfly.tieba.post.models.ThreadInfoWithPresence(threadProto, presence)
                )
            } else {
                ThreadMapper.fromProto(threadProto)
            }
            // ✅ 强制校正 threadId，确保与缓存 key 一致
            entity.copy(threadId = threadId)
        }

        currentCache[threadId] = entityToCache

        // 如果超过容量限制，移除最旧的
        if (currentCache.size > MAX_THREAD_CACHE_SIZE) {
            val oldestKey = currentCache.minByOrNull { it.value.timestamp }?.key
            if (oldestKey != null) {
                currentCache.remove(oldestKey)
            }
        }

        _threadsCacheInternal.value = currentCache
    }

    // ✅ 私有方法：标记帖子开始更新
    internal fun markThreadUpdating(threadId: Long) {
        _updatingThreadsInternal.value = _updatingThreadsInternal.value + threadId
    }

    // ✅ 私有方法：标记帖子完成更新
    internal fun markThreadUpdated(threadId: Long) {
        _updatingThreadsInternal.value = _updatingThreadsInternal.value - threadId
    }

    // ========== PostEntity StateFlow ==========

    override fun postFlow(threadId: Long, postId: Long): StateFlow<PostEntity?> =
        postsCache.map { cache ->
            cache[threadId]?.get(postId)
        }.stateIn(
            scope = applicationScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = postsCache.value[threadId]?.get(postId)
        )

    override fun postsFlow(threadId: Long, postIds: List<Long>): StateFlow<List<PostEntity>> =
        postsCache.map { cache ->
            cache[threadId]?.let { threadPosts ->
                postIds.mapNotNull { threadPosts[it] }
            } ?: emptyList()
        }.stateIn(
            scope = applicationScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = postsCache.value[threadId]?.let { threadPosts ->
                postIds.mapNotNull { threadPosts[it] }
            } ?: emptyList()
        )

    override fun isPostUpdating(threadId: Long, postId: Long): Flow<Boolean> =
        updatingPosts.map { it.contains(Pair(threadId, postId)) }

    // ✅ 私有方法：更新回复缓存
    private fun updatePostsCache(threadId: Long, posts: List<com.huanchengfly.tieba.post.api.models.protos.Post>) {
        val entities = PostMapper.fromProtos(posts, threadId)
        val currentCache = _postsCacheInternal.value.toMutableMap()
        val threadPostsMap = (currentCache[threadId] ?: emptyMap()).toMutableMap()

        // 更新或添加回复
        entities.forEach { entity ->
            threadPostsMap[entity.id] = entity
        }

        // 计算此线程的总回复数
        val totalPostCount = threadPostsMap.size

        // 如果此线程超过容量，移除该线程最旧的回复
        if (totalPostCount > MAX_POST_CACHE_SIZE) {
            val entriesToRemove = totalPostCount - MAX_POST_CACHE_SIZE
            val sortedByTime = threadPostsMap.values.sortedBy { it.timestamp }.take(entriesToRemove)
            sortedByTime.forEach { post ->
                threadPostsMap.remove(post.id)
            }
        }

        currentCache[threadId] = threadPostsMap
        _postsCacheInternal.value = currentCache
    }

    // ✅ 私有方法：标记回复开始更新
    internal fun markPostUpdating(threadId: Long, postId: Long) {
        _updatingPostsInternal.value = _updatingPostsInternal.value + Pair(threadId, postId)
    }

    // ✅ 私有方法：标记回复完成更新
    internal fun markPostUpdated(threadId: Long, postId: Long) {
        _updatingPostsInternal.value = _updatingPostsInternal.value - Pair(threadId, postId)
    }

    // ========== 内部 API：供 ThreadFeedRepository 向缓存写入数据 ==========

    /**
     * 批量更新或插入帖子到缓存（内部方法，仅供 ThreadFeedRepository 使用）
     *
     * @param entities 要更新的帖子实体列表
     */
    override fun upsertThreads(entities: List<ThreadEntity>) {
        val currentCache = _threadsCacheInternal.value.toMutableMap()

        // 批量更新或添加
        entities.forEach { entity ->
            currentCache[entity.threadId] = entity
        }

        // 如果超过容量限制，移除最旧的
        while (currentCache.size > MAX_THREAD_CACHE_SIZE) {
            val oldestKey = currentCache.minByOrNull { it.value.timestamp }?.key
            if (oldestKey != null) {
                currentCache.remove(oldestKey)
            }
        }

        _threadsCacheInternal.value = currentCache
    }

    /**
     * 更新单个帖子的 meta 字段（用于乐观更新）
     *
     * 供 ViewModel 层进行乐观更新和回滚，比如点赞状态变更。
     * 调用此方法会立即触发 threadFlow 的所有订阅者重组。
     *
     * @param threadId 帖子 ID
     * @param block 用于修改 meta 的函数，接收旧 meta 返回新 meta
     */
    override fun updateThreadMeta(threadId: Long, block: (com.huanchengfly.tieba.post.models.ThreadMeta) -> com.huanchengfly.tieba.post.models.ThreadMeta) {
        val currentCache = _threadsCacheInternal.value.toMutableMap()
        val entity = currentCache[threadId]

        if (entity != null) {
            val newMeta = block(entity.meta)
            currentCache[threadId] = entity.copy(meta = newMeta)
            _threadsCacheInternal.value = currentCache
        }
    }

    /**
     * 更新单个回复的 meta 字段（用于乐观更新）
     *
     * 供 ViewModel 层进行乐观更新和回滚，比如点赞状态变更。
     * 调用此方法会立即触发 postFlow 的所有订阅者重组。
     *
     * @param threadId 帖子 ID
     * @param postId 回复 ID
     * @param block 用于修改 meta 的函数，接收旧 meta 返回新 meta
     */
    override fun updatePostMeta(threadId: Long, postId: Long, block: (com.huanchengfly.tieba.post.models.PostMeta) -> com.huanchengfly.tieba.post.models.PostMeta) {
        val currentCache = _postsCacheInternal.value.toMutableMap()
        val threadPostsMap = (currentCache[threadId] ?: emptyMap()).toMutableMap()
        val post = threadPostsMap[postId]

        if (post != null) {
            val newMeta = block(post.meta)
            threadPostsMap[postId] = post.copy(meta = newMeta)
            currentCache[threadId] = threadPostsMap
            _postsCacheInternal.value = currentCache
        }
    }

}
