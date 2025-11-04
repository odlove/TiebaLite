package com.huanchengfly.tieba.core.runtime.service.notification

import android.content.Context

interface NotificationRenderer {
    fun render(context: Context, counts: NotificationCounts)
    fun onError(context: Context, throwable: Throwable?)
}
