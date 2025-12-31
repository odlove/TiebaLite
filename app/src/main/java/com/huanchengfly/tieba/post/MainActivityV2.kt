package com.huanchengfly.tieba.post

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.emitGlobalEvent
import com.huanchengfly.tieba.core.runtime.client.ClientUtils
import com.huanchengfly.tieba.core.ui.activityresult.ActivityResultPayload
import com.huanchengfly.tieba.core.ui.activityresult.LaunchActivityForResult
import com.huanchengfly.tieba.core.ui.utils.DevicePosture
import com.huanchengfly.tieba.post.arch.BaseComposeActivity
import com.huanchengfly.tieba.post.components.ClipBoardLinkDetector
import com.huanchengfly.tieba.post.navigation.MainActivityContent
import com.huanchengfly.tieba.post.navigation.MainActivityNavigationDelegate
import com.huanchengfly.tieba.core.ui.image.createImageUrlResolver
import com.huanchengfly.tieba.core.ui.utils.createDevicePostureFlow
import com.huanchengfly.tieba.post.services.notification.registerNotificationRuntime
import com.huanchengfly.tieba.post.utils.registerPickMediasLauncher
import com.microsoft.appcenter.analytics.Analytics
import com.stoyanvuchev.systemuibarstweaker.SystemUIBarsTweaker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivityV2 : BaseComposeActivity() {
    @Inject
    lateinit var clipBoardLinkDetector: ClipBoardLinkDetector

    private val notificationCountFlow: MutableSharedFlow<Int> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val navigationDelegate = MainActivityNavigationDelegate(this)

    private val pickMediasLauncher =
        registerPickMediasLauncher {
            globalEventBus.emitGlobalEvent(CommonUiEvent.SelectedImages(it.id, it.uris))
        }

    private val mLaunchActivityForResultLauncher = registerForActivityResult(
        LaunchActivityForResult()
    ) { result: ActivityResultPayload ->
        globalEventBus.emitGlobalEvent(
            CommonUiEvent.ActivityResult(result.requesterId, result.resultCode, result.intent)
        )
    }

    private val devicePostureFlow: StateFlow<DevicePosture> by lazy {
        createDevicePostureFlow(this)
    }

    private val imageUrlResolver = createImageUrlResolver()

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        navigationDelegate.handleNewIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        registerNotificationRuntime(notificationCountFlow)
        window.decorView.setBackgroundColor(0)
        window.setBackgroundDrawable(ColorDrawable(0))
        launch {
            ClientUtils.setActiveTimestamp()
        }
        navigationDelegate.handleLaunchIntent(intent)
    }

    override fun onCreateContent(systemUiController: SystemUIBarsTweaker) {
        super.onCreateContent(systemUiController)
        // fetchAccount() 和 requestNotificationPermission()
        // 已移至 Content() 中通过 LaunchedEffect 监听账号变化后触发
    }

    @Composable
    override fun Content() {
        MainActivityContent(
            clipBoardLinkDetector = clipBoardLinkDetector,
            notificationCountFlow = notificationCountFlow,
            devicePostureFlow = devicePostureFlow,
            imageUrlResolver = imageUrlResolver,
            pickMediasLauncher = pickMediasLauncher,
            launchActivityForResultLauncher = mLaunchActivityForResultLauncher,
            onOpenClipBoardLink = navigationDelegate::openClipBoardLink,
            onNavControllerReady = navigationDelegate::onNavControllerReady,
            onPageChanged = { route ->
                Analytics.trackEvent(
                    "PageChanged",
                    mapOf(
                        "page" to route,
                    )
                )
            },
        )
    }

}
