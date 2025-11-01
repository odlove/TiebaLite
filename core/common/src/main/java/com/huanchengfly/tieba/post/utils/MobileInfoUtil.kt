package com.huanchengfly.tieba.post.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager

object MobileInfoUtil {
    const val DEFAULT_IMEI = "000000000000000"

    @JvmStatic
    @SuppressLint("HardwareIds")
    fun getIMEI(context: Context): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return DEFAULT_IMEI
        }
        return try {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val imei = telephonyManager?.deviceId
            imei ?: DEFAULT_IMEI
        } catch (e: SecurityException) {
            DEFAULT_IMEI
        } catch (e: Exception) {
            DEFAULT_IMEI
        }
    }
}
