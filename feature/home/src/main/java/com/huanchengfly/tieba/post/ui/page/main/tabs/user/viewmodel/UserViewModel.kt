package com.huanchengfly.tieba.post.ui.page.main.tabs.user.viewmodel

import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.core.network.error.defaultErrorMessage
import com.huanchengfly.tieba.core.common.repository.UserProfileFacade
import com.huanchengfly.tieba.core.common.user.UserProfileInfo
import com.huanchengfly.tieba.core.mvi.BaseViewModel
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.DispatcherProvider
import com.huanchengfly.tieba.core.mvi.PartialChangeProducer
import com.huanchengfly.tieba.core.mvi.UiEvent
import com.huanchengfly.tieba.core.common.account.AccountInfo
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.ui.page.main.tabs.user.contract.UserPartialChange
import com.huanchengfly.tieba.post.ui.page.main.tabs.user.contract.UserUiEvent
import com.huanchengfly.tieba.post.ui.page.main.tabs.user.contract.UserUiIntent
import com.huanchengfly.tieba.post.ui.page.main.tabs.user.contract.UserUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@Stable
@HiltViewModel
class UserViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val userProfileRepository: UserProfileFacade
) : BaseViewModel<UserUiIntent, UserPartialChange, UserUiState, UserUiEvent>(dispatcherProvider) {
    override fun createInitialState(): UserUiState =
        UserUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<UserUiIntent, UserPartialChange, UserUiState> =
        UserPartialChangeProducer(userProfileRepository)

    override fun dispatchEvent(partialChange: UserPartialChange): UiEvent? =
        when (partialChange) {
            is UserPartialChange.Refresh.Failure -> CommonUiEvent.Toast(partialChange.errorMessage)
            else -> null
        }

    class UserPartialChangeProducer(
        private val userProfileRepository: UserProfileFacade
    ) : PartialChangeProducer<UserUiIntent, UserPartialChange, UserUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<UserUiIntent>): Flow<UserPartialChange> =
            merge(
                intentFlow.filterIsInstance<UserUiIntent.Refresh>().flatMapConcat { it.toPartialChangeFlow() }
            )

        private fun UserUiIntent.Refresh.toPartialChangeFlow(): Flow<UserPartialChange> {
            val account = AccountUtil.currentAccount
            return if (account == null) {
                listOf(UserPartialChange.Refresh.NotLogin).asFlow()
            } else {
                userProfileRepository
                    .userProfile(account.uid.toLong())
                    .map<UserProfileInfo, UserPartialChange> { profile ->
                        account.apply {
                            nameShow = profile.nameShow
                            portrait = profile.portrait.orEmpty()
                            intro = profile.intro
                            sex = profile.sex
                            fansNum = profile.fansNum
                            postNum = profile.postNum
                            threadNum = profile.threadNum
                            concernNum = profile.concernNum
                            tbAge = profile.tbAge
                            age = profile.age
                            birthdayShowStatus = profile.birthdayShowStatus
                            birthdayTime = profile.birthdayTime
                            constellation = profile.constellation
                            tiebaUid = profile.tiebaUid
                            loadSuccess = true
                            updateAll("uid = ?", uid)
                        }
                        UserPartialChange.Refresh.Success(account = account)
                    }
                    .onStart {
                        emit(UserPartialChange.Refresh.Start)
                        if (account.loadSuccess) {
                            emit(
                                UserPartialChange.Refresh.Success(
                                    account = account,
                                    isLocal = true
                                )
                            )
                        }
                    }
                    .catch {
                        it.printStackTrace()
                        emit(UserPartialChange.Refresh.Failure(errorMessage = it.defaultErrorMessage()))
                    }
            }
        }
    }
}
