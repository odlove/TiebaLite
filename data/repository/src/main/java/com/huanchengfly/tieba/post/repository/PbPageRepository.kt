package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.protos.OriginThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaUnknownException
import com.huanchengfly.tieba.core.common.feed.ThreadCard
import com.huanchengfly.tieba.core.common.repository.ThreadMetaStore
import com.huanchengfly.tieba.core.common.thread.ThreadPageData
import com.huanchengfly.tieba.core.common.thread.ThreadPostMeta
import com.huanchengfly.tieba.core.runtime.di.ApplicationScope
import com.huanchengfly.tieba.post.models.mappers.toThreadPageData
import com.huanchengfly.tieba.post.models.mappers.toThreadMeta
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


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
    private val threadMetaStore: ThreadMetaStore,
    @ApplicationScope private val applicationScope: CoroutineScope
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

    // 帖子缓存：threadId -> ThreadCard
    private val _threadsCacheInternal = MutableStateFlow<Map<Long, ThreadCard>>(emptyMap())

    // 帖子更新状态：threadId -> 是否正在更新
    private val _updatingThreadsInternal = MutableStateFlow<Set<Long>>(emptySet())

    // 楼层元数据缓存：threadId -> (postId -> ThreadPostMeta)
    private val _postMetaCacheInternal = MutableStateFlow<Map<Long, Map<Long, ThreadPostMeta>>>(emptyMap())

    // ========== 公开 StateFlow（使用 WhileSubscribed() 自动管理生命周期） ==========
    // ✅ 当有订阅者时保留缓存，无订阅者时 5 秒后自动清理

    private val threadsCache: StateFlow<Map<Long, ThreadCard>> =
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

    private val postMetaCache: StateFlow<Map<Long, Map<Long, ThreadPostMeta>>> =
        _postMetaCacheInternal.stateIn(
            scope = applicationScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
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
    ): Flow<ThreadPageData> =
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
                val data = response.data_ ?: throw TiebaUnknownException
                if (data.post_list.isEmpty()) {
                    throw EmptyDataException
                }
                if (
                    data.page == null
                    || data.thread?.author == null
                    || data.forum == null
                    || data.anti == null
                ) {
                    throw TiebaUnknownException
                }
                val thread = data.thread ?: throw TiebaUnknownException
                val threadAuthor = thread.author ?: throw TiebaUnknownException
                val forum = data.forum ?: throw TiebaUnknownException
                data.page ?: throw TiebaUnknownException
                data.anti ?: throw TiebaUnknownException
                val userList = data.user_list
                val postList = data.post_list.map { post ->
                    val author = post.author
                        ?: userList.first { user -> user.id == post.author_id }
                    val subPostList = post.sub_post_list
                    post.copy(
                        author_id = author.id,
                        author = post.author
                            ?: userList.first { user -> user.id == post.author_id },
                        from_forum = forum,
                        tid = thread.id,
                        sub_post_list = subPostList?.copy(
                            sub_post_list = subPostList.sub_post_list.map { subPost ->
                                subPost.copy(
                                    author = subPost.author
                                        ?: userList.first { user -> user.id == subPost.author_id }
                                )
                            }
                        ),
                        origin_thread_info = OriginThreadInfo(
                            author = threadAuthor
                        )
                    )
                }
                val firstFloorPost = data.first_floor_post
                val firstPost = postList.firstOrNull { it.floor == 1 }
                    ?: firstFloorPost?.let { first ->
                        val subPosts = first.sub_post_list
                        first.copy(
                            author_id = threadAuthor.id,
                            author = threadAuthor,
                            from_forum = forum,
                            tid = thread.id,
                            sub_post_list = subPosts?.copy(
                                sub_post_list = subPosts.sub_post_list.map { subPost ->
                                    subPost.copy(
                                        author = subPost.author
                                            ?: userList.first { user -> user.id == subPost.author_id }
                                    )
                                }
                            )
                        )
                    }

                val finalResponse = response.copy(
                    data_ = data.copy(
                        post_list = postList,
                        first_floor_post = firstPost,
                    )
                )

                // ✅ API 调用成功时更新 meta（无乐观更新）
                val effectiveThreadId = thread.id.takeIf { it != 0L } ?: threadId
                threadMetaStore.updateFromServer(effectiveThreadId, thread.toThreadMeta())
                // 同时更新楼层 meta 缓存
                val finalData = finalResponse.data_ ?: throw TiebaUnknownException
                val finalPostList = finalData.post_list
                val finalFirstPost = finalData.first_floor_post
                val allPosts = listOfNotNull(finalFirstPost) + finalPostList.filterNot { it.floor == 1 }
                if (allPosts.isNotEmpty()) {
                    updatePostMetaCache(effectiveThreadId, allPosts)
                }

                finalResponse.toThreadPageData()
            }

    override fun threadFlow(threadId: Long): StateFlow<ThreadCard?> =
        threadsCache.map { cache ->
            cache[threadId]
        }.stateIn(
            scope = applicationScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = threadsCache.value[threadId]
        )

    override fun threadsFlow(threadIds: List<Long>): StateFlow<List<ThreadCard>> =
        threadsCache.map { cache ->
            threadIds.mapNotNull { cache[it] }
        }.stateIn(
            scope = applicationScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = threadIds.mapNotNull { threadsCache.value[it] }
        )

    override fun isThreadUpdating(threadId: Long): Flow<Boolean> =
        updatingThreads.map { it.contains(threadId) }

    // ✅ 私有方法：标记帖子开始更新
    internal fun markThreadUpdating(threadId: Long) {
        _updatingThreadsInternal.value = _updatingThreadsInternal.value + threadId
    }

    // ✅ 私有方法：标记帖子完成更新
    internal fun markThreadUpdated(threadId: Long) {
        _updatingThreadsInternal.value = _updatingThreadsInternal.value - threadId
    }

    // ========== Post Meta StateFlow ==========

    override fun postMetaFlow(threadId: Long, postIds: List<Long>): StateFlow<Map<Long, ThreadPostMeta>> =
        postMetaCache.map { cache ->
            val threadPosts = cache[threadId].orEmpty()
            if (postIds.isEmpty()) {
                emptyMap()
            } else {
                postIds.mapNotNull { id ->
                    threadPosts[id]?.let { id to it }
                }.toMap()
            }
        }.stateIn(
            scope = applicationScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    private fun buildPostMeta(post: Post): ThreadPostMeta =
        ThreadPostMeta(
            hasAgree = post.agree?.hasAgree == 1,
            agreeNum = (post.agree?.diffAgreeNum ?: 0L).toInt(),
            subPostCount = post.sub_post_number
        )

    // ✅ 私有方法：更新楼层 meta 缓存
    private fun updatePostMetaCache(threadId: Long, posts: List<Post>) {
        val currentCache = _postMetaCacheInternal.value.toMutableMap()
        val threadPostsMap = (currentCache[threadId] ?: emptyMap()).toMutableMap()

        posts.forEach { post ->
            threadPostsMap[post.id] = buildPostMeta(post)
        }

        while (threadPostsMap.size > MAX_POST_CACHE_SIZE) {
            val oldestKey = threadPostsMap.keys.firstOrNull() ?: break
            threadPostsMap.remove(oldestKey)
        }

        currentCache[threadId] = threadPostsMap
        _postMetaCacheInternal.value = currentCache
    }

    override fun updatePostMeta(
        threadId: Long,
        postId: Long,
        hasAgree: Boolean
    ) {
        val currentCache = _postMetaCacheInternal.value.toMutableMap()
        val threadPostsMap = (currentCache[threadId] ?: emptyMap()).toMutableMap()
        val currentMeta = threadPostsMap[postId] ?: ThreadPostMeta()
        val nextAgreeNum = when {
            hasAgree && !currentMeta.hasAgree -> currentMeta.agreeNum + 1
            !hasAgree && currentMeta.hasAgree -> (currentMeta.agreeNum - 1).coerceAtLeast(0)
            else -> currentMeta.agreeNum
        }
        threadPostsMap.remove(postId)
        threadPostsMap[postId] = currentMeta.copy(
            hasAgree = hasAgree,
            agreeNum = nextAgreeNum
        )
        while (threadPostsMap.size > MAX_POST_CACHE_SIZE) {
            val oldestKey = threadPostsMap.keys.firstOrNull() ?: break
            threadPostsMap.remove(oldestKey)
        }
        currentCache[threadId] = threadPostsMap
        _postMetaCacheInternal.value = currentCache
    }

    // ========== 内部 API：供 ThreadFeedRepository 向缓存写入数据 ==========

    /**
     * 批量更新或插入帖子到缓存（内部方法，仅供 ThreadFeedRepository 使用）
     *
     * @param entities 要更新的帖子实体列表
     */
    override fun upsertThreads(entities: List<ThreadCard>) {
        val currentCache = _threadsCacheInternal.value.toMutableMap()

        // 批量更新或添加
        entities.forEach { entity ->
            currentCache[entity.threadId] = entity
        }

        // 如果超过容量限制，移除最旧的
        while (currentCache.size > MAX_THREAD_CACHE_SIZE) {
            val oldestKey = currentCache.keys.firstOrNull() ?: break
            currentCache.remove(oldestKey)
        }

        _threadsCacheInternal.value = currentCache
    }

}
