package com.huanchengfly.tieba.post.device

import android.os.Build
import com.huanchengfly.tieba.core.network.device.DeviceInfoProvider
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.utils.DeviceUtils
import com.huanchengfly.tieba.post.utils.MobileInfoUtil
import javax.inject.Inject

class AppDeviceInfoProvider @Inject constructor() : DeviceInfoProvider {
    override val imei: String?
        get() = MobileInfoUtil.getIMEI(App.INSTANCE)

    override val brand: String
        get() = Build.BRAND

    override val model: String
        get() = Build.MODEL

    override val osVersion: String
        get() = Build.VERSION.SDK_INT.toString()

    override val deviceScore: Float
        get() = DeviceUtils.getDeviceScore()

    override val screenDensity: Float
        get() = App.ScreenInfo.DENSITY

    override val screenHeight: Int
        get() = App.ScreenInfo.EXACT_SCREEN_HEIGHT

    override val screenWidth: Int
        get() = App.ScreenInfo.EXACT_SCREEN_WIDTH
}
