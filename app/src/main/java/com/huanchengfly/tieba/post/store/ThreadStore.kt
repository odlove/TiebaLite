package com.huanchengfly.tieba.post.store

import com.huanchengfly.tieba.post.models.ThreadEntity
import com.huanchengfly.tieba.post.models.PostEntity
import com.huanchengfly.tieba.post.models.ThreadMeta
import com.huanchengfly.tieba.post.models.PostMeta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * 合并策略（临时兼容）
 *
 * 这个枚举已经从核心 Store 中移除，但仍然被 ViewModel 使用。
 * TODO: 完全移除，改为在 Repository 层处理合并
 */
enum class MergeStrategy {
    REPLACE_ALL,
    PREFER_LOCAL_META,
    MERGE_BY_TIMESTAMP
}

/**
 * 临时兼容层 - ThreadStore 接口
 *
 * 这是一个占位符接口，用于维持 UI 层的兼容性。
 * 当前实现返回空 Flow，因为数据现在由 Repository 层管理。
 * 未来将被完全移除，UI 层应该订阅 Repository 的 StateFlow。
 */
interface ThreadStore {
    fun threadFlow(threadId: Long): Flow<ThreadEntity?> = emptyFlow()
    fun threadsFlow(threadIds: List<Long>): Flow<List<ThreadEntity>> = emptyFlow()
    fun isThreadUpdating(threadId: Long): Flow<Boolean> = emptyFlow()

    // 以下方法为了兼容现有代码，但不做实际操作
    @Deprecated(
        message = "已迁移至 PbPageRepository 和 ThreadFeedRepository，请通过这些新的聚合层获取数据",
        level = DeprecationLevel.WARNING
    )
    suspend fun upsertThreads(
        entities: Collection<ThreadEntity>,
        mergeStrategy: MergeStrategy = MergeStrategy.REPLACE_ALL
    ) {
        // 暂时无操作，数据由 Repository 管理
    }

    @Deprecated(
        message = "已迁移至 PbPageRepository 和 ThreadFeedRepository，请通过这些新的聚合层获取数据",
        level = DeprecationLevel.WARNING
    )
    suspend fun upsertPosts(
        threadId: Long,
        posts: Collection<PostEntity>,
        mergeStrategy: MergeStrategy = MergeStrategy.REPLACE_ALL
    ) {
        // 暂时无操作
    }

    @Deprecated(
        message = "已迁移至 PbPageRepository 和 ThreadFeedRepository，请通过这些新的聚合层获取数据",
        level = DeprecationLevel.WARNING
    )
    suspend fun updateThreadMeta(threadId: Long, block: (ThreadMeta) -> ThreadMeta) {
        // 暂时无操作
    }

    @Deprecated(
        message = "已迁移至 PbPageRepository 和 ThreadFeedRepository，请通过这些新的聚合层获取数据",
        level = DeprecationLevel.WARNING
    )
    suspend fun updatePostMeta(threadId: Long, postId: Long, block: (PostMeta) -> PostMeta) {
        // 暂时无操作
    }
}
