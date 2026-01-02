package com.huanchengfly.tieba.post.ui.page.main

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.LocalGlobalEventBus
import com.huanchengfly.tieba.core.mvi.emitGlobalEvent
import com.huanchengfly.tieba.core.ui.hiltViewModel
import com.huanchengfly.tieba.core.ui.locals.LocalDevicePosture
import com.huanchengfly.tieba.core.ui.locals.LocalNotificationCountFlow
import com.huanchengfly.tieba.core.ui.navigation.ProvideNavigator
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.THEME_DIAGNOSTICS_TAG
import com.huanchengfly.tieba.core.ui.windowsizeclass.LocalWindowSizeClass
import com.huanchengfly.tieba.post.ui.page.main.navigation.items.rememberMainNavigationItems
import com.huanchengfly.tieba.post.ui.page.main.navigation.scaffold.MainPageScaffold
import com.huanchengfly.tieba.post.ui.page.main.navigation.type.rememberMainNavigationContentPosition
import com.huanchengfly.tieba.post.ui.page.main.navigation.type.rememberMainNavigationType
import com.huanchengfly.tieba.post.ui.page.main.viewmodel.MainViewModel
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainPageContent(
    navigator: DestinationsNavigator,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val globalEventBus = LocalGlobalEventBus.current

    // 设备尺寸/姿态，用于决定导航形态与位置
    val windowSizeClass = LocalWindowSizeClass.current
    val windowHeightSizeClass by rememberUpdatedState(newValue = windowSizeClass.heightSizeClass)
    val windowWidthSizeClass by rememberUpdatedState(newValue = windowSizeClass.widthSizeClass)
    val foldingDevicePosture by LocalDevicePosture.current

    // 订阅主页面状态
    val uiState by viewModel.uiState.collectAsState()
    val messageCount = uiState.messageCount

    LaunchedEffect(messageCount) {
        Log.i(
            THEME_DIAGNOSTICS_TAG,
            "MainPage messageCount updated value=$messageCount"
        )
    }

    val notificationCountFlow = LocalNotificationCountFlow.current

    // 收到系统通知数量后同步到主页面
    LaunchedEffect(null) {
        notificationCountFlow.collect {
            Log.i(
                THEME_DIAGNOSTICS_TAG,
                "MainPage notificationCountFlow received count=$it"
            )
            viewModel.onMessageReceived(it)
        }
    }


    val pagerState = rememberPagerState(initialPage = 1) { 4 }

    val coroutineScope = rememberCoroutineScope()
    val themeColors = ExtendedTheme.colors

    // Tab 行为回调
    val onOpenExplore = remember(coroutineScope, pagerState) {
        {
            coroutineScope.launch {
                pagerState.scrollToPage(1)
            }
            Unit
        }
    }
    val onNotificationsClick = remember(viewModel) {
        {
            viewModel.clearMessageCount()
        }
    }

    val navigationItems = rememberMainNavigationItems(
        messageCount = messageCount,
        canOpenExplore = true,
        onOpenExplore = onOpenExplore,
        onNotificationsClick = onNotificationsClick
    )

    // 计算导航形态与布局
    val navigationType = rememberMainNavigationType(
        windowWidthSizeClass = windowWidthSizeClass,
        foldingDevicePosture = foldingDevicePosture
    )
    val navigationContentPosition = rememberMainNavigationContentPosition(
        windowHeightSizeClass = windowHeightSizeClass
    )

    val onReselected: (Int) -> Unit = {
        coroutineScope.emitGlobalEvent(
            globalEventBus,
            CommonUiEvent.Refresh(navigationItems[it].id)
        )
    }
    val onChangePosition: (Int) -> Unit = remember(coroutineScope, pagerState) {
        { index ->
            coroutineScope.launch {
                pagerState.scrollToPage(index)
            }
            Unit
        }
    }

    // 渲染主页面导航与内容
    ProvideNavigator(navigator = navigator) {
        MainPageScaffold(
            navigationType = navigationType,
            navigationContentPosition = navigationContentPosition,
            navigationItems = navigationItems,
            pagerState = pagerState,
            onChangePosition = onChangePosition,
            onReselected = onReselected,
            backgroundColor = themeColors.background
        )
    }
}
