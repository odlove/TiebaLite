package com.huanchengfly.tieba.core.network.identity

/**
 * Provides client-related identifiers required for common request parameters.
 */
interface ClientIdentityProvider {
    val clientId: String?
    val sampleId: String?
    val baiduId: String?
    val activeTimestamp: Long
    val finalCuid: String?
    val newCuid: String?
    val aid: String?
    val androidId: String?
}
