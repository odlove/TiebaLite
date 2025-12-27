package com.huanchengfly.tieba.post.utils

import android.content.Context

object MobileInfoUtil {
    const val DEFAULT_IMEI = "000000000000000"

    @JvmStatic
    fun getIMEI(context: Context): String {
        return DEFAULT_IMEI
    }
}
