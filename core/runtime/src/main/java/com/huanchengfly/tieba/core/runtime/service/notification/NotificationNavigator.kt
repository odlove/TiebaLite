package com.huanchengfly.tieba.core.runtime.service.notification

import android.app.PendingIntent
import android.content.Context

enum class NotificationChannelType {
    REPLY,
    MENTION
}

fun interface NotificationNavigator {
    fun createPendingIntent(context: Context, type: NotificationChannelType): PendingIntent?
}
