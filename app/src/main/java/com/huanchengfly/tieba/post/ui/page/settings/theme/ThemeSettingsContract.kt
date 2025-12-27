package com.huanchengfly.tieba.post.ui.page.settings.theme

import com.huanchengfly.tieba.core.common.theme.ThemeChannel
import com.huanchengfly.tieba.core.common.theme.ThemeSettingsSnapshot

data class ThemeSettingsUiState(
    val snapshot: ThemeSettingsSnapshot = ThemeSettingsSnapshot.default()
)

sealed interface ThemeSettingsAction {
    data class SetFollowSystemNight(val enabled: Boolean) : ThemeSettingsAction
    data class SetActiveChannel(val channel: ThemeChannel) : ThemeSettingsAction
    data class ApplyCustomTheme(
        val channel: ThemeChannel,
        val primaryColor: Int,
        val toolbarPrimary: Boolean,
        val statusBarDark: Boolean
    ) : ThemeSettingsAction
    data class SetUseDynamicTheme(val channel: ThemeChannel, val enabled: Boolean) : ThemeSettingsAction
    data class SetRawTheme(
        val channel: ThemeChannel,
        val themeKey: String,
        val useDynamicColorWanted: Boolean
    ) : ThemeSettingsAction
}
