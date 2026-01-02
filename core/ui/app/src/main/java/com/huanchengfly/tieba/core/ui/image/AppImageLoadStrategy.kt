package com.huanchengfly.tieba.core.ui.image

import android.content.Context
import com.huanchengfly.tieba.core.common.image.ImageLoadSettings
import com.huanchengfly.tieba.core.ui.image.ImageLoadStrategy
import com.huanchengfly.tieba.core.ui.image.ImageUrlResolver
import com.huanchengfly.tieba.post.preferences.appPreferences
import com.huanchengfly.tieba.post.utils.ImageUtil
import com.huanchengfly.tieba.post.utils.NetworkUtil

fun createImageUrlResolver(): ImageUrlResolver = object : ImageUrlResolver {
    override fun getUrl(
        context: Context,
        preferSmall: Boolean,
        originUrl: String,
        smallUrls: List<String?>
    ): String {
        return ImageUtil.getUrl(context, preferSmall, originUrl, *smallUrls.toTypedArray())
    }
}

val AppImageLoadStrategy: ImageLoadStrategy = strategy@{ context, skipNetworkCheck ->
    if (skipNetworkCheck) return@strategy true
    val preferences = context.appPreferences
    val loadType = preferences.imageLoadType?.toIntOrNull() ?: ImageLoadSettings.SMART_ORIGIN
    when (loadType) {
        ImageLoadSettings.SMART_ORIGIN,
        ImageLoadSettings.ALL_ORIGIN -> true
        ImageLoadSettings.SMART_LOAD -> NetworkUtil.isWifiConnected(context)
        ImageLoadSettings.ALL_NO -> false
        else -> true
    }
}
