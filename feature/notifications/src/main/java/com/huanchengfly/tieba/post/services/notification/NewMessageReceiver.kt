package com.huanchengfly.tieba.post.services.notification

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.huanchengfly.tieba.core.theme.compose.THEME_DIAGNOSTICS_TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class NewMessageReceiver(
    private val notificationCountFlow: MutableSharedFlow<Int>,
    private val coroutineScope: CoroutineScope
) : BroadcastReceiver() {
    @SuppressLint("RestrictedApi")
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == NotificationConstants.ACTION_NEW_MESSAGE) {
            val channel = intent.getStringExtra("channel")
            val count = intent.getIntExtra("count", 0)
            if (channel != null && channel == NotificationConstants.CHANNEL_TOTAL_ID) {
                coroutineScope.launch {
                    Log.i(
                        THEME_DIAGNOSTICS_TAG,
                        "notificationCountFlow emit channel=$channel count=$count"
                    )
                    notificationCountFlow.emit(count)
                }
            }
        }
    }
}
