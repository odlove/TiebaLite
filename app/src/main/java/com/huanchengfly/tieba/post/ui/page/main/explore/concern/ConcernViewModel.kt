package com.huanchengfly.tieba.post.ui.page.main.explore.concern

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.api.models.AgreeBean
import com.huanchengfly.tieba.post.api.models.protos.userLike.ConcernData
import com.huanchengfly.tieba.post.api.models.protos.userLike.UserLikeResponse
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.CommonUiEvent
import com.huanchengfly.tieba.post.arch.DispatcherProvider
import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.arch.PartialChangeProducer
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.repository.ForumOperationRepository
import com.huanchengfly.tieba.post.repository.UserInteractionRepository
import com.huanchengfly.tieba.post.store.MergeStrategy
import com.huanchengfly.tieba.post.store.ThreadStore
import com.huanchengfly.tieba.post.store.mappers.ThreadMapper
import com.huanchengfly.tieba.post.utils.appPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

/**
 * ConcernMetadata: 轻量级元数据，用于 ConcernPage 的 State
 *
 * @param recommendType 推荐类型（默认 1 = 普通帖子）
 *                      提供默认值实现用户要求的「优雅降级」
 */
@Immutable
data class ConcernMetadata(
    val recommendType: Int = 1,  // ✅ 默认值 1，优雅降级
)

@Stable
@HiltViewModel
class ConcernViewModel @Inject constructor(
    private val forumOperationRepository: ForumOperationRepository,
    private val userInteractionRepository: UserInteractionRepository,
    val threadStore: ThreadStore,  // ✅ 公开，供 UI 订阅
    dispatcherProvider: DispatcherProvider
) : BaseViewModel<ConcernUiIntent, ConcernPartialChange, ConcernUiState, ConcernUiEvent>(dispatcherProvider) {
    override fun createInitialState(): ConcernUiState = ConcernUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<ConcernUiIntent, ConcernPartialChange, ConcernUiState> =
        ExplorePartialChangeProducer()

    override fun dispatchEvent(partialChange: ConcernPartialChange): UiEvent? =
        when (partialChange) {
            is ConcernPartialChange.Refresh.Failure -> CommonUiEvent.Toast(partialChange.error.getErrorMessage())
            is ConcernPartialChange.LoadMore.Failure -> CommonUiEvent.Toast(partialChange.error.getErrorMessage())
            is ConcernPartialChange.Agree.Failure -> CommonUiEvent.Toast(partialChange.error.getErrorMessage())
            else -> null
        }

    private inner class ExplorePartialChangeProducer : PartialChangeProducer<ConcernUiIntent, ConcernPartialChange, ConcernUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<ConcernUiIntent>): Flow<ConcernPartialChange> =
            merge(
                intentFlow.filterIsInstance<ConcernUiIntent.Refresh>().flatMapConcat { produceRefreshPartialChange() },
                intentFlow.filterIsInstance<ConcernUiIntent.LoadMore>().flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ConcernUiIntent.Agree>().flatMapConcat { it.producePartialChange() },
            )

        private fun produceRefreshPartialChange(): Flow<ConcernPartialChange.Refresh> =
            forumOperationRepository.userLike("", App.INSTANCE.appPreferences.userLikeLastRequestUnix, 1)
                .onEach { response ->
                    // 写入 Store：从 ConcernData 提取 ThreadInfo，转换为 Entity，保护 2 秒内的乐观更新
                    val threadProtos = response.data_?.threadInfo?.mapNotNull { it.threadList } ?: emptyList()
                    val entities = threadProtos.map { ThreadMapper.fromProto(it) }
                    threadStore.upsertThreads(entities, MergeStrategy.PREFER_LOCAL_META)
                }
                .map<UserLikeResponse, ConcernPartialChange.Refresh> {
                    App.INSTANCE.appPreferences.userLikeLastRequestUnix = it.data_?.requestUnix ?: 0L
                    val data = it.toData()

                    // ✅ 构建 threadIds（仅包含有 threadList 的数据）
                    val threadIds = data.mapNotNull { concernData ->
                        concernData.threadList?.id
                    }.distinct().toImmutableList()

                    // ✅ 【性能优化】提前构建索引 Map，避免 O(n²) - O(n) 复杂度
                    val concernByCanonicalId = data.mapNotNull { concernData ->
                        val threadList = concernData.threadList ?: return@mapNotNull null
                        threadList.id to concernData
                    }.toMap()

                    // ✅ 使用 O(1) 查找构建 metadata - 总复杂度 O(n)
                    val metadata = threadIds.associateWith { threadId ->
                        val concernData = concernByCanonicalId[threadId]  // O(1) lookup
                        ConcernMetadata(
                            recommendType = concernData?.recommendType ?: 1  // ✅ 默认值 1，优雅降级
                        )
                    }.toPersistentMap()

                    ConcernPartialChange.Refresh.Success(
                        threadIds = threadIds,
                        metadata = metadata,
                        hasMore = it.data_?.hasMore == 1,
                        nextPageTag = it.data_?.pageTag ?: ""
                    )
                }
                .onStart { emit(ConcernPartialChange.Refresh.Start) }
                .catch { emit(ConcernPartialChange.Refresh.Failure(it)) }

        private fun ConcernUiIntent.LoadMore.producePartialChange(): Flow<ConcernPartialChange.LoadMore> =
            forumOperationRepository.userLike(pageTag, App.INSTANCE.appPreferences.userLikeLastRequestUnix, 2)
                .onEach { response ->
                    // 写入 Store：从 ConcernData 提取 ThreadInfo，转换为 Entity，保护 2 秒内的乐观更新
                    val threadProtos = response.data_?.threadInfo?.mapNotNull { it.threadList } ?: emptyList()
                    val entities = threadProtos.map { ThreadMapper.fromProto(it) }
                    threadStore.upsertThreads(entities, MergeStrategy.PREFER_LOCAL_META)
                }
                .map<UserLikeResponse, ConcernPartialChange.LoadMore> {
                    val data = it.toData()

                    // ✅ 构建 threadIds（仅包含有 threadList 的数据）
                    val threadIds = data.mapNotNull { concernData ->
                        concernData.threadList?.let { threadList ->
                            threadList.threadId.takeIf { it != 0L } ?: threadList.id
                        }
                    }.toImmutableList()

                    // ✅ 【性能优化】提前构建索引 Map，避免 O(n²) - O(n) 复杂度
                    val concernByCanonicalId = data.mapNotNull { concernData ->
                        val threadList = concernData.threadList ?: return@mapNotNull null
                        threadList.id to concernData
                    }.toMap()

                    // ✅ 使用 O(1) 查找构建 metadata - 总复杂度 O(n)
                    val metadata = threadIds.associateWith { threadId ->
                        val concernData = concernByCanonicalId[threadId]  // O(1) lookup
                        ConcernMetadata(
                            recommendType = concernData?.recommendType ?: 1  // ✅ 默认值 1，优雅降级
                        )
                    }.toPersistentMap()

                    ConcernPartialChange.LoadMore.Success(
                        threadIds = threadIds,
                        metadata = metadata,
                        hasMore = it.data_?.hasMore == 1,
                        nextPageTag = it.data_?.pageTag ?: ""
                    )
                }
                .onStart { emit(ConcernPartialChange.LoadMore.Start) }
                .catch { emit(ConcernPartialChange.LoadMore.Failure(error = it)) }

        private fun ConcernUiIntent.Agree.producePartialChange(): Flow<ConcernPartialChange.Agree> {
            var previousHasAgree = 0
            var previousAgreeNum = 0

            return userInteractionRepository.opAgree(
                threadId.toString(), postId.toString(), hasAgree, objType = 3
            )
                .map<AgreeBean, ConcernPartialChange.Agree> {
                    ConcernPartialChange.Agree.Success(threadId, hasAgree xor 1)
                }
                .catch {
                    // ✅ 失败时恢复原始值
                    threadStore.updateThreadMeta(threadId) { meta ->
                        meta.copy(
                            hasAgree = previousHasAgree,
                            agreeNum = previousAgreeNum
                        )
                    }
                    emit(ConcernPartialChange.Agree.Failure(threadId, hasAgree, it))
                }
                .onStart {
                    // ✅ 保存原始值 + 乐观更新
                    threadStore.updateThreadMeta(threadId) { meta ->
                        previousHasAgree = meta.hasAgree
                        previousAgreeNum = meta.agreeNum
                        meta.copy(
                            hasAgree = hasAgree xor 1,
                            agreeNum = if (hasAgree == 0) meta.agreeNum + 1 else meta.agreeNum - 1
                        )
                    }
                    emit(ConcernPartialChange.Agree.Start(threadId, hasAgree xor 1))
                }
        }

        private fun UserLikeResponse.toData(): List<ConcernData> {
            return data_?.threadInfo ?: emptyList()
        }
    }
}

sealed interface ConcernUiIntent : UiIntent {
    data object Refresh : ConcernUiIntent

    data class LoadMore(val pageTag: String) : ConcernUiIntent

    data class Agree(
        val threadId: Long,
        val postId: Long,
        val hasAgree: Int,
    ) : ConcernUiIntent
}

sealed interface ConcernPartialChange : PartialChange<ConcernUiState> {
    sealed class Agree private constructor() : ConcernPartialChange {
        // ✅ 删除 updateAgreeStatus() 方法，Store 已处理更新逻辑

        override fun reduce(oldState: ConcernUiState): ConcernUiState =
            when (this) {
                is Start, is Success, is Failure -> oldState  // ✅ Store 已更新，State 无需变化
            }

        data class Start(
            val threadId: Long,
            val hasAgree: Int
        ) : Agree()

        data class Success(
            val threadId: Long,
            val hasAgree: Int
        ) : Agree()

        data class Failure(
            val threadId: Long,
            val hasAgree: Int,
            val error: Throwable
        ) : Agree()
    }

    sealed class Refresh private constructor() : ConcernPartialChange {
        override fun reduce(oldState: ConcernUiState): ConcernUiState =
            when (this) {
                Start -> oldState.copy(isRefreshing = true)
                is Success -> oldState.copy(
                    isRefreshing = false,
                    threadIds = threadIds,
                    metadata = metadata,  // ✅ Refresh 完全替换 metadata，自动清理僵尸数据
                    hasMore = hasMore,
                    nextPageTag = nextPageTag
                )
                is Failure -> oldState.copy(isRefreshing = false)
            }

        data object Start : Refresh()

        data class Success(
            val threadIds: ImmutableList<Long>,
            val metadata: PersistentMap<Long, ConcernMetadata>,  // ✅ 轻量级元数据
            val hasMore: Boolean,
            val nextPageTag: String,
        ) : Refresh()

        data class Failure(
            val error: Throwable,
        ) : Refresh()
    }

    sealed class LoadMore private constructor() : ConcernPartialChange {
        override fun reduce(oldState: ConcernUiState): ConcernUiState =
            when (this) {
                Start -> oldState.copy(isLoadingMore = true)
                is Success -> {
                    val newThreadIds = (oldState.threadIds + threadIds).distinct().toImmutableList()
                    // ✅ 合并 metadata，只保留 newThreadIds 中的数据，清理僵尸数据
                    val mergedMetadata = (oldState.metadata + metadata).filterKeys { it in newThreadIds }
                    oldState.copy(
                        isLoadingMore = false,
                        threadIds = newThreadIds,
                        metadata = mergedMetadata.toPersistentMap(),  // ✅ 转换为 PersistentMap
                        hasMore = hasMore,
                        nextPageTag = nextPageTag
                    )
                }
                is Failure -> oldState.copy(isLoadingMore = false)
            }

        data object Start : LoadMore()

        data class Success(
            val threadIds: ImmutableList<Long>,
            val metadata: PersistentMap<Long, ConcernMetadata>,  // ✅ 轻量级元数据
            val hasMore: Boolean,
            val nextPageTag: String,
        ) : LoadMore()

        data class Failure(
            val error: Throwable,
        ) : LoadMore()
    }
}

data class ConcernUiState(
    val isRefreshing: Boolean = true,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val nextPageTag: String = "",
    val threadIds: ImmutableList<Long> = persistentListOf(),  // ✅ Store 订阅用的 threadId 列表
    val metadata: PersistentMap<Long, ConcernMetadata> = persistentMapOf(),  // ✅ 轻量级元数据 Map
): UiState

sealed interface ConcernUiEvent : UiEvent