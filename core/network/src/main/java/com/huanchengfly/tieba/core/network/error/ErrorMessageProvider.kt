package com.huanchengfly.tieba.core.network.error

interface ErrorMessageProvider {
    fun unknownError(): String
    fun networkTimeout(): String
    fun noConnectivity(): String
}
