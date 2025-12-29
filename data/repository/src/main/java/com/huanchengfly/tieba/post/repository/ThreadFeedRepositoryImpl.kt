package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.core.common.feed.ConcernMetadata
import com.huanchengfly.tieba.core.common.feed.FeedMetadata
import com.huanchengfly.tieba.core.common.feed.PersonalizedFeedPage
import com.huanchengfly.tieba.core.common.feed.ThreadFeedPage
import com.huanchengfly.tieba.core.common.repository.ThreadMetaStore
import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaUnknownException
import com.huanchengfly.tieba.post.models.mappers.resolveThreadId
import com.huanchengfly.tieba.post.models.mappers.toThreadCard
import com.huanchengfly.tieba.post.models.mappers.toThreadMeta
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ThreadFeedRepository 实现类
 *
 * 职责：
 * - 调用不同的 API 仓库获取帖子数据
 * - 转换 Proto 数据为 ThreadCard
 * - 写入 PbPageRepository 缓存
 * - 产出聚合结果 (threadIds + metadata)
 */
@Singleton
class ThreadFeedRepositoryImpl @Inject constructor(
    private val contentRecommendRepository: ContentRecommendRepository,
    private val personalizedRepository: PersonalizedRepository,
    private val pbPageRepository: PbPageRepository,
    private val threadMetaStore: ThreadMetaStore,
    private val frsPageRepository: FrsPageRepository,
    private val api: ITiebaApi,
) : ThreadFeedRepository {

    override fun hotThreadList(tabCode: String): Flow<ThreadFeedPage> =
        contentRecommendRepository.hotThreadList(tabCode)
            .onStart { }
            .catch { throw it }

    override fun personalizedThreads(page: Int): Flow<PersonalizedFeedPage> =
        personalizedRepository.personalizedFlow(1, page)
            .onStart { }
            .catch { throw it }

    override fun concernThreads(pageTag: String, page: Int): Flow<ThreadFeedPage> =
        frsPageRepository.frsPage(
            forumName = pageTag,
            page = page,
            loadType = 1,
            sortType = -1,
            forceNew = true
        )
            .onEach { data ->
                val threadCards = data.threadList
                pbPageRepository.upsertThreads(threadCards)
                val metaMap = threadCards.associate { card ->
                    card.threadId to card.toThreadMeta()
                }
                threadMetaStore.updateFromServer(metaMap)
            }
            .map { data ->
                val threadIds = data.threadList.map { it.threadId }.toImmutableList()
                ThreadFeedPage(
                    threadIds = threadIds,
                    metadata = threadIds.associateWith { ConcernMetadata() }.toPersistentMap()
                )
            }
            .onStart { }
            .catch { throw it }

    override fun forumThreadList(
        forumId: Long,
        forumName: String,
        page: Int,
        loadType: Int,
        sortType: Int,
        goodClassifyId: Int?
    ): Flow<ThreadFeedPage> =
        frsPageRepository.frsPage(
            forumName = forumName,
            page = page,
            loadType = loadType,
            sortType = sortType,
            goodClassifyId = goodClassifyId,
            forceNew = true
        )
            .onEach { data ->
                val threadCards = data.threadList
                pbPageRepository.upsertThreads(threadCards)
                val metaMap = threadCards.associate { card ->
                    card.threadId to card.toThreadMeta()
                }
                threadMetaStore.updateFromServer(metaMap)
            }
            .map { data ->
                val threadIds = data.threadList.map { it.threadId }.toImmutableList()
                ThreadFeedPage(
                    threadIds = threadIds,
                    metadata = threadIds.associateWith { FeedMetadata() }.toPersistentMap()
                )
            }
            .onStart { }
            .catch { throw it }

    override fun userLikeThreads(lastRequestUnix: Long, page: Int): Flow<ThreadFeedPage> =
        api.userLikeFlow("", lastRequestUnix, page)
            .onEach { response ->
                // ✅ 写入 PbPageRepository 缓存：转换 Proto -> Entity
                val threadProtos = response.data_?.threadInfo?.mapNotNull { it.threadList } ?: emptyList()
                val threadCards = threadProtos.map { it.toThreadCard() }
                pbPageRepository.upsertThreads(threadCards)
                val metaMap = threadProtos.associate { proto ->
                    proto.resolveThreadId() to proto.toThreadMeta()
                }
                threadMetaStore.updateFromServer(metaMap)
            }
            .map { response ->
                // ✅ 构建返回结果
                val data = response.data_ ?: throw TiebaUnknownException
                val threadProtos = data.threadInfo?.mapNotNull { it.threadList } ?: emptyList()

                // 构建 threadIds（仅包含有 threadList 的数据）
                val threadIds = threadProtos.map { it.id }.distinct().toImmutableList()

                // 为每个 threadId 创建对应的 metadata
                val metadata = threadIds.associateWith { ConcernMetadata() }.toPersistentMap()

                ThreadFeedPage(
                    threadIds = threadIds,
                    metadata = metadata
                )
            }
            .onStart { }
            .catch { throw it }
}
