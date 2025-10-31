package com.huanchengfly.tieba.post.api

import com.huanchengfly.tieba.core.network.account.AccountTokenRegistry

object AccountTokens {
    private val provider get() = AccountTokenRegistry.current

    val bduss: String?
        get() = provider.bduss

    val stoken: String?
        get() = provider.stoken

    val uid: String?
        get() = provider.uid

    val zid: String?
        get() = provider.zid

    val loginTbs: String?
        get() = provider.loginTbs

    val loginNameShow: String?
        get() = provider.nameShow

    fun requireBduss(): String = bduss ?: error("BDUSS is not available")

    fun requireStoken(): String = stoken ?: error("SToken is not available")

    fun requireUid(): String = uid ?: error("UID is not available")

    fun requireLoginTbs(): String = loginTbs ?: error("TBS is not available")
}
