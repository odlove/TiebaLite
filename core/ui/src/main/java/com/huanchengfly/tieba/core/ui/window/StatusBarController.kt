package com.huanchengfly.tieba.core.ui.window

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * 控制状态栏外观。
 */
class StatusBarController(private val window: Window) {
    private val insetsController = WindowInsetsControllerCompat(window, window.decorView)

    /**
     * 设置状态栏文字图标是否为深色。
     */
    fun setDarkIcons(dark: Boolean) {
        insetsController.isAppearanceLightStatusBars = dark
        setMiuiDarkIcons(dark)
        setFlymeDarkIcons(dark)
    }

    /**
     * 设置状态栏透明并铺满布局。
     */
    fun setTransparent(@ColorInt fallbackColor: Int = 0x00000000) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = fallbackColor
    }

    private fun setMiuiDarkIcons(dark: Boolean) {
        try {
            val clazz: Class<out Window> = window.javaClass
            val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
            val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
            val darkModeFlag = field.getInt(layoutParams)
            val extraFlagField = clazz.getMethod("setExtraFlags", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
            extraFlagField.invoke(window, if (dark) darkModeFlag else 0, darkModeFlag)
        } catch (_: Exception) {
        }
    }

    private fun setFlymeDarkIcons(dark: Boolean) {
        try {
            val lp = window.attributes
            val darkFlag = WindowManager.LayoutParams::class.java.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
            val meizuFlags = WindowManager.LayoutParams::class.java.getDeclaredField("meizuFlags")
            darkFlag.isAccessible = true
            meizuFlags.isAccessible = true
            val bit = darkFlag.getInt(null)
            var value = meizuFlags.getInt(lp)
            value = if (dark) value or bit else value and bit.inv()
            meizuFlags.setInt(lp, value)
            window.attributes = lp
        } catch (_: Exception) {
        }
    }
}

/**
 * 获取状态栏高度。
 */
fun Context.statusBarHeightPx(): Int {
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else (24 * resources.displayMetrics.density).toInt()
}

