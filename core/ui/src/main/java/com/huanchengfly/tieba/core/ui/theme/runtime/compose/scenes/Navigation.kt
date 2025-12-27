package com.huanchengfly.tieba.core.ui.theme.runtime.compose.scenes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ExperimentalMaterialApi
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
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedColors
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.navigationSelectedColor
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.navigationUnselectedColor
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
fun defaultNavigationColors(themeColors: ExtendedColors = ExtendedTheme.colors): NavigationColors =
    NavigationColors(
        background = themeColors.bottomBar,
        selectedContent = navigationSelectedColor(),
        unselectedContent = navigationUnselectedColor(),
        badgeBackground = navigationSelectedColor(),
        indicatorColor = themeColors.indicator
    )

@Composable
private fun navigationSurfaceColor(themeColors: ExtendedColors = ExtendedTheme.colors): Color =
    themeColors.bottomBar

@Composable
private fun navigationContentColor(isSelected: Boolean, themeColors: ExtendedColors = ExtendedTheme.colors): Color =
    if (isSelected) navigationSelectedColor() else navigationUnselectedColor()

@Composable
fun ThemeNavigationBar(
    items: ImmutableList<NavigationItemModel>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    onItemReselected: (Int) -> Unit = {},
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
                        if (selected) onItemReselected(index) else onItemSelected(index)
                        item.onClick?.invoke()
                    },
                    icon = {
                        NavigationIcon(
                            painter = item.iconPainter(selected),
                            selected = selected,
                            badgeText = item.badgeText,
                            badgeColor = colors.badgeBackground
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
    onItemSelected: (Int) -> Unit,
    onItemReselected: (Int) -> Unit = {},
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
                        if (selected) onItemReselected(index) else onItemSelected(index)
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
                            iconSize = 20.dp
                        )
                    },
                    alwaysShowLabel = false
                )
            }
        }
    }
}

@Composable
private fun NavigationIcon(
    painter: Painter,
    selected: Boolean,
    badgeText: String?,
    badgeColor: Color,
    iconSize: Dp = 24.dp
) {
    Box {
        Icon(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
        )
        if (!badgeText.isNullOrEmpty()) {
            androidx.compose.material.Text(
                textAlign = TextAlign.Center,
                fontSize = 10.sp,
                color = ExtendedTheme.colors.onAccent,
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
private fun BottomNavDivider(themeColors: ExtendedColors = ExtendedTheme.colors) {
    val dividerColor = themeColors.divider
    if (dividerColor.alpha > 0f) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(dividerColor)
        )
    }
}
