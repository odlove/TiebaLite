package com.huanchengfly.tieba.post.sign

import java.util.Calendar

object SignTimeUtils {
    @JvmStatic
    fun getTimeInMillis(timeStr: String?): Long {
        return time2Calendar(timeStr).timeInMillis
    }

    @JvmStatic
    fun time2Calendar(timeStr: String?): Calendar {
        val time = timeStr?.split(":") ?: emptyList()
        val hour = time.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = time.getOrNull(1)?.toIntOrNull() ?: 0
        val second = time.getOrNull(2)?.toIntOrNull() ?: 0
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, second)
            set(Calendar.MILLISECOND, 0)
        }
    }
}
