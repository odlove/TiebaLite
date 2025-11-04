package com.huanchengfly.tieba.core.runtime.service.sign

interface SignTaskRunner {
    suspend fun run(update: (SignNotificationUpdate) -> Unit)
    fun cancel()
}
