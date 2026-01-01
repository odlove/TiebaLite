package com.huanchengfly.tieba.post.ui.page.main.tabs.explore.concern.viewmodel

import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.core.network.error.defaultErrorMessage
import com.huanchengfly.tieba.core.common.feed.ConcernMetadata
import com.huanchengfly.tieba.core.common.feed.ThreadFeedPage
import com.huanchengfly.tieba.core.common.repository.ThreadCardRepository
import com.huanchengfly.tieba.core.common.repository.ThreadFeedFacade
import com.huanchengfly.tieba.core.common.repository.UserInteractionFacade
import com.huanchengfly.tieba.core.mvi.BaseViewModel
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.DispatcherProvider
import com.huanchengfly.tieba.core.mvi.PartialChangeProducer
import com.huanchengfly.tieba.core.mvi.UiEvent
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.concern.contract.ConcernPartialChange
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.concern.contract.ConcernUiEvent
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.concern.contract.ConcernUiIntent
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.concern.contract.ConcernUiState
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
    private val threadFeedRepository: ThreadFeedFacade,
    private val userInteractionRepository: UserInteractionFacade,
    val threadCardRepository: ThreadCardRepository,  // ✅ 公开，供 UI 订阅 Repository 缓存
    dispatcherProvider: DispatcherProvider
) : BaseViewModel<ConcernUiIntent, ConcernPartialChange, ConcernUiState, ConcernUiEvent>(dispatcherProvider) {
    override fun createInitialState(): ConcernUiState = ConcernUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<ConcernUiIntent, ConcernPartialChange, ConcernUiState> =
        ExplorePartialChangeProducer()

    override fun dispatchEvent(partialChange: ConcernPartialChange): UiEvent? =
        when (partialChange) {
            is ConcernPartialChange.Refresh.Failure -> CommonUiEvent.Toast(partialChange.error.defaultErrorMessage())
            is ConcernPartialChange.LoadMore.Failure -> CommonUiEvent.Toast(partialChange.error.defaultErrorMessage())
            is ConcernPartialChange.Agree.Failure -> CommonUiEvent.Toast(partialChange.error.defaultErrorMessage())
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
            val currentEntity = threadCardRepository.getThreadCard(threadId)
            val previousHasAgree = currentEntity?.hasAgree ?: 0
            val previousAgreeNum = currentEntity?.agreeNum ?: 0

            return userInteractionRepository.opAgree(
                threadId.toString(), postId.toString(), hasAgree, objType = 3
            )
                .map<Any, ConcernPartialChange.Agree> {
                    ConcernPartialChange.Agree.Success(threadId, hasAgree xor 1)
                }
                .catch {
                    // ✅ 失败时恢复原始值
                    threadCardRepository.updateAgreeStatus(threadId, previousHasAgree, previousAgreeNum)
                    emit(ConcernPartialChange.Agree.Failure(threadId, hasAgree, it))
                }
                .onStart {
                    // ✅ 乐观更新
                    threadCardRepository.updateAgreeStatus(
                        threadId,
                        hasAgree xor 1,
                        if (hasAgree == 0) previousAgreeNum + 1 else previousAgreeNum - 1
                    )
                    emit(ConcernPartialChange.Agree.Start(threadId, hasAgree xor 1))
                }
        }

    }
}
