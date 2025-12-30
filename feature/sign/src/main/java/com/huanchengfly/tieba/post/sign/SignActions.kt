package com.huanchengfly.tieba.post.sign

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.huanchengfly.tieba.core.runtime.service.sign.SignServiceConstants
import com.huanchengfly.tieba.post.preferences.appPreferences
import com.huanchengfly.tieba.post.receivers.AutoSignAlarm
import com.huanchengfly.tieba.post.services.OKSignService
import java.util.Calendar

object SignActions {
    @JvmStatic
    fun initAutoSign(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val autoSign = context.appPreferences.autoSign
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, AutoSignAlarm::class.java),
            pendingIntentFlagMutable()
        )
        if (autoSign) {
            val autoSignTimeStr = context.appPreferences.autoSignTime ?: "09:00"
            val time = autoSignTimeStr.split(":").toTypedArray()
            val hour = time[0].toInt()
            val minute = time[1].toInt()
            val calendar = Calendar.getInstance()
            calendar[Calendar.HOUR_OF_DAY] = hour
            calendar[Calendar.MINUTE] = minute
            if (calendar.timeInMillis >= System.currentTimeMillis()) {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }
        } else {
            alarmManager.cancel(pendingIntent)
        }
    }

    @JvmStatic
    fun startSign(context: Context) {
        context.appPreferences.signDay = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
        ContextCompat.startForegroundService(
            context,
            Intent(context, OKSignService::class.java)
                .setAction(SignServiceConstants.ACTION_START_SIGN)
        )
    }

    private fun pendingIntentFlagMutable(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            0
        }
    }
}
