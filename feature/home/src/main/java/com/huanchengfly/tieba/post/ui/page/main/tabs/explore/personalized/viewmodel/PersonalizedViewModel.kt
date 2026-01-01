package com.huanchengfly.tieba.post.ui.page.main.tabs.explore.personalized.viewmodel

import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.core.network.error.defaultErrorMessage
import com.huanchengfly.tieba.core.common.feed.PersonalizedFeedPage
import com.huanchengfly.tieba.core.common.interaction.DislikeRequest
import com.huanchengfly.tieba.core.common.repository.ThreadCardRepository
import com.huanchengfly.tieba.core.common.repository.ThreadFeedFacade
import com.huanchengfly.tieba.core.common.repository.UserInteractionFacade
import com.huanchengfly.tieba.core.mvi.BaseViewModel
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.DispatcherProvider
import com.huanchengfly.tieba.core.mvi.PartialChangeProducer
import com.huanchengfly.tieba.core.mvi.UiEvent
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.personalized.contract.PersonalizedPartialChange
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.personalized.contract.PersonalizedUiEvent
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.personalized.contract.PersonalizedUiIntent
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.personalized.contract.PersonalizedUiState
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
class PersonalizedViewModel @Inject constructor(
    private val threadFeedRepository: ThreadFeedFacade,
    private val userInteractionRepository: UserInteractionFacade,
    val threadCardRepository: ThreadCardRepository,  // ✅ 公开，供 UI 订阅 Repository 缓存
    dispatcherProvider: DispatcherProvider
) : BaseViewModel<PersonalizedUiIntent, PersonalizedPartialChange, PersonalizedUiState, PersonalizedUiEvent>(dispatcherProvider) {
    override fun createInitialState(): PersonalizedUiState = PersonalizedUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<PersonalizedUiIntent, PersonalizedPartialChange, PersonalizedUiState> =
        ExplorePartialChangeProducer()

    override fun dispatchEvent(partialChange: PersonalizedPartialChange): UiEvent? =
        when (partialChange) {
            is PersonalizedPartialChange.Refresh.Failure -> CommonUiEvent.Toast(partialChange.error.defaultErrorMessage())
            is PersonalizedPartialChange.LoadMore.Failure -> CommonUiEvent.Toast(partialChange.error.defaultErrorMessage())
            is PersonalizedPartialChange.Agree.Failure -> CommonUiEvent.Toast(partialChange.error.defaultErrorMessage())
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
                .map<PersonalizedFeedPage, PersonalizedPartialChange.Refresh> { feedPage ->
                    PersonalizedPartialChange.Refresh.Success(
                        threadIds = feedPage.threadIds,
                        metadata = feedPage.metadata
                    )
                }
                .onStart { emit(PersonalizedPartialChange.Refresh.Start) }
                .catch { emit(PersonalizedPartialChange.Refresh.Failure(it)) }

        private fun PersonalizedUiIntent.LoadMore.producePartialChange(): Flow<PersonalizedPartialChange.LoadMore> =
            threadFeedRepository
                .personalizedThreads(page)
                .map<PersonalizedFeedPage, PersonalizedPartialChange.LoadMore> { feedPage ->
                    PersonalizedPartialChange.LoadMore.Success(
                        currentPage = page,
                        threadIds = feedPage.threadIds,
                        metadata = feedPage.metadata
                    )
                }
                .onStart { emit(PersonalizedPartialChange.LoadMore.Start) }
                .catch { emit(PersonalizedPartialChange.LoadMore.Failure(currentPage = page, error = it)) }

        private fun PersonalizedUiIntent.Dislike.producePartialChange(): Flow<PersonalizedPartialChange.Dislike> =
            userInteractionRepository.submitDislike(
                DislikeRequest(
                    threadId = threadId.toString(),
                    dislikeIds = reasons.joinToString(",") { it.dislikeId.toString() },
                    forumId = forumId?.toString(),
                    clickTime = clickTime,
                    extra = reasons.joinToString(",") { it.extra },
                )
            ).map<Unit, PersonalizedPartialChange.Dislike> {
                PersonalizedPartialChange.Dislike.Success(threadId)
            }
                .catch { emit(PersonalizedPartialChange.Dislike.Failure(threadId, it)) }
                .onStart { emit(PersonalizedPartialChange.Dislike.Start(threadId)) }

        private fun PersonalizedUiIntent.Agree.producePartialChange(): Flow<PersonalizedPartialChange.Agree> {
            // ✅ 提前读取当前状态
            val currentEntity = threadCardRepository.getThreadCard(threadId)
            val previousHasAgree = currentEntity?.hasAgree ?: 0
            val previousAgreeNum = currentEntity?.agreeNum ?: 0

            return userInteractionRepository
                .opAgree(
                    threadId.toString(), postId.toString(), hasAgree, objType = 3
                )
                .map<Any, PersonalizedPartialChange.Agree> {
                    PersonalizedPartialChange.Agree.Success(
                        threadId,
                        hasAgree xor 1
                    )
                }
                .catch {
                    // ✅ 失败时恢复原始值
                    threadCardRepository.updateAgreeStatus(threadId, previousHasAgree, previousAgreeNum)
                    emit(PersonalizedPartialChange.Agree.Failure(threadId, hasAgree, it))
                }
                .onStart {
                    // ✅ 乐观更新
                    threadCardRepository.updateAgreeStatus(
                        threadId,
                        hasAgree xor 1,
                        if (hasAgree == 0) previousAgreeNum + 1 else previousAgreeNum - 1
                    )
                    emit(PersonalizedPartialChange.Agree.Start(threadId, hasAgree xor 1))
                }
        }

    }
}
