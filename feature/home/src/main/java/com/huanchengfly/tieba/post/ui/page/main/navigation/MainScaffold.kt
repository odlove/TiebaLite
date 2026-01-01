package com.huanchengfly.tieba.post.ui.page.main.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.huanchengfly.tieba.core.ui.compose.SnackbarScaffold
import com.huanchengfly.tieba.core.ui.compose.rememberSnackbarState
import com.huanchengfly.tieba.core.ui.utils.MainNavigationContentPosition
import com.huanchengfly.tieba.core.ui.utils.MainNavigationType
import com.huanchengfly.tieba.core.ui.widgets.compose.navigation.NavigationItem
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
    NavigationWrapper(
        currentPosition = pagerState.currentPage,
        onChangePosition = onChangePosition,
        onReselected = onReselected,
        navigationItems = navigationItems,
        navigationType = navigationType,
        navigationContentPosition = navigationContentPosition
    ) {
        val snackbarState = rememberSnackbarState()
        SnackbarScaffold(
            snackbarState = snackbarState,
            backgroundColor = backgroundColor,
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                MainBottomNavigation(
                    navigationType = navigationType,
                    currentPosition = pagerState.currentPage,
                    onChangePosition = onChangePosition,
                    onReselected = onReselected,
                    navigationItems = navigationItems,
                )
            }
        ) { paddingValues ->
            MainNavigationPager(
                pagerState = pagerState,
                navigationItems = navigationItems,
                contentPadding = paddingValues
            )
        }
    }
}
