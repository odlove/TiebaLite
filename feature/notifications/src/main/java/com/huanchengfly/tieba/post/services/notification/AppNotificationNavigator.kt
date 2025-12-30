package com.huanchengfly.tieba.post.services.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Build
import com.huanchengfly.tieba.core.runtime.service.notification.NotificationChannelType
import com.huanchengfly.tieba.core.runtime.service.notification.NotificationNavigator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppNotificationNavigator @Inject constructor() : NotificationNavigator {
    override fun createPendingIntent(context: Context, type: NotificationChannelType): PendingIntent? {
        val target = when (type) {
            NotificationChannelType.REPLY -> Uri.parse("tblite://notifications/0")
            NotificationChannelType.MENTION -> Uri.parse("tblite://notifications/1")
        }
        val intent = Intent(ACTION_VIEW, target)
        return PendingIntent.getActivity(
            context,
            type.ordinal,
            intent,
            pendingIntentFlagImmutable()
        )
    }

    private fun pendingIntentFlagImmutable(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
    }
}
