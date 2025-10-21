package com.huanchengfly.tieba.post.ui.page.forum.threadlist

import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.post.api.models.AgreeBean
import com.huanchengfly.tieba.post.api.models.protos.frsPage.Classify
import com.huanchengfly.tieba.post.api.models.protos.frsPage.FrsPageResponse
import com.huanchengfly.tieba.post.api.models.protos.updateAgreeStatus
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaUnknownException
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorCode
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.CommonUiEvent
import com.huanchengfly.tieba.post.arch.DispatcherProvider
import com.huanchengfly.tieba.post.arch.ImmutableHolder
import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.arch.PartialChangeProducer
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.arch.wrapImmutable
import com.huanchengfly.tieba.post.repository.FrsPageRepository
import com.huanchengfly.tieba.post.repository.UserInteractionRepository
import com.huanchengfly.tieba.post.store.MergeStrategy
import com.huanchengfly.tieba.post.store.ThreadStore
import com.huanchengfly.tieba.post.store.mappers.ThreadMapper
import com.huanchengfly.tieba.post.ui.models.ThreadItemData
import com.huanchengfly.tieba.post.ui.models.distinctById
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
import kotlin.math.min

abstract class ForumThreadListViewModel(
    val threadStore: ThreadStore,  // ✅ 公开，供 UI 订阅
    dispatcherProvider: DispatcherProvider
) :
    BaseViewModel<ForumThreadListUiIntent, ForumThreadListPartialChange, ForumThreadListUiState, ForumThreadListUiEvent>(dispatcherProvider) {
    override fun createInitialState(): ForumThreadListUiState = ForumThreadListUiState()

    override fun dispatchEvent(partialChange: ForumThreadListPartialChange): UiEvent? =
        when (partialChange) {
            is ForumThreadListPartialChange.FirstLoad.Failure -> CommonUiEvent.Toast(partialChange.error.getErrorMessage())
            is ForumThreadListPartialChange.Refresh.Failure -> CommonUiEvent.Toast(partialChange.error.getErrorMessage())
            is ForumThreadListPartialChange.LoadMore.Failure -> CommonUiEvent.Toast(partialChange.error.getErrorMessage())
            is ForumThreadListPartialChange.Agree.Failure -> {
                ForumThreadListUiEvent.AgreeFail(
                    partialChange.threadId,
                    partialChange.postId,
                    partialChange.hasAgree,
                    partialChange.error.getErrorCode(),
                    partialChange.error.getErrorMessage()
                )
            }

            else -> null
        }
}

enum class ForumThreadListType {
    Latest, Good
}

@Stable
@HiltViewModel
class LatestThreadListViewModel @Inject constructor(
    private val frsPageRepository: FrsPageRepository,
    private val userInteractionRepository: UserInteractionRepository,
    threadStore: ThreadStore,  // ✅ 注入 ThreadStore
    dispatcherProvider: DispatcherProvider
) : ForumThreadListViewModel(threadStore, dispatcherProvider) {
    override fun createPartialChangeProducer(): PartialChangeProducer<ForumThreadListUiIntent, ForumThreadListPartialChange, ForumThreadListUiState> =
        ForumThreadListPartialChangeProducer(frsPageRepository, userInteractionRepository, threadStore, ForumThreadListType.Latest)
}

@Stable
@HiltViewModel
class GoodThreadListViewModel @Inject constructor(
    private val frsPageRepository: FrsPageRepository,
    private val userInteractionRepository: UserInteractionRepository,
    threadStore: ThreadStore,  // ✅ 注入 ThreadStore
    dispatcherProvider: DispatcherProvider
) : ForumThreadListViewModel(threadStore, dispatcherProvider) {
    override fun createPartialChangeProducer(): PartialChangeProducer<ForumThreadListUiIntent, ForumThreadListPartialChange, ForumThreadListUiState> =
        ForumThreadListPartialChangeProducer(frsPageRepository, userInteractionRepository, threadStore, ForumThreadListType.Good)
}

private class ForumThreadListPartialChangeProducer(
    private val frsPageRepository: FrsPageRepository,
    private val userInteractionRepository: UserInteractionRepository,
    private val threadStore: ThreadStore,  // ✅ 注入 ThreadStore
    val type: ForumThreadListType
) :
    PartialChangeProducer<ForumThreadListUiIntent, ForumThreadListPartialChange, ForumThreadListUiState> {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun toPartialChangeFlow(intentFlow: Flow<ForumThreadListUiIntent>): Flow<ForumThreadListPartialChange> =
        merge(
            intentFlow.filterIsInstance<ForumThreadListUiIntent.FirstLoad>()
                .flatMapConcat { it.producePartialChange() },
            intentFlow.filterIsInstance<ForumThreadListUiIntent.Refresh>()
                .flatMapConcat { it.producePartialChange() },
            intentFlow.filterIsInstance<ForumThreadListUiIntent.LoadMore>()
                .flatMapConcat { it.producePartialChange() },
            intentFlow.filterIsInstance<ForumThreadListUiIntent.Agree>()
                .flatMapConcat { it.producePartialChange() },
        )

    private fun ForumThreadListUiIntent.FirstLoad.producePartialChange() =
        frsPageRepository.frsPage(
            forumName,
            1,
            1,
            sortType.takeIf { type == ForumThreadListType.Latest } ?: -1,
            goodClassifyId.takeIf { type == ForumThreadListType.Good }
        )
            .onEach { response ->
                // ✅ 写入 Store
                val threads = response.data_?.thread_list ?: emptyList()
                if (threads.isNotEmpty()) {
                    threadStore.upsertThreads(
                        threads.map { ThreadMapper.fromProto(it) },
                        MergeStrategy.REPLACE_ALL  // FirstLoad 完全替换
                    )
                }
            }
            .map<FrsPageResponse, ForumThreadListPartialChange.FirstLoad> { response ->
                if (response.data_?.page == null) throw TiebaUnknownException
                val threadIds = response.data_.thread_list.map { it.id }.distinct()
                ForumThreadListPartialChange.FirstLoad.Success(
                    response.data_.forum_rule?.title.takeIf {
                        type == ForumThreadListType.Latest && response.data_.forum_rule?.has_forum_rule == 1
                    },
                    threadIds,  // ✅ 改为返回 IDs
                    response.data_.thread_id_list,
                    (response.data_.forum?.good_classify ?: emptyList()).wrapImmutable(),
                    goodClassifyId.takeIf { type == ForumThreadListType.Good },
                    response.data_.page.has_more == 1
                )
            }
            .onStart { emit(ForumThreadListPartialChange.FirstLoad.Start) }
            .catch { emit(ForumThreadListPartialChange.FirstLoad.Failure(it)) }

    private fun ForumThreadListUiIntent.Refresh.producePartialChange() =
        frsPageRepository.frsPage(
            forumName,
            1,
            1,
            sortType.takeIf { type == ForumThreadListType.Latest } ?: -1,
            goodClassifyId.takeIf { type == ForumThreadListType.Good },
            forceNew = true
        )
            .onEach { response ->
                // ✅ 写入 Store
                val threads = response.data_?.thread_list ?: emptyList()
                if (threads.isNotEmpty()) {
                    threadStore.upsertThreads(
                        threads.map { ThreadMapper.fromProto(it) },
                        MergeStrategy.REPLACE_ALL  // Refresh 完全替换
                    )
                }
            }
            .map<FrsPageResponse, ForumThreadListPartialChange.Refresh> { response ->
                if (response.data_?.page == null) throw TiebaUnknownException
                val threadIds = response.data_.thread_list.map { it.id }.distinct()
                ForumThreadListPartialChange.Refresh.Success(
                    threadIds,  // ✅ 改为返回 IDs
                    response.data_.thread_id_list,
                    (response.data_.forum?.good_classify ?: emptyList()).wrapImmutable(),
                    goodClassifyId.takeIf { type == ForumThreadListType.Good },
                    response.data_.page.has_more == 1
                )
            }
            .onStart { emit(ForumThreadListPartialChange.Refresh.Start) }
            .catch { emit(ForumThreadListPartialChange.Refresh.Failure(it)) }

    private fun ForumThreadListUiIntent.LoadMore.producePartialChange(): Flow<ForumThreadListPartialChange.LoadMore> {
        val flow = if (threadListIds.isNotEmpty()) {
            val size = min(threadListIds.size, 30)
            frsPageRepository.threadList(
                forumId,
                forumName,
                currentPage,
                sortType,
                threadListIds.subList(0, size).joinToString(separator = ",") { "$it" }
            )
                .onEach { response ->
                    // ✅ 写入 Store
                    val threads = response.data_?.thread_list ?: emptyList()
                    if (threads.isNotEmpty()) {
                        threadStore.upsertThreads(
                            threads.map { ThreadMapper.fromProto(it) },
                            MergeStrategy.PREFER_LOCAL_META  // LoadMore 保护乐观更新
                        )
                    }
                }
                .map { response ->
                    if (response.data_ == null) throw TiebaUnknownException
                    val threadIds = response.data_.thread_list.map { it.id }
                    ForumThreadListPartialChange.LoadMore.Success(
                        threadIds = threadIds,  // ✅ 改为返回 IDs
                        threadListIds = threadListIds.drop(size),
                        currentPage = currentPage,
                        hasMore = response.data_.thread_list.isNotEmpty()
                    )
                }
        } else {
            frsPageRepository.frsPage(
                forumName,
                currentPage + 1,
                2,
                sortType.takeIf { type == ForumThreadListType.Latest } ?: -1,
                goodClassifyId.takeIf { type == ForumThreadListType.Good }
            )
                .onEach { response ->
                    // ✅ 写入 Store
                    val threads = response.data_?.thread_list ?: emptyList()
                    if (threads.isNotEmpty()) {
                        threadStore.upsertThreads(
                            threads.map { ThreadMapper.fromProto(it) },
                            MergeStrategy.PREFER_LOCAL_META  // LoadMore 保护乐观更新
                        )
                    }
                }
                .map<FrsPageResponse, ForumThreadListPartialChange.LoadMore> { response ->
                    if (response.data_?.page == null) throw TiebaUnknownException
                    val threadIds = response.data_.thread_list.map { it.id }
                    ForumThreadListPartialChange.LoadMore.Success(
                        threadIds = threadIds,  // ✅ 改为返回 IDs
                        threadListIds = response.data_.thread_id_list,
                        currentPage = currentPage + 1,
                        response.data_.page.has_more == 1
                    )
                }
        }
        return flow
            .onStart { emit(ForumThreadListPartialChange.LoadMore.Start) }
            .catch { emit(ForumThreadListPartialChange.LoadMore.Failure(it)) }
    }

    private fun ForumThreadListUiIntent.Agree.producePartialChange(): Flow<ForumThreadListPartialChange.Agree> {
        var previousHasAgree = 0
        var previousAgreeNum = 0

        return userInteractionRepository.opAgree(
            threadId.toString(),
            postId.toString(),
            hasAgree,
            objType = 3
        )
            .map<AgreeBean, ForumThreadListPartialChange.Agree> {
                ForumThreadListPartialChange.Agree.Success(
                    threadId,
                    hasAgree xor 1
                )
            }
            .catch {
                // ✅ 失败时恢复原始值
                threadStore.updateThreadMeta(threadId) { meta ->
                    meta.copy(
                        hasAgree = previousHasAgree,
                        agreeNum = previousAgreeNum
                    )
                }
                emit(
                    ForumThreadListPartialChange.Agree.Failure(
                        threadId,
                        postId,
                        hasAgree,
                        it
                    )
                )
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
                emit(ForumThreadListPartialChange.Agree.Start(threadId, hasAgree xor 1))
            }
    }
}

sealed interface ForumThreadListUiIntent : UiIntent {
    data class FirstLoad(
        val forumName: String,
        val sortType: Int = -1,
        val goodClassifyId: Int? = null,
    ) : ForumThreadListUiIntent

    data class Refresh(
        val forumName: String,
        val sortType: Int = -1,
        val goodClassifyId: Int? = null,
    ) : ForumThreadListUiIntent

    data class LoadMore(
        val forumId: Long,
        val forumName: String,
        val currentPage: Int,
        val threadListIds: List<Long>,
        val sortType: Int = -1,
        val goodClassifyId: Int? = null,
    ) : ForumThreadListUiIntent

    data class Agree(
        val threadId: Long,
        val postId: Long,
        val hasAgree: Int
    ) : ForumThreadListUiIntent
}

sealed interface ForumThreadListPartialChange : PartialChange<ForumThreadListUiState> {
    sealed class FirstLoad : ForumThreadListPartialChange {
        override fun reduce(oldState: ForumThreadListUiState): ForumThreadListUiState =
            when (this) {
                Start -> oldState
                is Success -> oldState.copy(
                    isRefreshing = false,
                    forumRuleTitle = forumRuleTitle,
                    threadIds = threadIds.toImmutableList(),  // ✅ 改为 threadIds
                    threadListIds = threadListIds.toImmutableList(),
                    goodClassifies = goodClassifies.toImmutableList(),
                    goodClassifyId = goodClassifyId,
                    currentPage = 1,
                    hasMore = hasMore
                )

                is Failure -> oldState.copy(isRefreshing = false)
            }

        data object Start : FirstLoad()

        data class Success(
            val forumRuleTitle: String?,
            val threadIds: List<Long>,  // ✅ 改为 threadIds
            val threadListIds: List<Long>,
            val goodClassifies: List<ImmutableHolder<Classify>>,
            val goodClassifyId: Int?,
            val hasMore: Boolean,
        ) : FirstLoad()

        data class Failure(
            val error: Throwable
        ) : FirstLoad()
    }

    sealed class Refresh : ForumThreadListPartialChange {
        override fun reduce(oldState: ForumThreadListUiState): ForumThreadListUiState =
            when (this) {
                Start -> oldState.copy(isRefreshing = true)
                is Success -> oldState.copy(
                    isRefreshing = false,
                    threadIds = threadIds.toImmutableList(),  // ✅ 改为 threadIds
                    threadListIds = threadListIds.toImmutableList(),
                    goodClassifies = goodClassifies.toImmutableList(),
                    goodClassifyId = goodClassifyId,
                    currentPage = 1,
                    hasMore = hasMore
                )

                is Failure -> oldState.copy(isRefreshing = false)
            }

        data object Start : Refresh()

        data class Success(
            val threadIds: List<Long>,  // ✅ 改为 threadIds
            val threadListIds: List<Long>,
            val goodClassifies: List<ImmutableHolder<Classify>>,
            val goodClassifyId: Int? = null,
            val hasMore: Boolean,
        ) : Refresh()

        data class Failure(
            val error: Throwable
        ) : Refresh()
    }

    sealed class LoadMore : ForumThreadListPartialChange {
        override fun reduce(oldState: ForumThreadListUiState): ForumThreadListUiState =
            when (this) {
                Start -> oldState.copy(isLoadingMore = true)
                is Success -> oldState.copy(
                    isLoadingMore = false,
                    threadIds = (oldState.threadIds + threadIds).distinct().toImmutableList(),  // ✅ 合并去重
                    threadListIds = threadListIds.toImmutableList(),
                    currentPage = currentPage,
                    hasMore = hasMore
                )

                is Failure -> oldState.copy(isLoadingMore = false)
            }

        data object Start : LoadMore()

        data class Success(
            val threadIds: List<Long>,  // ✅ 改为 threadIds
            val threadListIds: List<Long>,
            val currentPage: Int,
            val hasMore: Boolean,
        ) : LoadMore()

        data class Failure(
            val error: Throwable
        ) : LoadMore()
    }

    sealed class Agree : ForumThreadListPartialChange {
        // ✅ 删除 reduce 逻辑，Store 已处理更新
        override fun reduce(oldState: ForumThreadListUiState): ForumThreadListUiState =
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
            val postId: Long,
            val hasAgree: Int,
            val error: Throwable
        ) : Agree()
    }
}

data class ForumThreadListUiState(
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val goodClassifyId: Int? = null,
    val forumRuleTitle: String? = null,
    val threadIds: ImmutableList<Long> = persistentListOf(),  // ✅ 改为 threadIds（替代 threadList）
    val threadListIds: ImmutableList<Long> = persistentListOf(),
    val goodClassifies: ImmutableList<ImmutableHolder<Classify>> = persistentListOf(),
    val currentPage: Int = 1,
    val hasMore: Boolean = true,
) : UiState

sealed interface ForumThreadListUiEvent : UiEvent {
    data class AgreeFail(
        val threadId: Long,
        val postId: Long,
        val hasAgree: Int,
        val errorCode: Int,
        val errorMsg: String
    ) : ForumThreadListUiEvent

    data class Refresh(
        val isGood: Boolean,
        val sortType: Int
    ) : ForumThreadListUiEvent

    data class BackToTop(
        val isGood: Boolean
    ) : ForumThreadListUiEvent
}
