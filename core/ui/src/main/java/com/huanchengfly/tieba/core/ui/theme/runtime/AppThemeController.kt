package com.huanchengfly.tieba.core.ui.theme.runtime

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import com.huanchengfly.tieba.core.mvi.DispatcherProvider
import com.huanchengfly.tieba.core.runtime.di.ApplicationScope
import com.huanchengfly.tieba.core.common.ext.getColorCompat
import com.huanchengfly.tieba.core.ui.R
import com.huanchengfly.tieba.core.ui.theme.CustomThemeConfig
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private data class ThemeSnapshot(
    val spec: ThemeSpec,
    val rawTheme: String,
    val effectiveTheme: String,
    val resolvedTheme: String,
    val palette: ThemePalette,
    val customConfig: CustomThemeConfig?,
    val translucentConfig: TranslucentThemeConfig?,
    val useDynamicColor: Boolean,
    val toolbarPrimary: Boolean
)

@Singleton
class AppThemeController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val themeRepository: ThemeRepository,
    @ApplicationScope private val applicationScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider
) : ThemeController {

    private val themePaletteProvider: ThemePaletteProvider by lazy {
        ThemePaletteProvider.createInstance(context)
    }

    private val logTag = "AppThemeController"

private val state = MutableStateFlow(createSnapshot(themeRepository.theme, themeRepository.useDynamicColorTheme))

private val themeStateFlow: StateFlow<ThemeState> = state
    .map { it.toThemeState() }
    .stateIn(
        scope = applicationScope,
        started = SharingStarted.Eagerly,
        initialValue = state.value.toThemeState()
    )

    init {
        applicationScope.launch(dispatcherProvider.io) {
            themeRepository.themeFlow
                .distinctUntilChanged()
                .collect { themeKey ->
                    Log.i(logTag, "themeFlow emit themeKey=$themeKey")
                    refresh(themeKey, themeRepository.useDynamicColorTheme)
                }
        }
        applicationScope.launch(dispatcherProvider.io) {
            themeRepository.dynamicThemeFlow
                .distinctUntilChanged()
                .collect { useDynamic ->
                    Log.i(logTag, "dynamicThemeFlow emit useDynamic=$useDynamic")
                    refresh(themeRepository.theme ?: ThemeTokens.THEME_DEFAULT, useDynamic)
                }
        }
    }

    private fun refresh(themeKey: String?, useDynamic: Boolean) {
        val newSnapshot = createSnapshot(themeKey, useDynamic)
        if (newSnapshot == state.value) {
            return
        }
        Log.i(logTag, "refresh themeKey=$themeKey useDynamic=$useDynamic")
        state.value = newSnapshot
    }

    private fun createSnapshot(themeKey: String?, useDynamic: Boolean): ThemeSnapshot {
        val rawKey = themeKey
            ?.takeUnless { it.isBlank() }
            ?.removeSuffix("_dynamic")
            ?: ThemeTokens.THEME_DEFAULT
        val spec = ThemeCatalog.get(rawKey)
        val custom = loadCustomConfig()
        val translucent = loadTranslucentConfig()
        val toolbarPrimary = themeRepository.toolbarPrimaryColor
        val effectiveTheme = resolveEffectiveTheme(spec, translucent)
        val dynamicEnabled = useDynamic && spec.supportsDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        // 获取对应的 ThemeSpec（用于 effectiveTheme）
        val effectiveSpec = ThemeCatalog.get(effectiveTheme)
        val palette = themePaletteProvider.resolve(
            spec = effectiveSpec,
            useDynamicColor = dynamicEnabled,
            toolbarPrimary = toolbarPrimary,
            customConfig = custom,
            translucentConfig = translucent
        )
        Log.i(
            logTag,
            "createSnapshot raw=$rawKey effective=$effectiveTheme dynamicEnabled=$dynamicEnabled " +
                "toolbarPrimary=$toolbarPrimary translucentPath=${translucent.backgroundPath}"
        )
        val resolvedTheme = if (dynamicEnabled) "${effectiveTheme}_dynamic" else effectiveTheme
        return ThemeSnapshot(
            spec = spec,
            rawTheme = rawKey,
            effectiveTheme = effectiveTheme,
            resolvedTheme = resolvedTheme,
            palette = palette,
            customConfig = custom,
            translucentConfig = translucent,
            useDynamicColor = dynamicEnabled,
            toolbarPrimary = toolbarPrimary
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
                val variant = translucentConfig?.themeVariant ?: themeRepository.translucentBackgroundTheme
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

    private fun loadCustomConfig(): CustomThemeConfig {
        val primary = themeRepository.customPrimaryColor?.let { colorHex ->
            runCatching { Color.parseColor(colorHex) }.getOrNull()
        }
        val fallbackPrimary = ThemeDefaults.resolveAttr(R.attr.colorPrimary)
        val resolvedPrimary = primary ?: context.getColorCompat(fallbackPrimary)
        return CustomThemeConfig(
            primaryColor = resolvedPrimary,
            toolbarPrimary = themeRepository.toolbarPrimaryColor,
            statusBarDark = themeRepository.customStatusBarFontDark
        )
    }

    private fun loadTranslucentConfig(): TranslucentThemeConfig {
        return TranslucentThemeConfig(
            backgroundPath = themeRepository.translucentThemeBackgroundPath,
            primaryColor = themeRepository.translucentPrimaryColor?.let { runCatching { Color.parseColor(it) }.getOrNull() },
            themeVariant = themeRepository.translucentBackgroundTheme,
            blur = themeRepository.translucentBackgroundBlur,
            alpha = themeRepository.translucentBackgroundAlpha
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
            customConfig = customConfig,
            translucentConfig = translucentConfig,
            toolbarPrimary = toolbarPrimary
        )
    }

    override fun switchTheme(theme: String, recordOldTheme: Boolean) {
        val spec = ThemeCatalog.get(theme)
        val currentSpec = state.value.spec
        if (recordOldTheme && !currentSpec.isNight) {
            themeRepository.oldTheme = currentSpec.key
        }
        if (spec.isNight) {
            themeRepository.darkTheme = spec.key
        }
        themeRepository.theme = spec.key
        refresh(spec.key, themeRepository.useDynamicColorTheme)
    }

    override fun toggleNightMode() {
        val snapshot = state.value
        val isNight = ThemeCatalog.get(snapshot.effectiveTheme).isNight
        if (isNight) {
            val fallback = themeRepository.oldTheme ?: ThemeTokens.THEME_DEFAULT
            switchTheme(fallback, recordOldTheme = false)
        } else {
            val target = themeRepository.darkTheme ?: ThemeTokens.THEME_AMOLED_DARK
            switchTheme(target, recordOldTheme = false)
        }
    }

    override fun toggleDynamicTheme() {
        setUseDynamicTheme(!isUsingDynamicTheme)
    }

    override fun setUseDynamicTheme(useDynamicTheme: Boolean) {
        themeRepository.useDynamicColorTheme = useDynamicTheme
        refresh(state.value.rawTheme, useDynamicTheme)
    }

    private fun normalizeThemeKey(theme: String): String =
        if (theme.endsWith("_dynamic")) theme.removeSuffix("_dynamic") else theme

    override val isUsingDynamicTheme: Boolean
        get() = state.value.useDynamicColor

    override fun isNightTheme(theme: String): Boolean = ThemeCatalog.get(normalizeThemeKey(theme)).isNight

    override fun isTranslucentTheme(theme: String): Boolean =
        ThemeCatalog.get(normalizeThemeKey(theme)).type == ThemeType.TRANSLUCENT

    override fun shouldUseDarkStatusBarIcons(): Boolean {
        val snapshot = state.value
        return when (snapshot.spec.type) {
            ThemeType.CUSTOM -> snapshot.customConfig?.statusBarDark ?: false
            ThemeType.TRANSLUCENT -> {
                snapshot.translucentConfig?.themeVariant != ThemeTokens.TRANSLUCENT_THEME_DARK
            }
            ThemeType.STATIC -> !ThemeCatalog.get(snapshot.effectiveTheme).isNight
        }
    }

    override fun shouldUseDarkNavigationBarIcons(): Boolean =
        !ThemeCatalog.get(state.value.effectiveTheme).isNight

    override fun resolveCurrentTheme(checkDynamic: Boolean): String {
        val snapshot = state.value
        return if (checkDynamic) snapshot.resolvedTheme else snapshot.effectiveTheme
    }
}
