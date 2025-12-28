package com.huanchengfly.tieba.core.network.error

import com.huanchengfly.tieba.core.network.exception.TiebaException

private const val ERROR_UNKNOWN = -1

fun Throwable.getErrorCode(): Int = if (this is TiebaException) code else ERROR_UNKNOWN

fun Throwable.getErrorMessage(): String = defaultErrorMessage()
