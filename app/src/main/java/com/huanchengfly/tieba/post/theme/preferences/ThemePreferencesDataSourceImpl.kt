package com.huanchengfly.tieba.post.theme.preferences

import com.huanchengfly.tieba.core.ui.theme.preferences.ThemePreferencesDataSource
import com.huanchengfly.tieba.post.utils.AppPreferencesUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemePreferencesDataSourceImpl @Inject constructor(
    private val appPreferences: AppPreferencesUtils
) : ThemePreferencesDataSource {

    override val themeFlow = appPreferences.themeFlow

    override val dynamicThemeFlow = appPreferences.dynamicThemeFlow

    override var theme: String?
        get() = appPreferences.theme
        set(value) {
            appPreferences.theme = value
        }

    override var oldTheme: String?
        get() = appPreferences.oldTheme
        set(value) {
            appPreferences.oldTheme = value
        }

    override var darkTheme: String?
        get() = appPreferences.darkTheme
        set(value) {
            appPreferences.darkTheme = value
        }

    override var useDynamicColorTheme: Boolean
        get() = appPreferences.useDynamicColorTheme
        set(value) {
            appPreferences.useDynamicColorTheme = value
        }

    override var customPrimaryColor: String?
        get() = appPreferences.customPrimaryColor
        set(value) {
            appPreferences.customPrimaryColor = value
        }

    override var toolbarPrimaryColor: Boolean
        get() = appPreferences.toolbarPrimaryColor
        set(value) {
            appPreferences.toolbarPrimaryColor = value
        }

    override var customStatusBarFontDark: Boolean
        get() = appPreferences.customStatusBarFontDark
        set(value) {
            appPreferences.customStatusBarFontDark = value
        }

    override var translucentThemeBackgroundPath: String?
        get() = appPreferences.translucentThemeBackgroundPath
        set(value) {
            appPreferences.translucentThemeBackgroundPath = value
        }

    override var translucentPrimaryColor: String?
        get() = appPreferences.translucentPrimaryColor
        set(value) {
            appPreferences.translucentPrimaryColor = value
        }

    override var translucentBackgroundTheme: Int
        get() = appPreferences.translucentBackgroundTheme
        set(value) {
            appPreferences.translucentBackgroundTheme = value
        }

    override var translucentBackgroundBlur: Int
        get() = appPreferences.translucentBackgroundBlur
        set(value) {
            appPreferences.translucentBackgroundBlur = value
        }

    override var translucentBackgroundAlpha: Int
        get() = appPreferences.translucentBackgroundAlpha
        set(value) {
            appPreferences.translucentBackgroundAlpha = value
        }
}
