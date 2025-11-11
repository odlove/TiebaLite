package com.huanchengfly.tieba.core.ui.image

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf

interface ImageUrlResolver {
    fun getUrl(
        context: Context,
        preferSmall: Boolean,
        originUrl: String,
        smallUrls: List<String?>
    ): String
}

val LocalImageUrlResolver = staticCompositionLocalOf<ImageUrlResolver> {
    DefaultImageUrlResolver
}

private object DefaultImageUrlResolver : ImageUrlResolver {
    override fun getUrl(
        context: Context,
        preferSmall: Boolean,
        originUrl: String,
        smallUrls: List<String?>
    ): String {
        if (preferSmall) {
            smallUrls.firstOrNull { !it.isNullOrBlank() }?.let { return it }
        }
        return originUrl
    }
}
