package com.huanchengfly.tieba.post.services.sign

import android.app.Notification
import android.app.Service
import android.widget.Toast
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.huanchengfly.tieba.core.runtime.service.sign.SignForegroundStopMode
import com.huanchengfly.tieba.core.runtime.service.sign.SignNotificationController
import com.huanchengfly.tieba.core.runtime.service.sign.SignNotificationUpdate
import com.huanchengfly.tieba.core.ui.theme.runtime.ThemeColorResolver
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.services.sign.SignServiceConstants.NOTIFICATION_CHANNEL_ID
import com.huanchengfly.tieba.post.services.sign.SignServiceConstants.NOTIFICATION_ID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSignNotificationController @Inject constructor() : SignNotificationController {

    override fun startForeground(service: Service) {
        val notification = buildNotification(
            service,
            service.getString(R.string.title_loading_data),
            service.getString(R.string.text_please_wait),
            ongoing = true,
            contentIntent = null
        )
        ServiceCompat.startForeground(service, NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
    }

    override fun update(service: Service, update: SignNotificationUpdate) {
        update.toastMessage?.let {
            Toast.makeText(service, it, Toast.LENGTH_SHORT).show()
        }
        val notification = buildNotification(
            service,
            update.title,
            update.message,
            update.ongoing,
            update.contentIntent
        )
        NotificationManagerCompat.from(service).notify(NOTIFICATION_ID, notification)

        when (update.stopMode) {
            SignForegroundStopMode.DETACH ->
                ServiceCompat.stopForeground(service, ServiceCompat.STOP_FOREGROUND_DETACH)
            SignForegroundStopMode.REMOVE ->
                ServiceCompat.stopForeground(service, ServiceCompat.STOP_FOREGROUND_REMOVE)
            SignForegroundStopMode.NONE -> Unit
        }
    }

    override fun stop(service: Service, mode: SignForegroundStopMode) {
        when (mode) {
            SignForegroundStopMode.DETACH ->
                ServiceCompat.stopForeground(service, ServiceCompat.STOP_FOREGROUND_DETACH)
            SignForegroundStopMode.REMOVE ->
                ServiceCompat.stopForeground(service, ServiceCompat.STOP_FOREGROUND_REMOVE)
            SignForegroundStopMode.NONE -> Unit
        }
    }

    private fun buildNotification(
        service: Service,
        title: String,
        text: String?,
        ongoing: Boolean,
        contentIntent: android.app.PendingIntent?
    ): Notification {
        ensureChannel(service)

        val builder = NotificationCompat.Builder(service, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSubText(service.getString(R.string.title_oksign))
            .setSmallIcon(R.drawable.ic_oksign)
            .setAutoCancel(!ongoing)
            .setStyle(NotificationCompat.BigTextStyle())
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setColor(ThemeColorResolver.colorByAttr(service, R.attr.colorPrimary))
            .setOngoing(ongoing)

        if (contentIntent != null) {
            builder.setContentIntent(contentIntent)
        }

        return builder.build()
    }

    private fun ensureChannel(service: Service) {
        val manager = NotificationManagerCompat.from(service)
        val channel = NotificationChannelCompat.Builder(
            NOTIFICATION_CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_LOW
        )
            .setName(service.getString(R.string.title_oksign))
            .setLightsEnabled(false)
            .setShowBadge(false)
            .build()
        manager.createNotificationChannel(channel)
    }
}
