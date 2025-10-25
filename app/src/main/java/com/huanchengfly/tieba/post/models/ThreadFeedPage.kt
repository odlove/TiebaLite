package com.huanchengfly.tieba.post.models

import androidx.compose.runtime.Immutable
import com.huanchengfly.tieba.post.api.models.protos.FrsTabInfo
import com.huanchengfly.tieba.post.api.models.protos.RecommendTopicList
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

/**
 * 线程 Feed 页面的聚合结果
 *
 * 包含帖子ID列表和对应的元数据，用于多种 feed 场景
 * (热榜、推荐、关注、贴吧列表等)
 *
 * @param threadIds 帖子ID列表，用于查询 PbPageRepository.threadsFlow()
 * @param metadata 帖子相关的元数据映射，包含 feed 专属信息
 * @param topicList 推荐话题列表（仅热榜场景使用）
 * @param tabList 热榜分类标签列表（仅热榜场景使用）
 */
@Immutable
data class ThreadFeedPage(
    val threadIds: ImmutableList<Long> = persistentListOf(),
    val metadata: ImmutableMap<Long, FeedMetadata> = persistentMapOf(),
    val topicList: ImmutableList<RecommendTopicList> = persistentListOf(),
    val tabList: ImmutableList<FrsTabInfo> = persistentListOf()
)
