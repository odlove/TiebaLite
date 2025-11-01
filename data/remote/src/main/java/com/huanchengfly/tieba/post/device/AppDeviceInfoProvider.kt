package com.huanchengfly.tieba.post.device

import android.content.Context
import android.os.Build
import com.huanchengfly.tieba.core.network.device.DeviceInfoProvider
import com.huanchengfly.tieba.core.runtime.device.ScreenMetricsProvider
import com.huanchengfly.tieba.post.utils.DeviceUtils
import com.huanchengfly.tieba.post.utils.MobileInfoUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppDeviceInfoProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val screenMetricsProvider: ScreenMetricsProvider
) : DeviceInfoProvider {
    override val imei: String?
        get() = MobileInfoUtil.getIMEI(context)

    override val brand: String
        get() = Build.BRAND

    override val model: String
        get() = Build.MODEL

    override val osVersion: String
        get() = Build.VERSION.SDK_INT.toString()

    override val deviceScore: Float
        get() = DeviceUtils.getDeviceScore()

    override val screenDensity: Float
        get() = screenMetricsProvider.density

    override val screenHeight: Int
        get() = screenMetricsProvider.exactScreenHeightPx

    override val screenWidth: Int
        get() = screenMetricsProvider.exactScreenWidthPx
}
