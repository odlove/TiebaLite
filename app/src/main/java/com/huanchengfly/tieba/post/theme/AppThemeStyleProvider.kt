package com.huanchengfly.tieba.post.theme

import com.huanchengfly.tieba.core.ui.theme.ThemeTokens
import com.huanchengfly.tieba.core.ui.theme.runtime.ThemeStyleProvider
import com.huanchengfly.tieba.post.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppThemeStyleProvider @Inject constructor() : ThemeStyleProvider {
    override fun resolveThemeStyle(themeName: String): Int {
        val normalized = themeName.removeSuffix("_dynamic").lowercase()
        return when (normalized) {
            ThemeTokens.THEME_TRANSLUCENT,
            ThemeTokens.THEME_TRANSLUCENT_LIGHT -> R.style.TiebaLite_Translucent_Light
            ThemeTokens.THEME_TRANSLUCENT_DARK -> R.style.TiebaLite_Translucent_Dark
            ThemeTokens.THEME_DEFAULT -> R.style.TiebaLite_Tieba
            ThemeTokens.THEME_BLACK -> R.style.TiebaLite_Black
            ThemeTokens.THEME_PURPLE -> R.style.TiebaLite_Purple
            ThemeTokens.THEME_PINK -> R.style.TiebaLite_Pink
            ThemeTokens.THEME_RED -> R.style.TiebaLite_Red
            ThemeTokens.THEME_BLUE_DARK -> R.style.TiebaLite_Dark_Blue
            ThemeTokens.THEME_GREY_DARK -> R.style.TiebaLite_Dark_Grey
            ThemeTokens.THEME_AMOLED_DARK -> R.style.TiebaLite_Dark_Amoled
            ThemeTokens.THEME_CUSTOM -> R.style.TiebaLite_Custom
            else -> R.style.TiebaLite_Tieba
        }
    }
}
