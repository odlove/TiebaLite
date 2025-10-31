package com.huanchengfly.tieba.post.api

import com.huanchengfly.tieba.core.network.device.DeviceConfigRegistry
import com.huanchengfly.tieba.core.network.device.DeviceInfoProvider
import com.huanchengfly.tieba.core.network.device.DeviceInfoRegistry
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.device.AppDeviceInfoProvider

private val defaultUserAgent: String =
    "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/135.0.0.0 Mobile Safari/537.36"

fun getUserAgent(appendString: String? = null): String {
    val append = " ${appendString?.trim()}".takeIf { !appendString.isNullOrEmpty() }.orEmpty()
    val base = DeviceConfigRegistry.current.userAgent ?: defaultUserAgent
    return "$base$append"
}

fun getCookie(vararg cookies: Pair<String, () -> String?>): String {
    return cookies.map { it.first to it.second() }.filterNot { it.second.isNullOrEmpty() }
        .joinToString("; ") { "${it.first}:${it.second}" }
}

private val fallbackDeviceInfoProvider by lazy { AppDeviceInfoProvider() }

internal fun resolveDeviceInfo(): DeviceInfoProvider {
    val info = DeviceInfoRegistry.currentOrNull()
    return if (info == null || !info.hasValidMetrics()) fallbackDeviceInfoProvider else info
}

private fun DeviceInfoProvider.hasValidMetrics(): Boolean =
    screenHeight > 0 && screenWidth > 0 && screenDensity > 0f && brand.isNotBlank() && model.isNotBlank()

fun getScreenHeight(): Int =
    resolveDeviceInfo().screenHeight.takeUnless { it <= 0 } ?: App.ScreenInfo.EXACT_SCREEN_HEIGHT

fun getScreenWidth(): Int =
    resolveDeviceInfo().screenWidth.takeUnless { it <= 0 } ?: App.ScreenInfo.EXACT_SCREEN_WIDTH

fun getScreenDensity(): Float =
    resolveDeviceInfo().screenDensity.takeUnless { it <= 0f } ?: App.ScreenInfo.DENSITY

fun Boolean.booleanToString(): String = if (this) "1" else "0"
