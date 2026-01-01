package com.huanchengfly.tieba.post.ui.page.main.tabs.home.viewmodel

import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.core.network.model.CommonResponse
import com.huanchengfly.tieba.core.network.error.defaultErrorMessage
import com.huanchengfly.tieba.core.mvi.BaseViewModel
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.DispatcherProvider
import com.huanchengfly.tieba.core.mvi.PartialChangeProducer
import com.huanchengfly.tieba.core.mvi.UiEvent
import com.huanchengfly.tieba.core.common.history.HistoryItem
import com.huanchengfly.tieba.core.common.history.HistoryRepository
import com.huanchengfly.tieba.core.common.repository.ForumRecommendRepository
import com.huanchengfly.tieba.post.repository.ForumOperationRepository
import com.huanchengfly.tieba.post.repository.TopForumRepository
import com.huanchengfly.tieba.post.ui.page.main.tabs.home.contract.HomePartialChange
import com.huanchengfly.tieba.post.ui.page.main.tabs.home.contract.HomeUiEvent
import com.huanchengfly.tieba.post.ui.page.main.tabs.home.contract.HomeUiIntent
import com.huanchengfly.tieba.post.ui.page.main.tabs.home.contract.HomeUiState
import com.huanchengfly.tieba.post.utils.AccountUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.zip
import javax.inject.Inject

@Stable
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val forumRecommendRepository: ForumRecommendRepository,
    private val forumOperationRepository: ForumOperationRepository,
    private val topForumRepository: TopForumRepository,
    private val historyRepository: HistoryRepository,
    dispatcherProvider: DispatcherProvider
) : BaseViewModel<HomeUiIntent, HomePartialChange, HomeUiState, HomeUiEvent>(dispatcherProvider) {
    override fun createInitialState(): HomeUiState = HomeUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<HomeUiIntent, HomePartialChange, HomeUiState> =
        HomePartialChangeProducer()

    override fun dispatchEvent(partialChange: HomePartialChange): UiEvent? =
        when (partialChange) {
            is HomePartialChange.TopForums.Delete.Failure -> CommonUiEvent.Toast(partialChange.errorMessage)
            is HomePartialChange.TopForums.Add.Failure -> CommonUiEvent.Toast(partialChange.errorMessage)
            else -> null
        }

    private inner class HomePartialChangeProducer :
        PartialChangeProducer<HomeUiIntent, HomePartialChange, HomeUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<HomeUiIntent>): Flow<HomePartialChange> {
            return merge(
                intentFlow.filterIsInstance<HomeUiIntent.Refresh>()
                    .flatMapConcat { produceRefreshPartialChangeFlow() },
                intentFlow.filterIsInstance<HomeUiIntent.RefreshHistory>()
                    .flatMapConcat { produceRefreshHistoryPartialChangeFlow() },
                intentFlow.filterIsInstance<HomeUiIntent.TopForums.Delete>()
                    .flatMapConcat { it.toPartialChangeFlow() },
                intentFlow.filterIsInstance<HomeUiIntent.TopForums.Add>()
                    .flatMapConcat { it.toPartialChangeFlow() },
                intentFlow.filterIsInstance<HomeUiIntent.Unfollow>()
                    .flatMapConcat { it.toPartialChangeFlow() },
                intentFlow.filterIsInstance<HomeUiIntent.ToggleHistory>()
                    .flatMapConcat { it.toPartialChangeFlow() }
            )
        }

        @Suppress("USELESS_CAST")
        private fun produceRefreshPartialChangeFlow(): Flow<HomePartialChange.Refresh> =
            historyRepository.observe(HistoryRepository.TYPE_FORUM, 0)
                .zip(
                    forumRecommendRepository.forumRecommend()
                ) { historyForums, forumRecommend ->
                    val forums = forumRecommend.forums.map {
                        HomeUiState.Forum(
                            it.avatar.orEmpty(),
                            it.forumId,
                            it.forumName,
                            it.isSign,
                            it.levelId
                        )
                    }
                    val topForums = mutableListOf<HomeUiState.Forum>()
                    val topForumsDB = topForumRepository.getTopForumIds()
                    topForums.addAll(forums.filter { topForumsDB.contains(it.forumId) })
                    HomePartialChange.Refresh.Success(
                        forums,
                        topForums,
                        historyForums
                    ) as HomePartialChange.Refresh
                }
                .onStart { emit(HomePartialChange.Refresh.Start) }
                .catch { emit(HomePartialChange.Refresh.Failure(it)) }

        @Suppress("USELESS_CAST")
        private fun produceRefreshHistoryPartialChangeFlow(): Flow<HomePartialChange.RefreshHistory> =
            historyRepository.observe(HistoryRepository.TYPE_FORUM, 0)
                .map { HomePartialChange.RefreshHistory.Success(it) as HomePartialChange.RefreshHistory }
                .catch { emit(HomePartialChange.RefreshHistory.Failure(it)) }

        private fun HomeUiIntent.TopForums.Delete.toPartialChangeFlow() =
            flow {
                val deleted = topForumRepository.deleteTopForum(forumId)
                if (deleted) {
                    emit(HomePartialChange.TopForums.Delete.Success(forumId))
                } else {
                    emit(HomePartialChange.TopForums.Delete.Failure("forum $forumId is not top!"))
                }
            }.flowOn(Dispatchers.IO)
                .catch { emit(HomePartialChange.TopForums.Delete.Failure(it.defaultErrorMessage())) }

        private fun HomeUiIntent.TopForums.Add.toPartialChangeFlow() =
            flow {
                val success = topForumRepository.addTopForum(forum.forumId)
                if (success) {
                    emit(HomePartialChange.TopForums.Add.Success(forum))
                } else {
                    emit(HomePartialChange.TopForums.Add.Failure("未知错误"))
                }
            }.flowOn(Dispatchers.IO)
                .catch { emit(HomePartialChange.TopForums.Add.Failure(it.defaultErrorMessage())) }

        private fun HomeUiIntent.Unfollow.toPartialChangeFlow() =
            forumOperationRepository.unlikeForum(forumId, forumName, AccountUtil.requireLoginInfo().tbs)
                .map<CommonResponse, HomePartialChange.Unfollow> {
                    HomePartialChange.Unfollow.Success(forumId)
                }
                .catch { emit(HomePartialChange.Unfollow.Failure(it.defaultErrorMessage())) }

    private fun HomeUiIntent.ToggleHistory.toPartialChangeFlow() =
        flowOf(HomePartialChange.ToggleHistory(!currentExpand))
    }
}
