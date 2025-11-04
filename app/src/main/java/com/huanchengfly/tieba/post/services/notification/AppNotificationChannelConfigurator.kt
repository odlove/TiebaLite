package com.huanchengfly.tieba.post.services.notification

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.huanchengfly.tieba.core.runtime.service.notification.NotificationChannelConfigurator
import com.huanchengfly.tieba.core.common.ResourceProvider
import com.huanchengfly.tieba.post.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppNotificationChannelConfigurator @Inject constructor(
    private val resourceProvider: ResourceProvider
) : NotificationChannelConfigurator {

    override fun configure(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = NotificationManagerCompat.from(context)
        val groupName = resourceProvider.getString(R.string.notification_group_messages)
        val group = NotificationChannelGroup(NotificationConstants.CHANNEL_GROUP_ID, groupName)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            group.description = resourceProvider.getString(R.string.notification_group_messages_description)
        }
        manager.createNotificationChannelGroup(group)

        val replyChannelName = resourceProvider.getString(R.string.notification_channel_reply)
        val mentionChannelName = resourceProvider.getString(R.string.notification_channel_mention)

        val replyChannel = NotificationChannel(
            NotificationConstants.CHANNEL_REPLY_ID,
            replyChannelName,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            this.group = NotificationConstants.CHANNEL_GROUP_ID
            setShowBadge(true)
        }

        val mentionChannel = NotificationChannel(
            NotificationConstants.CHANNEL_MENTION_ID,
            mentionChannelName,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            this.group = NotificationConstants.CHANNEL_GROUP_ID
            setShowBadge(true)
        }

        manager.createNotificationChannel(replyChannel)
        manager.createNotificationChannel(mentionChannel)
    }
}
