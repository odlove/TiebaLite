package com.huanchengfly.tieba.core.ui.theme.runtime

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import com.huanchengfly.tieba.core.runtime.di.ApplicationScope
import com.huanchengfly.tieba.core.common.ext.getColorCompat
import com.huanchengfly.tieba.core.ui.R
import com.huanchengfly.tieba.core.ui.theme.CustomThemeConfig
import com.huanchengfly.tieba.core.common.theme.ThemeChannel
import com.huanchengfly.tieba.core.common.theme.ThemeChannelConfig
import com.huanchengfly.tieba.core.common.theme.ThemeSettingsSnapshot
import com.huanchengfly.tieba.core.ui.theme.ThemeCatalog
import com.huanchengfly.tieba.core.ui.theme.ThemeController
import com.huanchengfly.tieba.core.ui.theme.ThemePalette
import com.huanchengfly.tieba.core.ui.theme.ThemeSpec
import com.huanchengfly.tieba.core.ui.theme.ThemeState
import com.huanchengfly.tieba.core.ui.theme.ThemeTokens
import com.huanchengfly.tieba.core.ui.theme.ThemeType
import com.huanchengfly.tieba.core.ui.theme.TranslucentThemeConfig
import com.huanchengfly.tieba.core.ui.theme.data.ThemeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class ThemeSnapshot(
    val spec: ThemeSpec,
    val rawTheme: String,
    val effectiveTheme: String,
    val resolvedTheme: String,
    val palette: ThemePalette,
    val semanticColors: ThemeSemanticColors,
    val customConfig: CustomThemeConfig?,
    val translucentConfig: TranslucentThemeConfig?,
    val useDynamicColor: Boolean,
    val toolbarPrimary: Boolean,
    val channel: ThemeChannel,
    val followSystemNight: Boolean
)

@Singleton
class AppThemeController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val themeRepository: ThemeRepository,
    @ApplicationScope private val applicationScope: CoroutineScope
) : ThemeController {

    private val themePaletteProvider: ThemePaletteProvider by lazy {
        ThemePaletteProvider.createInstance(context)
    }

    private val logTag = "AppThemeController"

    private val snapshotState: StateFlow<ThemeSnapshot> = themeRepository.settingsFlow
        .map { settings -> createSnapshot(settings) }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = createSnapshot(themeRepository.currentSettings())
        )

    private val themeStateFlow: StateFlow<ThemeState> = snapshotState
        .map { it.toThemeState() }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly,
            initialValue = snapshotState.value.toThemeState()
        )

    private fun createSnapshot(settings: ThemeSettingsSnapshot): ThemeSnapshot {
        val channelConfig = settings.currentChannelConfig()
        val rawKey = channelConfig.rawTheme
            .takeUnless { it.isBlank() }
            ?.removeSuffix("_dynamic")
            ?: ThemeTokens.THEME_DEFAULT
        val spec = ThemeCatalog.get(rawKey)
        val custom = buildCustomConfig(channelConfig)
        val translucent = buildTranslucentConfig(channelConfig)
        val toolbarPrimary = channelConfig.toolbarPrimary
        val effectiveTheme = resolveEffectiveTheme(spec, translucent)
        val dynamicEnabled = channelConfig.useDynamicColorWanted &&
            spec.supportsDynamicColor &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        val effectiveSpec = ThemeCatalog.get(effectiveTheme)
        val resolvedPalette = themePaletteProvider.resolve(
            spec = effectiveSpec,
            useDynamicColor = dynamicEnabled,
            toolbarPrimary = toolbarPrimary,
            customConfig = custom,
            translucentConfig = translucent
        )
        val palette = resolvedPalette.palette
        val semantics = resolvedPalette.semanticColors
        Log.i(
            logTag,
            "createSnapshot raw=$rawKey effective=$effectiveTheme dynamicEnabled=$dynamicEnabled " +
                "toolbarPrimary=$toolbarPrimary translucentPath=${translucent?.backgroundPath}"
        )
        val resolvedTheme = if (dynamicEnabled) "${effectiveTheme}_dynamic" else effectiveTheme
        return ThemeSnapshot(
            spec = spec,
            rawTheme = rawKey,
            effectiveTheme = effectiveTheme,
            resolvedTheme = resolvedTheme,
            palette = palette,
            semanticColors = semantics,
            customConfig = custom,
            translucentConfig = translucent,
            useDynamicColor = dynamicEnabled,
            toolbarPrimary = toolbarPrimary,
            channel = settings.activeChannel,
            followSystemNight = settings.followSystemNight
        )
    }

    private fun resolveEffectiveTheme(
        spec: ThemeSpec,
        translucentConfig: TranslucentThemeConfig?
    ): String {
        if (spec.type != ThemeType.TRANSLUCENT) {
            return spec.key
        }
        return when (spec.key) {
            ThemeTokens.THEME_TRANSLUCENT -> {
                val variant = translucentConfig?.themeVariant ?: ThemeTokens.TRANSLUCENT_THEME_LIGHT
                if (variant == ThemeTokens.TRANSLUCENT_THEME_DARK) {
                    ThemeTokens.THEME_TRANSLUCENT_DARK
                } else {
                    ThemeTokens.THEME_TRANSLUCENT_LIGHT
                }
            }
            ThemeTokens.THEME_TRANSLUCENT_LIGHT,
            ThemeTokens.THEME_TRANSLUCENT_DARK -> spec.key
            else -> spec.key
        }
    }

    private fun buildCustomConfig(channelConfig: ThemeChannelConfig): CustomThemeConfig? {
        val persisted = channelConfig.custom ?: return null
        val fallbackPrimary = ThemeDefaults.resolveAttr(R.attr.colorPrimary)
        val resolvedPrimary = persisted.primaryColor ?: context.getColorCompat(fallbackPrimary)
        return CustomThemeConfig(
            primaryColor = resolvedPrimary,
            toolbarPrimary = channelConfig.toolbarPrimary,
            statusBarDark = persisted.statusBarDark
        )
    }

    private fun buildTranslucentConfig(channelConfig: ThemeChannelConfig): TranslucentThemeConfig? {
        val persisted = channelConfig.translucent ?: return null
        return TranslucentThemeConfig(
            backgroundPath = persisted.backgroundPath,
            primaryColor = persisted.primaryColor,
            themeVariant = persisted.themeVariant,
            blur = persisted.blur,
            alpha = persisted.alpha
        )
    }

    override val themeState: StateFlow<ThemeState>
        get() = themeStateFlow

    private fun ThemeSnapshot.toThemeState(): ThemeState {
        val effectiveSpec = ThemeCatalog.get(effectiveTheme)
        return ThemeState(
            rawTheme = rawTheme,
            effectiveTheme = effectiveTheme,
            resolvedTheme = resolvedTheme,
            isNightMode = effectiveSpec.isNight,
            isTranslucent = spec.type == ThemeType.TRANSLUCENT,
            useDynamicColor = useDynamicColor,
            palette = palette,
            semanticColors = semanticColors,
            customConfig = customConfig,
            translucentConfig = translucentConfig,
            toolbarPrimary = toolbarPrimary
        )
    }

    override fun switchTheme(theme: String, recordOldTheme: Boolean) {
        val spec = ThemeCatalog.get(theme)
        val targetChannel = if (spec.isNight) ThemeChannel.NIGHT else ThemeChannel.DAY
        applicationScope.launch {
            themeRepository.updateChannel(targetChannel) { config ->
                config.copy(rawTheme = spec.key)
            }
            themeRepository.setActiveChannel(targetChannel)
        }
    }

    override fun toggleNightMode() {
        val nextChannel = if (snapshotState.value.channel == ThemeChannel.DAY) {
            ThemeChannel.NIGHT
        } else {
            ThemeChannel.DAY
        }
        applicationScope.launch {
            themeRepository.setActiveChannel(nextChannel)
        }
    }

    override fun toggleDynamicTheme() {
        setUseDynamicTheme(!isUsingDynamicTheme)
    }

    override fun setUseDynamicTheme(useDynamicTheme: Boolean) {
        val channel = snapshotState.value.channel
        applicationScope.launch {
            themeRepository.updateChannel(channel) { config ->
                config.copy(useDynamicColorWanted = useDynamicTheme)
            }
        }
    }

    private fun normalizeThemeKey(theme: String): String =
        if (theme.endsWith("_dynamic")) theme.removeSuffix("_dynamic") else theme

    override val isUsingDynamicTheme: Boolean
        get() = snapshotState.value.useDynamicColor

    override fun isNightTheme(theme: String): Boolean = ThemeCatalog.get(normalizeThemeKey(theme)).isNight

    override fun isTranslucentTheme(theme: String): Boolean =
        ThemeCatalog.get(normalizeThemeKey(theme)).type == ThemeType.TRANSLUCENT

    override fun shouldUseDarkStatusBarIcons(): Boolean {
        val snapshot = snapshotState.value
        return when (snapshot.spec.type) {
            ThemeType.CUSTOM -> snapshot.customConfig?.statusBarDark ?: false
            ThemeType.TRANSLUCENT -> {
                snapshot.translucentConfig?.themeVariant != ThemeTokens.TRANSLUCENT_THEME_DARK
            }
            ThemeType.STATIC -> !ThemeCatalog.get(snapshot.effectiveTheme).isNight
        }
    }

    override fun shouldUseDarkNavigationBarIcons(): Boolean =
        !ThemeCatalog.get(snapshotState.value.effectiveTheme).isNight

    override fun resolveCurrentTheme(checkDynamic: Boolean): String {
        val snapshot = snapshotState.value
        return if (checkDynamic) snapshot.resolvedTheme else snapshot.effectiveTheme
    }
}
