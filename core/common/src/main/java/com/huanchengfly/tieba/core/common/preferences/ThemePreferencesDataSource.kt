package com.huanchengfly.tieba.core.common.preferences

import com.huanchengfly.tieba.core.common.theme.ThemeChannel
import com.huanchengfly.tieba.core.common.theme.ThemeChannelConfig
import com.huanchengfly.tieba.core.common.theme.ThemeSettingsSnapshot
import kotlinx.coroutines.flow.Flow

/**
 * 面向主题系统的偏好访问接口，统一通过 ThemeSettingsSnapshot 暴露日/夜配置。
 */
interface ThemePreferencesDataSource {
    val themeSettingsFlow: Flow<ThemeSettingsSnapshot>
    suspend fun updateThemeSettings(transform: (ThemeSettingsSnapshot) -> ThemeSettingsSnapshot)
    suspend fun updateChannel(
        channel: ThemeChannel,
        reducer: (ThemeChannelConfig) -> ThemeChannelConfig
    )
    suspend fun setActiveChannel(channel: ThemeChannel)
    suspend fun setFollowSystemNight(enable: Boolean)
}
