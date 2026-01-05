package com.huanchengfly.tieba.core.ui.compose.widgets

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.github.panpf.sketch.compose.AsyncImage
import com.github.panpf.sketch.fetch.newFileUri
import com.huanchengfly.tieba.core.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.core.theme.compose.LocalThemeState
import com.huanchengfly.tieba.core.theme.compose.THEME_DIAGNOSTICS_TAG

@Composable
fun TranslucentThemeBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val extendedColors = ExtendedTheme.colors
    val themeState = LocalThemeState.current
    val isTranslucent = extendedColors.isTranslucent

    SideEffect {
        Log.i(
            THEME_DIAGNOSTICS_TAG,
            "TranslucentThemeBackground recomposed theme=${extendedColors.theme} " +
                "translucent=$isTranslucent background=${extendedColors.background} " +
                "window=${extendedColors.windowBackground}"
        )
    }

    DisposableEffect(Unit) {
        Log.i(
            THEME_DIAGNOSTICS_TAG,
            "TranslucentThemeBackground entered"
        )
        onDispose {
            Log.i(
                THEME_DIAGNOSTICS_TAG,
                "TranslucentThemeBackground disposed"
            )
        }
    }

    Surface(
        color = extendedColors.background,
        modifier = modifier
    ) {
        if (isTranslucent) {
            val backgroundPath = themeState.translucentConfig?.backgroundPath
            val backgroundUri = remember(backgroundPath) {
                backgroundPath?.takeIf { it.isNotBlank() }?.let { newFileUri(it) }
            }

            SideEffect {
                Log.i(
                    THEME_DIAGNOSTICS_TAG,
                    "TranslucentThemeBackground image layer path=$backgroundPath uri=$backgroundUri"
                )
            }

            LaunchedEffect(backgroundPath) {
                Log.i(
                    THEME_DIAGNOSTICS_TAG,
                    "TranslucentThemeBackground backgroundPath changed -> $backgroundPath"
                )
            }
            backgroundUri?.let { uri ->
                AsyncImage(
                    imageUri = uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        content()
    }
}
