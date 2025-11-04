package com.huanchengfly.tieba.post

import android.annotation.SuppressLint
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Surface
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.plusAssign
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.github.panpf.sketch.compose.AsyncImage
import com.github.panpf.sketch.fetch.newFileUri
import androidx.compose.material.navigation.BottomSheetNavigator
import androidx.compose.material.navigation.ModalBottomSheetLayout
import androidx.compose.material.navigation.bottomSheet
import androidx.compose.material.navigation.rememberBottomSheetNavigator
import androidx.core.app.NotificationManagerCompat
import com.stoyanvuchev.systemuibarstweaker.SystemUIBarsTweaker
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.arch.BaseComposeActivity
import com.huanchengfly.tieba.core.mvi.GlobalEvent
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.emitGlobalEvent
import com.huanchengfly.tieba.core.mvi.onGlobalEvent
import com.huanchengfly.tieba.post.components.ClipBoardForumLink
import com.huanchengfly.tieba.post.components.ClipBoardLink
import com.huanchengfly.tieba.post.components.ClipBoardLinkDetector
import com.huanchengfly.tieba.post.components.ClipBoardThreadLink
import com.huanchengfly.tieba.post.services.NotifyJobService
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.THEME_DIAGNOSTICS_TAG
import com.huanchengfly.tieba.post.ui.page.NavGraphs
import com.huanchengfly.tieba.post.ui.page.destinations.ForumPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.ThreadPageDestination
import com.huanchengfly.tieba.post.ui.utils.DevicePosture
import com.huanchengfly.tieba.post.ui.utils.isBookPosture
import com.huanchengfly.tieba.post.ui.utils.isSeparating
import com.huanchengfly.tieba.post.ui.widgets.compose.AlertDialog
import com.huanchengfly.tieba.post.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.post.ui.widgets.compose.AvatarIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.Dialog
import com.huanchengfly.tieba.post.ui.widgets.compose.DialogNegativeButton
import com.huanchengfly.tieba.post.ui.widgets.compose.DialogPositiveButton
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberDialogState
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.core.runtime.client.ClientUtils
import com.huanchengfly.tieba.post.utils.JobServiceUtil
import com.huanchengfly.tieba.post.utils.PermissionUtils
import com.huanchengfly.tieba.post.utils.PickMediasRequest
import com.huanchengfly.tieba.post.utils.QuickPreviewUtil
import com.huanchengfly.tieba.post.utils.TiebaUtil
import com.huanchengfly.tieba.post.collectPreferenceAsState
import com.huanchengfly.tieba.post.dataStore
import com.huanchengfly.tieba.core.ui.activityresult.ActivityResultPayload
import com.huanchengfly.tieba.core.ui.activityresult.LaunchActivityForResult
import com.huanchengfly.tieba.core.ui.activityresult.LaunchActivityRequest
import com.huanchengfly.tieba.post.utils.isIgnoringBatteryOptimizations
import com.huanchengfly.tieba.post.utils.newIntentFilter
import com.huanchengfly.tieba.post.utils.registerPickMediasLauncher
import com.huanchengfly.tieba.post.utils.requestIgnoreBatteryOptimizations
import com.huanchengfly.tieba.post.utils.requestPermission
import com.microsoft.appcenter.analytics.Analytics
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import java.util.concurrent.atomic.AtomicInteger
import com.ramcosta.composedestinations.utils.currentDestinationFlow
import com.huanchengfly.tieba.core.ui.navigation.LocalDestination
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.concurrent.atomic.AtomicBoolean

val LocalNotificationCountFlow =
    staticCompositionLocalOf<Flow<Int>> { throw IllegalStateException("not allowed here!") }
val LocalDevicePosture =
    staticCompositionLocalOf<State<DevicePosture>> { throw IllegalStateException("not allowed here!") }
val LocalNavController =
    staticCompositionLocalOf<NavHostController> { throw IllegalStateException("not allowed here!") }

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun rememberBottomSheetNavigator(
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    skipHalfExpanded: Boolean = false
): BottomSheetNavigator {
    val sheetState = rememberModalBottomSheetState(
        ModalBottomSheetValue.Hidden,
        animationSpec = animationSpec,
        skipHalfExpanded = skipHalfExpanded
    )
    return remember(sheetState) { BottomSheetNavigator(sheetState) }
}

@AndroidEntryPoint
class MainActivityV2 : BaseComposeActivity() {
    @Inject
    lateinit var clipBoardLinkDetector: ClipBoardLinkDetector
    private val handler = Handler(Looper.getMainLooper())
    private val newMessageReceiver: BroadcastReceiver = NewMessageReceiver()

    private val notificationCountFlow: MutableSharedFlow<Int> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

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
        WindowInfoTracker.getOrCreate(this)
            .windowLayoutInfo(this)
            .flowWithLifecycle(lifecycle)
            .map { layoutInfo ->
                val foldingFeature =
                    layoutInfo.displayFeatures
                        .filterIsInstance<FoldingFeature>()
                        .firstOrNull()
                when {
                    isBookPosture(foldingFeature) ->
                        DevicePosture.BookPosture(foldingFeature.bounds)

                    isSeparating(foldingFeature) ->
                        DevicePosture.Separating(foldingFeature.bounds, foldingFeature.orientation)

                    else -> DevicePosture.NormalPosture
                }
            }
            .onEach {
                Log.i(
                    THEME_DIAGNOSTICS_TAG,
                    "devicePostureFlow emit posture=${it::class.java.simpleName} value=$it"
                )
            }
            .stateIn(
                scope = lifecycleScope,
                started = SharingStarted.Eagerly,
                initialValue = DevicePosture.NormalPosture
            )
    }

    private var direction: Direction? = null
    private var waitingNavCollectorToNavigate = AtomicBoolean(false)
    private var myNavController: NavHostController? = null
        set(value) {
            field = value
            if (value != null && waitingNavCollectorToNavigate.get() && direction != null) {
                launch {
                    value.currentDestinationFlow
                        .take(1)
                        .collect {
                            val dir = direction
                            if (waitingNavCollectorToNavigate.get() && dir != null) {
                                value.navigate(dir)
                                waitingNavCollectorToNavigate.set(false)
                                direction = null
                            }
                        }
                }
            }
        }

    private fun navigate(direction: Direction) {
        if (myNavController == null) {
            waitingNavCollectorToNavigate.set(true)
            this.direction = direction
        } else {
            myNavController?.navigate(direction)
        }
    }

    private fun checkIntent(intent: Intent): Boolean {
        return if (intent.data?.scheme == "com.baidu.tieba" && intent.data?.host == "unidispatch") {
            val uri = intent.data ?: return false
            when (uri.path.orEmpty().lowercase()) {
                "/frs" -> {
                    val forumName = uri.getQueryParameter("kw") ?: return true
                    navigate(ForumPageDestination(forumName))
                }

                "/pb" -> {
                    val threadId = uri.getQueryParameter("tid")?.toLongOrNull() ?: return true
                    navigate(ThreadPageDestination(threadId))
                }
            }
            true
        } else return if (intent.data?.host == "tieba.baidu.com") {
            val uri = intent.data ?: return false
            when {
                uri.path.orEmpty().lowercase() == "/f" -> {
                    val forumName = uri.getQueryParameter("kw") ?: return true
                    navigate(ForumPageDestination(forumName))
                }

                uri.path.orEmpty().lowercase().startsWith("/p/") -> {
                    val threadId = uri.pathSegments.getOrNull(1)?.toLongOrNull() ?: return true
                    navigate(ThreadPageDestination(threadId))
                }
            }
            true
        } else {
            false
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (!checkIntent(intent)) {
            myNavController?.handleDeepLink(intent)
        }
    }

    private fun shouldRequestNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return false
        }
        if (!AccountUtil.isLoggedIn()) {
            return false
        }
        if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            return false
        }
        if (appPreferences.notificationPermissionRequested) {
            return false
        }
        return true
    }

    private fun requestNotificationPermission() {
        if (!shouldRequestNotificationPermission()) {
            return
        }
        appPreferences.notificationPermissionRequested = true
        requestPermission {
            permissions = listOf(PermissionUtils.POST_NOTIFICATIONS)
            description = getString(R.string.desc_permission_post_notifications)
            onDenied = {
                toastShort(R.string.tip_no_permission)
            }
        }
    }

    private fun initAutoSign() {
        runCatching {
            TiebaUtil.initAutoSign(this)
        }
    }

    override fun onStart() {
        super.onStart()
        runCatching {
            ContextCompat.registerReceiver(
                this,
                newMessageReceiver,
                newIntentFilter(NotifyJobService.ACTION_NEW_MESSAGE),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            startService(Intent(this, NotifyJobService::class.java))
            val builder = JobInfo.Builder(
                JobServiceUtil.getJobId(this),
                ComponentName(this, NotifyJobService::class.java)
            )
                .setPersisted(true)
                .setPeriodic(30 * 60 * 1000L)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler.schedule(builder.build())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        window.decorView.setBackgroundColor(0)
        window.setBackgroundDrawable(ColorDrawable(0))
        launch {
            ClientUtils.setActiveTimestamp()
        }
        intent?.let { checkIntent(it) }
    }

    override fun onCreateContent(systemUiController: SystemUIBarsTweaker) {
        super.onCreateContent(systemUiController)
        // fetchAccount() 和 requestNotificationPermission()
        // 已移至 Content() 中通过 LaunchedEffect 监听账号变化后触发
        initAutoSign()
    }

    private fun openClipBoardLink(link: ClipBoardLink) {
        when (link) {
            is ClipBoardThreadLink -> {
                myNavController?.navigate(Uri.parse("tblite://thread/${link.threadId}"))
            }

            is ClipBoardForumLink -> {
                myNavController?.navigate(Uri.parse("tblite://forum/${link.forumName}"))
            }

            else -> {
//                launchUrl(this, link.url)
            }
        }
    }

    @Composable
    private fun ClipBoardDetectDialog() {
        val previewInfo by clipBoardLinkDetector.previewInfoStateFlow.collectAsState()

        val dialogState = rememberDialogState()

        LaunchedEffect(previewInfo) {
            if (previewInfo != null && !dialogState.show) {
                dialogState.show()
            }
        }

        Dialog(
            onDismiss = { clipBoardLinkDetector.clearPreview() },
            dialogState = dialogState,
            title = {
                Text(text = stringResource(id = R.string.title_dialog_clip_board_tieba_url))
            },
            buttons = {
                DialogPositiveButton(text = stringResource(id = R.string.button_open)) {
                    previewInfo?.let {
                        openClipBoardLink(it.clipBoardLink)
                    }
                }
                DialogNegativeButton(text = stringResource(id = R.string.btn_close))
            },
        ) {
            previewInfo?.let {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    border = BorderStroke(1.dp, ExtendedTheme.colors.divider),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        it.icon?.let { icon ->
                            if (icon.type == QuickPreviewUtil.Icon.TYPE_DRAWABLE_RES) {
                                AvatarIcon(
                                    resId = icon.res,
                                    size = Sizes.Medium,
                                    contentDescription = null
                                )
                            } else {
                                Avatar(
                                    data = icon.url,
                                    size = Sizes.Medium,
                                    contentDescription = null
                                )
                            }
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            it.title?.let { title ->
                                Text(text = title, style = MaterialTheme.typography.subtitle1)
                            }
                            it.subtitle?.let { subtitle ->
                                Text(text = subtitle, style = MaterialTheme.typography.body2)
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val okSignAlertDialogState = rememberDialogState()
        ClipBoardDetectDialog()

        val contentRecomposeCounter = remember { AtomicInteger(0) }
        val currentThemeKey = ExtendedTheme.colors.theme
        SideEffect {
            val count = contentRecomposeCounter.incrementAndGet()
            Log.i(
                THEME_DIAGNOSTICS_TAG,
                "MainActivityV2.Content recomposed count=$count theme=$currentThemeKey"
            )
        }
        DisposableEffect(Unit) {
            Log.i(
                THEME_DIAGNOSTICS_TAG,
                "MainActivityV2.Content entered"
            )
            onDispose {
                Log.i(
                    THEME_DIAGNOSTICS_TAG,
                    "MainActivityV2.Content disposed"
                )
            }
        }

        // 监听账号初始化完成后刷新账号信息
        val currentAccount = AccountUtil.LocalAccount.current
        LaunchedEffect(currentAccount) {
            if (currentAccount != null) {
                // 账号已登录，刷新账号信息
                AccountUtil.fetchAccountFlow()
                    .flowOn(Dispatchers.IO)
                    .catch { e ->
                        toastShort(e.getErrorMessage())
                        e.printStackTrace()
                    }
                    .collect()

                // 账号初始化完成后，请求通知权限
                requestNotificationPermission()
            }
        }

        AlertDialog(
            dialogState = okSignAlertDialogState,
            title = { Text(text = stringResource(id = R.string.title_dialog_oksign_battery_optimization)) },
            content = { Text(text = stringResource(id = R.string.message_dialog_oksign_battery_optimization)) },
            buttons = {
                DialogPositiveButton(
                    text = stringResource(id = R.string.button_go_to_ignore_battery_optimization),
                    onClick = {
                        requestIgnoreBatteryOptimizations()
                    }
                )
                DialogNegativeButton(
                    text = stringResource(id = R.string.button_cancel)
                )
                DialogNegativeButton(
                    text = stringResource(id = R.string.button_dont_remind_again),
                    onClick = {
                        appPreferences.ignoreBatteryOptimizationsDialog = true
                    }
                )
            },
        )
        LaunchedEffect(Unit) {
            if (appPreferences.autoSign && !isIgnoringBatteryOptimizations() && !appPreferences.ignoreBatteryOptimizationsDialog) {
                okSignAlertDialogState.show()
            }
        }
        onGlobalEvent<CommonUiEvent.StartSelectImages> {
            val mediaType = it.mediaType
            if (mediaType is PickMediasRequest.MediaType) {
                pickMediasLauncher.launch(
                    PickMediasRequest(it.id, it.maxCount, mediaType)
                )
            } else {
                Log.w(
                    "MainActivityV2",
                    "StartSelectImages ignored: unsupported mediaType ${mediaType::class.java.simpleName}"
                )
            }
        }
        onGlobalEvent<CommonUiEvent.StartActivityForResult> {
            mLaunchActivityForResultLauncher.launch(
                LaunchActivityRequest(
                    it.requesterId,
                    it.intent
                )
            )
        }
        TiebaLiteLocalProvider {
            TranslucentThemeBackground {
                val navController = rememberNavController()
                val engine = TiebaNavHostDefaults.rememberNavHostEngine()
                val navigator = TiebaNavHostDefaults.rememberBottomSheetNavigator()
                val currentDestination by navController.currentDestinationAsState()

                navController.navigatorProvider += navigator

                LaunchedEffect(currentDestination) {
                    val curDest = currentDestination
                    if (curDest != null) {
                        Analytics.trackEvent(
                            "PageChanged",
                            mapOf(
                                "page" to curDest.route,
                            )
                        )
                    }
                }

                CompositionLocalProvider(
                    LocalNavController provides navController,
                    LocalDestination provides currentDestination,
                ) {
                    SideEffect {
                        Log.i(
                            THEME_DIAGNOSTICS_TAG,
                            "MainActivityV2.NavHost composition currentDestination=${currentDestination?.route}"
                        )
                    }

                    ModalBottomSheetLayout(
                        bottomSheetNavigator = navigator,
                        sheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                        sheetBackgroundColor = ExtendedTheme.colors.windowBackground,
                        scrimColor = Color.Black.copy(alpha = 0.32f),
                    ) {
                        SideEffect {
                            Log.i(
                                THEME_DIAGNOSTICS_TAG,
                                "MainActivityV2.ModalBottomSheetLayout recomposed"
                            )
                        }
                        DestinationsNavHost(
                            navController = navController,
                            navGraph = NavGraphs.root,
                            engine = engine,
                        )
                    }
                }

                SideEffect {
                    Log.i(
                        THEME_DIAGNOSTICS_TAG,
                        "MainActivityV2 rememberNavController SideEffect current=${navController.currentDestination?.route}"
                    )
                    myNavController = navController
                }
            }
        }
    }

    @Composable
    private fun TranslucentThemeBackground(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit,
    ) {
        val extendedColors = ExtendedTheme.colors
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
                val context = LocalContext.current
                val backgroundPath by context.dataStore.collectPreferenceAsState(
                    key = stringPreferencesKey("translucent_theme_background_path"),
                    defaultValue = ""
                )
                val backgroundUri = remember(backgroundPath) { newFileUri(backgroundPath) }

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
                AsyncImage(
                    imageUri = backgroundUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            content()
        }
    }

    @Composable
    fun TiebaLiteLocalProvider(content: @Composable () -> Unit) {
        val recomposeCounter = remember { AtomicInteger(0) }
        SideEffect {
            val count = recomposeCounter.incrementAndGet()
            Log.i(
                THEME_DIAGNOSTICS_TAG,
                "TiebaLiteLocalProvider recomposed count=$count"
            )
        }
        val devicePostureState = devicePostureFlow.collectAsState()

        LaunchedEffect(devicePostureState.value) {
            Log.i(
                THEME_DIAGNOSTICS_TAG,
                "TiebaLiteLocalProvider devicePosture=${devicePostureState.value}"
            )
        }

        CompositionLocalProvider(
            LocalNotificationCountFlow provides notificationCountFlow,
            LocalDevicePosture provides devicePostureState,
        ) {
            SideEffect {
                Log.i(
                    THEME_DIAGNOSTICS_TAG,
                    "TiebaLiteLocalProvider locals provided notificationReplay=${notificationCountFlow.replayCache}"
                )
            }
            DisposableEffect(Unit) {
                Log.i(
                    THEME_DIAGNOSTICS_TAG,
                    "TiebaLiteLocalProvider entered"
                )
                onDispose {
                    Log.i(
                        THEME_DIAGNOSTICS_TAG,
                        "TiebaLiteLocalProvider disposed"
                    )
                }
            }
            content()
        }
    }

    private inner class NewMessageReceiver : BroadcastReceiver() {
        @SuppressLint("RestrictedApi")
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == NotifyJobService.ACTION_NEW_MESSAGE) {
                val channel = intent.getStringExtra("channel")
                val count = intent.getIntExtra("count", 0)
                if (channel != null && channel == NotifyJobService.CHANNEL_TOTAL) {
                    lifecycleScope.launch {
                        Log.i(
                            THEME_DIAGNOSTICS_TAG,
                            "notificationCountFlow emit channel=$channel count=$count"
                        )
                        notificationCountFlow.emit(count)
                    }
                }
            }
        }
    }
}

private object TiebaNavHostDefaults {
    private val AnimationSpec = spring(
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntOffset.VisibilityThreshold
    )

    @Composable
    @OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
    fun rememberNavHostEngine() = rememberAnimatedNavHostEngine(
        navHostContentAlignment = Alignment.TopStart,
        rootDefaultAnimations = RootNavGraphDefaultAnimations(
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = AnimationSpec,
                    initialOffset = { it }
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = AnimationSpec,
                    targetOffset = { -it }
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = AnimationSpec,
                    initialOffset = { -it }
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = AnimationSpec,
                    targetOffset = { it }
                )
            },
        ),
    )

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun rememberBottomSheetNavigator(): BottomSheetNavigator = rememberBottomSheetNavigator(
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        skipHalfExpanded = true
    )
}
