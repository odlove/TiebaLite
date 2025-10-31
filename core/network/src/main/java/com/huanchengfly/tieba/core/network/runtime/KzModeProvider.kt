package com.huanchengfly.tieba.core.network.runtime

import java.util.concurrent.atomic.AtomicReference

/**
 * Provides the current KZ mode flag used by network requests.
 */
interface KzModeProvider {
    val isKzEnabled: Boolean
}

/**
 * Registry for the active [KzModeProvider].
 */
object KzModeRegistry {
    private val providerRef: AtomicReference<KzModeProvider> =
        AtomicReference(object : KzModeProvider {
            override val isKzEnabled: Boolean = true
        })

    fun register(provider: KzModeProvider) {
        providerRef.set(provider)
    }

    val current: KzModeProvider
        get() = providerRef.get()
}
