package com.huanchengfly.tieba.core.ui.compose

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.huanchengfly.tieba.core.ui.navigation.LocalDestination
import com.ramcosta.composedestinations.spec.DestinationSpec
import kotlinx.coroutines.flow.collect
import kotlin.coroutines.cancellation.CancellationException

@SuppressLint("RestrictedApi")
@Composable
fun MyBackHandler(
    enabled: Boolean,
    currentScreen: DestinationSpec<*>? = null,
    onBack: () -> Unit,
) {
    val currentDestination = LocalDestination.current

    val shouldEnable =
        enabled && (currentScreen == null || currentDestination?.baseRoute == currentScreen.baseRoute)

    BackHandler(enabled = shouldEnable, onBack = onBack)
}

@SuppressLint("RestrictedApi")
@Composable
fun MyPredictiveBackHandler(
    enabled: Boolean,
    currentScreen: DestinationSpec<*>? = null,
    onProgressChange: ((Float) -> Unit)? = null,
    onBack: () -> Unit,
    onCancel: (() -> Unit)? = null,
) {
    val currentDestination = LocalDestination.current
    val shouldEnable = enabled && (currentScreen == null || currentDestination?.baseRoute == currentScreen.baseRoute)
    var backProgress by remember { mutableFloatStateOf(0f) }

    PredictiveBackHandler(enabled = shouldEnable) { progress ->
        try {
            progress.collect { backEvent ->
                backProgress = backEvent.progress
                onProgressChange?.invoke(backProgress)
            }
            onBack()
        } catch (e: CancellationException) {
            backProgress = 0f
            onProgressChange?.invoke(0f)
            onCancel?.invoke()
        }
    }
}
