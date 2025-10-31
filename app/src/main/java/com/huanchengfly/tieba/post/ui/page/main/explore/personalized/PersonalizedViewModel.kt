package com.huanchengfly.tieba.post.ui.page.main.explore.personalized

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.post.api.models.AgreeBean
import com.huanchengfly.tieba.core.network.model.CommonResponse
import com.huanchengfly.tieba.post.api.models.protos.personalized.DislikeReason
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
import com.huanchengfly.tieba.post.models.DislikeBean
import com.huanchengfly.tieba.post.models.ThreadFeedPage
import com.huanchengfly.tieba.post.models.PersonalizedMetadata
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
class PersonalizedViewModel @Inject constructor(
    private val threadFeedRepository: ThreadFeedRepository,
    private val userInteractionRepository: UserInteractionRepository,
    val pbPageRepository: PbPageRepository,  // ✅ 公开，供 UI 订阅 Repository 缓存
    dispatcherProvider: DispatcherProvider
) : BaseViewModel<PersonalizedUiIntent, PersonalizedPartialChange, PersonalizedUiState, PersonalizedUiEvent>(dispatcherProvider) {
    override fun createInitialState(): PersonalizedUiState = PersonalizedUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<PersonalizedUiIntent, PersonalizedPartialChange, PersonalizedUiState> =
        ExplorePartialChangeProducer()

    override fun dispatchEvent(partialChange: PersonalizedPartialChange): UiEvent? =
        when (partialChange) {
            is PersonalizedPartialChange.Refresh.Failure -> CommonUiEvent.Toast(partialChange.error.getErrorMessage())
            is PersonalizedPartialChange.LoadMore.Failure -> CommonUiEvent.Toast(partialChange.error.getErrorMessage())
            is PersonalizedPartialChange.Agree.Failure -> CommonUiEvent.Toast(partialChange.error.getErrorMessage())
            is PersonalizedPartialChange.Refresh.Success -> PersonalizedUiEvent.RefreshSuccess(
                partialChange.threadIds.size  // ✅ 使用 threadIds.size 而非 data.size
            )

            else -> null
        }

    private inner class ExplorePartialChangeProducer : PartialChangeProducer<PersonalizedUiIntent, PersonalizedPartialChange, PersonalizedUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<PersonalizedUiIntent>): Flow<PersonalizedPartialChange> =
            merge(
                intentFlow.filterIsInstance<PersonalizedUiIntent.Refresh>().flatMapConcat { produceRefreshPartialChange() },
                intentFlow.filterIsInstance<PersonalizedUiIntent.LoadMore>().flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<PersonalizedUiIntent.Dislike>().flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<PersonalizedUiIntent.Agree>().flatMapConcat { it.producePartialChange() },
            )

        private fun produceRefreshPartialChange(): Flow<PersonalizedPartialChange.Refresh> =
            threadFeedRepository
                .personalizedThreads(1)
                .map<ThreadFeedPage, PersonalizedPartialChange.Refresh> { feedPage ->
                    @Suppress("UNCHECKED_CAST")
                    PersonalizedPartialChange.Refresh.Success(
                        threadIds = feedPage.threadIds,
                        metadata = feedPage.metadata as PersistentMap<Long, PersonalizedMetadata>
                    )
                }
                .onStart { emit(PersonalizedPartialChange.Refresh.Start) }
                .catch { emit(PersonalizedPartialChange.Refresh.Failure(it)) }

        private fun PersonalizedUiIntent.LoadMore.producePartialChange(): Flow<PersonalizedPartialChange.LoadMore> =
            threadFeedRepository
                .personalizedThreads(page)
                .map<ThreadFeedPage, PersonalizedPartialChange.LoadMore> { feedPage ->
                    @Suppress("UNCHECKED_CAST")
                    PersonalizedPartialChange.LoadMore.Success(
                        currentPage = page,
                        threadIds = feedPage.threadIds,
                        metadata = feedPage.metadata as PersistentMap<Long, PersonalizedMetadata>
                    )
                }
                .onStart { emit(PersonalizedPartialChange.LoadMore.Start) }
                .catch { emit(PersonalizedPartialChange.LoadMore.Failure(currentPage = page, error = it)) }

        private fun PersonalizedUiIntent.Dislike.producePartialChange(): Flow<PersonalizedPartialChange.Dislike> =
            userInteractionRepository.submitDislike(
                DislikeBean(
                    threadId.toString(),
                    reasons.joinToString(",") { it.get { dislikeId }.toString() },
                    forumId?.toString(),
                    clickTime,
                    reasons.joinToString(",") { it.get { extra } },
                )
            ).map<CommonResponse, PersonalizedPartialChange.Dislike> { PersonalizedPartialChange.Dislike.Success(threadId) }
                .catch { emit(PersonalizedPartialChange.Dislike.Failure(threadId, it)) }
                .onStart { emit(PersonalizedPartialChange.Dislike.Start(threadId)) }

        private fun PersonalizedUiIntent.Agree.producePartialChange(): Flow<PersonalizedPartialChange.Agree> {
            // ✅ 提前读取当前状态
            val currentEntity = pbPageRepository.threadFlow(threadId).value
            val previousHasAgree = currentEntity?.meta?.hasAgree ?: 0
            val previousAgreeNum = currentEntity?.meta?.agreeNum ?: 0

            return userInteractionRepository
                .opAgree(
                    threadId.toString(), postId.toString(), hasAgree, objType = 3
                )
                .map<AgreeBean, PersonalizedPartialChange.Agree> {
                    PersonalizedPartialChange.Agree.Success(
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
                    emit(PersonalizedPartialChange.Agree.Failure(threadId, hasAgree, it))
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
                    emit(PersonalizedPartialChange.Agree.Start(threadId, hasAgree xor 1))
                }
        }

    }
}

sealed interface PersonalizedUiIntent : UiIntent {
    data object Refresh : PersonalizedUiIntent

    data class LoadMore(val page: Int) : PersonalizedUiIntent

    data class Agree(
        val threadId: Long,
        val postId: Long,
        val hasAgree: Int
    ) : PersonalizedUiIntent

    data class Dislike(
        val forumId: Long?,
        val threadId: Long,
        val reasons: List<ImmutableHolder<DislikeReason>>,
        val clickTime: Long
    ) : PersonalizedUiIntent
}

sealed interface PersonalizedPartialChange : PartialChange<PersonalizedUiState> {
    sealed class Agree private constructor() : PersonalizedPartialChange {
        // ✅ 删除 updateAgreeStatus() 方法，Store 已处理更新逻辑

        override fun reduce(oldState: PersonalizedUiState): PersonalizedUiState =
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

    sealed class Dislike private constructor() : PersonalizedPartialChange {
        override fun reduce(oldState: PersonalizedUiState): PersonalizedUiState =
            when (this) {
                is Start -> {
                    if (!oldState.hiddenThreadIds.contains(threadId)) {
                        oldState.copy(hiddenThreadIds = (oldState.hiddenThreadIds + threadId).toImmutableList())
                    } else {
                        oldState
                    }
                }
                is Success -> {
                    if (!oldState.hiddenThreadIds.contains(threadId)) {
                        oldState.copy(hiddenThreadIds = (oldState.hiddenThreadIds + threadId).toImmutableList())
                    } else {
                        oldState
                    }
                }
                is Failure -> oldState
            }

        data class Start(
            val threadId: Long,
        ) : Dislike()

        data class Success(
            val threadId: Long,
        ) : Dislike()

        data class Failure(
            val threadId: Long,
            val error: Throwable,
        ) : Dislike()
    }

    sealed class Refresh private constructor() : PersonalizedPartialChange {
        override fun reduce(oldState: PersonalizedUiState): PersonalizedUiState =
            when (this) {
                Start -> oldState.copy(isRefreshing = true)
                is Success -> {
                    val oldSize = oldState.threadIds.size
                    val newThreadIds = (threadIds + oldState.threadIds).distinct().toImmutableList()
                    // ✅ Refresh 完全替换 metadata，自动清理僵尸数据
                    val newMetadata = metadata
                    oldState.copy(
                        isRefreshing = false,
                        currentPage = 1,
                        threadIds = newThreadIds,
                        metadata = newMetadata,
                        refreshPosition = if (oldState.threadIds.isEmpty()) 0 else (newThreadIds.size - oldSize),
                    )
                }

                is Failure -> oldState.copy(
                    isRefreshing = false,
                    error = error.wrapImmutable()
                )
            }

        data object Start : Refresh()

        data class Success(
            val threadIds: ImmutableList<Long>,
            val metadata: PersistentMap<Long, PersonalizedMetadata>,  // ✅ 使用 PersistentMap
        ) : Refresh()

        data class Failure(
            val error: Throwable,
        ) : Refresh()
    }

    sealed class LoadMore private constructor() : PersonalizedPartialChange {
        override fun reduce(oldState: PersonalizedUiState): PersonalizedUiState =
            when (this) {
                Start -> oldState.copy(isLoadingMore = true)
                is Success -> {
                    val newThreadIds = (oldState.threadIds + threadIds).distinct().toImmutableList()
                    // ✅ 合并 metadata，只保留 newThreadIds 中存在的数据，清理僵尸数据
                    val mergedMetadata = (oldState.metadata + metadata).filterKeys { it in newThreadIds }
                    oldState.copy(
                        isLoadingMore = false,
                        currentPage = currentPage,
                        threadIds = newThreadIds,
                        metadata = mergedMetadata.toPersistentMap(),  // ✅ 转换为 PersistentMap
                    )
                }

                is Failure -> oldState.copy(
                    isLoadingMore = false,
                    error = error.wrapImmutable()
                )
            }

        data object Start : LoadMore()

        data class Success(
            val currentPage: Int,
            val threadIds: ImmutableList<Long>,
            val metadata: PersistentMap<Long, PersonalizedMetadata>,  // ✅ 使用 PersistentMap
        ) : LoadMore()

        data class Failure(
            val currentPage: Int,
            val error: Throwable,
        ) : LoadMore()
    }
}

data class PersonalizedUiState(
    val isRefreshing: Boolean = true,
    val isLoadingMore: Boolean = false,
    val error: ImmutableHolder<Throwable>? = null,
    val currentPage: Int = 1,
    val threadIds: ImmutableList<Long> = persistentListOf(),  // Store 订阅用的 threadId 列表
        val metadata: PersistentMap<Long, PersonalizedMetadata> = persistentMapOf(),  // ✅ 不可变 Map，存储 UI 专属元数据
    val hiddenThreadIds: ImmutableList<Long> = persistentListOf(),
    val refreshPosition: Int = 0,
): UiState

sealed interface PersonalizedUiEvent : UiEvent {
    data class RefreshSuccess(val count: Int) : PersonalizedUiEvent
}