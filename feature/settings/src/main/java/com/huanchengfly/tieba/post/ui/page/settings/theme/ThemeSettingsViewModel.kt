package com.huanchengfly.tieba.post.ui.page.settings.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huanchengfly.tieba.core.common.theme.PersistedCustomThemeConfig
import com.huanchengfly.tieba.core.common.theme.PersistedTranslucentThemeConfig
import com.huanchengfly.tieba.core.common.theme.ThemeChannel
import com.huanchengfly.tieba.core.common.theme.ThemeChannelConfig
import com.huanchengfly.tieba.core.common.theme.ThemeSettingsSnapshot
import com.huanchengfly.tieba.core.common.theme.ThemeTokens
import com.huanchengfly.tieba.core.common.theme.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ThemeSettingsViewModel @Inject constructor(
    private val themeRepository: ThemeRepository
) : ViewModel() {

    private val internalState: StateFlow<ThemeSettingsSnapshot> = themeRepository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = themeRepository.currentSettings()
        )

    val themeSettings: StateFlow<ThemeSettingsSnapshot> = internalState

    val uiState: StateFlow<ThemeSettingsUiState> = internalState
        .map { ThemeSettingsUiState(snapshot = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeSettingsUiState(snapshot = internalState.value)
        )

    fun onAction(action: ThemeSettingsAction) {
        when (action) {
            is ThemeSettingsAction.SetFollowSystemNight -> setFollowSystemNight(action.enabled)
            is ThemeSettingsAction.SetActiveChannel -> setActiveChannel(action.channel)
            is ThemeSettingsAction.ApplyCustomTheme -> applyCustomTheme(
                channel = action.channel,
                primaryColor = action.primaryColor,
                toolbarPrimary = action.toolbarPrimary,
                statusBarDark = action.statusBarDark
            )
            is ThemeSettingsAction.SetUseDynamicTheme -> setUseDynamicTheme(
                action.channel,
                action.enabled
            )
            is ThemeSettingsAction.SetRawTheme -> updateChannel(action.channel) { config ->
                config.copy(
                    rawTheme = action.themeKey,
                    useDynamicColorWanted = action.useDynamicColorWanted
                )
            }
        }
    }

    fun setFollowSystemNight(enable: Boolean) {
        viewModelScope.launch {
            themeRepository.setFollowSystemNight(enable)
        }
    }

    fun setActiveChannel(channel: ThemeChannel) {
        viewModelScope.launch {
            themeRepository.setActiveChannel(channel)
        }
    }

    fun applyCustomTheme(
        channel: ThemeChannel,
        primaryColor: Int,
        toolbarPrimary: Boolean,
        statusBarDark: Boolean
    ) {
        viewModelScope.launch {
            themeRepository.updateChannel(channel) { config ->
                config.copy(
                    rawTheme = ThemeTokens.THEME_CUSTOM,
                    useDynamicColorWanted = false,
                    toolbarPrimary = toolbarPrimary,
                    custom = PersistedCustomThemeConfig(
                        primaryColor = primaryColor,
                        statusBarDark = statusBarDark
                    )
                )
            }
        }
    }

    fun setUseDynamicTheme(channel: ThemeChannel, enabled: Boolean) {
        viewModelScope.launch {
            themeRepository.updateChannel(channel) { config ->
                config.copy(useDynamicColorWanted = enabled)
            }
        }
    }

    fun updateChannel(
        channel: ThemeChannel,
        reducer: (ThemeChannelConfig) -> ThemeChannelConfig
    ) {
        viewModelScope.launch {
            themeRepository.updateChannel(channel, reducer)
        }
    }

    fun updateTranslucentConfig(
        channel: ThemeChannel,
        transform: (PersistedTranslucentThemeConfig) -> PersistedTranslucentThemeConfig
    ) {
        viewModelScope.launch {
            themeRepository.updateChannel(channel) { config ->
                val base = config.translucent ?: DEFAULT_TRANSLUCENT_CONFIG
                config.copy(translucent = transform(base))
            }
        }
    }

    companion object {
        private val DEFAULT_TRANSLUCENT_CONFIG = PersistedTranslucentThemeConfig(
            backgroundPath = null,
            primaryColor = null,
            themeVariant = ThemeTokens.TRANSLUCENT_THEME_LIGHT,
            blur = 0,
            alpha = 255
        )
    }
}
