package com.huanchengfly.tieba.core.ui.theme.data

import com.huanchengfly.tieba.core.common.preferences.ThemePreferencesDataSource
import com.huanchengfly.tieba.core.common.theme.ThemeChannel
import com.huanchengfly.tieba.core.common.theme.ThemeChannelConfig
import com.huanchengfly.tieba.core.common.theme.ThemeSettingsSnapshot
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@Singleton
class ThemeRepositoryImpl @Inject constructor(
    private val preferences: ThemePreferencesDataSource
) : ThemeRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val settingsState = preferences.themeSettingsFlow
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeSettingsSnapshot.default()
        )

    override val settingsFlow: Flow<ThemeSettingsSnapshot> = settingsState

    override fun currentSettings(): ThemeSettingsSnapshot = settingsState.value

    override suspend fun updateChannel(
        channel: ThemeChannel,
        reducer: (ThemeChannelConfig) -> ThemeChannelConfig
    ) {
        preferences.updateChannel(channel, reducer)
    }

    override suspend fun setActiveChannel(channel: ThemeChannel) {
        preferences.setActiveChannel(channel)
    }

    override suspend fun setFollowSystemNight(enable: Boolean) {
        preferences.setFollowSystemNight(enable)
    }
}
