package com.huanchengfly.tieba.post.data.theme

import com.huanchengfly.tieba.post.utils.AppPreferencesUtils
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Singleton
class ThemeRepositoryImpl @Inject constructor(
    private val preferences: AppPreferencesUtils
) : ThemeRepository {

    override val themeFlow: Flow<String> = preferences.themeFlow.distinctUntilChanged()

    override val dynamicThemeFlow: Flow<Boolean> = preferences.dynamicThemeFlow.distinctUntilChanged()

    override var theme: String?
        get() = preferences.theme
        set(value) {
            preferences.theme = value
        }

    override var oldTheme: String?
        get() = preferences.oldTheme
        set(value) {
            preferences.oldTheme = value
        }

    override var darkTheme: String?
        get() = preferences.darkTheme
        set(value) {
            preferences.darkTheme = value
        }

    override var useDynamicColorTheme: Boolean
        get() = preferences.useDynamicColorTheme
        set(value) {
            preferences.useDynamicColorTheme = value
        }

    override var customPrimaryColor: String?
        get() = preferences.customPrimaryColor
        set(value) {
            preferences.customPrimaryColor = value
        }

    override var toolbarPrimaryColor: Boolean
        get() = preferences.toolbarPrimaryColor
        set(value) {
            preferences.toolbarPrimaryColor = value
        }

    override var customStatusBarFontDark: Boolean
        get() = preferences.customStatusBarFontDark
        set(value) {
            preferences.customStatusBarFontDark = value
        }

    override var translucentThemeBackgroundPath: String?
        get() = preferences.translucentThemeBackgroundPath
        set(value) {
            preferences.translucentThemeBackgroundPath = value
        }

    override var translucentPrimaryColor: String?
        get() = preferences.translucentPrimaryColor
        set(value) {
            preferences.translucentPrimaryColor = value
        }

    override var translucentBackgroundTheme: Int
        get() = preferences.translucentBackgroundTheme
        set(value) {
            preferences.translucentBackgroundTheme = value
        }

    override var translucentBackgroundBlur: Int
        get() = preferences.translucentBackgroundBlur
        set(value) {
            preferences.translucentBackgroundBlur = value
        }

    override var translucentBackgroundAlpha: Int
        get() = preferences.translucentBackgroundAlpha
        set(value) {
            preferences.translucentBackgroundAlpha = value
        }
}
