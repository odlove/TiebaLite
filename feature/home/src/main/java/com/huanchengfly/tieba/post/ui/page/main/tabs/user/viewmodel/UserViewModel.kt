package com.huanchengfly.tieba.post.ui.page.main.tabs.user.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huanchengfly.tieba.core.common.account.AccountInfo
import com.huanchengfly.tieba.core.common.repository.UserProfileFacade
import com.huanchengfly.tieba.core.common.user.UserProfileInfo
import com.huanchengfly.tieba.core.network.error.defaultErrorMessage
import com.huanchengfly.tieba.post.ui.page.main.tabs.user.contract.UserUiState
import com.huanchengfly.tieba.post.utils.AccountUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userProfileRepository: UserProfileFacade
) : ViewModel() {
    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    private var refreshJob: Job? = null

    fun refresh() {
        val account = AccountUtil.currentAccount
        if (account == null) {
            _uiState.update { it.copy(isLoading = false, account = null) }
            return
        }

        if (account.loadSuccess) {
            _uiState.update { it.copy(account = account) }
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            userProfileRepository
                .userProfile(account.uid.toLong())
                .catch { error ->
                    error.printStackTrace()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.defaultErrorMessage()
                        )
                    }
                }
                .collect { profile ->
                    applyProfile(account, profile)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            account = account,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun applyProfile(account: AccountInfo, profile: UserProfileInfo) {
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
    }
}
