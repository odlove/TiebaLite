package com.huanchengfly.tieba.post.ui.page.main.explore.personalized

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.api.models.AgreeBean
import com.huanchengfly.tieba.post.api.models.CommonResponse
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.personalized.DislikeReason
import com.huanchengfly.tieba.post.api.models.protos.personalized.PersonalizedResponse
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
import com.huanchengfly.tieba.post.models.DislikeBean
import com.huanchengfly.tieba.post.repository.PersonalizedRepository
import com.huanchengfly.tieba.post.repository.UserInteractionRepository
import com.huanchengfly.tieba.post.store.MergeStrategy
import com.huanchengfly.tieba.post.store.ThreadStore
import com.huanchengfly.tieba.post.store.mappers.ThreadMapper
import com.huanchengfly.tieba.post.ui.models.ThreadItemData
import com.huanchengfly.tieba.post.ui.models.distinctById
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
 * Personalized 页面元数据
 *
 * 存储每个线程的 UI 专属信息（非 Thread Proto 数据）
 *
 * @param personalized 个性化推荐数据（用于不喜欢按钮）
 * @param blocked 是否被屏蔽
 */
@Immutable
data class PersonalizedMetadata(
    val personalized: ImmutableHolder<com.huanchengfly.tieba.post.api.models.protos.personalized.ThreadPersonalized>?,
    val blocked: Boolean
)

@Stable
@HiltViewModel
class PersonalizedViewModel @Inject constructor(
    private val personalizedRepository: PersonalizedRepository,
    private val userInteractionRepository: UserInteractionRepository,
    val threadStore: ThreadStore,  // ✅ 改为 public，供 PersonalizedPage 订阅
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
            personalizedRepository
                .personalizedFlow(1, 1)
                .onEach { response ->
                    // 写入 Store：转换 Proto -> Entity，保护 2 秒内的乐观更新
                    val filteredThreads = response.toData()
                        .filter {
                            !App.INSTANCE.appPreferences.blockVideo || it.get { videoInfo } == null
                        }
                        .filter { it.get { ala_info } == null }
                    val entities = filteredThreads.map { holder ->
                        val (threadInfo) = holder
                        ThreadMapper.fromProto(threadInfo)
                    }
                    threadStore.upsertThreads(entities, MergeStrategy.PREFER_LOCAL_META)
                }
                .map<PersonalizedResponse, PersonalizedPartialChange.Refresh> { response ->
                    val data = response.toData()
                        .filter {
                            !App.INSTANCE.appPreferences.blockVideo || it.get { videoInfo } == null
                        }
                        .filter { it.get { ala_info } == null }
                    val threadPersonalizedData = response.data_?.thread_personalized ?: emptyList()

                    // ✅ 构建 threadIds 和 metadata，不再构建完整 ThreadItemData
                    val threadIds = data.map { holder ->
                        val threadId = holder.get { threadId }
                        val id = holder.get { id }
                        threadId.takeIf { it != 0L } ?: id
                    }.toImmutableList()

                    // ✅ 【性能优化】提前构建索引 Map，避免 O(n²) - O(n) 复杂度
                    val threadByCanonicalId = data.associateBy { holder ->
                        val tid = holder.get { threadId }
                        val id = holder.get { id }
                        tid.takeIf { it != 0L } ?: id
                    }

                    // ✅ 【性能优化】提前构建 personalized 索引，避免二次 O(n²)
                    val personalizedByTid = threadPersonalizedData.associateBy { it.tid }

                    // ✅ 使用 O(1) 查找构建 metadata - 总复杂度 O(n)
                    val metadata = threadIds.associateWith { threadId ->
                        val thread = threadByCanonicalId[threadId]  // O(1) lookup
                        val threadPersonalized = thread?.get { id }?.let { personalizedByTid[it] }  // O(1) lookup
                            ?.wrapImmutable()
                        val threadItemData = if (thread != null) {
                            ThreadItemData(thread = thread, personalized = threadPersonalized)
                        } else {
                            null
                        }
                        PersonalizedMetadata(
                            personalized = threadPersonalized,
                            blocked = threadItemData?.blocked ?: false
                        )
                    }.toPersistentMap()

                    PersonalizedPartialChange.Refresh.Success(
                        threadIds = threadIds,
                        metadata = metadata
                    )
                }
                .onStart { emit(PersonalizedPartialChange.Refresh.Start) }
                .catch { emit(PersonalizedPartialChange.Refresh.Failure(it)) }

        private fun PersonalizedUiIntent.LoadMore.producePartialChange(): Flow<PersonalizedPartialChange.LoadMore> =
            personalizedRepository
                .personalizedFlow(2, page)
                .onEach { response ->
                    // 写入 Store：转换 Proto -> Entity，保护 2 秒内的乐观更新
                    val filteredThreads = response.toData()
                        .filter {
                            !App.INSTANCE.appPreferences.blockVideo || it.get { videoInfo } == null
                        }
                        .filter { it.get { ala_info } == null }
                    val entities = filteredThreads.map { holder ->
                        val (threadInfo) = holder
                        ThreadMapper.fromProto(threadInfo)
                    }
                    threadStore.upsertThreads(entities, MergeStrategy.PREFER_LOCAL_META)
                }
                .map<PersonalizedResponse, PersonalizedPartialChange.LoadMore> { response ->
                    val data = response.toData()
                        .filter {
                            !App.INSTANCE.appPreferences.blockVideo || it.get { videoInfo } == null
                        }
                        .filter { it.get { ala_info } == null }
                    val threadPersonalizedData = response.data_?.thread_personalized ?: emptyList()

                    // ✅ 构建 threadIds 和 metadata
                    val threadIds = data.map { holder ->
                        val threadId = holder.get { threadId }
                        val id = holder.get { id }
                        threadId.takeIf { it != 0L } ?: id
                    }.toImmutableList()

                    // ✅ 【性能优化】提前构建索引 Map，避免 O(n²) - O(n) 复杂度
                    val threadByCanonicalId = data.associateBy { holder ->
                        val tid = holder.get { threadId }
                        val id = holder.get { id }
                        tid.takeIf { it != 0L } ?: id
                    }

                    // ✅ 【性能优化】提前构建 personalized 索引，避免二次 O(n²)
                    val personalizedByTid = threadPersonalizedData.associateBy { it.tid }

                    // ✅ 使用 O(1) 查找构建 metadata - 总复杂度 O(n)
                    val metadata = threadIds.associateWith { threadId ->
                        val thread = threadByCanonicalId[threadId]  // O(1) lookup
                        val threadPersonalized = thread?.get { id }?.let { personalizedByTid[it] }  // O(1) lookup
                            ?.wrapImmutable()
                        val threadItemData = if (thread != null) {
                            ThreadItemData(thread = thread, personalized = threadPersonalized)
                        } else {
                            null
                        }
                        PersonalizedMetadata(
                            personalized = threadPersonalized,
                            blocked = threadItemData?.blocked ?: false
                        )
                    }.toPersistentMap()

                    PersonalizedPartialChange.LoadMore.Success(
                        currentPage = page,
                        threadIds = threadIds,
                        metadata = metadata
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
            var previousHasAgree = 0
            var previousAgreeNum = 0

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
                    threadStore.updateThreadMeta(threadId) { meta ->
                        meta.copy(
                            hasAgree = previousHasAgree,
                            agreeNum = previousAgreeNum
                        )
                    }
                    emit(PersonalizedPartialChange.Agree.Failure(threadId, hasAgree, it))
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
                    emit(PersonalizedPartialChange.Agree.Start(threadId, hasAgree xor 1))
                }
        }

        private fun PersonalizedResponse.toData(): ImmutableList<ImmutableHolder<ThreadInfo>> {
            return (data_?.thread_list ?: emptyList()).wrapImmutable()
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