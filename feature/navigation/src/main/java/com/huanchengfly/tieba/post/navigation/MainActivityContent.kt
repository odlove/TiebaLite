package com.huanchengfly.tieba.post.navigation

import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.navigation.ModalBottomSheetLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.plusAssign
import com.huanchengfly.tieba.post.runtime.preview.ClipBoardLink
import com.huanchengfly.tieba.core.ui.account.HandleAccountRefreshAndNotificationPermission
import com.huanchengfly.tieba.core.ui.activityresult.LaunchActivityRequest
import com.huanchengfly.tieba.core.ui.compose.HandleCommonUiActivityEvents
import com.huanchengfly.tieba.core.ui.image.ImageUrlResolver
import com.huanchengfly.tieba.core.ui.navigation.TiebaNavHostDefaults
import com.huanchengfly.tieba.core.ui.utils.DevicePosture
import com.huanchengfly.tieba.core.ui.locals.LocalNavController
import com.huanchengfly.tieba.core.ui.navigation.LocalDestination
import com.huanchengfly.tieba.core.ui.preferences.ProvideAppPreferences
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.THEME_DIAGNOSTICS_TAG
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.TranslucentThemeBackground
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.menuBackground
import com.huanchengfly.tieba.core.ui.widgets.compose.ProvideAccountActions
import com.huanchengfly.tieba.post.components.ClipBoardDetectDialog
import com.huanchengfly.tieba.post.components.ClipBoardLinkDetector
import com.huanchengfly.tieba.post.preferences.appPreferences
import com.huanchengfly.tieba.post.ui.page.login.destinations.LoginPageDestination
import com.huanchengfly.tieba.post.sign.SignBatteryOptimizationPrompt
import com.huanchengfly.tieba.post.utils.PickMediasRequest
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainActivityContent(
    clipBoardLinkDetector: ClipBoardLinkDetector,
    notificationCountFlow: MutableSharedFlow<Int>,
    devicePostureFlow: StateFlow<DevicePosture>,
    imageUrlResolver: ImageUrlResolver,
    pickMediasLauncher: ActivityResultLauncher<PickMediasRequest>,
    launchActivityForResultLauncher: ActivityResultLauncher<LaunchActivityRequest>,
    onOpenClipBoardLink: (ClipBoardLink) -> Unit,
    onNavControllerReady: (NavHostController) -> Unit,
    onPageChanged: (String) -> Unit = {},
) {
    ClipBoardDetectDialog(clipBoardLinkDetector, onOpenClipBoardLink)

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

    HandleAccountRefreshAndNotificationPermission()
    SignBatteryOptimizationPrompt()
    HandleCommonUiActivityEvents(
        pickMediasLauncher = pickMediasLauncher,
        launchActivityForResultLauncher = launchActivityForResultLauncher,
    )

    val context = LocalContext.current
    ProvideAppPreferences(appPreferences = context.appPreferences) {
        TiebaLiteLocalProvider(
            notificationCountFlow = notificationCountFlow,
            devicePostureFlow = devicePostureFlow,
            imageUrlResolver = imageUrlResolver,
        ) {
            TranslucentThemeBackground {
                val navController = rememberNavController()
                val engine = TiebaNavHostDefaults.rememberNavHostEngine()
                val navigator = TiebaNavHostDefaults.rememberBottomSheetNavigator()
                val currentDestination by navController.currentDestinationAsState()

                navController.navigatorProvider += navigator

                LaunchedEffect(currentDestination) {
                    val curDest = currentDestination
                    if (curDest != null) {
                        onPageChanged(curDest.route)
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
                        sheetBackgroundColor = ExtendedTheme.colors.menuBackground,
                        sheetContentColor = ExtendedTheme.colors.text,
                        scrimColor = ExtendedTheme.colors.indicator.copy(alpha = 0.32f),
                    ) {
                        SideEffect {
                            Log.i(
                                THEME_DIAGNOSTICS_TAG,
                                "MainActivityV2.ModalBottomSheetLayout recomposed"
                            )
                        }
                        ProvideAccountActions(
                            onAddAccount = {
                                navController.navigate(LoginPageDestination.route)
                            }
                        ) {
                            ProvideHomeNavigationActions(navController = navController) {
                                DestinationsNavHost(
                                    navController = navController,
                                    navGraph = AppNavGraphs.root,
                                    engine = engine,
                                )
                            }
                        }
                    }
                }

                SideEffect {
                    Log.i(
                        THEME_DIAGNOSTICS_TAG,
                        "MainActivityV2 rememberNavController SideEffect current=${navController.currentDestination?.route}"
                    )
                    onNavControllerReady(navController)
                }
            }
        }
    }
}
