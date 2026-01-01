package com.huanchengfly.tieba.post.ui.page.main

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.LocalGlobalEventBus
import com.huanchengfly.tieba.core.mvi.collectPartialAsState
import com.huanchengfly.tieba.core.mvi.emitGlobalEvent
import com.huanchengfly.tieba.core.ui.locals.LocalDevicePosture
import com.huanchengfly.tieba.core.ui.locals.LocalNotificationCountFlow
import com.huanchengfly.tieba.core.ui.navigation.ProvideNavigator
import com.huanchengfly.tieba.core.ui.pageViewModel
import com.huanchengfly.tieba.core.ui.preferences.LocalAppPreferences
import com.huanchengfly.tieba.core.ui.preferences.rememberPreferenceAsState
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.THEME_DIAGNOSTICS_TAG
import com.huanchengfly.tieba.core.ui.windowsizeclass.LocalWindowSizeClass
import com.huanchengfly.tieba.post.ui.page.main.contract.MainUiIntent
import com.huanchengfly.tieba.post.ui.page.main.contract.MainUiState
import com.huanchengfly.tieba.post.ui.page.main.navigation.MainPageScaffold
import com.huanchengfly.tieba.post.ui.page.main.navigation.rememberMainNavigationContentPosition
import com.huanchengfly.tieba.post.ui.page.main.navigation.rememberMainNavigationItems
import com.huanchengfly.tieba.post.ui.page.main.navigation.rememberMainNavigationType
import com.huanchengfly.tieba.post.ui.page.main.viewmodel.MainViewModel
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainPageContent(
    navigator: DestinationsNavigator,
    viewModel: MainViewModel = pageViewModel<MainUiIntent, MainViewModel>(emptyList()),
) {
    val appPreferences = LocalAppPreferences.current
    val globalEventBus = LocalGlobalEventBus.current
    val windowSizeClass = LocalWindowSizeClass.current
    val windowHeightSizeClass by rememberUpdatedState(newValue = windowSizeClass.heightSizeClass)
    val windowWidthSizeClass by rememberUpdatedState(newValue = windowSizeClass.widthSizeClass)
    val foldingDevicePosture by LocalDevicePosture.current

    val messageCount by viewModel.uiState.collectPartialAsState(
        prop1 = MainUiState::messageCount,
        initial = 0
    )

    LaunchedEffect(messageCount) {
        Log.i(
            THEME_DIAGNOSTICS_TAG,
            "MainPage messageCount updated value=$messageCount"
        )
    }

    val notificationCountFlow = LocalNotificationCountFlow.current
    LaunchedEffect(null) {
        notificationCountFlow.collect {
            Log.i(
                THEME_DIAGNOSTICS_TAG,
                "MainPage notificationCountFlow received count=$it"
            )
            viewModel.send(MainUiIntent.NewMessage.Receive(it))
        }
    }

    val hideExplore by rememberPreferenceAsState(
        key = booleanPreferencesKey("hideExplore"),
        defaultValue = appPreferences.hideExplore
    )
    val pageCount by remember {
        derivedStateOf {
            if (hideExplore) 3 else 4
        }
    }
    val pagerState = rememberPagerState { pageCount }
    LaunchedEffect(hideExplore) {
        if (pagerState.currentPage == 3 && hideExplore) {
            pagerState.scrollToPage(2)
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val themeColors = ExtendedTheme.colors
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
            viewModel.send(MainUiIntent.NewMessage.Clear)
        }
    }
    val navigationItems = rememberMainNavigationItems(
        hideExplore = hideExplore,
        messageCount = messageCount,
        canOpenExplore = !appPreferences.hideExplore,
        onOpenExplore = onOpenExplore,
        onNotificationsClick = onNotificationsClick
    )
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
