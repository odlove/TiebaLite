package com.huanchengfly.tieba.core.theme2.runtime

import com.huanchengfly.tieba.core.theme2.model.ThemeChannel
import com.huanchengfly.tieba.core.theme2.model.ThemeMode
import com.huanchengfly.tieba.core.theme2.model.ThemeProfile
import com.huanchengfly.tieba.core.theme2.model.ThemeSettings

object ThemeDefaults {
    private const val DEFAULT_DAY_THEME = "tieba"
    private const val DEFAULT_NIGHT_THEME = "amoled_dark"

    val defaultProfile: ThemeProfile = ThemeProfile(
        day = ThemeSettings(mode = ThemeMode.STATIC, themeKey = DEFAULT_DAY_THEME),
        night = ThemeSettings(mode = ThemeMode.STATIC, themeKey = DEFAULT_NIGHT_THEME),
        manualChannel = ThemeChannel.DAY,
        followSystemNight = true
    )
}
