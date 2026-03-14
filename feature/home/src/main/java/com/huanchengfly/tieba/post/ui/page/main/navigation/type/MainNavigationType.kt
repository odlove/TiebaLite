package com.huanchengfly.tieba.post.ui.page.main.navigation.type

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.huanchengfly.tieba.core.ui.device.DevicePosture
import com.huanchengfly.tieba.core.ui.device.MainNavigationContentPosition
import com.huanchengfly.tieba.core.ui.device.MainNavigationType
import com.huanchengfly.tieba.core.ui.device.WindowHeightSizeClass
import com.huanchengfly.tieba.core.ui.device.WindowWidthSizeClass

@Composable
internal fun rememberMainNavigationType(
    windowWidthSizeClass: WindowWidthSizeClass,
    foldingDevicePosture: DevicePosture,
): MainNavigationType {
    val isBookPosture = foldingDevicePosture is DevicePosture.BookPosture
    return remember(windowWidthSizeClass, isBookPosture) {
        when (windowWidthSizeClass) {
            WindowWidthSizeClass.Compact -> MainNavigationType.BOTTOM_NAVIGATION
            WindowWidthSizeClass.Medium -> MainNavigationType.NAVIGATION_RAIL
            WindowWidthSizeClass.Expanded -> {
                if (isBookPosture) {
                    MainNavigationType.NAVIGATION_RAIL
                } else {
                    MainNavigationType.PERMANENT_NAVIGATION_DRAWER
                }
            }
            else -> MainNavigationType.BOTTOM_NAVIGATION
        }
    }
}

/**
 * Content inside Navigation Rail/Drawer can also be positioned at top, bottom or center for
 * ergonomics and reachability depending upon the height of the device.
 */
@Composable
internal fun rememberMainNavigationContentPosition(
    windowHeightSizeClass: WindowHeightSizeClass,
): MainNavigationContentPosition =
    remember(windowHeightSizeClass) {
        when (windowHeightSizeClass) {
            WindowHeightSizeClass.Compact -> MainNavigationContentPosition.TOP
            WindowHeightSizeClass.Medium,
            WindowHeightSizeClass.Expanded -> MainNavigationContentPosition.CENTER
            else -> MainNavigationContentPosition.TOP
        }
    }
