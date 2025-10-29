package com.huanchengfly.tieba.core.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.LocalContentColor
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TitleCentredToolbar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    insets: Boolean = true,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: (@Composable ColumnScope.() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = MaterialTheme.colors.onSurface,
    statusBarColor: Color = backgroundColor,
    titleTextStyle: TextStyle = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
) {
    TopAppBarContainer(
        topBar = {
            TopAppBar(
                backgroundColor = backgroundColor,
                contentColor = contentColor,
                elevation = 0.dp
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            navigationIcon?.invoke()
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxHeight(),
                            content = actions
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ProvideTextStyle(value = titleTextStyle) {
                            CompositionLocalProvider(LocalContentColor provides contentColor) {
                                title()
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier,
        insets = insets,
        statusBarColor = statusBarColor,
        backgroundColor = backgroundColor,
        content = content
    )
}

@Composable
fun Toolbar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = MaterialTheme.colors.onSurface,
    statusBarColor: Color = backgroundColor,
    titleTextStyle: TextStyle = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    TopAppBarContainer(
        topBar = {
            TopAppBar(
                title = {
                    ProvideTextStyle(value = titleTextStyle) {
                        CompositionLocalProvider(LocalContentColor provides contentColor) {
                            title()
                        }
                    }
                },
                actions = {
                    CompositionLocalProvider(LocalContentColor provides contentColor) {
                        actions()
                    }
                },
                navigationIcon = (@Composable {
                    CompositionLocalProvider(LocalContentColor provides contentColor) {
                        navigationIcon?.invoke()
                    }
                }).takeIf { navigationIcon != null },
                backgroundColor = backgroundColor,
                contentColor = contentColor,
                elevation = 0.dp
            )
        },
        modifier = modifier,
        statusBarColor = statusBarColor,
        backgroundColor = backgroundColor,
        content = content
    )
}

@Composable
fun BackNavigationIcon(
    onBackPressed: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onBackPressed, modifier = modifier) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}

@Composable
fun ActionItem(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}
