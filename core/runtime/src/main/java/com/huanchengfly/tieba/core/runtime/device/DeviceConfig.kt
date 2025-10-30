package com.huanchengfly.tieba.core.runtime.device

data class DeviceConfig(
    val isOaidSupported: Boolean,
    val oaid: String,
    val encodedOaid: String,
    val statusCode: Int,
    val isTrackLimited: Boolean,
    val userAgent: String?,
    val appFirstInstallTime: Long,
    val appLastUpdateTime: Long,
)

interface DeviceConfigHolder {
    var config: DeviceConfig
}

class MutableDeviceConfigHolder : DeviceConfigHolder {
    override var config: DeviceConfig = DeviceConfig(
        isOaidSupported = false,
        oaid = "",
        encodedOaid = "",
        statusCode = -200,
        isTrackLimited = false,
        userAgent = null,
        appFirstInstallTime = 0L,
        appLastUpdateTime = 0L
    )
}
