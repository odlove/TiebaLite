package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.feed.ThreadFeedPage
import com.huanchengfly.tieba.core.common.forum.ForumRecommendResult
import com.huanchengfly.tieba.core.common.hottopic.HotTopicItem
import kotlinx.coroutines.flow.Flow

/**
 * 内容推荐数据仓库接口
 *
 * 负责处理内容推荐相关的数据获取，包括热门帖子、推荐贴吧、话题列表等
 */
interface ContentRecommendRepository {
    /**
     * 获取热门帖子列表
     *
     * @param tabCode 标签代码（如"all"表示全部）
     * @return 热门帖子列表数据流（common ThreadFeedPage）。
     *         该调用会同步更新线程缓存与 MetaStore。
     */
    fun hotThreadList(
        tabCode: String
    ): Flow<ThreadFeedPage>

    /**
     * 获取推荐贴吧列表
     *
     * @return 推荐贴吧数据流
     */
    fun forumRecommend(): Flow<ForumRecommendResult>

    /**
     * 获取话题列表
     *
     * @return 话题列表数据流
     */
    fun topicList(): Flow<List<HotTopicItem>>
}
