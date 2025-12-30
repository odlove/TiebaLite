package com.huanchengfly.tieba.post.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.huanchengfly.tieba.post.sign.SignActions

class AutoSignAlarm : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        runCatching {
            SignActions.startSign(context)
        }
    }

    companion object {
        val TAG: String = "AutoSignAlarm"
    }
}
