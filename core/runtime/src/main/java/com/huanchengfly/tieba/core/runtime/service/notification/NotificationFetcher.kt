package com.huanchengfly.tieba.core.runtime.service.notification

fun interface NotificationFetchCallback {
    fun onComplete(result: Result<NotificationCounts>)
}

interface NotificationFetcher {
    fun fetch(callback: NotificationFetchCallback)
    fun cancel()
}
