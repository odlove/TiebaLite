package com.huanchengfly.tieba.post.ui.page.main.navigation.chrome.rail

import androidx.compose.runtime.Composable
import com.huanchengfly.tieba.post.ui.page.main.navigation.compose.ThemeNavigationRail
import com.huanchengfly.tieba.core.ui.device.MainNavigationContentPosition
import com.huanchengfly.tieba.core.ui.compose.widgets.AccountNavIcon
import com.huanchengfly.tieba.post.ui.page.main.navigation.items.NavigationItem
import com.huanchengfly.tieba.post.ui.page.main.navigation.items.toNavigationModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun NavigationRail(
    currentPosition: Int,
    onItemClick: (index: Int, isReselected: Boolean) -> Unit,
    navigationItems: ImmutableList<NavigationItem>,
    navigationContentPosition: MainNavigationContentPosition
) {
    val models = navigationItems.map { item ->
        item.toNavigationModel()
    }.toImmutableList()

    ThemeNavigationRail(
        items = models,
        selectedIndex = currentPosition,
        onItemClick = onItemClick,
        header = { AccountNavIcon(spacer = false) }
    )
}
