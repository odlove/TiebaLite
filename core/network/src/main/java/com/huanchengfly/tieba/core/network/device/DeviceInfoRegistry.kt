package com.huanchengfly.tieba.core.network.device

import java.util.concurrent.atomic.AtomicReference

object DeviceInfoRegistry {
    private val providerRef = AtomicReference<DeviceInfoProvider>(EmptyDeviceInfoProvider)

    fun register(provider: DeviceInfoProvider) {
        providerRef.set(provider)
    }

    fun current(): DeviceInfoProvider = providerRef.get()

    fun currentOrNull(): DeviceInfoProvider? {
        val provider = providerRef.get()
        return if (provider === EmptyDeviceInfoProvider) null else provider
    }

    private object EmptyDeviceInfoProvider : DeviceInfoProvider {
        override val imei: String? = null
        override val brand: String = ""
        override val model: String = ""
        override val osVersion: String = ""
        override val deviceScore: Float = 0f
        override val screenDensity: Float = 0f
        override val screenHeight: Int = 0
        override val screenWidth: Int = 0
    }
}
