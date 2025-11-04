package com.huanchengfly.tieba.post.ui.common.theme.compose

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.huanchengfly.tieba.core.ui.theme.ThemeController
import com.huanchengfly.tieba.core.ui.theme.ThemeState
import com.huanchengfly.tieba.core.ui.theme.runtime.ThemeBridge
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.THEME_DIAGNOSTICS_TAG
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ProvideThemeController as CoreProvideThemeController
import com.huanchengfly.tieba.post.di.entrypoints.ThemeControllerEntryPoint
import dagger.hilt.android.EntryPointAccessors
import android.util.Log

@Composable
fun ProvideThemeController(content: @Composable () -> Unit) {
    val application = LocalContext.current.applicationContext as Application
    val themeController = remember(application) {
        EntryPointAccessors.fromApplication(
            application,
            ThemeControllerEntryPoint::class.java
        ).themeController()
    }
    val themeState = themeController.themeState.collectAsState()
    LaunchedEffect(themeState.value) {
        logThemeState(themeState.value)
    }

    CoreProvideThemeController(
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
    val application = LocalContext.current.applicationContext as Application
    return remember(application) {
        EntryPointAccessors.fromApplication(
            application,
            ThemeControllerEntryPoint::class.java
        ).themeBridge()
    }
}
