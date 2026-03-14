package com.huanchengfly.tieba.post.ui.page.main.navigation.chrome.bottom

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.huanchengfly.tieba.post.ui.page.main.navigation.compose.NavigationItemModel
import com.huanchengfly.tieba.post.ui.page.main.navigation.compose.ThemeNavigationBar
import com.huanchengfly.tieba.post.ui.page.main.navigation.items.NavigationItem
import com.huanchengfly.tieba.post.ui.page.main.navigation.items.toNavigationModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun BottomNavigation(
    currentPosition: Int,
    onItemClick: (index: Int, isReselected: Boolean) -> Unit,
    navigationItems: ImmutableList<NavigationItem>,
) {
    val models: ImmutableList<NavigationItemModel> = navigationItems.map { item ->
        item.toNavigationModel()
    }.toImmutableList()

    ThemeNavigationBar(
        items = models,
        selectedIndex = currentPosition,
        onItemClick = onItemClick,
        modifier = Modifier.navigationBarsPadding()
    )
}
