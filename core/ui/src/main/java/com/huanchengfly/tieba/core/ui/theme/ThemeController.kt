package com.huanchengfly.tieba.core.ui.theme

import kotlinx.coroutines.flow.StateFlow

/**
 * 主题控制器抽象，提供当前主题状态与切换操作，便于在多模块环境下注入实现。
 */
interface ThemeController {
    /**
     * 主题状态。外层可通过该 Flow 感知主题变更。
     */
    val themeState: StateFlow<ThemeState>

    /**
     * 切换主题。
     *
     * @param theme 目标主题标识。
     * @param recordOldTheme 是否记录旧主题，用于夜间模式切换回退。
     */
    fun switchTheme(theme: String, recordOldTheme: Boolean = true)

    /**
     * 切换夜间模式：如果当前为夜间，则恢复到旧主题；否则切换至记录的夜间主题。
     */
    fun toggleNightMode()

    /**
     * 切换动态配色开关。
     */
    fun toggleDynamicTheme()

    /**
     * 设置是否启用动态配色。
     */
    fun setUseDynamicTheme(useDynamicTheme: Boolean)

    /**
     * 当前是否启用动态配色。
     */
    val isUsingDynamicTheme: Boolean

    /**
     * 判断给定主题是否为夜间模式。
     */
    fun isNightTheme(theme: String): Boolean

    /**
     * 判断给定主题是否为半透明主题。
     */
    fun isTranslucentTheme(theme: String): Boolean

    /**
     * 当前状态栏是否使用深色字体。
     */
    fun shouldUseDarkStatusBarIcons(): Boolean

    /**
     * 当前导航栏是否使用深色字体。
     */
    fun shouldUseDarkNavigationBarIcons(): Boolean

    /**
     * 根据当前状态计算实际应用的主题标识。
     *
     * @param checkDynamic 是否考虑动态配色。
     */
    fun resolveCurrentTheme(checkDynamic: Boolean = false): String
}
