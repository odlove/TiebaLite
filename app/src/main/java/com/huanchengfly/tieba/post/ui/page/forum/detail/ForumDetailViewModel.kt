package com.huanchengfly.tieba.post.ui.page.forum.detail

import androidx.compose.runtime.Immutable
import com.huanchengfly.tieba.post.api.models.protos.RecommendForumInfo
import com.huanchengfly.tieba.post.api.models.protos.getForumDetail.GetForumDetailResponse
import com.huanchengfly.tieba.core.mvi.BaseViewModel
import com.huanchengfly.tieba.core.mvi.DispatcherProvider
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.core.mvi.PartialChange
import com.huanchengfly.tieba.core.mvi.PartialChangeProducer
import com.huanchengfly.tieba.core.mvi.UiEvent
import com.huanchengfly.tieba.core.mvi.UiIntent
import com.huanchengfly.tieba.core.mvi.UiState
import com.huanchengfly.tieba.core.mvi.wrapImmutable
import com.huanchengfly.tieba.post.repository.ForumInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@HiltViewModel
class ForumDetailViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val forumInfoRepository: ForumInfoRepository
) : BaseViewModel<ForumDetailUiIntent, ForumDetailPartialChange, ForumDetailUiState, UiEvent>(dispatcherProvider) {
    override fun createInitialState(): ForumDetailUiState = ForumDetailUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<ForumDetailUiIntent, ForumDetailPartialChange, ForumDetailUiState> =
        ForumDetailPartialChangeProducer(forumInfoRepository)

    private class ForumDetailPartialChangeProducer(
        private val forumInfoRepository: ForumInfoRepository
    ) :
        PartialChangeProducer<ForumDetailUiIntent, ForumDetailPartialChange, ForumDetailUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<ForumDetailUiIntent>): Flow<ForumDetailPartialChange> =
            merge(
                intentFlow.filterIsInstance<ForumDetailUiIntent.Load>()
                    .flatMapConcat { it.producePartialChange() }
            )

        private fun ForumDetailUiIntent.Load.producePartialChange(): Flow<ForumDetailPartialChange.Load> =
            forumInfoRepository
                .getForumDetail(forumId)
                .map<GetForumDetailResponse, ForumDetailPartialChange.Load> {
                    val forumInfo = it.data_?.forum_info
                    checkNotNull(forumInfo) { "forumInfo is null" }
                    ForumDetailPartialChange.Load.Success(
                        forumInfo
                    )
                }
                .onStart { emit(ForumDetailPartialChange.Load.Start) }
                .catch { emit(ForumDetailPartialChange.Load.Failure(it)) }
    }
}

sealed interface ForumDetailUiIntent : UiIntent {
    data class Load(val forumId: Long) : ForumDetailUiIntent
}

sealed interface ForumDetailPartialChange : PartialChange<ForumDetailUiState> {
    sealed class Load : ForumDetailPartialChange {
        override fun reduce(oldState: ForumDetailUiState): ForumDetailUiState = when (this) {
            Start -> oldState.copy(
                isLoading = true,
            )

            is Success -> oldState.copy(
                isLoading = false,
                error = null,
                forumInfo = forumInfo.wrapImmutable()
            )

            is Failure -> oldState.copy(
                isLoading = false,
                error = error.wrapImmutable()
            )
        }

        data object Start : Load()

        data class Success(val forumInfo: RecommendForumInfo) : Load()

        data class Failure(val error: Throwable) : Load()
    }
}

@Immutable
data class ForumDetailUiState(
    val isLoading: Boolean = true,
    val error: ImmutableHolder<Throwable>? = null,

    val forumInfo: ImmutableHolder<RecommendForumInfo>? = null,
) : UiState