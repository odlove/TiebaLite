package com.huanchengfly.tieba.core.common.feed

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

open class FeedMetadata

data class PersonalizedMetadata(
    val personalized: PersonalizedInfo? = null,
    val blocked: Boolean = false,
) : FeedMetadata()

data class ConcernMetadata(
    val recommendType: Int = 1
) : FeedMetadata()

data class PersonalizedInfo(
    val threadId: Long = 0L,
    val dislikeReasons: List<DislikeReason> = emptyList(),
    val extra: String? = null,
)

data class DislikeReason(
    val dislikeReason: String = "",
    val dislikeId: Int = 0,
    val extra: String = "",
)

data class HotTopic(
    val topicId: Long = 0L,
    val topicName: String = "",
    val tag: Int = 0,
)

data class HotTab(
    val tabName: String = "",
    val tabCode: String = "",
)

data class ThreadFeedPage(
    val threadIds: ImmutableList<Long> = persistentListOf(),
    val metadata: PersistentMap<Long, FeedMetadata> = persistentMapOf(),
    val topicList: ImmutableList<HotTopic> = persistentListOf(),
    val tabList: ImmutableList<HotTab> = persistentListOf(),
)
