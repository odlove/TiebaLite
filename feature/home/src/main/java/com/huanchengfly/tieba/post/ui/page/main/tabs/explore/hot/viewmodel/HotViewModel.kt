package com.huanchengfly.tieba.post.ui.page.main.tabs.explore.hot.viewmodel

import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.core.network.error.defaultErrorMessage
import com.huanchengfly.tieba.core.common.feed.ThreadFeedPage
import com.huanchengfly.tieba.core.common.repository.ThreadCardRepository
import com.huanchengfly.tieba.core.common.repository.ThreadFeedFacade
import com.huanchengfly.tieba.core.common.repository.UserInteractionFacade
import com.huanchengfly.tieba.core.mvi.BaseViewModel
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.DispatcherProvider
import com.huanchengfly.tieba.core.mvi.PartialChangeProducer
import com.huanchengfly.tieba.core.mvi.UiEvent
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.hot.contract.HotPartialChange
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.hot.contract.HotUiEvent
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.hot.contract.HotUiIntent
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.hot.contract.HotUiState
import dagger.hilt.android.lifecycle.HiltViewModel
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
class HotViewModel @Inject constructor(
    private val threadFeedRepository: ThreadFeedFacade,
    private val userInteractionRepository: UserInteractionFacade,
    val threadCardRepository: ThreadCardRepository,  // ✅ 公开，供 UI 订阅 Repository 缓存
    dispatcherProvider: DispatcherProvider
) : BaseViewModel<HotUiIntent, HotPartialChange, HotUiState, HotUiEvent>(dispatcherProvider) {
    override fun createInitialState(): HotUiState = HotUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<HotUiIntent, HotPartialChange, HotUiState> =
        HotPartialChangeProducer()

    override fun dispatchEvent(partialChange: HotPartialChange): UiEvent? =
        when (partialChange) {
            is HotPartialChange.Agree.Failure -> CommonUiEvent.Toast(partialChange.error.defaultErrorMessage())
            else -> null
        }

    private inner class HotPartialChangeProducer :
        PartialChangeProducer<HotUiIntent, HotPartialChange, HotUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<HotUiIntent>): Flow<HotPartialChange> =
            merge(
                intentFlow.filterIsInstance<HotUiIntent.Load>()
                    .flatMapConcat { produceLoadPartialChange() },
                intentFlow.filterIsInstance<HotUiIntent.RefreshThreadList>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<HotUiIntent.Agree>()
                    .flatMapConcat { it.producePartialChange() },
            )

        private fun produceLoadPartialChange(): Flow<HotPartialChange.Load> =
            threadFeedRepository.hotThreadList("all")
                .map<ThreadFeedPage, HotPartialChange.Load> { feedPage ->
                    HotPartialChange.Load.Success(
                        topicList = feedPage.topicList,
                        tabList = feedPage.tabList,
                        threadIds = feedPage.threadIds
                    )
                }
                .onStart { emit(HotPartialChange.Load.Start) }
                .catch { emit(HotPartialChange.Load.Failure(it)) }

        private fun HotUiIntent.RefreshThreadList.producePartialChange(): Flow<HotPartialChange.RefreshThreadList> =
            threadFeedRepository.hotThreadList(tabCode)
                .map<ThreadFeedPage, HotPartialChange.RefreshThreadList> { feedPage ->
                    HotPartialChange.RefreshThreadList.Success(
                        tabCode = tabCode,
                        threadIds = feedPage.threadIds
                    )
                }
                .onStart { emit(HotPartialChange.RefreshThreadList.Start(tabCode)) }
                .catch { emit(HotPartialChange.RefreshThreadList.Failure(tabCode, it)) }

        private fun HotUiIntent.Agree.producePartialChange(): Flow<HotPartialChange.Agree> {
            // ✅ 提前读取当前状态
            val currentEntity = threadCardRepository.getThreadCard(threadId)
            val previousHasAgree = currentEntity?.hasAgree ?: 0
            val previousAgreeNum = currentEntity?.agreeNum ?: 0

            return userInteractionRepository.opAgree(
                threadId.toString(), postId.toString(), hasAgree, objType = 3
            )
                .map<Any, HotPartialChange.Agree> {
                    HotPartialChange.Agree.Success(
                        threadId,
                        hasAgree xor 1
                    )
                }
                .catch {
                    // ✅ 失败时恢复原始值
                    threadCardRepository.updateAgreeStatus(threadId, previousHasAgree, previousAgreeNum)
                    emit(HotPartialChange.Agree.Failure(threadId, hasAgree, it))
                }
                .onStart {
                    // ✅ 乐观更新
                    threadCardRepository.updateAgreeStatus(
                        threadId,
                        hasAgree xor 1,
                        if (hasAgree == 0) previousAgreeNum + 1 else previousAgreeNum - 1
                    )
                    emit(HotPartialChange.Agree.Start(threadId, hasAgree xor 1))
                }
        }
    }
}
