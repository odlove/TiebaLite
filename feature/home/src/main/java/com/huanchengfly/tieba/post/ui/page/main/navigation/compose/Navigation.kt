package com.huanchengfly.tieba.post.ui.page.main.navigation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.NavigationRail
import androidx.compose.material.NavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.core.theme2.compose.Theme2Theme
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class NavigationItemModel(
    val id: String,
    val iconPainter: @Composable (selected: Boolean) -> Painter,
    val title: String,
    val badgeText: String? = null,
    val onClick: (() -> Unit)? = null,
)

@Stable
data class NavigationColors(
    val background: Color,
    val selectedContent: Color,
    val unselectedContent: Color,
    val badgeBackground: Color,
    val indicatorColor: Color,
)

@Composable
fun defaultNavigationColors(): NavigationColors =
    NavigationColors(
        background = Theme2Theme.colors.surfaceNav,
        selectedContent = Theme2Theme.colors.stateActive,
        unselectedContent = Theme2Theme.colors.stateUnselected,
        badgeBackground = Theme2Theme.colors.stateActive,
        indicatorColor = Theme2Theme.colors.outlineLow
    )

@Composable
private fun NavigationIcon(
    painter: Painter,
    selected: Boolean,
    badgeText: String?,
    badgeColor: Color,
    iconSize: Dp = 24.dp,
    contentDescription: String? = null
) {
    Box {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
        )
        if (!badgeText.isNullOrEmpty()) {
            androidx.compose.material.Text(
                textAlign = TextAlign.Center,
                fontSize = 10.sp,
                color = Theme2Theme.colors.contentOnBrand,
                text = badgeText,
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .align(Alignment.TopEnd)
                    .background(
                        color = badgeColor,
                        shape = CircleShape
                    ),
            )
        }
    }
}

@Composable
private fun BottomNavDivider() {
    val dividerColor = Theme2Theme.colors.outlineLow
    if (dividerColor.alpha > 0f) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(dividerColor)
        )
    }
}

@Composable
fun ThemeNavigationBar(
    items: ImmutableList<NavigationItemModel>,
    selectedIndex: Int,
    onItemClick: (index: Int, isReselected: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    colors: NavigationColors = defaultNavigationColors(),
    elevation: Dp = 0.dp,
) {
    Column(modifier = modifier.navigationBarsPadding()) {
        BottomNavDivider()
        BottomNavigation(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.background,
            contentColor = colors.selectedContent,
            elevation = elevation
        ) {
            items.forEachIndexed { index, item ->
                val selected = index == selectedIndex
                BottomNavigationItem(
                    selected = selected,
                    onClick = {
                        onItemClick(index, selected)
                        item.onClick?.invoke()
                    },
                    icon = {
                        NavigationIcon(
                            painter = item.iconPainter(selected),
                            selected = selected,
                            badgeText = item.badgeText,
                            badgeColor = colors.badgeBackground,
                            contentDescription = item.title
                        )
                    },
                    selectedContentColor = colors.selectedContent,
                    unselectedContentColor = colors.unselectedContent,
                    alwaysShowLabel = false,
                )
            }
        }
    }
}

@Composable
fun ThemeNavigationRail(
    items: ImmutableList<NavigationItemModel>,
    selectedIndex: Int,
    onItemClick: (index: Int, isReselected: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    colors: NavigationColors = defaultNavigationColors(),
    elevation: Dp = 0.dp,
    header: @Composable (ColumnScope.() -> Unit)? = null,
) {
    NavigationRail(
        modifier = modifier
            .fillMaxHeight()
            .navigationBarsPadding(),
        backgroundColor = colors.background,
        contentColor = colors.unselectedContent,
        elevation = elevation,
        header = header
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
        ) {
            items.forEachIndexed { index, item ->
                val selected = index == selectedIndex
                NavigationRailItem(
                    selected = selected,
                    onClick = {
                        onItemClick(index, selected)
                        item.onClick?.invoke()
                    },
                    selectedContentColor = colors.selectedContent,
                    unselectedContentColor = colors.unselectedContent,
                    icon = {
                        NavigationIcon(
                            painter = item.iconPainter(selected),
                            selected = selected,
                            badgeText = item.badgeText,
                            badgeColor = colors.badgeBackground,
                            iconSize = 20.dp,
                            contentDescription = item.title
                        )
                    },
                    alwaysShowLabel = false
                )
            }
        }
    }
}
