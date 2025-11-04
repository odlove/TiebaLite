package com.huanchengfly.tieba.core.runtime.service.sign

import android.app.PendingIntent

data class SignNotificationUpdate(
    val title: String,
    val message: String?,
    val ongoing: Boolean,
    val contentIntent: PendingIntent? = null,
    val toastMessage: String? = null,
    val stopMode: SignForegroundStopMode = SignForegroundStopMode.NONE
)
