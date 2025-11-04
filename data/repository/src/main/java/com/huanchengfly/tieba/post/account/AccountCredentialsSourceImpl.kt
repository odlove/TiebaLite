package com.huanchengfly.tieba.post.account

import com.huanchengfly.tieba.core.network.account.AccountCredentialsSource
import com.huanchengfly.tieba.post.data.account.AccountService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountCredentialsSourceImpl @Inject constructor(
    private val accountService: AccountService
) : AccountCredentialsSource {
    override val bduss: String?
        get() = accountService.getBduss()

    override val stoken: String?
        get() = accountService.getSToken()

    override val cookie: String?
        get() = accountService.getCookie()

    override val uid: String?
        get() = accountService.getUid()

    override val zid: String?
        get() = accountService.getAccountInfo { zid }

    override val isLoggedIn: Boolean
        get() = accountService.isLoggedIn()

    override val loginTbs: String?
        get() = accountService.getLoginInfo()?.tbs

    override val nameShow: String?
        get() = accountService.getLoginInfo()?.nameShow
}
