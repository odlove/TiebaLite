package com.huanchengfly.tieba.post.models.mappers

import com.huanchengfly.tieba.core.common.feed.FeedMetadata
import com.huanchengfly.tieba.core.common.feed.HotTab
import com.huanchengfly.tieba.core.common.feed.HotTopic
import com.huanchengfly.tieba.core.common.feed.ThreadCard
import com.huanchengfly.tieba.core.common.feed.ThreadFeedPage
import com.huanchengfly.tieba.core.common.thread.ThreadMeta
import com.huanchengfly.tieba.post.api.models.protos.FrsTabInfo
import com.huanchengfly.tieba.post.api.models.protos.RecommendTopicList
import com.huanchengfly.tieba.post.api.models.protos.hotThreadList.HotThreadListResponse
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentMap

data class HotThreadListMapped(
    val threadCards: List<ThreadCard>,
    val metaMap: Map<Long, ThreadMeta>,
    val feedPage: ThreadFeedPage,
)

fun HotThreadListResponse.toHotThreadListMapped(): HotThreadListMapped {
    val data = data_
    if (data == null) {
        return HotThreadListMapped(
            threadCards = emptyList(),
            metaMap = emptyMap(),
            feedPage = ThreadFeedPage(),
        )
    }

    val threadInfos = data.threadInfo
    val threadCards = threadInfos.map { it.toThreadCard() }
    val threadIds = threadInfos.map { it.resolveThreadId() }.toImmutableList()
    val metaMap = threadInfos.associate { info ->
        info.resolveThreadId() to info.toThreadMeta()
    }
    val topicList = data.topicList.map { it.toHotTopic() }.toImmutableList()
    val tabList = data.hotThreadTabInfo.map { it.toHotTab() }.toImmutableList()

    val feedPage = ThreadFeedPage(
        threadIds = threadIds,
        metadata = threadIds.associateWith { FeedMetadata() }.toPersistentMap(),
        topicList = topicList,
        tabList = tabList,
    )

    return HotThreadListMapped(
        threadCards = threadCards,
        metaMap = metaMap,
        feedPage = feedPage,
    )
}

private fun RecommendTopicList.toHotTopic(): HotTopic =
    HotTopic(
        topicId = topicId,
        topicName = topicName,
        type = type.toInt(),
        discussNum = discussNum,
        tag = tag.toInt(),
        topicDesc = topicDesc,
        topicPic = topicPic,
    )

private fun FrsTabInfo.toHotTab(): HotTab =
    HotTab(
        tabId = tabId,
        tabType = tabType,
        tabName = tabName,
        tabUrl = tabUrl,
        tabGid = tabGid,
        tabTitle = tabTitle,
        isGeneralTab = isGeneralTab,
        tabCode = tabCode,
        tabVersion = tabVersion.toInt(),
        isDefault = isDefault,
    )
