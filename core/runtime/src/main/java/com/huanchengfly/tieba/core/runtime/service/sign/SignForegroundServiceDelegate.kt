package com.huanchengfly.tieba.core.runtime.service.sign

import android.app.Service
import android.content.Intent
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignForegroundServiceDelegate @Inject constructor(
    private val signTaskRunner: SignTaskRunner,
    private val notificationController: SignNotificationController
) {

    private val running = AtomicBoolean(false)

    fun onStartCommand(
        service: Service,
        intent: Intent?,
        scope: CoroutineScope
    ): Int {
        if (intent?.action != SignActions.ACTION_START_SIGN) {
            notificationController.stop(service, SignForegroundStopMode.NONE)
            service.stopSelf()
            return Service.START_NOT_STICKY
        }

        if (!running.compareAndSet(false, true)) {
            return Service.START_NOT_STICKY
        }

        notificationController.startForeground(service)

        scope.launch {
            try {
                signTaskRunner.run { update ->
                    notificationController.update(service, update)
                }
            } finally {
                running.set(false)
                notificationController.stop(service, SignForegroundStopMode.DETACH)
                service.stopSelf()
            }
        }

        return Service.START_NOT_STICKY
    }

    fun onDestroy(service: Service) {
        running.set(false)
        signTaskRunner.cancel()
        notificationController.stop(service, SignForegroundStopMode.DETACH)
    }
}
