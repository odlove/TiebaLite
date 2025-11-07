package com.huanchengfly.tieba.post.ui.page.thread

import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.core.mvi.BaseViewModel
import com.huanchengfly.tieba.core.mvi.DispatcherProvider
import com.huanchengfly.tieba.core.mvi.PartialChangeProducer
import com.huanchengfly.tieba.core.mvi.UiEvent
import com.huanchengfly.tieba.post.repository.ContentModerationRepository
import com.huanchengfly.tieba.post.repository.PbPageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

@Stable
@HiltViewModel
class ThreadViewModel @Inject constructor(
    val pbPageRepository: PbPageRepository,
    private val contentModerationRepository: ContentModerationRepository,
    dispatcherProvider: DispatcherProvider,
    private val effectMapper: ThreadEffectMapper,
    private val useCaseRegistry: ThreadUseCaseRegistry
) : BaseViewModel<ThreadUiIntent, ThreadPartialChange, ThreadUiState, ThreadPageEffect>(dispatcherProvider) {
    override fun createInitialState(): ThreadUiState = ThreadUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<ThreadUiIntent, ThreadPartialChange, ThreadUiState> =
        ThreadPartialChangeProducer()

    override fun dispatchEvent(partialChange: ThreadPartialChange): UiEvent? =
        effectMapper.map(partialChange)

    fun checkReportPost(postId: String) = contentModerationRepository.checkReportPost(postId)

    private inner class ThreadPartialChangeProducer :
        PartialChangeProducer<ThreadUiIntent, ThreadPartialChange, ThreadUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<ThreadUiIntent>): Flow<ThreadPartialChange> =
            merge(
                intentFlow.filterIsInstance<ThreadUiIntent.Init>().flatMapConcat { useCaseRegistry.execute(it) },
                intentFlow.filterIsInstance<ThreadUiIntent.Load>().flatMapConcat { useCaseRegistry.execute(it) },
                intentFlow.filterIsInstance<ThreadUiIntent.LoadFirstPage>().flatMapConcat { useCaseRegistry.execute(it) },
                intentFlow.filterIsInstance<ThreadUiIntent.LoadMore>().flatMapConcat { useCaseRegistry.execute(it) },
                intentFlow.filterIsInstance<ThreadUiIntent.LoadPrevious>().flatMapConcat { useCaseRegistry.execute(it) },
                intentFlow.filterIsInstance<ThreadUiIntent.LoadLatestPosts>().flatMapConcat { useCaseRegistry.execute(it) },
                intentFlow.filterIsInstance<ThreadUiIntent.LoadMyLatestReply>().flatMapConcat { useCaseRegistry.execute(it) },
                intentFlow.filterIsInstance<ThreadUiIntent.ToggleImmersiveMode>().flatMapConcat { useCaseRegistry.execute(it) },
                intentFlow.filterIsInstance<ThreadUiIntent.AddFavorite>().flatMapConcat { useCaseRegistry.execute(it) },
                intentFlow.filterIsInstance<ThreadUiIntent.RemoveFavorite>().flatMapConcat { useCaseRegistry.execute(it) },
                intentFlow.filterIsInstance<ThreadUiIntent.AgreeThread>().flatMapConcat { useCaseRegistry.execute(it) },
                intentFlow.filterIsInstance<ThreadUiIntent.AgreePost>().flatMapConcat { useCaseRegistry.execute(it) },
                intentFlow.filterIsInstance<ThreadUiIntent.DeletePost>().flatMapConcat { useCaseRegistry.execute(it) },
                intentFlow.filterIsInstance<ThreadUiIntent.DeleteThread>().flatMapConcat { useCaseRegistry.execute(it) },
                intentFlow.filterIsInstance<ThreadUiIntent.UpdateFavoriteMark>().flatMapConcat { useCaseRegistry.execute(it) },
            )
    }
}
