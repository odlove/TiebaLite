package com.huanchengfly.tieba.core.network.identity

import java.util.concurrent.atomic.AtomicReference

/**
 * Global registry allowing the network layer to obtain the active [ClientIdentityProvider].
 */
object ClientIdentityRegistry {
    private val providerRef = AtomicReference<ClientIdentityProvider>(EmptyClientIdentityProvider)
    private val fallbackRef = AtomicReference<ClientIdentityProvider>(EmptyClientIdentityProvider)
    private val handlerRef = AtomicReference<BaiduIdHandler>(NoOpBaiduIdHandler)

    /**
     * Registers the [provider] that should be used when constructing network requests.
     */
    fun register(provider: ClientIdentityProvider) {
        providerRef.set(provider)
    }

    fun registerFallback(provider: ClientIdentityProvider) {
        fallbackRef.set(provider)
    }

    /**
     * Returns the currently registered provider; falls back to an empty implementation.
     */
    val current: ClientIdentityProvider
        get() {
            val primary = providerRef.get()
            val fallback = fallbackRef.get()
            return when {
                primary === EmptyClientIdentityProvider && fallback === EmptyClientIdentityProvider -> EmptyClientIdentityProvider
                primary === EmptyClientIdentityProvider -> fallback
                fallback === EmptyClientIdentityProvider -> primary
                else -> CompositeClientIdentityProvider(primary, fallback)
            }
        }

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
        override val finalCuid: String? = null
        override val newCuid: String? = null
        override val aid: String? = null
        override val androidId: String? = null
    }

    private class CompositeClientIdentityProvider(
        private val primary: ClientIdentityProvider,
        private val fallback: ClientIdentityProvider
    ) : ClientIdentityProvider {
        override val clientId: String?
            get() = primary.clientId ?: fallback.clientId

        override val sampleId: String?
            get() = primary.sampleId ?: fallback.sampleId

        override val baiduId: String?
            get() = primary.baiduId ?: fallback.baiduId

        override val activeTimestamp: Long
            get() = primary.activeTimestamp.takeIf { it != 0L } ?: fallback.activeTimestamp

        override val finalCuid: String?
            get() = primary.finalCuid ?: fallback.finalCuid

        override val newCuid: String?
            get() = primary.newCuid ?: fallback.newCuid

        override val aid: String?
            get() = primary.aid ?: fallback.aid

        override val androidId: String?
            get() = primary.androidId ?: fallback.androidId
    }

    private object NoOpBaiduIdHandler : BaiduIdHandler {
        override fun saveBaiduId(baiduId: String) = Unit
    }
}
