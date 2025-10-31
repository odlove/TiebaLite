package com.huanchengfly.tieba.core.network.identity

/**
 * Handles persistence of BaiduId extracted from network responses.
 */
interface BaiduIdHandler {
    fun saveBaiduId(baiduId: String)
}
