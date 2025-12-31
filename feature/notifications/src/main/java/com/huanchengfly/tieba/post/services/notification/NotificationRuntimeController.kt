package com.huanchengfly.tieba.post.services.notification

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.huanchengfly.tieba.post.services.NotifyJobService
import com.huanchengfly.tieba.post.utils.JobServiceUtil
import com.huanchengfly.tieba.post.utils.newIntentFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow

class NotificationRuntimeController(
    private val context: Context,
    private val notificationCountFlow: MutableSharedFlow<Int>,
    private val coroutineScope: CoroutineScope,
) {
    private val newMessageReceiver = NewMessageReceiver(notificationCountFlow, coroutineScope)
    private var isReceiverRegistered = false

    fun onStart() {
        if (!isReceiverRegistered) {
            runCatching {
                ContextCompat.registerReceiver(
                    context,
                    newMessageReceiver,
                    newIntentFilter(NotificationConstants.ACTION_NEW_MESSAGE),
                    ContextCompat.RECEIVER_NOT_EXPORTED
                )
                isReceiverRegistered = true
            }
        }
        runCatching {
            context.startService(Intent(context, NotifyJobService::class.java))
            val builder = JobInfo.Builder(
                JobServiceUtil.getJobId(context),
                ComponentName(context, NotifyJobService::class.java)
            )
                .setPersisted(true)
                .setPeriodic(30 * 60 * 1000L)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler.schedule(builder.build())
        }
    }

    fun onStop() {
        if (isReceiverRegistered) {
            runCatching {
                context.unregisterReceiver(newMessageReceiver)
            }
            isReceiverRegistered = false
        }
    }
}
