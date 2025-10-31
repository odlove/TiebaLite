package com.huanchengfly.tieba.core.network.identity

import java.util.concurrent.atomic.AtomicReference

/**
 * Global registry allowing the network layer to obtain the active [ClientIdentityProvider].
 */
object ClientIdentityRegistry {
    private val providerRef = AtomicReference<ClientIdentityProvider>(EmptyClientIdentityProvider)
    private val handlerRef = AtomicReference<BaiduIdHandler>(NoOpBaiduIdHandler)

    /**
     * Registers the [provider] that should be used when constructing network requests.
     */
    fun register(provider: ClientIdentityProvider) {
        providerRef.set(provider)
    }

    /**
     * Returns the currently registered provider; falls back to an empty implementation.
     */
    val current: ClientIdentityProvider
        get() = providerRef.get()

    fun registerBaiduIdHandler(handler: BaiduIdHandler) {
        handlerRef.set(handler)
    }

    val baiduIdHandler: BaiduIdHandler
        get() = handlerRef.get()

    private object EmptyClientIdentityProvider : ClientIdentityProvider {
        override val clientId: String? = null
        override val sampleId: String? = null
        override val baiduId: String? = null
        override val activeTimestamp: Long = System.currentTimeMillis()
    }

    private object NoOpBaiduIdHandler : BaiduIdHandler {
        override fun saveBaiduId(baiduId: String) = Unit
    }
}
