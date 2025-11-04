package com.huanchengfly.tieba.post.utils.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.ui.common.theme.ThemeColorResolver
import com.huanchengfly.tieba.post.utils.ColorUtils
import com.huanchengfly.tieba.post.utils.appPreferences

fun Color.darken(i: Float = 0.1F): Color {
    return Color(ColorUtils.getDarkerColor(toArgb(), i))
}

fun Color.calcStatusBarColor(): Color {
    val context = App.INSTANCE
    var darkerStatusBar = true
    val themeState = ThemeColorResolver.state(context)
    val isToolbarPrimaryColor = context.appPreferences.toolbarPrimaryColor
    if (themeState.isTranslucent || !isToolbarPrimaryColor || themeState.useDynamicColor) {
        darkerStatusBar = false
    } else if (!context.appPreferences.statusBarDarker) {
        darkerStatusBar = false
    }
    return if (darkerStatusBar) darken() else this
}
