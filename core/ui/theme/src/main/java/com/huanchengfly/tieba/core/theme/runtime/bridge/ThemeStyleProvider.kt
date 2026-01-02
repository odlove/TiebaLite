package com.huanchengfly.tieba.core.theme.runtime.bridge

interface ThemeStyleProvider {
    fun resolveThemeStyle(themeName: String): Int
}
