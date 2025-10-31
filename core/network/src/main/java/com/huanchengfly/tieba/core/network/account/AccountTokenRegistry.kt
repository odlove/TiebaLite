package com.huanchengfly.tieba.core.network.account

import java.util.concurrent.atomic.AtomicReference

/**
 * Holds the active [AccountTokenProvider] so non-DI aware components can access account tokens.
 */
object AccountTokenRegistry {
    private val providerRef = AtomicReference<AccountTokenProvider>(EmptyAccountTokenProvider)

    fun register(provider: AccountTokenProvider) {
        providerRef.set(provider)
    }

    val current: AccountTokenProvider
        get() = providerRef.get()

    private object EmptyAccountTokenProvider : AccountTokenProvider {
        override val bduss: String? = null
        override val stoken: String? = null
        override val cookie: String? = null
        override val uid: String? = null
        override val zid: String? = null
        override val isLoggedIn: Boolean = false
        override val loginTbs: String? = null
        override val nameShow: String? = null
    }
}
