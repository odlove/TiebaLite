package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.models.protos.threadList.ThreadListResponse
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaUnknownException
import com.huanchengfly.tieba.post.models.ThreadFeedPage
import com.huanchengfly.tieba.post.models.FeedMetadata
import com.huanchengfly.tieba.post.models.ConcernMetadata
import com.huanchengfly.tieba.post.models.PersonalizedMetadata
import com.huanchengfly.tieba.post.models.mappers.ThreadMapper
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
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
 * - 转换 Proto 数据为 ThreadEntity
 * - 写入 PbPageRepository 缓存
 * - 产出聚合结果 (threadIds + metadata)
 */
@Singleton
class ThreadFeedRepositoryImpl @Inject constructor(
    private val contentRecommendRepository: ContentRecommendRepository,
    private val personalizedRepository: PersonalizedRepository,
    private val frsPageRepository: FrsPageRepository,
    private val forumOperationRepository: ForumOperationRepository,
    private val pbPageRepository: PbPageRepository,
) : ThreadFeedRepository {

    override fun hotThreadList(tabCode: String): Flow<ThreadFeedPage> =
        contentRecommendRepository.hotThreadList(tabCode)
            .onEach { response ->
                // ✅ 写入 PbPageRepository 缓存：转换 Proto -> Entity
                val threadProtos = response.data_?.threadInfo ?: emptyList()
                val entities = threadProtos.map { ThreadMapper.fromProto(it) }
                pbPageRepository.upsertThreads(entities)
            }
            .map { response ->
                // ✅ 构建返回结果
                val threadProtos = response.data_?.threadInfo ?: emptyList()
                val threadIds = threadProtos.map { it.id }.toImmutableList()

                // ✅ 提取热榜特有数据
                val topicList = (response.data_?.topicList ?: emptyList()).toImmutableList()
                val tabList = (response.data_?.hotThreadTabInfo ?: emptyList()).toImmutableList()

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
                // ✅ 写入 PbPageRepository 缓存
                val threadProtos = response.data_?.thread_list ?: emptyList()
                val entities = threadProtos.map { ThreadMapper.fromProto(it) }
                pbPageRepository.upsertThreads(entities)
            }
            .map { response ->
                // ✅ 构建返回结果
                val threadProtos = response.data_?.thread_list ?: emptyList()

                val threadIds = threadProtos.map { proto ->
                    val threadId = proto.threadId
                    val id = proto.id
                    threadId.takeIf { it != 0L } ?: id
                }.toImmutableList()

                ThreadFeedPage(
                    threadIds = threadIds,
                    metadata = threadIds.associateWith { PersonalizedMetadata() }.toPersistentMap()
                )
            }
            .onStart { }
            .catch { throw it }

    override fun concernThreads(pageTag: String, page: Int): Flow<ThreadFeedPage> =
        frsPageRepository.frsPage(
            forumName = pageTag,
            page = page,
            loadType = 1,
            sortType = -1
        )
            .onEach { response ->
                // ✅ 写入 PbPageRepository 缓存
                val threadProtos = response.data_?.thread_list ?: emptyList()
                val entities = threadProtos.map { ThreadMapper.fromProto(it) }
                pbPageRepository.upsertThreads(entities)
            }
            .map { response ->
                // ✅ 构建返回结果
                val threadProtos = response.data_?.thread_list ?: emptyList()
                val threadIds = threadProtos.map { it.id }.toImmutableList()

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
            goodClassifyId = goodClassifyId
        )
            .onEach { response ->
                // ✅ 写入 PbPageRepository 缓存
                val threadProtos = response.data_?.thread_list ?: emptyList()
                val entities = threadProtos.map { ThreadMapper.fromProto(it) }
                pbPageRepository.upsertThreads(entities)
            }
            .map { response ->
                // ✅ 构建返回结果
                if (response.data_ == null) throw TiebaUnknownException

                val threadProtos = response.data_.thread_list
                val threadIds = threadProtos.map { it.id }.toImmutableList()

                ThreadFeedPage(
                    threadIds = threadIds,
                    metadata = threadIds.associateWith { FeedMetadata() }.toPersistentMap()
                )
            }
            .onStart { }
            .catch { throw it }

    override fun userLikeThreads(lastRequestUnix: Long, page: Int): Flow<ThreadFeedPage> =
        forumOperationRepository.userLike("", lastRequestUnix, page)
            .onEach { response ->
                // ✅ 写入 PbPageRepository 缓存：转换 Proto -> Entity
                val threadProtos = response.data_?.threadInfo?.mapNotNull { it.threadList } ?: emptyList()
                val entities = threadProtos.map { ThreadMapper.fromProto(it) }
                pbPageRepository.upsertThreads(entities)
            }
            .map { response ->
                // ✅ 构建返回结果
                val threadProtos = response.data_?.threadInfo?.mapNotNull { it.threadList } ?: emptyList()

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
