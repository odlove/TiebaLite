package com.huanchengfly.tieba.post.utils

import android.annotation.SuppressLint
import com.huanchengfly.tieba.core.network.device.DeviceConfigRegistry
import com.huanchengfly.tieba.core.runtime.identity.DeviceIdentityRegistry
import com.huanchengfly.tieba.core.common.util.Base32
import com.huanchengfly.tieba.post.utils.helios.Hasher

object UIDUtil {
    @get:SuppressLint("HardwareIds")
    val androidId: String
        get() = getAndroidId("")

    @SuppressLint("HardwareIds")
    fun getAndroidId(defaultValue: String): String =
        DeviceIdentityRegistry.current.getAndroidId(defaultValue)

    fun getOAID(): String {
        val deviceConfig = DeviceConfigRegistry.current
        if (deviceConfig.encodedOaid.isBlank()) return ""
        val raw = "A10-${deviceConfig.encodedOaid}-"
        val sign = Base32.encode(Hasher.hash(raw.toByteArray()))
        return "$raw$sign"
    }

    fun getAid(): String = DeviceIdentityRegistry.current.aid

    val newCUID: String
        get() = DeviceIdentityRegistry.current.newCuid

    val cUID: String
        get() = DeviceIdentityRegistry.current.cuid

    val finalCUID: String
        get() = DeviceIdentityRegistry.current.finalCuid

    @get:SuppressLint("ApplySharedPref")
    val uUID: String
        get() = DeviceIdentityRegistry.current.uuid
}
