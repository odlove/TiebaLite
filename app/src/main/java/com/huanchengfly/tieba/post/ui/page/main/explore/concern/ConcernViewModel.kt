package com.huanchengfly.tieba.post.ui.page.main.explore.concern

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.post.api.models.AgreeBean
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.core.mvi.BaseViewModel
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.DispatcherProvider
import com.huanchengfly.tieba.core.mvi.PartialChange
import com.huanchengfly.tieba.core.mvi.PartialChangeProducer
import com.huanchengfly.tieba.core.mvi.UiEvent
import com.huanchengfly.tieba.core.mvi.UiIntent
import com.huanchengfly.tieba.core.mvi.UiState
import com.huanchengfly.tieba.post.models.ThreadFeedPage
import com.huanchengfly.tieba.post.models.ConcernMetadata
import com.huanchengfly.tieba.post.repository.ThreadFeedRepository
import com.huanchengfly.tieba.post.repository.PbPageRepository
import com.huanchengfly.tieba.post.repository.UserInteractionRepository
import com.huanchengfly.tieba.post.store.ThreadStore
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

@Stable
@HiltViewModel
class ConcernViewModel @Inject constructor(
    private val threadFeedRepository: ThreadFeedRepository,
    private val userInteractionRepository: UserInteractionRepository,
    val pbPageRepository: PbPageRepository,  // ✅ 公开，供 UI 订阅 Repository 缓存
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
            threadFeedRepository.userLikeThreads(0, 1)
                .map<ThreadFeedPage, ConcernPartialChange.Refresh> { feedPage ->
                    @Suppress("UNCHECKED_CAST")
                    ConcernPartialChange.Refresh.Success(
                        threadIds = feedPage.threadIds,
                        metadata = feedPage.metadata as PersistentMap<Long, ConcernMetadata>,
                        hasMore = true,  // ThreadFeedPage 不包含 hasMore，由 Repository 管理
                        nextPageTag = ""
                    )
                }
                .onStart { emit(ConcernPartialChange.Refresh.Start) }
                .catch { emit(ConcernPartialChange.Refresh.Failure(it)) }

        private fun ConcernUiIntent.LoadMore.producePartialChange(): Flow<ConcernPartialChange.LoadMore> =
            threadFeedRepository.concernThreads(pageTag, 2)
                .map<ThreadFeedPage, ConcernPartialChange.LoadMore> { feedPage ->
                    @Suppress("UNCHECKED_CAST")
                    ConcernPartialChange.LoadMore.Success(
                        threadIds = feedPage.threadIds,
                        metadata = feedPage.metadata as PersistentMap<Long, ConcernMetadata>,
                        hasMore = true,  // ThreadFeedPage 不包含 hasMore，由 Repository 管理
                        nextPageTag = ""
                    )
                }
                .onStart { emit(ConcernPartialChange.LoadMore.Start) }
                .catch { emit(ConcernPartialChange.LoadMore.Failure(error = it)) }

        private fun ConcernUiIntent.Agree.producePartialChange(): Flow<ConcernPartialChange.Agree> {
            // ✅ 提前读取当前状态
            val currentEntity = pbPageRepository.threadFlow(threadId).value
            val previousHasAgree = currentEntity?.meta?.hasAgree ?: 0
            val previousAgreeNum = currentEntity?.meta?.agreeNum ?: 0

            return userInteractionRepository.opAgree(
                threadId.toString(), postId.toString(), hasAgree, objType = 3
            )
                .map<AgreeBean, ConcernPartialChange.Agree> {
                    ConcernPartialChange.Agree.Success(threadId, hasAgree xor 1)
                }
                .catch {
                    // ✅ 失败时恢复原始值
                    currentEntity?.let { entity ->
                        pbPageRepository.upsertThreads(
                            listOf(
                                entity.copy(
                                    meta = entity.meta.copy(
                                        hasAgree = previousHasAgree,
                                        agreeNum = previousAgreeNum
                                    )
                                )
                            )
                        )
                    }
                    emit(ConcernPartialChange.Agree.Failure(threadId, hasAgree, it))
                }
                .onStart {
                    // ✅ 乐观更新
                    currentEntity?.let { entity ->
                        pbPageRepository.upsertThreads(
                            listOf(
                                entity.copy(
                                    meta = entity.meta.copy(
                                        hasAgree = hasAgree xor 1,
                                        agreeNum = if (hasAgree == 0) entity.meta.agreeNum + 1 else entity.meta.agreeNum - 1
                                    )
                                )
                            )
                        )
                    }
                    emit(ConcernPartialChange.Agree.Start(threadId, hasAgree xor 1))
                }
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