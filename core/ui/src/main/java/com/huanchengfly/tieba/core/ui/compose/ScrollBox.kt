package com.huanchengfly.tieba.core.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private fun sizeOf(width: Float, height: Float) = Size(width = width, height = height)

/**
 * 带可拖动滚动条的通用滚动容器。
 *
 * @param modifier 外层修饰符
 * @param scrollBarStroke 滚动条厚度
 * @param scrollBarColor 滚动条颜色
 * @param content 实际内容（必须存在至少一个子元素）
 */
@Composable
fun UniversalScrollBox(
    modifier: Modifier = Modifier,
    scrollBarStroke: Dp = 2.dp,
    scrollBarColor: Color = MaterialTheme.colors.secondary.copy(alpha = 0.5f),
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        var outerSize by remember { mutableStateOf(IntSize(width = 0, height = 0)) }
        var sizeRatio by remember { mutableStateOf(sizeOf(0f, 0f)) }
        var barSize by remember { mutableStateOf(sizeOf(0f, 0f)) }
        var dragOffset by remember { mutableStateOf(sizeOf(0f, 0f)) }

        Layout(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = if (barSize.width > 0) 16.dp else 0.dp,
                    end = if (barSize.height > 0) 16.dp else 0.dp
                )
                .clipToBounds()
                .align(Alignment.TopStart),
            content = content
        ) { measurables, constraints ->
            outerSize = IntSize(width = constraints.maxWidth, height = constraints.maxHeight)
            val placeables = measurables.map { measurable ->
                measurable.measure(
                    constraints = constraints.copy(
                        maxHeight = Int.MAX_VALUE,
                        maxWidth = Int.MAX_VALUE
                    )
                )
            }
            val innerSize = IntSize(width = placeables[0].width, height = placeables[0].height)

            val needWidth = innerSize.width > outerSize.width
            val needHeight = innerSize.height > outerSize.height

            sizeRatio = sizeOf(
                width = if (needWidth) innerSize.width.toFloat() / outerSize.width.toFloat() else 0f,
                height = if (needHeight) innerSize.height.toFloat() / outerSize.height.toFloat() else 0f
            )
            barSize = sizeOf(
                width = if (needWidth) (outerSize.width * outerSize.width).toFloat() / innerSize.width else 0f,
                height = if (needHeight) (outerSize.height * outerSize.height).toFloat() / innerSize.height else 0f
            )

            layout(width = outerSize.width, height = outerSize.height) {
                placeables.forEach {
                    it.place(
                        x = (-dragOffset.width * sizeRatio.width).toInt(),
                        y = (-dragOffset.height * sizeRatio.height).toInt(),
                        zIndex = 0f
                    )
                }
            }
        }

        if (barSize.width > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(scrollBarStroke)
                    .align(Alignment.BottomStart)
                    .background(Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxHeight()
                        .offset { IntOffset(dragOffset.width.roundToInt(), 0) }
                        .width(barSize.width.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(scrollBarColor)
                        .draggable(
                            state = rememberDraggableState { delta ->
                                var widthOffset = dragOffset.width + delta
                                widthOffset = widthOffset.coerceIn(0f, outerSize.width - barSize.width)
                                dragOffset = sizeOf(widthOffset, dragOffset.height)
                            },
                            orientation = Orientation.Horizontal
                        )
                )
            }
        }

        if (barSize.height > 0) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(scrollBarStroke)
                    .align(Alignment.TopEnd)
                    .background(Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .fillMaxWidth()
                        .offset { IntOffset(0, dragOffset.height.roundToInt()) }
                        .height(barSize.height.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(scrollBarColor)
                        .draggable(
                            state = rememberDraggableState { delta ->
                                var heightOffset = dragOffset.height + delta
                                heightOffset = heightOffset.coerceIn(0f, outerSize.height - barSize.height)
                                dragOffset = sizeOf(dragOffset.width, heightOffset)
                            },
                            orientation = Orientation.Vertical
                        )
                )
            }
        }
    }
}
