package com.huanchengfly.tieba.core.runtime.service.notification

import android.content.Context

fun interface NotificationChannelConfigurator {
    fun configure(context: Context)
}
