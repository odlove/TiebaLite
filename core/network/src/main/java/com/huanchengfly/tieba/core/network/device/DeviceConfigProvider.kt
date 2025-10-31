package com.huanchengfly.tieba.core.network.device

/**
 * Provides device-related configuration values required by the network layer.
 */
interface DeviceConfigProvider {
    val isOaidSupported: Boolean
    val oaid: String
    val encodedOaid: String
    val statusCode: Int
    val isTrackLimited: Boolean
    val userAgent: String?
    val appFirstInstallTime: Long
    val appLastUpdateTime: Long
}
