package com.huanchengfly.tieba.core.network.runtime

import java.util.concurrent.atomic.AtomicReference

interface SignSecretProvider {
    val appSecret: String
}

object SignSecretRegistry {
    private val providerRef: AtomicReference<SignSecretProvider> =
        AtomicReference(object : SignSecretProvider {
            override val appSecret: String = DEFAULT_APP_SECRET
        })

    fun register(provider: SignSecretProvider) {
        providerRef.set(provider)
    }

    val current: SignSecretProvider
        get() = providerRef.get()

    private const val DEFAULT_APP_SECRET = "tiebaclient!!!"
}
