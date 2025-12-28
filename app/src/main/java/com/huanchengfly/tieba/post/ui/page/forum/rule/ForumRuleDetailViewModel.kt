package com.huanchengfly.tieba.post.ui.page.forum.rule

import androidx.compose.runtime.Immutable
import com.huanchengfly.tieba.core.common.forum.ForumRuleAuthor
import com.huanchengfly.tieba.core.common.forum.ForumRuleDetail
import com.huanchengfly.tieba.core.common.forum.ForumRuleItem
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
import com.huanchengfly.tieba.post.ui.common.PbContentRender
import com.huanchengfly.tieba.post.ui.page.thread.renders
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
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@HiltViewModel
class ForumRuleDetailViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val forumInfoRepository: ForumInfoRepository
) : BaseViewModel<ForumRuleDetailUiIntent, ForumRuleDetailPartialChange, ForumRuleDetailUiState, UiEvent>(dispatcherProvider) {
    override fun createInitialState(): ForumRuleDetailUiState = ForumRuleDetailUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<ForumRuleDetailUiIntent, ForumRuleDetailPartialChange, ForumRuleDetailUiState> =
        ForumRuleDetailPartialChangeProducer(forumInfoRepository)

    private class ForumRuleDetailPartialChangeProducer(
        private val forumInfoRepository: ForumInfoRepository
    ) :
        PartialChangeProducer<ForumRuleDetailUiIntent, ForumRuleDetailPartialChange, ForumRuleDetailUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<ForumRuleDetailUiIntent>): Flow<ForumRuleDetailPartialChange> =
            merge(
                intentFlow.filterIsInstance<ForumRuleDetailUiIntent.Load>()
                    .flatMapConcat { it.producePartialChange() }
            )

        private fun ForumRuleDetailUiIntent.Load.producePartialChange(): Flow<ForumRuleDetailPartialChange.Load> =
            forumInfoRepository
                .forumRuleDetail(forumId)
                .map<ForumRuleDetail, ForumRuleDetailPartialChange.Load> { data ->
                    ForumRuleDetailPartialChange.Load.Success(
                        title = data.title,
                        publishTime = data.publishTime,
                        preface = data.preface,
                        data = data.rules.map { it.toData() }.toImmutableList(),
                        author = data.author
                    )
                }
                .onStart { emit(ForumRuleDetailPartialChange.Load.Start) }
                .catch { emit(ForumRuleDetailPartialChange.Load.Failure(it)) }
    }
}

sealed interface ForumRuleDetailUiIntent : UiIntent {
    data class Load(val forumId: Long) : ForumRuleDetailUiIntent
}

sealed interface ForumRuleDetailPartialChange : PartialChange<ForumRuleDetailUiState> {
    sealed class Load : ForumRuleDetailPartialChange {
        override fun reduce(oldState: ForumRuleDetailUiState): ForumRuleDetailUiState =
            when (this) {
                Start -> oldState.copy(
                    isLoading = true,
                )

                is Success -> oldState.copy(
                    isLoading = false,
                    error = null,
                    title = title,
                    publishTime = publishTime,
                    preface = preface,
                    data = data,
                    author = author?.wrapImmutable()
                )

                is Failure -> oldState.copy(
                    isLoading = false,
                    error = error.wrapImmutable()
                )
            }

        data object Start : Load()

        data class Success(
            val title: String,
            val publishTime: String,
            val preface: String,
            val data: ImmutableList<ForumRuleItemData>,
            val author: ForumRuleAuthor?,
        ) : Load()

        data class Failure(val error: Throwable) : Load()
    }
}

data class ForumRuleDetailUiState(
    val isLoading: Boolean = true,
    val error: ImmutableHolder<Throwable>? = null,

    val title: String = "",
    val publishTime: String = "",
    val preface: String = "",
    val data: ImmutableList<ForumRuleItemData> = persistentListOf(),
    val author: ImmutableHolder<ForumRuleAuthor>? = null,
) : UiState

@Immutable
data class ForumRuleItemData(
    val title: String,
    val contentRenders: ImmutableList<PbContentRender>,
)

private fun ForumRuleItem.toData(): ForumRuleItemData =
    ForumRuleItemData(
        title = title,
        contentRenders = content.renders
    )
