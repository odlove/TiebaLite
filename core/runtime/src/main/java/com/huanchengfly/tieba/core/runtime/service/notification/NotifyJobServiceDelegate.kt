package com.huanchengfly.tieba.core.runtime.service.notification

import android.content.Context
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotifyJobServiceDelegate @Inject constructor(
    private val channelConfigurator: NotificationChannelConfigurator,
    private val notificationFetcher: NotificationFetcher,
    private val notificationRenderer: NotificationRenderer
) {

    private val running = AtomicBoolean(false)
    private val rescheduleRequested = AtomicBoolean(false)

    fun onStartJob(
        context: Context,
        completion: (needsReschedule: Boolean) -> Unit
    ) {
        if (!running.compareAndSet(false, true)) {
            val needsReschedule = rescheduleRequested.getAndSet(false)
            completion(needsReschedule)
            return
        }
        rescheduleRequested.set(false)
        channelConfigurator.configure(context)
        notificationFetcher.fetch { result ->
            val wasRunning = running.getAndSet(false)
            if (!wasRunning) {
                if (rescheduleRequested.getAndSet(false)) {
                    completion(true)
                }
                return@fetch
            }
            result.onSuccess { counts ->
                notificationRenderer.render(context, counts)
                completion(false)
            }.onFailure { error ->
                notificationRenderer.onError(context, error)
                completion(true)
            }
        }
    }

    fun onStopJob() {
        if (running.getAndSet(false)) {
            rescheduleRequested.set(true)
            notificationFetcher.cancel()
        }
    }
}
