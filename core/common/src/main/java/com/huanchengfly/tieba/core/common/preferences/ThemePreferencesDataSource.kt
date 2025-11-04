package com.huanchengfly.tieba.core.common.preferences

import kotlinx.coroutines.flow.Flow

/**
 * 面向主题系统的偏好访问接口，仅暴露主题相关的字段与 Flow。
 */
interface ThemePreferencesDataSource {
    val themeFlow: Flow<String>
    val dynamicThemeFlow: Flow<Boolean>

    var theme: String?
    var oldTheme: String?
    var darkTheme: String?
    var useDynamicColorTheme: Boolean

    var customPrimaryColor: String?
    var toolbarPrimaryColor: Boolean
    var customStatusBarFontDark: Boolean

    var translucentThemeBackgroundPath: String?
    var translucentPrimaryColor: String?
    var translucentBackgroundTheme: Int
    var translucentBackgroundBlur: Int
    var translucentBackgroundAlpha: Int
}
