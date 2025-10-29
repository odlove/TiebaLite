package com.huanchengfly.tieba.core.ui.compose

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.DrawerDefaults
import androidx.compose.material.FabPosition
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.contentColorFor
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Stable
class SnackbarState internal constructor(
    val scaffoldState: ScaffoldState,
    private val coroutineScope: CoroutineScope
) {
    val hostState: SnackbarHostState get() = scaffoldState.snackbarHostState

    fun showSnackbar(
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
        dismissCurrent: Boolean = false
    ): Job = coroutineScope.launch {
        if (dismissCurrent) {
            hostState.currentSnackbarData?.dismiss()
        }
        hostState.showSnackbar(message = message, actionLabel = actionLabel, duration = duration)
    }

    suspend fun showSnackbarSuspending(
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
    ): SnackbarResult = hostState.showSnackbar(message = message, actionLabel = actionLabel, duration = duration)

    fun dismissCurrentSnackbar(): Job = coroutineScope.launch {
        hostState.currentSnackbarData?.dismiss()
    }
}

@Composable
fun rememberSnackbarState(
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): SnackbarState = remember(scaffoldState, coroutineScope) {
    SnackbarState(scaffoldState, coroutineScope)
}

val LocalSnackbarState = staticCompositionLocalOf<SnackbarState> {
    error("SnackbarState is not provided. Make sure you are inside SnackbarScaffold.")
}

@Composable
fun SnackbarScaffold(
    snackbarState: SnackbarState = rememberSnackbarState(),
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable (SnackbarHostState) -> Unit = { SwipeToDismissSnackbarHost(it) },
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    isFloatingActionButtonDocked: Boolean = false,
    drawerContent: @Composable (ColumnScope.() -> Unit)? = null,
    drawerGesturesEnabled: Boolean = true,
    drawerShape: Shape = MaterialTheme.shapes.large,
    drawerElevation: Dp = DrawerDefaults.Elevation,
    drawerBackgroundColor: Color = MaterialTheme.colors.surface,
    drawerContentColor: Color = MaterialTheme.colors.contentColorFor(drawerBackgroundColor),
    drawerScrimColor: Color = DrawerDefaults.scrimColor,
    backgroundColor: Color = MaterialTheme.colors.background,
    contentColor: Color = if (backgroundColor.alpha == 0f) MaterialTheme.colors.onSurface else MaterialTheme.colors.contentColorFor(backgroundColor),
    content: @Composable (PaddingValues) -> Unit
) {
    CompositionLocalProvider(LocalSnackbarState provides snackbarState) {
        MyScaffold(
            modifier = modifier,
            scaffoldState = snackbarState.scaffoldState,
            topBar = topBar,
            bottomBar = bottomBar,
            snackbarHost = snackbarHost,
            floatingActionButton = floatingActionButton,
            floatingActionButtonPosition = floatingActionButtonPosition,
            isFloatingActionButtonDocked = isFloatingActionButtonDocked,
            drawerContent = drawerContent,
            drawerGesturesEnabled = drawerGesturesEnabled,
            drawerShape = drawerShape,
            drawerElevation = drawerElevation,
            drawerBackgroundColor = drawerBackgroundColor,
            drawerContentColor = drawerContentColor,
            drawerScrimColor = drawerScrimColor,
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            content = content
        )
    }
}
