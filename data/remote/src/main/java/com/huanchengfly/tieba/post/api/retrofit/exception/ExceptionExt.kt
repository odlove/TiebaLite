package com.huanchengfly.tieba.post.api.retrofit.exception

import com.huanchengfly.tieba.core.network.error.ErrorMessageProvider
import com.huanchengfly.tieba.core.network.error.ErrorMessages
import com.huanchengfly.tieba.core.network.error.defaultErrorMessage
import com.huanchengfly.tieba.core.network.exception.TiebaException
import com.huanchengfly.tieba.post.api.Error

fun registerErrorMessageProvider(provider: ErrorMessageProvider) {
    ErrorMessages.register(provider)
}

fun Throwable.getErrorCode(): Int {
    return if (this is TiebaException) {
        code
    } else {
        Error.ERROR_UNKNOWN
    }
}

fun Throwable.getErrorMessage(): String = defaultErrorMessage()
