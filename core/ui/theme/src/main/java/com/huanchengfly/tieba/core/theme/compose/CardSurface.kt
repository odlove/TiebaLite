package com.huanchengfly.tieba.core.theme.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 统一的卡片容器，确保所有列表/卡片默认使用 `ExtendedTheme.colors.card` 以及对应的文字颜色。
 */
@Composable
fun CardSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    elevation: Dp = 0.dp,
    border: BorderStroke? = null,
    backgroundColor: androidx.compose.ui.graphics.Color = cardBackgroundColor(),
    contentColor: androidx.compose.ui.graphics.Color = cardContentColor(),
    plain: Boolean = false,
    content: @Composable () -> Unit
) {
    val resolvedShape = if (plain) RectangleShape else shape
    val resolvedBackground = if (plain) Color.Transparent else backgroundColor

    Surface(
        modifier = modifier,
        shape = resolvedShape,
        color = resolvedBackground,
        contentColor = contentColor,
        border = border,
        elevation = elevation,
        content = content
    )
}
