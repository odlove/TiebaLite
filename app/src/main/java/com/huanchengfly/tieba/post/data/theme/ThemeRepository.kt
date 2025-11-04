package com.huanchengfly.tieba.post.data.theme

import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
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
