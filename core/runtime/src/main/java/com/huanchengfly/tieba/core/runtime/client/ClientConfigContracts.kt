package com.huanchengfly.tieba.core.runtime.client

import android.content.Context

/**
 * Provides read/write access to client-side identifiers required for network requests.
 */
interface ClientConfigRepository {
    suspend fun load(): ClientConfigState
    suspend fun sync()
    suspend fun updateBaiduId(id: String)
    suspend fun touchActiveTimestamp()
}

data class ClientConfigState(
    val clientId: String?,
    val sampleId: String?,
    val baiduId: String?,
    val activeTimestamp: Long
)
