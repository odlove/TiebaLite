package com.huanchengfly.tieba.post.ui.widgets.compose

import android.annotation.SuppressLint
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.huanchengfly.tieba.post.LocalDestination
import com.ramcosta.composedestinations.spec.DestinationSpec
import kotlinx.coroutines.flow.collect
import kotlin.coroutines.cancellation.CancellationException

/**
 * Predictive back handler with destination filtering support.
 *
 * This component wraps Android's PredictiveBackHandler to provide:
 * - Destination-based filtering (only handles back events on specific screens)
 * - Progress callbacks during the back gesture
 * - Proper cancellation handling
 *
 * @param enabled Whether this handler should intercept back events
 * @param currentScreen Optional destination filter - only handles back events on this screen
 * @param onProgressChange Optional callback invoked during the back gesture with progress (0f to 1f)
 * @param onBack Callback invoked when the back gesture is completed
 * @param onCancel Optional callback invoked when the back gesture is cancelled
 */
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
            // Gesture completed - execute the back action
            onBack()
        } catch (e: CancellationException) {
            // Gesture cancelled - reset progress
            backProgress = 0f
            onProgressChange?.invoke(0f)
            onCancel?.invoke()
        }
    }
}
