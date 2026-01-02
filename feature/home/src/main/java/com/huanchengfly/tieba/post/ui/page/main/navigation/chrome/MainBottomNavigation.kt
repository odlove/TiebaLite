package com.huanchengfly.tieba.post.ui.page.main.navigation.chrome

import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.huanchengfly.tieba.core.theme.compose.scenes.NavigationItemModel
import com.huanchengfly.tieba.core.theme.compose.scenes.ThemeNavigationBar
import com.huanchengfly.tieba.post.ui.page.main.navigation.items.NavigationItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun BottomNavigation(
    currentPosition: Int,
    onChangePosition: (position: Int) -> Unit,
    onReselected: (position: Int) -> Unit,
    navigationItems: ImmutableList<NavigationItem>,
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
    ThemeNavigationBar(
        items = models,
        selectedIndex = currentPosition,
        onItemSelected = { index ->
            if (index == currentPosition) {
                onReselected(index)
            } else {
                onChangePosition(index)
            }
            navigationItems.getOrNull(index)?.onClick?.invoke()
        },
        onItemReselected = onReselected,
        modifier = Modifier.navigationBarsPadding()
    )
}
