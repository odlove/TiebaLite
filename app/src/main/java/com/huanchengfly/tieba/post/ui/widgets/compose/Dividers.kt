package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.core.ui.compose.CommonHorizontalDivider
import com.huanchengfly.tieba.core.ui.compose.CommonVerticalDivider
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme

@Composable
fun HorizontalDivider(
    modifier: Modifier = Modifier,
    color: Color = ExtendedTheme.colors.divider,
    height: Dp = 16.dp,
    width: Dp = 1.dp,
) {
    CommonHorizontalDivider(
        modifier = modifier,
        color = color,
        height = height,
        width = width
    )
}

@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    color: Color = ExtendedTheme.colors.divider,
    thickness: Dp = 1.dp,
    startIndent: Dp = 0.dp
) {
    CommonVerticalDivider(
        modifier = modifier,
        color = color,
        thickness = thickness,
        startIndent = startIndent
    )
}
