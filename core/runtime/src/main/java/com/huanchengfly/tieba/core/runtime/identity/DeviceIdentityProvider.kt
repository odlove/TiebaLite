package com.huanchengfly.tieba.core.runtime.identity

interface DeviceIdentityProvider {
    fun getAndroidId(defaultValue: String = ""): String

    val cuid: String

    val finalCuid: String

    val newCuid: String

    val aid: String

    val uuid: String
}

object DeviceIdentityRegistry {
    private var provider: DeviceIdentityProvider = EmptyDeviceIdentityProvider

    val current: DeviceIdentityProvider
        get() = provider

    fun register(provider: DeviceIdentityProvider) {
        this.provider = provider
    }
}

private object EmptyDeviceIdentityProvider : DeviceIdentityProvider {
    override fun getAndroidId(defaultValue: String): String = defaultValue

    override val cuid: String
        get() = ""

    override val finalCuid: String
        get() = ""

    override val newCuid: String
        get() = ""

    override val aid: String
        get() = ""

    override val uuid: String
        get() = ""
}
