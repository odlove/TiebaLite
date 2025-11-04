package com.huanchengfly.tieba.post.ui.common.theme.compose

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.huanchengfly.tieba.core.ui.theme.ThemeController
import com.huanchengfly.tieba.core.ui.theme.ThemePalette
import com.huanchengfly.tieba.core.ui.theme.ThemeState
import com.huanchengfly.tieba.post.di.entrypoints.ThemeControllerEntryPoint
import dagger.hilt.android.EntryPointAccessors
import android.util.Log
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberUpdatedState
import java.util.concurrent.atomic.AtomicInteger
import com.huanchengfly.tieba.post.ui.common.theme.compose.THEME_DIAGNOSTICS_TAG

val LocalThemeController = staticCompositionLocalOf<ThemeController> {
    error("ThemeController not provided. Wrap your composition with ProvideThemeController.")
}

val LocalThemeState = staticCompositionLocalOf<ThemeState> {
    error("ThemeState not provided. Wrap your composition with ProvideThemeController.")
}

@Composable
fun ProvideThemeController(
    content: @Composable () -> Unit
) {
    val application = LocalContext.current.applicationContext as Application
    val themeController = remember(application) {
        EntryPointAccessors.fromApplication(
            application,
            ThemeControllerEntryPoint::class.java
        ).themeController()
    }
    val themeState = themeController.themeState.collectAsState()
    LaunchedEffect(themeState.value) {
        val value = themeState.value
        Log.i(
            THEME_DIAGNOSTICS_TAG,
            "ThemeController emit raw=${value.rawTheme} effective=${value.effectiveTheme} " +
                "resolved=${value.resolvedTheme} translucent=${value.isTranslucent} dynamic=${value.useDynamicColor}"
        )
    }

    ProvideThemeController(
        controller = themeController,
        themeState = themeState,
        content = content
    )
}

@Composable
fun ProvideThemeController(
    controller: ThemeController,
    themeState: State<ThemeState>,
    content: @Composable () -> Unit
) {
    val recomposeCounter = remember { AtomicInteger(0) }
    val state = rememberUpdatedState(themeState.value)

    SideEffect {
        val count = recomposeCounter.incrementAndGet()
        Log.i(
            THEME_DIAGNOSTICS_TAG,
            "ProvideThemeController recomposed count=$count raw=${state.value.rawTheme} " +
                "effective=${state.value.effectiveTheme}"
        )
    }
    DisposableEffect(Unit) {
        Log.i(
            THEME_DIAGNOSTICS_TAG,
            "ProvideThemeController compose entered raw=${state.value.rawTheme} effective=${state.value.effectiveTheme}"
        )
        onDispose {
            Log.i(
                THEME_DIAGNOSTICS_TAG,
                "ProvideThemeController disposed raw=${state.value.rawTheme} effective=${state.value.effectiveTheme}"
            )
        }
    }
    CompositionLocalProvider(
        LocalThemeController provides controller,
        LocalThemeState provides themeState.value,
        content = content
    )
}

@Composable
fun currentThemeController(): ThemeController = LocalThemeController.current

@Composable
fun currentThemeState(): ThemeState = LocalThemeState.current

@Composable
fun rememberThemePalette(): ThemePalette {
    val state = currentThemeState()
    return remember(state) { state.palette }
}
