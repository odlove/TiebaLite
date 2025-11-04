package com.huanchengfly.tieba.post.ui.common.theme.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.huanchengfly.tieba.core.ui.theme.CustomThemeConfig
import com.huanchengfly.tieba.core.ui.theme.ThemeCatalog
import com.huanchengfly.tieba.core.ui.theme.ThemePalette
import com.huanchengfly.tieba.core.ui.theme.ThemeSpec
import com.huanchengfly.tieba.core.ui.theme.TranslucentThemeConfig
import com.huanchengfly.tieba.post.ui.common.theme.ThemePaletteProvider

@Composable
fun rememberThemePaletteProvider(): ThemePaletteProvider {
    val context = LocalContext.current
    return remember(context) { ThemePaletteProvider.createInstance(context) }
}

@Composable
fun rememberThemePalette(
    themeKey: String,
    useDynamicColor: Boolean = currentThemeState().useDynamicColor,
    toolbarPrimary: Boolean = currentThemeState().toolbarPrimary,
    customConfig: CustomThemeConfig? = currentThemeState().customConfig,
    translucentConfig: TranslucentThemeConfig? = currentThemeState().translucentConfig
): ThemePalette {
    return rememberThemePalette(
        spec = ThemeCatalog.get(themeKey),
        useDynamicColor = useDynamicColor,
        toolbarPrimary = toolbarPrimary,
        customConfig = customConfig,
        translucentConfig = translucentConfig
    )
}

@Composable
fun rememberThemePalette(
    spec: ThemeSpec,
    useDynamicColor: Boolean = currentThemeState().useDynamicColor,
    toolbarPrimary: Boolean = currentThemeState().toolbarPrimary,
    customConfig: CustomThemeConfig? = currentThemeState().customConfig,
    translucentConfig: TranslucentThemeConfig? = currentThemeState().translucentConfig
): ThemePalette {
    val provider = rememberThemePaletteProvider()
    return remember(spec, useDynamicColor, toolbarPrimary, customConfig, translucentConfig) {
        provider.resolve(
            spec = spec,
            useDynamicColor = useDynamicColor,
            toolbarPrimary = toolbarPrimary,
            customConfig = customConfig,
            translucentConfig = translucentConfig
        )
    }
}
