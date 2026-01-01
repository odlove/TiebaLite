package com.huanchengfly.tieba.post.ui.page.main.tabs.user.contract

import com.huanchengfly.tieba.core.common.account.AccountInfo
import com.huanchengfly.tieba.core.mvi.PartialChange

sealed interface UserPartialChange : PartialChange<UserUiState> {
    sealed class Refresh : UserPartialChange {
        override fun reduce(oldState: UserUiState): UserUiState =
            when (this) {
                Start -> oldState.copy(isLoading = true)
                NotLogin -> oldState.copy(isLoading = false, account = null)
                is Success -> {
                    if (isLocal) {
                        oldState.copy(account = account)
                    } else {
                        oldState.copy(isLoading = false, account = account)
                    }
                }
                is Failure -> oldState.copy(isLoading = false)
            }

        data object Start : Refresh()

        data object NotLogin : Refresh()

        data class Success(val account: AccountInfo, val isLocal: Boolean = false) : Refresh()

        data class Failure(val errorMessage: String) : Refresh()
    }
}
