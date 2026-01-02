package com.huanchengfly.tieba.post.ui.page.main.navigation.scaffold

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.huanchengfly.tieba.core.ui.device.MainNavigationContentPosition
import com.huanchengfly.tieba.core.ui.device.MainNavigationType
import com.huanchengfly.tieba.post.ui.page.main.navigation.chrome.BottomNavigation
import com.huanchengfly.tieba.post.ui.page.main.navigation.chrome.NavigationDrawerContent
import com.huanchengfly.tieba.post.ui.page.main.navigation.chrome.NavigationRail
import com.huanchengfly.tieba.post.ui.page.main.navigation.items.NavigationItem
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun MainPageScaffold(
    navigationType: MainNavigationType,
    navigationContentPosition: MainNavigationContentPosition,
    navigationItems: ImmutableList<NavigationItem>,
    pagerState: PagerState,
    onChangePosition: (Int) -> Unit,
    onReselected: (Int) -> Unit,
    backgroundColor: Color,
) {
    val currentPosition = pagerState.currentPage
    val showBottomNavigation = navigationType == MainNavigationType.BOTTOM_NAVIGATION
    val showNavigationRail = navigationType == MainNavigationType.NAVIGATION_RAIL
    val showNavigationDrawer = navigationType == MainNavigationType.PERMANENT_NAVIGATION_DRAWER

    Scaffold(
        backgroundColor = backgroundColor,
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(visible = showBottomNavigation) {
                BottomNavigation(
                    currentPosition = currentPosition,
                    onChangePosition = onChangePosition,
                    onReselected = onReselected,
                    navigationItems = navigationItems,
                )
            }
        }
    ) { paddingValues ->
        Row(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(visible = showNavigationDrawer) {
                NavigationDrawerContent(
                    currentPosition = currentPosition,
                    onChangePosition = onChangePosition,
                    onReselected = onReselected,
                    navigationItems = navigationItems,
                    navigationContentPosition = navigationContentPosition
                )
            }
            AnimatedVisibility(visible = showNavigationRail) {
                NavigationRail(
                    currentPosition = currentPosition,
                    onChangePosition = onChangePosition,
                    onReselected = onReselected,
                    navigationItems = navigationItems,
                    navigationContentPosition = navigationContentPosition
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                MainNavigationPager(
                    pagerState = pagerState,
                    navigationItems = navigationItems,
                    contentPadding = paddingValues
                )
            }
        }
    }
}
