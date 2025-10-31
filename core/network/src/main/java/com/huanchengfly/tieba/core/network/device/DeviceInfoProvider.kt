package com.huanchengfly.tieba.core.network.device

/**
 * Provides device identifiers and characteristics required for network parameters.
 */
interface DeviceInfoProvider {
    val imei: String?
    val brand: String
    val model: String
    val osVersion: String
    val deviceScore: Float
    val screenDensity: Float
    val screenHeight: Int
    val screenWidth: Int
}
