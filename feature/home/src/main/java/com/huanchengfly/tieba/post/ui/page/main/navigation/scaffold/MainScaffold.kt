package com.huanchengfly.tieba.post.ui.page.main.navigation.scaffold

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
import com.huanchengfly.tieba.post.ui.page.main.navigation.chrome.bottom.BottomNavigation
import com.huanchengfly.tieba.post.ui.page.main.navigation.chrome.drawer.NavigationDrawerContent
import com.huanchengfly.tieba.post.ui.page.main.navigation.chrome.rail.NavigationRail
import com.huanchengfly.tieba.post.ui.page.main.navigation.items.NavigationItem
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun MainPageScaffold(
    navigationType: MainNavigationType,
    navigationContentPosition: MainNavigationContentPosition,
    navigationItems: ImmutableList<NavigationItem>,
    pagerState: PagerState,
    onItemClick: (index: Int, isReselected: Boolean) -> Unit,
    backgroundColor: Color,
) {
    val currentPosition = pagerState.currentPage

    Scaffold(
        backgroundColor = backgroundColor,
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (navigationType == MainNavigationType.BOTTOM_NAVIGATION) {
                BottomNavigation(
                    currentPosition = currentPosition,
                    onItemClick = onItemClick,
                    navigationItems = navigationItems,
                )
            }
        }
    ) { paddingValues ->
        Row(modifier = Modifier.fillMaxSize()) {
            if (navigationType == MainNavigationType.PERMANENT_NAVIGATION_DRAWER) {
                NavigationDrawerContent(
                    currentPosition = currentPosition,
                    onItemClick = onItemClick,
                    navigationItems = navigationItems,
                    navigationContentPosition = navigationContentPosition
                )
            }
            if (navigationType == MainNavigationType.NAVIGATION_RAIL) {
                NavigationRail(
                    currentPosition = currentPosition,
                    onItemClick = onItemClick,
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
