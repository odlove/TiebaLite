package com.huanchengfly.tieba.post.ui.page.forum

import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.core.common.forum.ForumInfo
import com.huanchengfly.tieba.core.common.forum.ForumLikeResult
import com.huanchengfly.tieba.core.common.forum.ForumPageData
import com.huanchengfly.tieba.core.common.forum.ForumSignInfo
import com.huanchengfly.tieba.core.common.forum.ForumSignResult
import com.huanchengfly.tieba.core.common.forum.ForumSignUserInfo
import com.huanchengfly.tieba.core.network.error.getErrorCode
import com.huanchengfly.tieba.core.network.error.getErrorMessage
import com.huanchengfly.tieba.core.network.model.CommonResponse
import com.huanchengfly.tieba.core.mvi.BaseViewModel
import com.huanchengfly.tieba.core.mvi.DispatcherProvider
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.core.mvi.PartialChange
import com.huanchengfly.tieba.core.mvi.PartialChangeProducer
import com.huanchengfly.tieba.core.mvi.UiEvent
import com.huanchengfly.tieba.core.mvi.UiIntent
import com.huanchengfly.tieba.core.mvi.UiState
import com.huanchengfly.tieba.core.mvi.wrapImmutable
import com.huanchengfly.tieba.post.repository.ForumOperationRepository
import com.huanchengfly.tieba.post.repository.FrsPageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@Stable
@HiltViewModel
class ForumViewModel @Inject constructor(
    private val frsPageRepository: FrsPageRepository,
    private val forumOperationRepository: ForumOperationRepository,
    dispatcherProvider: DispatcherProvider
) : BaseViewModel<ForumUiIntent, ForumPartialChange, ForumUiState, ForumUiEvent>(dispatcherProvider) {
    override fun createInitialState(): ForumUiState = ForumUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<ForumUiIntent, ForumPartialChange, ForumUiState> =
        ForumPartialChangeProducer()

    override fun dispatchEvent(partialChange: ForumPartialChange): UiEvent? {
        return when (partialChange) {
            is ForumPartialChange.SignIn.Success -> ForumUiEvent.SignIn.Success(
                partialChange.signBonusPoint,
                partialChange.userSignRank
            )

            is ForumPartialChange.SignIn.Failure -> ForumUiEvent.SignIn.Failure(
                partialChange.error.getErrorCode(),
                partialChange.error.getErrorMessage()
            )

            is ForumPartialChange.Like.Success -> ForumUiEvent.Like.Success(partialChange.data.memberSum)
            is ForumPartialChange.Like.Failure -> ForumUiEvent.Like.Failure(
                partialChange.error.getErrorCode(),
                partialChange.error.getErrorMessage()
            )

            is ForumPartialChange.Unlike.Success -> ForumUiEvent.Unlike.Success
            is ForumPartialChange.Unlike.Failure -> ForumUiEvent.Unlike.Failure(
                partialChange.error.getErrorCode(),
                partialChange.error.getErrorMessage()
            )

            else -> null
        }
    }

    private inner class ForumPartialChangeProducer :
        PartialChangeProducer<ForumUiIntent, ForumPartialChange, ForumUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<ForumUiIntent>): Flow<ForumPartialChange> =
            merge(
                intentFlow.filterIsInstance<ForumUiIntent.Load>()
                    .flatMapConcat { it.produceLoadPartialChange() },
                intentFlow.filterIsInstance<ForumUiIntent.SignIn>()
                    .flatMapConcat { it.produceLoadPartialChange() },
                intentFlow.filterIsInstance<ForumUiIntent.Like>()
                    .flatMapConcat { it.produceLoadPartialChange() },
                intentFlow.filterIsInstance<ForumUiIntent.Unlike>()
                    .flatMapConcat { it.produceLoadPartialChange() },
                intentFlow.filterIsInstance<ForumUiIntent.ToggleShowHeader>()
                    .flatMapConcat { it.produceLoadPartialChange() },
            )

        private fun ForumUiIntent.Load.produceLoadPartialChange() =
            frsPageRepository.frsPage(forumName, 1, 1, sortType, null, true)
                .map<ForumPageData, ForumPartialChange.Load> { data ->
                    ForumPartialChange.Load.Success(
                        data.forum,
                        data.tbs
                    )
                }
                .onStart { emit(ForumPartialChange.Load.Start) }
                .catch { emit(ForumPartialChange.Load.Failure(it)) }

        private fun ForumUiIntent.SignIn.produceLoadPartialChange() =
            forumOperationRepository.sign("$forumId", forumName, tbs)
                .map<ForumSignResult, ForumPartialChange.SignIn> { result ->
                    ForumPartialChange.SignIn.Success(
                        result.signBonusPoint,
                        result.levelUpScore,
                        result.contSignNum,
                        result.userSignRank,
                        result.isSignIn,
                        result.level,
                        result.levelName,
                    )
                }
                .catch { emit(ForumPartialChange.SignIn.Failure(it)) }

        private fun ForumUiIntent.Like.produceLoadPartialChange() =
            forumOperationRepository.likeForum("$forumId", forumName, tbs)
                .map<ForumLikeResult, ForumPartialChange.Like> {
                    ForumPartialChange.Like.Success(it)
                }
                .catch { emit(ForumPartialChange.Like.Failure(it)) }

        private fun ForumUiIntent.Unlike.produceLoadPartialChange() =
            forumOperationRepository.unlikeForum("$forumId", forumName, tbs)
                .map<CommonResponse, ForumPartialChange.Unlike> {
                    ForumPartialChange.Unlike.Success
                }
                .catch { emit(ForumPartialChange.Unlike.Failure(it)) }

        private fun ForumUiIntent.ToggleShowHeader.produceLoadPartialChange() =
            flowOf(ForumPartialChange.ToggleShowHeader(showHeader))
    }
}

sealed interface ForumUiIntent : UiIntent {
    data class Load(
        val forumName: String,
        val sortType: Int = -1
    ) : ForumUiIntent

    data class SignIn(
        val forumId: Long,
        val forumName: String,
        val tbs: String
    ) : ForumUiIntent

    data class Like(
        val forumId: Long,
        val forumName: String,
        val tbs: String
    ) : ForumUiIntent

    data class Unlike(
        val forumId: Long,
        val forumName: String,
        val tbs: String
    ) : ForumUiIntent

    data class ToggleShowHeader(
        val showHeader: Boolean
    ) : ForumUiIntent
}

sealed interface ForumPartialChange : PartialChange<ForumUiState> {
    sealed class Load : ForumPartialChange {
        override fun reduce(oldState: ForumUiState): ForumUiState = when (this) {
            Start -> oldState.copy(isLoading = true)
            is Success -> oldState.copy(
                isLoading = true,
                isError = false,
                forum = forum.wrapImmutable(),
                tbs = tbs
            )

            is Failure -> oldState.copy(isLoading = false, isError = true)
        }

        object Start : Load()

        data class Success(
            val forum: ForumInfo,
            val tbs: String?
        ) : Load()

        data class Failure(
            val error: Throwable
        ) : Load()
    }

    sealed class SignIn : ForumPartialChange {
        override fun reduce(oldState: ForumUiState): ForumUiState = when (this) {
            is Failure -> oldState
            is Success -> oldState.copy(
                forum = oldState.forum?.getImmutable {
                    val previousSignInfo = oldState.forum.get { signInfo }
                    val previousUserInfo = previousSignInfo?.userInfo
                    copy(
                        userLevel = level,
                        levelName = levelName,
                        curScore = oldState.forum.get { curScore } + signBonusPoint,
                        levelupScore = levelUpScore,
                        signInfo = ForumSignInfo(
                            userInfo = ForumSignUserInfo(
                                userId = previousUserInfo?.userId ?: 0L,
                                isSignIn = isSignIn,
                                userSignRank = userSignRank,
                                contSignNum = contSignNum,
                            )
                        )
                    )
                }
            )
        }

        data class Success(
            val signBonusPoint: Int,
            val levelUpScore: Int,
            val contSignNum: Int,
            val userSignRank: Int,
            val isSignIn: Int,
            val level: Int,
            val levelName: String
        ) : SignIn()

        data class Failure(
            val error: Throwable
        ) : SignIn()
    }

    sealed class Like : ForumPartialChange {
        override fun reduce(oldState: ForumUiState): ForumUiState = when (this) {
            is Failure -> oldState
            is Success -> oldState.copy(
                forum = oldState.forum?.getImmutable {
                    copy(
                        isLike = 1,
                        curScore = data.curScore,
                        levelupScore = data.levelUpScore,
                        userLevel = data.levelId,
                        levelName = data.levelName,
                        memberNum = data.memberSum.toIntOrNull() ?: memberNum
                    )
                }
            )
        }

        data class Success(val data: ForumLikeResult) : Like()

        data class Failure(val error: Throwable) : Like()
    }

    sealed class Unlike : ForumPartialChange {
        override fun reduce(oldState: ForumUiState): ForumUiState = when (this) {
            is Failure -> oldState
            is Success -> oldState.copy(
                forum = oldState.forum?.getImmutable { copy(isLike = 0) }
            )
        }

        object Success : Unlike()

        data class Failure(val error: Throwable) : Unlike()
    }

    data class ToggleShowHeader(val showHeader: Boolean) : ForumPartialChange {
        override fun reduce(oldState: ForumUiState): ForumUiState =
            oldState.copy(showForumHeader = showHeader)
    }
}

data class ForumUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val forum: ImmutableHolder<ForumInfo>? = null,
    val tbs: String? = null,
    val showForumHeader: Boolean = true
) : UiState

sealed interface ForumUiEvent : UiEvent {
    sealed interface SignIn : ForumUiEvent {
        data class Success(
            val signBonusPoint: Int,
            val userSignRank: Int,
        ) : SignIn

        data class Failure(
            val errorCode: Int,
            val errorMsg: String,
        ) : SignIn
    }

    sealed interface Like : ForumUiEvent {
        data class Success(
            val memberSum: String
        ) : Like

        data class Failure(
            val errorCode: Int,
            val errorMsg: String,
        ) : Like
    }

    sealed interface Unlike : ForumUiEvent {
        object Success : Like

        data class Failure(
            val errorCode: Int,
            val errorMsg: String,
        ) : Like
    }
}
