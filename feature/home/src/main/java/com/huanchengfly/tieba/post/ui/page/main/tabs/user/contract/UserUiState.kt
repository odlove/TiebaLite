package com.huanchengfly.tieba.post.ui.page.main.tabs.user.contract

import com.huanchengfly.tieba.core.common.account.AccountInfo

data class UserUiState(
    val isLoading: Boolean = false,
    val account: AccountInfo? = null,
    val errorMessage: String? = null
)
