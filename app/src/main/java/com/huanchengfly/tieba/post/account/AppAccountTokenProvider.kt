package com.huanchengfly.tieba.post.account

import com.huanchengfly.tieba.core.network.account.AccountTokenProvider
import com.huanchengfly.tieba.post.utils.AccountUtil
import javax.inject.Inject

class AppAccountTokenProvider @Inject constructor() : AccountTokenProvider {
    override val bduss: String?
        get() = AccountUtil.getBduss()

    override val stoken: String?
        get() = AccountUtil.getSToken()

    override val cookie: String?
        get() = AccountUtil.getCookie()

    override val uid: String?
        get() = AccountUtil.getUid()

    override val zid: String?
        get() = AccountUtil.getAccountInfo { zid }

    override val isLoggedIn: Boolean
        get() = AccountUtil.isLoggedIn()

    override val loginTbs: String?
        get() = AccountUtil.getLoginInfo()?.tbs

    override val nameShow: String?
        get() = AccountUtil.getLoginInfo()?.nameShow
}
