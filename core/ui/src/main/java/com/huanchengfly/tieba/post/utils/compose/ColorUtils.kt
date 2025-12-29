package com.huanchengfly.tieba.post.utils.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.huanchengfly.tieba.core.ui.theme.runtime.ThemeColorResolver
import com.huanchengfly.tieba.post.preferences.appPreferences
import com.huanchengfly.tieba.post.utils.ColorUtils

fun Color.darken(i: Float = 0.1F): Color {
    return Color(ColorUtils.getDarkerColor(toArgb(), i))
}

@Composable
fun Color.calcStatusBarColor(): Color {
    val context = LocalContext.current
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
