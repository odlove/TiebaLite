package com.huanchengfly.tieba.post.services.notification

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.huanchengfly.tieba.core.common.ResourceProvider
import com.huanchengfly.tieba.core.runtime.service.notification.NotificationChannelType
import com.huanchengfly.tieba.core.runtime.service.notification.NotificationCounts
import com.huanchengfly.tieba.core.runtime.service.notification.NotificationRenderer
import com.huanchengfly.tieba.core.runtime.service.notification.NotificationNavigator
import com.huanchengfly.tieba.core.ui.theme.runtime.ThemeColorResolver
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.services.notification.NotificationConstants.ACTION_NEW_MESSAGE
import com.huanchengfly.tieba.post.services.notification.NotificationConstants.CHANNEL_MENTION_ID
import com.huanchengfly.tieba.post.services.notification.NotificationConstants.CHANNEL_REPLY_ID
import com.huanchengfly.tieba.post.services.notification.NotificationConstants.CHANNEL_TOTAL_ID
import com.huanchengfly.tieba.post.services.notification.NotificationConstants.NOTIFICATION_MENTION_ID
import com.huanchengfly.tieba.post.services.notification.NotificationConstants.NOTIFICATION_REPLY_ID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppNotificationRenderer @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val navigator: NotificationNavigator
) : NotificationRenderer {

    override fun render(context: Context, counts: NotificationCounts) {
        val manager = NotificationManagerCompat.from(context)
        if (counts.replies > 0) {
            val title = resourceProvider.getString(R.string.tips_message_reply, counts.replies.toString())
        val builder = baseBuilder(context, CHANNEL_REPLY_ID)
                .setSubText(resourceProvider.getString(R.string.notification_channel_reply))
                .setContentTitle(title)
            navigator.createPendingIntent(context, NotificationChannelType.REPLY)?.let(builder::setContentIntent)
            manager.notify(NOTIFICATION_REPLY_ID, builder.build())
            broadcast(context, CHANNEL_REPLY_ID, counts.replies)
        } else {
            manager.cancel(NOTIFICATION_REPLY_ID)
            broadcast(context, CHANNEL_REPLY_ID, 0)
        }

        if (counts.mentions > 0) {
            val title = resourceProvider.getString(R.string.tips_message_at, counts.mentions.toString())
            val builder = baseBuilder(context, CHANNEL_MENTION_ID)
                .setSubText(resourceProvider.getString(R.string.notification_channel_mention))
                .setContentTitle(title)
            navigator.createPendingIntent(context, NotificationChannelType.MENTION)?.let(builder::setContentIntent)
            manager.notify(NOTIFICATION_MENTION_ID, builder.build())
            broadcast(context, CHANNEL_MENTION_ID, counts.mentions)
        } else {
            manager.cancel(NOTIFICATION_MENTION_ID)
            broadcast(context, CHANNEL_MENTION_ID, 0)
        }

        broadcast(context, CHANNEL_TOTAL_ID, counts.total)
    }

    override fun onError(context: Context, throwable: Throwable?) {
        // keep previous notifications; reschedule handled by JobScheduler
    }

    private fun baseBuilder(context: Context, channelId: String): NotificationCompat.Builder {
        val color = ThemeColorResolver.colorByAttr(context, R.attr.colorPrimary)
        return NotificationCompat.Builder(context, channelId)
            .setContentText(resourceProvider.getString(R.string.tip_touch_to_view))
            .setSmallIcon(R.drawable.ic_round_drafts)
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(color)
    }

    private fun broadcast(context: Context, channelId: String, count: Int) {
        context.sendBroadcast(
            Intent(ACTION_NEW_MESSAGE)
                .putExtra("channel", channelId)
                .putExtra("count", count)
        )
    }
}
