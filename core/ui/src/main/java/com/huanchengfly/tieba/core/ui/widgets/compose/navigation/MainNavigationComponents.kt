package com.huanchengfly.tieba.core.ui.widgets.compose.navigation

import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.navigationSelectedColor
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.navigationUnselectedColor
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.topBarContentColor
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.topBarSecondaryColor
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.scenes.NavigationItemModel
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.scenes.ThemeDrawerSheet
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.scenes.ThemeNavigationBar
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.scenes.ThemeNavigationRail
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.scenes.ThemeNavigationDrawerItem
import com.huanchengfly.tieba.core.ui.utils.MainNavigationContentPosition
import com.huanchengfly.tieba.core.ui.widgets.compose.AccountNavIcon
import com.huanchengfly.tieba.core.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.core.ui.widgets.compose.AvatarIcon
import com.huanchengfly.tieba.core.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.utils.AccountUtil.LocalAccount
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

private enum class LayoutType {
    HEADER, CONTENT
}

@Composable
fun PermanentNavigationDrawer(
    drawerContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ThemeDrawerSheet(modifier = modifier) {
        Row(Modifier.fillMaxSize()) {
            drawerContent()
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
        }
    }
}

private val ActiveIndicatorHeight = 56.dp
private val ActiveIndicatorWidth = 240.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NavigationDrawerItem(
    label: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    badge: (@Composable () -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val activeColor = navigationSelectedColor()
    val inactiveColor = navigationUnselectedColor()
    val inactiveSecondaryColor = topBarSecondaryColor()
    val containerColor = if (selected) activeColor.copy(alpha = 0.12f) else Color.Transparent

    Surface(
        selected = selected,
        onClick = onClick,
        modifier = modifier
            .height(ActiveIndicatorHeight)
            .fillMaxWidth(),
        shape = shape,
        color = containerColor,
        interactionSource = interactionSource,
    ) {
        Row(
            Modifier.padding(start = 16.dp, end = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                val iconColor = if (selected) activeColor else inactiveSecondaryColor
                CompositionLocalProvider(LocalContentColor provides iconColor, content = icon)
                Spacer(Modifier.width(12.dp))
            }
            Box(Modifier.weight(1f)) {
                val labelColor = if (selected) activeColor else inactiveColor
                CompositionLocalProvider(LocalContentColor provides labelColor, content = label)
            }
            if (badge != null) {
                Spacer(Modifier.width(12.dp))
                val badgeColor = if (selected) activeColor else inactiveSecondaryColor
                CompositionLocalProvider(LocalContentColor provides badgeColor, content = badge)
            }
        }
    }
}

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun NavigationDrawerContent(
    currentPosition: Int,
    onChangePosition: (position: Int) -> Unit,
    onReselected: (position: Int) -> Unit,
    navigationItems: ImmutableList<NavigationItem>,
    navigationContentPosition: MainNavigationContentPosition
) {
    PositionLayout(
        modifier = Modifier
            .width(ActiveIndicatorWidth)
            .background(ExtendedTheme.colors.bottomBar)
            .padding(16.dp),
        content = {
            Column(
                modifier = Modifier
                    .layoutId(LayoutType.HEADER)
                    .statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val account = LocalAccount.current
                if (account != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        AccountNavIcon(spacer = false, size = Sizes.Large)
                        Text(
                            text = account.nameShow ?: account.name,
                            style = MaterialTheme.typography.subtitle1,
                            color = ExtendedTheme.colors.text
                        )
                    }
                } else {
                    val context = LocalContext.current
                    val appName = remember(context) { resolveAppName(context) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarIcon(
                            icon = Icons.Rounded.Person,
                            size = Sizes.Small,
                            contentDescription = appName,
                            backgroundColor = ExtendedTheme.colors.chip,
                            color = ExtendedTheme.colors.onChip
                        )
                        Text(
                            text = appName.uppercase(),
                            style = MaterialTheme.typography.h6,
                            color = ExtendedTheme.colors.primary
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .layoutId(LayoutType.CONTENT)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                navigationItems.fastForEachIndexed { index, navigationItem ->
                    ThemeNavigationDrawerItem(
                        selected = index == currentPosition,
                        onClick = {
                            if (index == currentPosition) {
                                onReselected(index)
                            } else {
                                onChangePosition(index)
                            }
                        },
                        label = { Text(text = navigationItem.title(index == currentPosition)) },
                        icon = {
                            val painter = rememberAnimatedVectorPainter(
                                animatedImageVector = navigationItem.icon(),
                                atEnd = index == currentPosition
                            )
                            Icon(
                                painter = painter,
                                contentDescription = navigationItem.title(index == currentPosition),
                                modifier = Modifier.size(24.dp),
                            )
                        },
                        badge = if (navigationItem.badge) {
                            {
                                Text(
                                    textAlign = TextAlign.Center,
                                    fontSize = 10.sp,
                                    color = ExtendedTheme.colors.onPrimary,
                                    text = navigationItem.badgeText ?: "",
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(
                                            color = ExtendedTheme.colors.primary,
                                            shape = CircleShape
                                        ),
                                )
                            }
                        } else null
                    )
                }
            }
        },
        navigationContentPosition = navigationContentPosition
    )
}

@Composable
private fun PositionLayout(
    navigationContentPosition: MainNavigationContentPosition,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = content,
        measurePolicy = { measurables, constraints ->
            lateinit var headerMeasurable: Measurable
            lateinit var contentMeasurable: Measurable
            measurables.forEach {
                when (it.layoutId) {
                    LayoutType.HEADER -> headerMeasurable = it
                    LayoutType.CONTENT -> contentMeasurable = it
                    else -> error("Unknown layoutId encountered!")
                }
            }

            val headerPlaceable = headerMeasurable.measure(constraints)
            val contentPlaceable = contentMeasurable.measure(
                constraints.offset(vertical = -headerPlaceable.height)
            )
            layout(constraints.maxWidth, constraints.maxHeight) {
                headerPlaceable.placeRelative(0, 0)

                val nonContentVerticalSpace = constraints.maxHeight - contentPlaceable.height

                val contentPlaceableY = when (navigationContentPosition) {
                    MainNavigationContentPosition.TOP -> 0
                    MainNavigationContentPosition.CENTER -> nonContentVerticalSpace / 2
                }.coerceAtLeast(headerPlaceable.height)

                contentPlaceable.placeRelative(0, contentPlaceableY)
            }
        }
    )
}

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

@Composable
fun BottomNavigationDivider(
) {
    val themeColors = ExtendedTheme.colors
    if (!themeColors.isNightMode) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(themeColors.divider)
        )
    }
}

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

@Immutable
data class NavigationItem @OptIn(ExperimentalAnimationGraphicsApi::class) constructor(
    val id: String,
    val icon: @Composable () -> AnimatedImageVector,
    val title: @Composable (selected: Boolean) -> String,
    val badge: Boolean = false,
    val badgeText: String? = null,
    val onClick: (() -> Unit)? = null,
    val content: @Composable () -> Unit = {},
)
private fun resolveAppName(context: android.content.Context): String {
    val applicationInfo = context.applicationInfo
    val labelRes = applicationInfo.labelRes
    return when {
        labelRes != 0 -> context.getString(labelRes)
        applicationInfo.nonLocalizedLabel != null -> applicationInfo.nonLocalizedLabel.toString()
        else -> context.packageManager.getApplicationLabel(applicationInfo)?.toString()
            ?: context.packageName
    }
}
