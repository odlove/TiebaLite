package com.huanchengfly.tieba.core.ui.theme.runtime.compose.scenes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Tab
import androidx.compose.material.TabPosition
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.core.ui.compose.ScrollableTabRow
import com.huanchengfly.tieba.core.ui.compose.TabPosition as ScrollableTabPosition
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.searchBoxBackgroundColor
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.searchBoxContentColor
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.tabIndicatorColor
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.tabSelectedColor
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.tabUnselectedColor
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.topBarContentColor
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.topBarIconTint

/**
 * 语义化顶栏封装；居中标题采用 TopAppBar 外层，标题槽内再居中，保留官方 padding/ripple。
 */
@Composable
fun ThemeTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = ExtendedTheme.colors.topBar,
    contentColor: Color = topBarContentColor(),
    elevation: Dp = 0.dp,
    insets: Boolean = true,
    statusBarColor: Color = backgroundColor,
    centerTitle: Boolean = false,
) {
    Column(modifier = modifier) {
        if (insets) {
            Spacer(
                modifier = Modifier
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .fillMaxWidth()
                    .background(statusBarColor)
            )
        }
        if (centerTitle) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                TopAppBar(
                    backgroundColor = backgroundColor,
                    contentColor = contentColor,
                    elevation = elevation,
                    navigationIcon = navigationIcon?.let {
                        @Composable {
                            CompositionLocalProvider(LocalContentColor provides contentColor) { it() }
                        }
                    },
                    actions = {
                        CompositionLocalProvider(LocalContentColor provides contentColor) { actions() }
                    },
                    title = {} // 覆盖层单独居中
                )
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CompositionLocalProvider(LocalContentColor provides contentColor) {
                        ProvideTextStyle(value = MaterialTheme.typography.h6) { title() }
                    }
                }
            }
        } else {
            TopAppBar(
                backgroundColor = backgroundColor,
                contentColor = contentColor,
                elevation = elevation,
                navigationIcon = navigationIcon?.let {
                    @Composable {
                        CompositionLocalProvider(LocalContentColor provides contentColor) { it() }
                    }
                },
                actions = {
                    CompositionLocalProvider(LocalContentColor provides contentColor) { actions() }
                },
                title = {
                    CompositionLocalProvider(LocalContentColor provides contentColor) {
                        ProvideTextStyle(value = MaterialTheme.typography.h6) { title() }
                    }
                }
            )
        }
    }
}

data class ThemeTab(
    val text: String,
    val icon: (@Composable () -> Unit)? = null,
)

@Composable
fun ThemeTabRow(
    tabs: List<ThemeTab>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    indicatorColor: Color = tabIndicatorColor(),
    backgroundColor: Color = ExtendedTheme.colors.topBar,
    selectedContentColor: Color = tabSelectedColor(),
    unselectedContentColor: Color = tabUnselectedColor(),
    divider: @Composable (() -> Unit)? = null,
    indicator: @Composable (List<TabPosition>) -> Unit = { positions ->
        ThemeTabIndicator(positions = positions, selectedIndex = selectedIndex, color = indicatorColor)
    },
    onTabSelected: (Int) -> Unit,
) {
    TabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier,
        backgroundColor = backgroundColor,
        contentColor = selectedContentColor,
        indicator = indicator,
        divider = divider ?: {}
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = index == selectedIndex
            Tab(
                selected = selected,
                onClick = { onTabSelected(index) },
                selectedContentColor = selectedContentColor,
                unselectedContentColor = unselectedContentColor
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tab.icon?.invoke()
                    Text(
                        text = tab.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = if (tab.icon != null) 8.dp else 0.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeScrollableTabRow(
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    indicatorColor: Color = tabIndicatorColor(),
    backgroundColor: Color = ExtendedTheme.colors.topBar,
    selectedContentColor: Color = tabSelectedColor(),
    unselectedContentColor: Color = tabUnselectedColor(),
    edgePadding: Dp = TabRowDefaults.ScrollableTabRowPadding,
    divider: @Composable (() -> Unit)? = null,
    indicator: @Composable (List<ScrollableTabPosition>) -> Unit = { positions ->
        ThemeScrollableTabIndicator(positions = positions, selectedIndex = selectedIndex, color = indicatorColor)
    },
    content: @Composable (selectedContentColor: Color, unselectedContentColor: Color) -> Unit,
) {
    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier,
        backgroundColor = backgroundColor,
        contentColor = selectedContentColor,
        edgePadding = edgePadding,
        indicator = indicator,
        divider = divider ?: {}
    ) {
        content(selectedContentColor, unselectedContentColor)
    }
}

@Composable
fun ThemeScrollableTabRow(
    tabs: List<ThemeTab>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    indicatorColor: Color = tabIndicatorColor(),
    backgroundColor: Color = ExtendedTheme.colors.topBar,
    selectedContentColor: Color = tabSelectedColor(),
    unselectedContentColor: Color = tabUnselectedColor(),
    edgePadding: Dp = TabRowDefaults.ScrollableTabRowPadding,
    divider: @Composable (() -> Unit)? = null,
    indicator: @Composable (List<ScrollableTabPosition>) -> Unit = { positions ->
        ThemeScrollableTabIndicator(positions = positions, selectedIndex = selectedIndex, color = indicatorColor)
    },
    onTabSelected: (Int) -> Unit,
) {
    ThemeScrollableTabRow(
        selectedIndex = selectedIndex,
        modifier = modifier,
        indicatorColor = indicatorColor,
        backgroundColor = backgroundColor,
        selectedContentColor = selectedContentColor,
        unselectedContentColor = unselectedContentColor,
        edgePadding = edgePadding,
        divider = divider,
        indicator = indicator
    ) { selectedColor, unselectedColor ->
        tabs.forEachIndexed { index, tab ->
            val selected = index == selectedIndex
            Tab(
                selected = selected,
                onClick = { onTabSelected(index) },
                selectedContentColor = selectedColor,
                unselectedContentColor = unselectedColor
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tab.icon?.invoke()
                    Text(
                        text = tab.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = if (tab.icon != null) 8.dp else 0.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeTabIndicator(
    positions: List<TabPosition>,
    selectedIndex: Int,
    color: Color
) {
    TabRowDefaults.Indicator(
        modifier = Modifier.tabIndicatorOffset(positions[selectedIndex]),
        color = color
    )
}

@Composable
private fun ThemeScrollableTabIndicator(
    positions: List<ScrollableTabPosition>,
    selectedIndex: Int,
    color: Color
) {
    if (positions.isEmpty()) return
    val position = positions[selectedIndex]
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.BottomStart)
            .offset(x = position.left)
            .width(position.width)
            .height(2.dp)
            .background(color)
    )
}

@Composable
fun ThemeSearchBox(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    singleLine: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.body1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val backgroundColor = searchBoxBackgroundColor()
    val contentColor = searchBoxContentColor()
    val interactionSource = remember { MutableInteractionSource() }
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = enabled,
                        onClick = onClick
                    )
                } else Modifier
            ),
        enabled = enabled,
        singleLine = singleLine,
        textStyle = textStyle,
        visualTransformation = visualTransformation,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        leadingIcon = leadingIcon?.let {
            @Composable {
                CompositionLocalProvider(LocalContentColor provides topBarIconTint()) {
                    it()
                }
            }
        },
        trailingIcon = trailingIcon?.let {
            @Composable {
                CompositionLocalProvider(LocalContentColor provides topBarIconTint()) {
                    it()
                }
            }
        },
        placeholder = placeholder?.let {
            @Composable {
                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    ProvideTextStyle(value = textStyle) { it() }
                }
            }
        },
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = backgroundColor,
            textColor = contentColor,
            placeholderColor = contentColor,
            leadingIconColor = contentColor,
            trailingIconColor = contentColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}
