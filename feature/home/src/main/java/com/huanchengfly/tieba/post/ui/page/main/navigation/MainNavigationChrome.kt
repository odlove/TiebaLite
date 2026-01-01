package com.huanchengfly.tieba.post.ui.page.main.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.huanchengfly.tieba.core.ui.utils.DevicePosture
import com.huanchengfly.tieba.core.ui.utils.MainNavigationContentPosition
import com.huanchengfly.tieba.core.ui.utils.MainNavigationType
import com.huanchengfly.tieba.core.ui.widgets.compose.navigation.BottomNavigation as CoreBottomNavigation
import com.huanchengfly.tieba.core.ui.widgets.compose.navigation.NavigationDrawerContent
import com.huanchengfly.tieba.core.ui.widgets.compose.navigation.NavigationItem
import com.huanchengfly.tieba.core.ui.widgets.compose.navigation.NavigationRail as CoreNavigationRail
import com.huanchengfly.tieba.core.ui.windowsizeclass.WindowHeightSizeClass
import com.huanchengfly.tieba.core.ui.windowsizeclass.WindowWidthSizeClass
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun MainBottomNavigation(
    navigationType: MainNavigationType,
    currentPosition: Int,
    onChangePosition: (Int) -> Unit,
    onReselected: (Int) -> Unit,
    navigationItems: ImmutableList<NavigationItem>,
) {
    AnimatedVisibility(visible = navigationType == MainNavigationType.BOTTOM_NAVIGATION) {
        CoreBottomNavigation(
            currentPosition = currentPosition,
            onChangePosition = onChangePosition,
            onReselected = onReselected,
            navigationItems = navigationItems,
        )
    }
}

@Composable
internal fun NavigationWrapper(
    currentPosition: Int,
    onChangePosition: (position: Int) -> Unit,
    onReselected: (position: Int) -> Unit,
    navigationItems: ImmutableList<NavigationItem>,
    navigationType: MainNavigationType,
    navigationContentPosition: MainNavigationContentPosition,
    content: @Composable () -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(visible = navigationType == MainNavigationType.PERMANENT_NAVIGATION_DRAWER) {
            NavigationDrawerContent(
                currentPosition = currentPosition,
                onChangePosition = onChangePosition,
                onReselected = onReselected,
                navigationItems = navigationItems,
                navigationContentPosition = navigationContentPosition
            )
        }
        AnimatedVisibility(visible = navigationType == MainNavigationType.NAVIGATION_RAIL) {
            CoreNavigationRail(
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
            content()
        }
    }
}

@Composable
internal fun rememberMainNavigationType(
    windowWidthSizeClass: WindowWidthSizeClass,
    foldingDevicePosture: DevicePosture,
): MainNavigationType {
    val navigationType by remember(windowWidthSizeClass, foldingDevicePosture) {
        derivedStateOf {
            when (windowWidthSizeClass) {
                WindowWidthSizeClass.Compact -> {
                    MainNavigationType.BOTTOM_NAVIGATION
                }

                WindowWidthSizeClass.Medium -> {
                    MainNavigationType.NAVIGATION_RAIL
                }

                WindowWidthSizeClass.Expanded -> {
                    if (foldingDevicePosture is DevicePosture.BookPosture) {
                        MainNavigationType.NAVIGATION_RAIL
                    } else {
                        MainNavigationType.PERMANENT_NAVIGATION_DRAWER
                    }
                }

                else -> {
                    MainNavigationType.BOTTOM_NAVIGATION
                }
            }
        }
    }
    return navigationType
}

/**
 * Content inside Navigation Rail/Drawer can also be positioned at top, bottom or center for
 * ergonomics and reachability depending upon the height of the device.
 */
@Composable
internal fun rememberMainNavigationContentPosition(
    windowHeightSizeClass: WindowHeightSizeClass,
): MainNavigationContentPosition {
    val navigationContentPosition by remember(windowHeightSizeClass) {
        derivedStateOf {
            when (windowHeightSizeClass) {
                WindowHeightSizeClass.Compact -> {
                    MainNavigationContentPosition.TOP
                }

                WindowHeightSizeClass.Medium,
                WindowHeightSizeClass.Expanded -> {
                    MainNavigationContentPosition.CENTER
                }

                else -> {
                    MainNavigationContentPosition.TOP
                }
            }
        }
    }
    return navigationContentPosition
}
