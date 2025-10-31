package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.models.protos.pbPage.PbPageResponse
import com.huanchengfly.tieba.post.models.ThreadEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * PbPage（帖子详情页）数据仓库接口
 */
interface PbPageRepository {
    /**
     * 获取帖子详情页数据
     *
     * @param threadId 帖子ID
     * @param page 页码（默认为1）
     * @param postId 指定回复ID（默认为0，表示不指定）
     * @param forumId 贴吧ID（可选）
     * @param seeLz 是否只看楼主（默认为false）
     * @param sortType 排序类型（0-正序，1-倒序，2-热门，默认为0）
     * @param back 是否向前翻页（默认为false）
     * @param from 来源标识（例如"store_thread"表示从收藏进入）
     * @param lastPostId 上一次加载的最后一条回复ID（用于增量加载）
     * @return 帖子详情页数据流
     */
    fun pbPage(
        threadId: Long,
        page: Int = 1,
        postId: Long = 0,
        forumId: Long? = null,
        seeLz: Boolean = false,
        sortType: Int = 0,
        back: Boolean = false,
        from: String = "",
        lastPostId: Long? = null,
    ): Flow<PbPageResponse>

    /**
     * 订阅单个帖子的缓存数据
     *
     * @param threadId 帖子ID
     * @return 帖子实体的 StateFlow（可能为 null）
     */
    fun threadFlow(threadId: Long): StateFlow<ThreadEntity?>

    /**
     * 订阅多个帖子的缓存数据
     *
     * @param threadIds 帖子ID列表
     * @return 帖子实体列表的 StateFlow
     */
    fun threadsFlow(threadIds: List<Long>): StateFlow<List<ThreadEntity>>

    /**
     * 订阅帖子是否正在更新
     *
     * @param threadId 帖子ID
     * @return 是否正在更新的 Flow
     */
    fun isThreadUpdating(threadId: Long): Flow<Boolean>

    /**
     * 订阅单个回复的缓存数据
     *
     * @param threadId 帖子ID
     * @param postId 回复ID
     * @return 回复实体的 StateFlow（可能为 null）
     */
    fun postFlow(threadId: Long, postId: Long): StateFlow<com.huanchengfly.tieba.post.models.PostEntity?>

    /**
     * 订阅多个回复的缓存数据
     *
     * @param threadId 帖子ID
     * @param postIds 回复ID列表
     * @return 回复实体列表的 StateFlow
     */
    fun postsFlow(threadId: Long, postIds: List<Long>): StateFlow<List<com.huanchengfly.tieba.post.models.PostEntity>>

    /**
     * 订阅回复是否正在更新
     *
     * @param threadId 帖子ID
     * @param postId 回复ID
     * @return 是否正在更新的 Flow
     */
    fun isPostUpdating(threadId: Long, postId: Long): Flow<Boolean>

    /**
     * 批量更新或插入帖子到缓存（仅供 ThreadFeedRepository 使用）
     *
     * @param entities 要更新的帖子实体列表
     */
    fun upsertThreads(entities: List<com.huanchengfly.tieba.post.models.ThreadEntity>)

    /**
     * 更新单个帖子的 meta 字段（用于乐观更新）
     *
     * 供 ViewModel 层进行乐观更新和回滚，比如点赞状态变更。
     * 调用此方法会立即触发 threadFlow 的所有订阅者重组。
     *
     * @param threadId 帖子 ID
     * @param block 用于修改 meta 的函数，接收旧 meta 返回新 meta
     */
    fun updateThreadMeta(threadId: Long, block: (com.huanchengfly.tieba.post.models.ThreadMeta) -> com.huanchengfly.tieba.post.models.ThreadMeta)

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
    fun updatePostMeta(threadId: Long, postId: Long, block: (com.huanchengfly.tieba.post.models.PostMeta) -> com.huanchengfly.tieba.post.models.PostMeta)
}
