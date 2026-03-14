package com.huanchengfly.tieba.core.theme2.runtime

import com.huanchengfly.tieba.core.theme2.model.ThemeChannel
import com.huanchengfly.tieba.core.theme2.model.ThemeMode
import com.huanchengfly.tieba.core.theme2.model.ThemeProfile
import com.huanchengfly.tieba.core.theme2.model.ThemeSettings
import com.huanchengfly.tieba.core.theme2.semantic.ThemeSemanticCatalog

object ThemeDefaults {
    private const val DEFAULT_DAY_THEME = ThemeSemanticCatalog.THEME_TIEBA
    private const val DEFAULT_NIGHT_THEME = ThemeSemanticCatalog.THEME_DARK_BLACK

    val defaultProfile: ThemeProfile = ThemeProfile(
        day = ThemeSettings(mode = ThemeMode.STATIC, themeKey = DEFAULT_DAY_THEME),
        night = ThemeSettings(mode = ThemeMode.STATIC, themeKey = DEFAULT_NIGHT_THEME),
        manualChannel = ThemeChannel.DAY,
        followSystemNight = true
    )
}
