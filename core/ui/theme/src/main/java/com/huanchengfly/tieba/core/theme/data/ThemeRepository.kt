package com.huanchengfly.tieba.core.theme.data

import com.huanchengfly.tieba.core.common.theme.ThemeChannel
import com.huanchengfly.tieba.core.common.theme.ThemeChannelConfig
import com.huanchengfly.tieba.core.common.theme.ThemeSettingsSnapshot
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    val settingsFlow: Flow<ThemeSettingsSnapshot>

    fun currentSettings(): ThemeSettingsSnapshot

    suspend fun updateChannel(
        channel: ThemeChannel,
        reducer: (ThemeChannelConfig) -> ThemeChannelConfig
    )

    suspend fun setActiveChannel(channel: ThemeChannel)

    suspend fun setFollowSystemNight(enable: Boolean)
}
