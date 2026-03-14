package com.huanchengfly.tieba.core.theme2.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.huanchengfly.tieba.core.theme2.semantic.ThemeSemanticCatalog
import com.huanchengfly.tieba.core.theme2.model.ThemeSnapshot
import com.huanchengfly.tieba.core.theme2.runtime.entrypoints.Theme2RuntimeEntryPoint
import dagger.hilt.android.EntryPointAccessors

@Stable
data class Theme2Colors(
    val surfacePrimary: Color = Color.Unspecified,
    val surfaceNav: Color = Color.Unspecified,
    val stateActive: Color = Color.Unspecified,
    val stateUnselected: Color = Color.Unspecified,
    val contentOnBrand: Color = Color.Unspecified,
    val outlineLow: Color = Color.Unspecified
)

val LocalTheme2Snapshot = staticCompositionLocalOf {
    ThemeSnapshot(ThemeSemanticCatalog.light())
}

val LocalTheme2Colors = staticCompositionLocalOf {
    val semantic = ThemeSemanticCatalog.light()
    Theme2Colors(
        surfacePrimary = Color(semantic.surfacePrimary),
        surfaceNav = Color(semantic.surfaceNav),
        stateActive = Color(semantic.stateActive),
        stateUnselected = Color(semantic.stateUnselected),
        contentOnBrand = Color(semantic.contentOnBrand),
        outlineLow = Color(semantic.outlineLow)
    )
}

private fun ThemeSnapshot.toTheme2Colors(): Theme2Colors =
    Theme2Colors(
        surfacePrimary = Color(semantic.surfacePrimary),
        surfaceNav = Color(semantic.surfaceNav),
        stateActive = Color(semantic.stateActive),
        stateUnselected = Color(semantic.stateUnselected),
        contentOnBrand = Color(semantic.contentOnBrand),
        outlineLow = Color(semantic.outlineLow)
    )

@Composable
fun ProvideTheme2Runtime(content: @Composable () -> Unit) {
    val appContext = LocalContext.current.applicationContext
    val application = appContext as? android.app.Application
        ?: error("ProvideTheme2Runtime requires an Application context")
    val runtime = remember(application) {
        EntryPointAccessors.fromApplication(
            application,
            Theme2RuntimeEntryPoint::class.java
        ).themeRuntime()
    }
    val snapshotState = runtime.snapshotFlow.collectAsState()
    val colors = remember(snapshotState.value) { snapshotState.value.toTheme2Colors() }

    CompositionLocalProvider(
        LocalTheme2Snapshot provides snapshotState.value,
        LocalTheme2Colors provides colors,
        content = content
    )
}

object Theme2Theme {
    val colors: Theme2Colors
        @Composable
        get() = LocalTheme2Colors.current

    val snapshot: ThemeSnapshot
        @Composable
        get() = LocalTheme2Snapshot.current
}
