package com.huanchengfly.tieba.core.network.error

import com.huanchengfly.tieba.core.network.exception.TiebaException

object ErrorMessages {
    private var provider: ErrorMessageProvider = object : ErrorMessageProvider {
        override fun unknownError(): String = "Unknown error"
        override fun networkTimeout(): String = "Network timeout"
        override fun noConnectivity(): String = "No network connection"
    }

    fun register(provider: ErrorMessageProvider) {
        this.provider = provider
    }

    fun current(): ErrorMessageProvider = provider
}

fun Throwable.defaultErrorMessage(): String {
    val message = when (this) {
        is TiebaException -> this.message
        else -> this.message
    }
    return if (!message.isNullOrEmpty()) message else ErrorMessages.current().unknownError()
}
