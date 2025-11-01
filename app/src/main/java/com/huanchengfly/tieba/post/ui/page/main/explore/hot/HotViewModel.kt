package com.huanchengfly.tieba.post.ui.page.main.explore.hot

import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.post.api.models.AgreeBean
import com.huanchengfly.tieba.post.api.models.protos.FrsTabInfo
import com.huanchengfly.tieba.post.api.models.protos.RecommendTopicList
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.core.mvi.BaseViewModel
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.DispatcherProvider
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.core.mvi.PartialChange
import com.huanchengfly.tieba.core.mvi.PartialChangeProducer
import com.huanchengfly.tieba.core.mvi.UiEvent
import com.huanchengfly.tieba.core.mvi.UiIntent
import com.huanchengfly.tieba.core.mvi.UiState
import com.huanchengfly.tieba.core.mvi.wrapImmutable
import com.huanchengfly.tieba.post.models.ThreadFeedPage
import com.huanchengfly.tieba.post.repository.ThreadFeedRepository
import com.huanchengfly.tieba.post.repository.PbPageRepository
import com.huanchengfly.tieba.post.repository.UserInteractionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
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
    private val threadFeedRepository: ThreadFeedRepository,
    private val userInteractionRepository: UserInteractionRepository,
    val pbPageRepository: PbPageRepository,  // ✅ 公开，供 UI 订阅 Repository 缓存
    dispatcherProvider: DispatcherProvider
) : BaseViewModel<HotUiIntent, HotPartialChange, HotUiState, HotUiEvent>(dispatcherProvider) {
    override fun createInitialState(): HotUiState = HotUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<HotUiIntent, HotPartialChange, HotUiState> =
        HotPartialChangeProducer()

    override fun dispatchEvent(partialChange: HotPartialChange): UiEvent? =
        when (partialChange) {
            is HotPartialChange.Agree.Failure -> CommonUiEvent.Toast(partialChange.error.getErrorMessage())
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
            val currentEntity = pbPageRepository.threadFlow(threadId).value
            val previousHasAgree = currentEntity?.meta?.hasAgree ?: 0
            val previousAgreeNum = currentEntity?.meta?.agreeNum ?: 0

            return userInteractionRepository.opAgree(
                threadId.toString(), postId.toString(), hasAgree, objType = 3
            )
                .map<AgreeBean, HotPartialChange.Agree> {
                    HotPartialChange.Agree.Success(
                        threadId,
                        hasAgree xor 1
                    )
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
                    emit(HotPartialChange.Agree.Failure(threadId, hasAgree, it))
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
                    emit(HotPartialChange.Agree.Start(threadId, hasAgree xor 1))
                }
        }
    }
}

sealed interface HotUiIntent : UiIntent {
    object Load : HotUiIntent

    data class RefreshThreadList(val tabCode: String) : HotUiIntent

    data class Agree(
        val threadId: Long,
        val postId: Long,
        val hasAgree: Int
    ) : HotUiIntent
}

sealed interface HotPartialChange : PartialChange<HotUiState> {
    sealed class Load : HotPartialChange {
        override fun reduce(oldState: HotUiState): HotUiState =
            when (this) {
                Start -> oldState.copy(isRefreshing = true)
                is Success -> oldState.copy(
                    isRefreshing = false,
                    currentTabCode = "all",
                    topicList = topicList.wrapImmutable(),
                    tabList = tabList.wrapImmutable(),
                    threadIds = threadIds
                )

                is Failure -> oldState.copy(isRefreshing = false)
            }

        object Start : Load()

        data class Success(
            val topicList: List<RecommendTopicList>,
            val tabList: List<FrsTabInfo>,
            val threadIds: ImmutableList<Long>,
        ) : Load()

        data class Failure(
            val error: Throwable
        ) : Load()
    }

    sealed class RefreshThreadList : HotPartialChange {
        override fun reduce(oldState: HotUiState): HotUiState =
            when (this) {
                is Start -> oldState.copy(isLoadingThreadList = true, currentTabCode = tabCode)
                is Success -> oldState.copy(
                    isLoadingThreadList = false,
                    currentTabCode = tabCode,
                    threadIds = threadIds
                )

                is Failure -> oldState.copy(isLoadingThreadList = false)
            }

        data class Start(val tabCode: String) : RefreshThreadList()

        data class Success(
            val tabCode: String,
            val threadIds: ImmutableList<Long>,
        ) : RefreshThreadList()

        data class Failure(
            val tabCode: String,
            val error: Throwable
        ) : RefreshThreadList()
    }

    sealed class Agree private constructor() : HotPartialChange {
        // ✅ 删除 updateAgreeStatus() 方法，Store 已处理更新逻辑

        override fun reduce(oldState: HotUiState): HotUiState =
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
}

data class HotUiState(
    val isRefreshing: Boolean = true,
    val currentTabCode: String = "all",
    val isLoadingThreadList: Boolean = false,
    val topicList: ImmutableList<ImmutableHolder<RecommendTopicList>> = persistentListOf(),
    val tabList: ImmutableList<ImmutableHolder<FrsTabInfo>> = persistentListOf(),
    val threadIds: ImmutableList<Long> = persistentListOf(),  // ✅ Store 订阅用的 threadId 列表
) : UiState

sealed interface HotUiEvent : UiEvent
