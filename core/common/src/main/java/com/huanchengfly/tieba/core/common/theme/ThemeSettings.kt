package com.huanchengfly.tieba.core.common.theme

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ThemeChannel {
    DAY,
    NIGHT
}

@Serializable
data class ThemeChannelConfig(
    val rawTheme: String,
    val useDynamicColorWanted: Boolean,
    val toolbarPrimary: Boolean,
    val custom: PersistedCustomThemeConfig? = null,
    val translucent: PersistedTranslucentThemeConfig? = null,
    val overrides: ThemeOverrides = ThemeOverrides()
)

@Serializable
data class PersistedCustomThemeConfig(
    val primaryColor: Int?,
    val statusBarDark: Boolean
)

@Serializable
data class PersistedTranslucentThemeConfig(
    val backgroundPath: String?,
    val primaryColor: Int?,
    val themeVariant: Int,
    val blur: Int,
    val alpha: Int
)

@Serializable
data class ThemeOverrides(
    val extras: Map<String, String> = emptyMap()
)

@Serializable
data class ThemeSettingsSnapshot(
    val activeChannel: ThemeChannel,
    val followSystemNight: Boolean,
    @SerialName("light_preset")
    val light: ThemeChannelConfig,
    @SerialName("dark_preset")
    val dark: ThemeChannelConfig
) {
    fun currentChannelConfig(): ThemeChannelConfig =
        if (activeChannel == ThemeChannel.DAY) light else dark

    companion object {
        const val DEFAULT_LIGHT_THEME = "tieba"
        const val DEFAULT_DARK_THEME = "amoled_dark"

        fun default(): ThemeSettingsSnapshot = ThemeSettingsSnapshot(
            activeChannel = ThemeChannel.DAY,
            followSystemNight = true,
            light = ThemeChannelConfig(
                rawTheme = DEFAULT_LIGHT_THEME,
                useDynamicColorWanted = false,
                toolbarPrimary = false
            ),
            dark = ThemeChannelConfig(
                rawTheme = DEFAULT_DARK_THEME,
                useDynamicColorWanted = false,
                toolbarPrimary = false
            )
        )
    }
}
