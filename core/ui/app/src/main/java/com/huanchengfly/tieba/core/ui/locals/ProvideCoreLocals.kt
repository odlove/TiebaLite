package com.huanchengfly.tieba.core.ui.locals

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.huanchengfly.tieba.core.ui.image.AppImageLoadStrategy
import com.huanchengfly.tieba.core.ui.image.ImageUrlResolver
import com.huanchengfly.tieba.core.ui.image.LocalImageLoadStrategy
import com.huanchengfly.tieba.core.ui.image.LocalImageUrlResolver
import com.huanchengfly.tieba.core.ui.preferences.LocalPreferencesDataStore
import com.huanchengfly.tieba.core.theme.compose.THEME_DIAGNOSTICS_TAG
import com.huanchengfly.tieba.core.ui.device.DevicePosture
import com.huanchengfly.tieba.post.dataStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.atomic.AtomicInteger

@Composable
fun ProvideCoreLocals(
    notificationCountFlow: MutableSharedFlow<Int>,
    devicePostureFlow: StateFlow<DevicePosture>,
    imageUrlResolver: ImageUrlResolver,
    content: @Composable () -> Unit,
) {
    val recomposeCounter = remember { AtomicInteger(0) }
    SideEffect {
        val count = recomposeCounter.incrementAndGet()
        Log.i(
            THEME_DIAGNOSTICS_TAG,
            "ProvideCoreLocals recomposed count=$count"
        )
    }
    val devicePostureState = devicePostureFlow.collectAsState()

    LaunchedEffect(devicePostureState.value) {
        Log.i(
            THEME_DIAGNOSTICS_TAG,
            "ProvideCoreLocals devicePosture=${devicePostureState.value}"
        )
    }

    val context = LocalContext.current
    CompositionLocalProvider(
        LocalNotificationCountFlow provides notificationCountFlow,
        LocalDevicePosture provides devicePostureState,
        LocalImageLoadStrategy provides AppImageLoadStrategy,
        LocalImageUrlResolver provides imageUrlResolver,
        LocalPreferencesDataStore provides context.dataStore,
    ) {
        SideEffect {
            Log.i(
                THEME_DIAGNOSTICS_TAG,
                "ProvideCoreLocals locals provided notificationReplay=${notificationCountFlow.replayCache}"
            )
        }
        DisposableEffect(Unit) {
            Log.i(
                THEME_DIAGNOSTICS_TAG,
                "ProvideCoreLocals entered"
            )
            onDispose {
                Log.i(
                    THEME_DIAGNOSTICS_TAG,
                    "ProvideCoreLocals disposed"
                )
            }
        }
        content()
    }
}
