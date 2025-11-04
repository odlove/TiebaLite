package com.huanchengfly.tieba.core.runtime.service.sign

import android.app.Service

interface SignNotificationController {
    fun startForeground(service: Service)
    fun update(service: Service, update: SignNotificationUpdate)
    fun stop(service: Service, mode: SignForegroundStopMode = SignForegroundStopMode.DETACH)
}
