package com.huanchengfly.tieba.core.ui.theme.runtime.compose

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.huanchengfly.tieba.core.ui.theme.ThemeState
import com.huanchengfly.tieba.core.ui.theme.runtime.ThemeBridge
import com.huanchengfly.tieba.core.ui.theme.runtime.entrypoints.ThemeRuntimeEntryPoint
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ThemeCompositionLocalsKt.ProvideThemeController as BaseProvideThemeController
import dagger.hilt.android.EntryPointAccessors

@Composable
fun ProvideThemeController(content: @Composable () -> Unit) {
    val appContext = LocalContext.current.applicationContext
    val application = appContext as? android.app.Application
        ?: error("ProvideThemeController requires an Application context")
    val themeController = remember(application) {
        EntryPointAccessors.fromApplication(
            application,
            ThemeRuntimeEntryPoint::class.java
        ).themeController()
    }
    val themeState = themeController.themeState.collectAsState()
    LaunchedEffect(themeState.value) {
        logThemeState(themeState.value)
    }

    BaseProvideThemeController(
        controller = themeController,
        themeState = themeState,
        content = content
    )
}

private fun logThemeState(state: ThemeState) {
    Log.i(
        THEME_DIAGNOSTICS_TAG,
        "ThemeController emit raw=${state.rawTheme} effective=${state.effectiveTheme} " +
            "resolved=${state.resolvedTheme} translucent=${state.isTranslucent} dynamic=${state.useDynamicColor}"
    )
}

@Composable
fun rememberThemeBridge(): ThemeBridge {
    val appContext = LocalContext.current.applicationContext
    require(appContext is android.app.Application) {
        "ThemeBridge is only accessible when the Composition runs inside an Application context."
    }
    return remember(appContext) {
        EntryPointAccessors.fromApplication(
            appContext,
            ThemeRuntimeEntryPoint::class.java
        ).themeBridge()
    }
}
