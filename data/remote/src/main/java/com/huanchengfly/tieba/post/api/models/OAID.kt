package com.huanchengfly.tieba.post.api.models

import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.core.network.device.DeviceConfigRegistry

private val deviceConfig get() = DeviceConfigRegistry.current

data class OAID(
    @SerializedName("v")
    val encodedOAID: String = deviceConfig.encodedOaid,
    @SerializedName("sc")
    val statusCode: Int = deviceConfig.statusCode,
    @SerializedName("sup")
    val support: Int = if (deviceConfig.isOaidSupported) 1 else 0,
    val isTrackLimited: Int = if (deviceConfig.isTrackLimited) 1 else 0
)
