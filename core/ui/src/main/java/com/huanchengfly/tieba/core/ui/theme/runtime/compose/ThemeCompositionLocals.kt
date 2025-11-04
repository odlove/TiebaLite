package com.huanchengfly.tieba.core.ui.theme.runtime.compose

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import com.huanchengfly.tieba.core.ui.theme.ThemeController
import com.huanchengfly.tieba.core.ui.theme.ThemePalette
import com.huanchengfly.tieba.core.ui.theme.ThemeState
import java.util.concurrent.atomic.AtomicInteger

val LocalThemeController = staticCompositionLocalOf<ThemeController> {
    error("ThemeController not provided. Wrap your composition with ProvideThemeController.")
}

val LocalThemeState = staticCompositionLocalOf<ThemeState> {
    error("ThemeState not provided. Wrap your composition with ProvideThemeController.")
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
        LocalThemeState provides state.value
    ) {
        content()
    }
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
