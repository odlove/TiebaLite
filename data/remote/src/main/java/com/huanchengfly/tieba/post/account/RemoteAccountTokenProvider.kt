package com.huanchengfly.tieba.post.account

import com.huanchengfly.tieba.core.network.account.AccountCredentialsSource
import com.huanchengfly.tieba.core.network.account.AccountTokenProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteAccountTokenProvider @Inject constructor(
    private val credentials: AccountCredentialsSource
) : AccountTokenProvider {
    override val bduss: String?
        get() = credentials.bduss

    override val stoken: String?
        get() = credentials.stoken

    override val cookie: String?
        get() = credentials.cookie

    override val uid: String?
        get() = credentials.uid

    override val zid: String?
        get() = credentials.zid

    override val isLoggedIn: Boolean
        get() = credentials.isLoggedIn

    override val loginTbs: String?
        get() = credentials.loginTbs

    override val nameShow: String?
        get() = credentials.nameShow
}
