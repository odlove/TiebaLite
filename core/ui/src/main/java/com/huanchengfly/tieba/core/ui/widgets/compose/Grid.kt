package com.huanchengfly.tieba.core.ui.widgets.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.huanchengfly.tieba.core.ui.compose.GridScope as CoreGridScope
import com.huanchengfly.tieba.core.ui.compose.GridCounter as CoreGridCounter
import com.huanchengfly.tieba.core.ui.compose.items as coreItems
import com.huanchengfly.tieba.core.ui.compose.itemsIndexed as coreItemsIndexed

typealias GridScope = CoreGridScope
typealias GridCounter = CoreGridCounter

fun <T> GridScope.items(
    items: List<T>,
    span: (T) -> Int = { 1 },
    content: @Composable (T) -> Unit
) = coreItems(items, span, content)

fun <T> GridScope.itemsIndexed(
    items: List<T>,
    span: (index: Int, item: T) -> Int = { _, _ -> 1 },
    content: @Composable (index: Int, item: T) -> Unit
) = coreItemsIndexed(items, span, content)

@Composable
fun VerticalGrid(
    column: Int,
    modifier: Modifier = Modifier,
    rowModifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: GridScope.() -> Unit
) = com.huanchengfly.tieba.core.ui.compose.VerticalGrid(
    column = column,
    modifier = modifier,
    rowModifier = rowModifier,
    verticalArrangement = verticalArrangement,
    horizontalArrangement = horizontalArrangement,
    content = content
)
