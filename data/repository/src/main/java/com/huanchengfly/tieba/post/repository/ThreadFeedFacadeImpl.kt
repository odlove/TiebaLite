package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.feed.ConcernMetadata
import com.huanchengfly.tieba.core.common.feed.DislikeReason
import com.huanchengfly.tieba.core.common.feed.FeedMetadata
import com.huanchengfly.tieba.core.common.feed.HotTab
import com.huanchengfly.tieba.core.common.feed.HotTopic
import com.huanchengfly.tieba.core.common.feed.PersonalizedInfo
import com.huanchengfly.tieba.core.common.feed.PersonalizedMetadata
import com.huanchengfly.tieba.core.common.feed.ThreadFeedPage
import com.huanchengfly.tieba.core.common.repository.ThreadFeedFacade
import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaUnknownException
import com.huanchengfly.tieba.post.models.mappers.ThreadMapper
import com.huanchengfly.tieba.post.utils.BlockManager.shouldBlock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

@Singleton
class ThreadFeedFacadeImpl @Inject constructor(
    private val contentRecommendRepository: ContentRecommendRepository,
    private val personalizedRepository: PersonalizedRepository,
    private val forumOperationRepository: ForumOperationRepository,
    private val pbPageRepository: PbPageRepository,
    private val api: ITiebaApi,
    private val forumPreferences: ForumPreferences,
) : ThreadFeedFacade {

    override fun hotThreadList(tabCode: String): Flow<ThreadFeedPage> =
        contentRecommendRepository.hotThreadList(tabCode)
            .onEach { response ->
                val threadProtos = response.data_?.threadInfo ?: emptyList()
                val entities = threadProtos.map { ThreadMapper.fromProto(it) }
                pbPageRepository.upsertThreads(entities)
            }
            .map { response ->
                val threadProtos = response.data_?.threadInfo ?: emptyList()
                val threadIds = threadProtos.map { it.id }.toImmutableList()
                val topicList = (response.data_?.topicList ?: emptyList()).map {
                    HotTopic(
                        topicId = it.topicId,
                        topicName = it.topicName,
                        tag = it.tag.toInt()
                    )
                }.toImmutableList()
                val tabList = (response.data_?.hotThreadTabInfo ?: emptyList()).map {
                    HotTab(
                        tabName = it.tabName,
                        tabCode = it.tabCode
                    )
                }.toImmutableList()
                ThreadFeedPage(
                    threadIds = threadIds,
                    metadata = threadIds.associateWith { FeedMetadata() }.toPersistentMap(),
                    topicList = topicList,
                    tabList = tabList
                )
            }
            .onStart { }
            .catch { throw it }

    override fun personalizedThreads(page: Int): Flow<ThreadFeedPage> =
        personalizedRepository.personalizedFlow(1, page)
            .onEach { response ->
                val threadProtos = response.data_?.thread_list ?: emptyList()
                val entities = threadProtos.map { ThreadMapper.fromProto(it) }
                pbPageRepository.upsertThreads(entities)
            }
            .map { response ->
                val threadProtos = response.data_?.thread_list ?: emptyList()
                val threadIds = threadProtos.map { proto ->
                    val threadId = proto.threadId
                    val id = proto.id
                    threadId.takeIf { it != 0L } ?: id
                }.toImmutableList()
                val threadInfoMap = threadProtos.associateBy { proto ->
                    val threadId = proto.threadId
                    val id = proto.id
                    threadId.takeIf { it != 0L } ?: id
                }

                val personalizedMap = response.data_?.thread_personalized
                    ?.associateBy { it.tid }
                    .orEmpty()

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
                            extra = info.extra
                        )
                    }
                    val blocked = threadInfo?.shouldBlock() == true
                    PersonalizedMetadata(personalized = personalized, blocked = blocked)
                }.toPersistentMap()

                ThreadFeedPage(
                    threadIds = threadIds,
                    metadata = metadata
                )
            }
            .onStart { }
            .catch { throw it }

    override fun concernThreads(pageTag: String, page: Int): Flow<ThreadFeedPage> =
        api.frsPage(
            forumName = pageTag,
            page = page,
            loadType = 1,
            sortType = -1
        )
            .map { it.filterThreadList(forumPreferences) }
            .onEach { response ->
                val threadProtos = response.data_?.thread_list ?: emptyList()
                val entities = threadProtos.map { ThreadMapper.fromProto(it) }
                pbPageRepository.upsertThreads(entities)
            }
            .map { response ->
                val threadProtos = response.data_?.thread_list ?: emptyList()
                val threadIds = threadProtos.map { it.id }.toImmutableList()
                ThreadFeedPage(
                    threadIds = threadIds,
                    metadata = threadIds.associateWith { ConcernMetadata() }.toPersistentMap()
                )
            }
            .onStart { }
            .catch { throw it }

    override fun userLikeThreads(lastRequestUnix: Long, page: Int): Flow<ThreadFeedPage> =
        forumOperationRepository.userLike("", lastRequestUnix, page)
            .onEach { response ->
                val threadProtos = response.data_?.threadInfo?.mapNotNull { it.threadList } ?: emptyList()
                val entities = threadProtos.map { ThreadMapper.fromProto(it) }
                pbPageRepository.upsertThreads(entities)
            }
            .map { response ->
                val data = response.data_ ?: throw TiebaUnknownException
                val threadProtos = data.threadInfo?.mapNotNull { it.threadList } ?: emptyList()
                val threadIds = threadProtos.map { it.id }.distinct().toImmutableList()
                val metadata = threadIds.associateWith { ConcernMetadata() }.toPersistentMap()
                ThreadFeedPage(
                    threadIds = threadIds,
                    metadata = metadata
                )
            }
            .onStart { }
            .catch { throw it }
}
