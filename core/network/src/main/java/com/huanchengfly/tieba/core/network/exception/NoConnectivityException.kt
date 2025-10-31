package com.huanchengfly.tieba.core.network.exception

class NoConnectivityException(
    message: String
) : TiebaLocalException(NETWORK_ERROR_CODE, message) {
    override fun toString(): String {
        return "NoConnectivityException(message=$message)"
    }

    companion object {
        private const val NETWORK_ERROR_CODE = 10
    }
}
