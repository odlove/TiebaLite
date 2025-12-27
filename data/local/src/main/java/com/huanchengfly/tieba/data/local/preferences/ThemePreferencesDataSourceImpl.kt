package com.huanchengfly.tieba.data.local.preferences

import android.content.Context
import android.graphics.Color
import androidx.datastore.core.DataStore
import com.huanchengfly.tieba.core.common.preferences.AppPreferencesDataSource
import com.huanchengfly.tieba.core.common.preferences.ThemePreferencesDataSource
import com.huanchengfly.tieba.core.common.theme.PersistedCustomThemeConfig
import com.huanchengfly.tieba.core.common.theme.PersistedTranslucentThemeConfig
import com.huanchengfly.tieba.core.common.theme.ThemeChannel
import com.huanchengfly.tieba.core.common.theme.ThemeOverrides
import com.huanchengfly.tieba.core.common.theme.ThemeChannelConfig as DomainThemeChannelConfig
import com.huanchengfly.tieba.core.common.theme.ThemeSettingsSnapshot
import com.huanchengfly.tieba.data.local.proto.CustomThemeConfig as ProtoCustomThemeConfig
import com.huanchengfly.tieba.data.local.proto.ThemeChannel as ProtoChannel
import com.huanchengfly.tieba.data.local.proto.ThemeChannelConfig as ProtoChannelConfig
import com.huanchengfly.tieba.data.local.proto.ThemeSettings
import com.huanchengfly.tieba.data.local.proto.TranslucentThemeConfig as ProtoTranslucentThemeConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class ThemePreferencesDataSourceImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val appPreferences: AppPreferencesDataSource
) : ThemePreferencesDataSource {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val migrationMutex = Mutex()
    private val dataStore: DataStore<ThemeSettings> = context.themeSettingsDataStore

    init {
        scope.launch {
            migrateLegacyIfNeeded()
        }
    }

    override val themeSettingsFlow: Flow<ThemeSettingsSnapshot> =
        dataStore.data.map { it.toSnapshot() }

    override suspend fun updateThemeSettings(transform: (ThemeSettingsSnapshot) -> ThemeSettingsSnapshot) {
        dataStore.updateData { current ->
            val snapshot = current.toSnapshot()
            transform(snapshot).toProto()
        }
    }

    override suspend fun updateChannel(
        channel: ThemeChannel,
        reducer: (DomainThemeChannelConfig) -> DomainThemeChannelConfig
    ) {
        updateThemeSettings { snapshot ->
            when (channel) {
                ThemeChannel.DAY -> snapshot.copy(light = reducer(snapshot.light))
                ThemeChannel.NIGHT -> snapshot.copy(dark = reducer(snapshot.dark))
            }
        }
    }

    override suspend fun setActiveChannel(channel: ThemeChannel) {
        updateThemeSettings { it.copy(activeChannel = channel) }
    }

    override suspend fun setFollowSystemNight(enable: Boolean) {
        updateThemeSettings { it.copy(followSystemNight = enable) }
    }

    private suspend fun migrateLegacyIfNeeded() {
        migrationMutex.withLock {
            dataStore.updateData { current ->
                if (current.lightPreset.rawTheme.isNotBlank()) {
                    return@updateData current
                }
                legacySnapshot().toProto()
            }
        }
    }

    private fun legacySnapshot(): ThemeSettingsSnapshot {
        val lightConfig = DomainThemeChannelConfig(
            rawTheme = appPreferences.theme ?: ThemeSettingsSnapshot.default().light.rawTheme,
            useDynamicColorWanted = appPreferences.useDynamicColorTheme,
            toolbarPrimary = appPreferences.toolbarPrimaryColor,
            custom = appPreferences.customPrimaryColor?.let { colorHex ->
                PersistedCustomThemeConfig(primaryColor = parseColor(colorHex), statusBarDark = appPreferences.customStatusBarFontDark)
            },
            translucent = if (appPreferences.translucentThemeBackgroundPath != null) {
                PersistedTranslucentThemeConfig(
                    backgroundPath = appPreferences.translucentThemeBackgroundPath,
                    primaryColor = parseColor(appPreferences.translucentPrimaryColor),
                    themeVariant = appPreferences.translucentBackgroundTheme,
                    blur = appPreferences.translucentBackgroundBlur,
                    alpha = appPreferences.translucentBackgroundAlpha
                )
            } else null,
            overrides = ThemeOverrides()
        )
        val darkConfig = DomainThemeChannelConfig(
            rawTheme = appPreferences.darkTheme ?: ThemeSettingsSnapshot.default().dark.rawTheme,
            useDynamicColorWanted = appPreferences.useDynamicColorTheme,
            toolbarPrimary = appPreferences.toolbarPrimaryColor,
            custom = null,
            translucent = null,
            overrides = ThemeOverrides()
        )
        val activeChannel = if (lightConfig.rawTheme == appPreferences.theme) ThemeChannel.DAY else ThemeChannel.NIGHT
        return ThemeSettingsSnapshot(
            activeChannel = activeChannel,
            followSystemNight = appPreferences.followSystemNight,
            light = lightConfig,
            dark = darkConfig
        )
    }

    private fun ThemeSettings.toSnapshot(): ThemeSettingsSnapshot {
        if (this.lightPreset.rawTheme.isBlank()) {
            return ThemeSettingsSnapshot.default()
        }
        return ThemeSettingsSnapshot(
            activeChannel = when (this.activeChannel) {
                ProtoChannel.NIGHT -> ThemeChannel.NIGHT
                else -> ThemeChannel.DAY
            },
            followSystemNight = this.followSystemNight,
            light = this.lightPreset.toDomain(),
            dark = this.darkPreset.toDomain()
        )
    }

    private fun DomainThemeChannelConfig.toProto(): ProtoChannelConfig =
        ProtoChannelConfig.newBuilder()
            .setRawTheme(rawTheme)
            .setUseDynamicColorWanted(useDynamicColorWanted)
            .setToolbarPrimary(toolbarPrimary)
            .apply {
                this@toProto.custom?.let { customConfig ->
                    setCustom(
                        ProtoCustomThemeConfig.newBuilder()
                            .setPrimaryColorArgb(customConfig.primaryColor ?: 0)
                            .setStatusBarDark(customConfig.statusBarDark)
                            .build()
                    )
                }
                this@toProto.translucent?.let { translucentConfig ->
                    setTranslucent(
                        ProtoTranslucentThemeConfig.newBuilder()
                            .setBackgroundPath(translucentConfig.backgroundPath ?: "")
                            .setPrimaryColorArgb(translucentConfig.primaryColor ?: 0)
                            .setThemeVariant(translucentConfig.themeVariant)
                            .setBlur(translucentConfig.blur)
                            .setAlpha(translucentConfig.alpha)
                            .build()
                    )
                }
                putAllOverrides(this@toProto.overrides.extras)
            }
            .build()

    private fun ThemeSettingsSnapshot.toProto(): ThemeSettings =
        ThemeSettings.newBuilder()
            .setActiveChannel(
                when (activeChannel) {
                    ThemeChannel.DAY -> ProtoChannel.DAY
                    ThemeChannel.NIGHT -> ProtoChannel.NIGHT
                }
            )
            .setFollowSystemNight(followSystemNight)
            .setLightPreset(light.toProto())
            .setDarkPreset(dark.toProto())
            .build()

    private fun ProtoChannelConfig.toDomain(): DomainThemeChannelConfig =
        DomainThemeChannelConfig(
            rawTheme = rawTheme.ifBlank { ThemeSettingsSnapshot.default().light.rawTheme },
            useDynamicColorWanted = this.useDynamicColorWanted,
            toolbarPrimary = this.toolbarPrimary,
            custom = if (hasCustom()) {
                PersistedCustomThemeConfig(
                    primaryColor = custom.primaryColorArgb.takeIf { it != 0 },
                    statusBarDark = custom.statusBarDark
                )
            } else null,
            translucent = if (hasTranslucent()) {
                PersistedTranslucentThemeConfig(
                    backgroundPath = translucent.backgroundPath.takeIf { it.isNotBlank() },
                    primaryColor = translucent.primaryColorArgb.takeIf { it != 0 },
                    themeVariant = translucent.themeVariant,
                    blur = translucent.blur,
                    alpha = translucent.alpha
                )
            } else null,
            overrides = ThemeOverrides(overridesMap)
        )

    private fun parseColor(color: String?): Int? =
        color?.takeIf { it.isNotBlank() }?.let {
            runCatching { android.graphics.Color.parseColor(it) }.getOrNull()
        }
}
