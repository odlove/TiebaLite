package com.huanchengfly.tieba.core.network.runtime

fun interface NetworkStatusProvider {
    fun isConnected(): Boolean

    companion object {
        val AlwaysConnected = NetworkStatusProvider { true }
    }
}
