package com.huanchengfly.tieba.post.device

import com.huanchengfly.tieba.core.network.device.DeviceConfigProvider
import com.huanchengfly.tieba.core.runtime.device.DeviceConfigHolder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDeviceConfigProvider @Inject constructor(
    private val deviceConfigHolder: DeviceConfigHolder
) : DeviceConfigProvider {
    private val config get() = deviceConfigHolder.config

    override val isOaidSupported: Boolean
        get() = config.isOaidSupported

    override val oaid: String
        get() = config.oaid

    override val encodedOaid: String
        get() = config.encodedOaid

    override val statusCode: Int
        get() = config.statusCode

    override val isTrackLimited: Boolean
        get() = config.isTrackLimited

    override val userAgent: String?
        get() = config.userAgent

    override val appFirstInstallTime: Long
        get() = config.appFirstInstallTime

    override val appLastUpdateTime: Long
        get() = config.appLastUpdateTime
}
