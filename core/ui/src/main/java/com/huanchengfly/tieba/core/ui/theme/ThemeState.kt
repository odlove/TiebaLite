package com.huanchengfly.tieba.core.ui.theme

/**
 * 描述当前主题状态的基础数据。
 *
 * @property rawTheme 原始主题标识（不含透明/动态处理）。
 * @property effectiveTheme 应用透明主题映射后的主题标识。
 * @property resolvedTheme 最终应用主题（在透明主题处理基础上考虑动态配色）。
 * @property isNightMode 当前是否处于夜间模式。
 * @property isTranslucent 当前主题是否为半透明主题。
 * @property useDynamicColor 是否启用动态配色。
 * @property palette 当前主题使用的颜色调色板。
 * @property customConfig 自定义主题的额外配置。
 * @property translucentConfig 透明主题的额外配置。
 * @property toolbarPrimary Toolbar 是否使用主色。
 */
data class ThemeState(
    val rawTheme: String,
    val effectiveTheme: String,
    val resolvedTheme: String,
    val isNightMode: Boolean,
    val isTranslucent: Boolean,
    val useDynamicColor: Boolean,
    val palette: ThemePalette,
    val customConfig: CustomThemeConfig?,
    val translucentConfig: TranslucentThemeConfig?,
    val toolbarPrimary: Boolean
)
