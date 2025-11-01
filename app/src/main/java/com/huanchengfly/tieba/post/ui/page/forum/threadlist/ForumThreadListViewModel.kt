package com.huanchengfly.tieba.post.ui.page.forum.threadlist

import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.post.api.models.AgreeBean
import com.huanchengfly.tieba.post.api.models.protos.frsPage.Classify
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorCode
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
import com.huanchengfly.tieba.post.api.models.protos.frsPage.FrsPageResponse
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaUnknownException
import com.huanchengfly.tieba.post.repository.FrsPageRepository
import com.huanchengfly.tieba.post.repository.PbPageRepository
import com.huanchengfly.tieba.post.repository.UserInteractionRepository
import com.huanchengfly.tieba.post.ui.models.ThreadItemData
import com.huanchengfly.tieba.post.ui.models.distinctById
import com.huanchengfly.tieba.post.utils.AppPreferencesUtils
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
    val pbPageRepository: PbPageRepository,  // ✅ 公开，供 UI 订阅 Repository 缓存
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
    pbPageRepository: PbPageRepository,
    dispatcherProvider: DispatcherProvider,
    private val appPreferences: AppPreferencesUtils
) : ForumThreadListViewModel(pbPageRepository, dispatcherProvider) {
    override fun createPartialChangeProducer(): PartialChangeProducer<ForumThreadListUiIntent, ForumThreadListPartialChange, ForumThreadListUiState> =
        ForumThreadListPartialChangeProducer(
            frsPageRepository,
            userInteractionRepository,
            pbPageRepository,
            ForumThreadListType.Latest,
            appPreferences
        )
}

@Stable
@HiltViewModel
class GoodThreadListViewModel @Inject constructor(
    private val frsPageRepository: FrsPageRepository,
    private val userInteractionRepository: UserInteractionRepository,
    pbPageRepository: PbPageRepository,
    dispatcherProvider: DispatcherProvider,
    private val appPreferences: AppPreferencesUtils
) : ForumThreadListViewModel(pbPageRepository, dispatcherProvider) {
    override fun createPartialChangeProducer(): PartialChangeProducer<ForumThreadListUiIntent, ForumThreadListPartialChange, ForumThreadListUiState> =
        ForumThreadListPartialChangeProducer(
            frsPageRepository,
            userInteractionRepository,
            pbPageRepository,
            ForumThreadListType.Good,
            appPreferences
        )
}

private class ForumThreadListPartialChangeProducer(
    private val frsPageRepository: FrsPageRepository,
    private val userInteractionRepository: UserInteractionRepository,
    private val pbPageRepository: PbPageRepository,
    val type: ForumThreadListType,
    private val appPreferences: AppPreferencesUtils
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
            .map<FrsPageResponse, ForumThreadListPartialChange.FirstLoad> { response ->
                val data = response.data_ ?: throw TiebaUnknownException
                val page = data.page ?: throw TiebaUnknownException
                val threadList = data.thread_list.map {
                    ThreadItemData(
                        thread = it.wrapImmutable(),
                        hideBlockedContent = appPreferences.hideBlockedContent
                    )
                }
                val forumRule = data.forum_rule
                ForumThreadListPartialChange.FirstLoad.Success(
                    forumRule?.title.takeIf {
                        type == ForumThreadListType.Latest && forumRule?.has_forum_rule == 1
                    },
                    threadList.map { it.wrapImmutable() },
                    data.thread_id_list,
                    (data.forum?.good_classify ?: emptyList()).wrapImmutable(),
                    goodClassifyId.takeIf { type == ForumThreadListType.Good },
                    page.has_more == 1
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
            .map<FrsPageResponse, ForumThreadListPartialChange.Refresh> { response ->
                val data = response.data_ ?: throw TiebaUnknownException
                val page = data.page ?: throw TiebaUnknownException
                val threadList = data.thread_list.map {
                    ThreadItemData(
                        thread = it.wrapImmutable(),
                        hideBlockedContent = appPreferences.hideBlockedContent
                    )
                }
                ForumThreadListPartialChange.Refresh.Success(
                    threadList.map { it.wrapImmutable() },
                    data.thread_id_list,
                    (data.forum?.good_classify ?: emptyList()).wrapImmutable(),
                    goodClassifyId.takeIf { type == ForumThreadListType.Good },
                    page.has_more == 1
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
            ).map { response ->
                val data = response.data_ ?: throw TiebaUnknownException
                val threadList = data.thread_list.map {
                    ThreadItemData(
                        thread = it.wrapImmutable(),
                        hideBlockedContent = appPreferences.hideBlockedContent
                    )
                }
                ForumThreadListPartialChange.LoadMore.Success(
                    threadList = threadList.map { it.wrapImmutable() },
                    threadListIds = threadListIds.drop(size),
                    currentPage = currentPage,
                    hasMore = data.thread_list.isNotEmpty()
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
                .map<FrsPageResponse, ForumThreadListPartialChange.LoadMore> { response ->
                    val data = response.data_ ?: throw TiebaUnknownException
                    val page = data.page ?: throw TiebaUnknownException
                    val threadList = data.thread_list.map {
                        ThreadItemData(
                            thread = it.wrapImmutable(),
                            hideBlockedContent = appPreferences.hideBlockedContent
                        )
                    }
                    ForumThreadListPartialChange.LoadMore.Success(
                        threadList = threadList.map { it.wrapImmutable() },
                        threadListIds = data.thread_id_list,
                        currentPage = currentPage + 1,
                        hasMore = page.has_more == 1
                    )
                }
        }
        return flow
            .onStart { emit(ForumThreadListPartialChange.LoadMore.Start) }
            .catch { emit(ForumThreadListPartialChange.LoadMore.Failure(it)) }
    }

    private fun ForumThreadListUiIntent.Agree.producePartialChange(): Flow<ForumThreadListPartialChange.Agree> {
        // ✅ 提前读取当前状态
        val currentEntity = pbPageRepository.threadFlow(threadId).value
        val previousHasAgree = currentEntity?.meta?.hasAgree ?: 0
        val previousAgreeNum = currentEntity?.meta?.agreeNum ?: 0

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
                    threadList = threadList.toImmutableList(),
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
            val threadList: List<ImmutableHolder<ThreadItemData>>,
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
                    threadList = threadList.toImmutableList(),
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
            val threadList: List<ImmutableHolder<ThreadItemData>>,
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
                    threadList = ((oldState.threadList.map { it.get { this } } + threadList.map { it.get { this } }).distinctById().map { it.wrapImmutable() }).toImmutableList(),
                    threadListIds = threadListIds.toImmutableList(),
                    currentPage = currentPage,
                    hasMore = hasMore
                )

                is Failure -> oldState.copy(isLoadingMore = false)
            }

        data object Start : LoadMore()

        data class Success(
            val threadList: List<ImmutableHolder<ThreadItemData>>,
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
    val threadList: ImmutableList<ImmutableHolder<ThreadItemData>> = persistentListOf(),
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
