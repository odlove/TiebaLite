package com.huanchengfly.tieba.core.ui.theme.runtime.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.huanchengfly.tieba.core.ui.theme.ThemeCatalog
import com.huanchengfly.tieba.core.ui.theme.ThemeController
import com.huanchengfly.tieba.core.ui.theme.ThemePalette
import com.huanchengfly.tieba.core.ui.theme.ThemeSpec
import com.huanchengfly.tieba.core.ui.theme.ThemeState
import com.huanchengfly.tieba.core.ui.theme.ThemeTokens
import com.huanchengfly.tieba.core.ui.theme.ThemeType
import com.huanchengfly.tieba.core.ui.theme.TranslucentThemeConfig
import com.huanchengfly.tieba.core.ui.theme.runtime.ThemePaletteProvider
import com.huanchengfly.tieba.core.ui.preferences.LocalPreferencesDataStore
import androidx.compose.runtime.CompositionLocalProvider
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun PreviewTheme(
    themeKey: String = ThemeTokens.THEME_DEFAULT,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val previewState = remember(themeKey, context) {
        createPreviewThemeState(context, ThemeCatalog.get(themeKey))
    }
    val controller = remember(previewState) { PreviewThemeController(previewState) }
    val themeStateState = controller.themeState.collectAsState()
    val previewDataStore = remember(context) {
        val previewFile = File.createTempFile("preview_", ".preferences_pb", context.cacheDir)
        PreferenceDataStoreFactory.create(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        ) {
            previewFile
        }
    }
    CompositionLocalProvider(LocalPreferencesDataStore provides previewDataStore) {
        ProvideThemeController(controller = controller, themeState = themeStateState) {
            content()
        }
    }
}

private fun createPreviewThemeState(context: android.content.Context, spec: ThemeSpec): ThemeState {
    val provider = ThemePaletteProvider.createInstance(context)
    val translucentConfig = if (spec.type == ThemeType.TRANSLUCENT) TranslucentThemeConfig(
        backgroundPath = null,
        primaryColor = null,
        themeVariant = ThemeTokens.TRANSLUCENT_THEME_LIGHT,
        blur = 0,
        alpha = 255
    ) else null
    val palette: ThemePalette = provider.resolve(
        spec = spec,
        useDynamicColor = false,
        toolbarPrimary = false,
        customConfig = null,
        translucentConfig = translucentConfig
    )
    return ThemeState(
        rawTheme = spec.key,
        effectiveTheme = spec.key,
        resolvedTheme = spec.key,
        isNightMode = spec.isNight,
        isTranslucent = spec.type == ThemeType.TRANSLUCENT,
        useDynamicColor = false,
        palette = palette,
        customConfig = null,
        translucentConfig = translucentConfig,
        toolbarPrimary = false
    )
}

private class PreviewThemeController(initialState: ThemeState) : ThemeController {
    private val stateFlow = MutableStateFlow(initialState)
    override val themeState: StateFlow<ThemeState> = stateFlow

    override fun switchTheme(theme: String, recordOldTheme: Boolean) { }

    override fun toggleNightMode() { }

    override fun toggleDynamicTheme() { }

    override fun setUseDynamicTheme(useDynamicTheme: Boolean) { }

    override val isUsingDynamicTheme: Boolean
        get() = stateFlow.value.useDynamicColor

    override fun isNightTheme(theme: String): Boolean = ThemeCatalog.get(theme).isNight

    override fun isTranslucentTheme(theme: String): Boolean =
        ThemeCatalog.get(theme).type == ThemeType.TRANSLUCENT

    override fun shouldUseDarkStatusBarIcons(): Boolean = !stateFlow.value.isNightMode

    override fun shouldUseDarkNavigationBarIcons(): Boolean = !stateFlow.value.isNightMode

    override fun resolveCurrentTheme(checkDynamic: Boolean): String = stateFlow.value.resolvedTheme
}
