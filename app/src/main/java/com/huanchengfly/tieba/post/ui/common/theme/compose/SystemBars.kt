package com.huanchengfly.tieba.post.ui.common.theme.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.stoyanvuchev.systemuibarstweaker.SystemBarStyle
import com.stoyanvuchev.systemuibarstweaker.SystemUIBarsTweaker

@Composable
fun ApplySystemBars(
    systemUIBarsTweaker: SystemUIBarsTweaker,
    statusBarColor: Color = Color.Transparent,
    navigationBarColor: Color = Color.Transparent
) {
    val themeController = LocalThemeController.current
    SideEffect {
        val statusBarDarkIcons = themeController.shouldUseDarkStatusBarIcons()
        val navigationBarDarkIcons = themeController.shouldUseDarkNavigationBarIcons()

        systemUIBarsTweaker.tweakStatusBarStyle(
            SystemBarStyle(
                color = statusBarColor,
                darkIcons = statusBarDarkIcons
            )
        )
        systemUIBarsTweaker.tweakNavigationBarStyle(
            SystemBarStyle(
                color = navigationBarColor,
                darkIcons = navigationBarDarkIcons
            )
        )
    }
}
