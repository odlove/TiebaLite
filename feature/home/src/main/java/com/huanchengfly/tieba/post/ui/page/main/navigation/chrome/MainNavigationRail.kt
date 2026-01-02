package com.huanchengfly.tieba.post.ui.page.main.navigation.chrome

import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.runtime.Composable
import com.huanchengfly.tieba.core.theme.compose.scenes.NavigationItemModel
import com.huanchengfly.tieba.core.theme.compose.scenes.ThemeNavigationRail
import com.huanchengfly.tieba.core.ui.device.MainNavigationContentPosition
import com.huanchengfly.tieba.core.ui.compose.widgets.AccountNavIcon
import com.huanchengfly.tieba.post.ui.page.main.navigation.items.NavigationItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun NavigationRail(
    currentPosition: Int,
    onChangePosition: (position: Int) -> Unit,
    onReselected: (position: Int) -> Unit,
    navigationItems: ImmutableList<NavigationItem>,
    navigationContentPosition: MainNavigationContentPosition
) {
    val models: ImmutableList<NavigationItemModel> = navigationItems.map { item ->
        NavigationItemModel(
            id = item.id,
            iconPainter = { selected ->
                rememberAnimatedVectorPainter(
                    animatedImageVector = item.icon(),
                    atEnd = selected
                )
            },
            title = item.title(false),
            badgeText = if (item.badge) item.badgeText else null,
            onClick = item.onClick
        )
    }.toImmutableList()
    ThemeNavigationRail(
        items = models,
        selectedIndex = currentPosition,
        onItemSelected = { index ->
            if (index == currentPosition) {
                onReselected(index)
            } else {
                onChangePosition(index)
            }
        },
        onItemReselected = onReselected,
        header = { AccountNavIcon(spacer = false) }
    )
}
