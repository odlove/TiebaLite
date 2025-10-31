package com.huanchengfly.tieba.core.network.device

import java.util.concurrent.atomic.AtomicReference

/**
 * Holds the active [DeviceConfigProvider] so request builders can access device configuration.
 */
object DeviceConfigRegistry {
    private val providerRef = AtomicReference<DeviceConfigProvider>(EmptyDeviceConfigProvider)

    fun register(provider: DeviceConfigProvider) {
        providerRef.set(provider)
    }

    val current: DeviceConfigProvider
        get() = providerRef.get()

    private object EmptyDeviceConfigProvider : DeviceConfigProvider {
        override val isOaidSupported: Boolean = false
        override val oaid: String = ""
        override val encodedOaid: String = ""
        override val statusCode: Int = -200
        override val isTrackLimited: Boolean = false
        override val userAgent: String? = null
        override val appFirstInstallTime: Long = 0L
        override val appLastUpdateTime: Long = 0L
    }
}
