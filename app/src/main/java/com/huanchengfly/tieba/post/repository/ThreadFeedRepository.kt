package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.models.ThreadFeedPage
import kotlinx.coroutines.flow.Flow

/**
 * 线程 Feed 聚合仓库
 *
 * 职责：
 * - 调用不同的 API 仓库获取帖子数据
 * - 转换 Proto 数据为 ThreadEntity
 * - 写入 PbPageRepository 缓存
 * - 产出聚合结果 (threadIds + metadata)
 *
 * 这是 API 层和缓存层之间的聚合层，负责复用逻辑的统一入口
 */
interface ThreadFeedRepository {
    /**
     * 获取热榜帖子
     *
     * @param tabCode 选项卡代码，如 "all"、"picture"、"video" 等
     * @return ThreadFeedPage Flow，包含帖子ID列表
     */
    fun hotThreadList(tabCode: String): Flow<ThreadFeedPage>

    /**
     * 获取推荐帖子
     *
     * @param page 页码
     * @return ThreadFeedPage Flow
     */
    fun personalizedThreads(page: Int): Flow<ThreadFeedPage>

    /**
     * 获取关注列表帖子
     *
     * @param pageTag 分页标签
     * @param page 页码
     * @return ThreadFeedPage Flow，包含帖子ID和推荐类型等元数据
     */
    fun concernThreads(pageTag: String, page: Int): Flow<ThreadFeedPage>

    /**
     * 获取贴吧帖子列表
     *
     * @param forumId 贴吧ID
     * @param forumName 贴吧名称
     * @param page 页码
     * @param loadType 加载类型：1=首页/刷新，2=翻页
     * @param sortType 排序类型：-1=综合，其他值见 API 文档
     * @param goodClassifyId 精品分类ID（可选）
     * @return ThreadFeedPage Flow
     */
    fun forumThreadList(
        forumId: Long,
        forumName: String,
        page: Int,
        loadType: Int = 1,
        sortType: Int = -1,
        goodClassifyId: Int? = null
    ): Flow<ThreadFeedPage>

    /**
     * 获取用户关注的帖子（个人关注列表）
     *
     * @param lastRequestUnix 上次请求的时间戳
     * @param page 页码
     * @return ThreadFeedPage Flow，包含帖子ID和推荐类型等元数据
     */
    fun userLikeThreads(lastRequestUnix: Long, page: Int): Flow<ThreadFeedPage>
}
