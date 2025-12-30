package com.huanchengfly.tieba.post.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.AlarmManager
import android.app.PendingIntent
import android.os.Build
import com.huanchengfly.tieba.post.preferences.appPreferences
import com.huanchengfly.tieba.post.sign.SignActions
import com.huanchengfly.tieba.post.sign.SignTimeUtils
import java.util.Calendar

class BootCompleteSignReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            val autoSign = context.appPreferences.autoSign
            if (autoSign) {
                val autoSignTimeStr = context.appPreferences.autoSignTime
                if (SignTimeUtils.getTimeInMillis(autoSignTimeStr) > System.currentTimeMillis()) {
                    SignActions.initAutoSign(context)
                } else {
                    val signDay = context.appPreferences.signDay
                    if (signDay != Calendar.getInstance()[Calendar.DAY_OF_MONTH]) {
                        SignActions.startSign(context)
                    }
                    val alarmManager =
                        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val time = SignTimeUtils.time2Calendar(autoSignTimeStr).apply {
                        add(Calendar.DAY_OF_MONTH, 1)
                    }.timeInMillis
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        0,
                        Intent(context, AutoSignAlarm::class.java),
                        pendingIntentFlagMutable()
                    )
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        time,
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                    )
                }
            }
        }
    }

    private fun pendingIntentFlagMutable(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            0
        }
    }
}
