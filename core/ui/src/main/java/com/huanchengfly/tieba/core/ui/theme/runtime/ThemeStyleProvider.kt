package com.huanchengfly.tieba.core.ui.theme.runtime

interface ThemeStyleProvider {
    fun resolveThemeStyle(themeName: String): Int
}
