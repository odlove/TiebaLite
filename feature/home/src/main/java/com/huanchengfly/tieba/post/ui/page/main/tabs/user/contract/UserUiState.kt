package com.huanchengfly.tieba.post.ui.page.main.tabs.user.contract

import com.huanchengfly.tieba.core.common.account.AccountInfo
import com.huanchengfly.tieba.core.mvi.UiState

data class UserUiState(
    val isLoading: Boolean = false,
    val account: AccountInfo? = null
) : UiState
