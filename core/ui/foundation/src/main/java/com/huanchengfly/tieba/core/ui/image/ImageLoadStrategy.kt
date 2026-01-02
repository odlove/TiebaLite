package com.huanchengfly.tieba.core.ui.image

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf

typealias ImageLoadStrategy = (context: Context, skipNetworkCheck: Boolean) -> Boolean

val LocalImageLoadStrategy = staticCompositionLocalOf<ImageLoadStrategy> {
    { _, _ -> true }
}
