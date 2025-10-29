package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.core.ui.compose.UniversalScrollBox as CoreUniversalScrollBox

@Composable
fun UniversalScrollBox(
    modifier: Modifier = Modifier,
    scrollBarStroke: Dp = 2.dp,
    scrollBarColor: Color = androidx.compose.material.MaterialTheme.colors.secondary.copy(alpha = 0.5f),
    content: @Composable () -> Unit
) {
    CoreUniversalScrollBox(
        modifier = modifier,
        scrollBarStroke = scrollBarStroke,
        scrollBarColor = scrollBarColor,
        content = content
    )
}
