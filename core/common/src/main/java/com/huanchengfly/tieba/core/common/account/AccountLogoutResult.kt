package com.huanchengfly.tieba.core.common.account

data class AccountLogoutResult(
    val success: Boolean,
    val switchedToAccount: AccountInfo? = null
)
