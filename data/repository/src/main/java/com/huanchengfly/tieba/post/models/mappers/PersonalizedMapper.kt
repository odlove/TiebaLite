package com.huanchengfly.tieba.post.models.mappers

import com.huanchengfly.tieba.core.common.feed.DislikeReason
import com.huanchengfly.tieba.core.common.feed.PersonalizedFeedPage
import com.huanchengfly.tieba.core.common.feed.PersonalizedInfo
import com.huanchengfly.tieba.core.common.feed.PersonalizedMetadata
import com.huanchengfly.tieba.core.common.feed.ThreadCard
import com.huanchengfly.tieba.core.common.thread.ThreadMeta
import com.huanchengfly.tieba.post.api.models.protos.personalized.PersonalizedResponse
import com.huanchengfly.tieba.post.utils.BlockManager.shouldBlock
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentMap

data class PersonalizedMapped(
    val threadCards: List<ThreadCard>,
    val metaMap: Map<Long, ThreadMeta>,
    val feedPage: PersonalizedFeedPage,
)

fun PersonalizedResponse.toPersonalizedMapped(): PersonalizedMapped {
    val data = data_ ?: return PersonalizedMapped(
        threadCards = emptyList(),
        metaMap = emptyMap(),
        feedPage = PersonalizedFeedPage(),
    )

    val threadInfos = data.thread_list
    val removedThreadIds = threadInfos
        .filter { it.ala_info != null }
        .map { it.resolveThreadId() }
        .toSet()

    val filteredThreadInfos = if (removedThreadIds.isEmpty()) {
        threadInfos
    } else {
        threadInfos.filter { it.resolveThreadId() !in removedThreadIds }
    }

    val threadCards = filteredThreadInfos.map { it.toThreadCard() }
    val threadIds = filteredThreadInfos.map { it.resolveThreadId() }.toImmutableList()
    val threadInfoMap = filteredThreadInfos.associateBy { it.resolveThreadId() }
    val metaMap = filteredThreadInfos.associate { info ->
        info.resolveThreadId() to info.toThreadMeta()
    }

    val personalizedMap = data.thread_personalized
        .filter { it.tid !in removedThreadIds }
        .associateBy { it.tid }

    val metadata = threadIds.associateWith { threadId ->
        val threadInfo = threadInfoMap[threadId]
        val personalized = personalizedMap[threadId]?.let { info ->
            PersonalizedInfo(
                threadId = info.tid,
                dislikeReasons = info.dislikeResource.map { reason ->
                    DislikeReason(
                        dislikeReason = reason.dislikeReason,
                        dislikeId = reason.dislikeId.toInt(),
                        extra = reason.extra
                    )
                },
                weight = info.weight,
                source = info.source,
                extra = info.extra
            )
        }
        PersonalizedMetadata(
            personalized = personalized,
            blocked = threadInfo?.shouldBlock() == true
        )
    }.toPersistentMap()

    return PersonalizedMapped(
        threadCards = threadCards,
        metaMap = metaMap,
        feedPage = PersonalizedFeedPage(
            threadIds = threadIds,
            metadata = metadata
        )
    )
}
