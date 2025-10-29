package com.huanchengfly.tieba.core.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PullToRefreshLayout(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    indicator: @Composable BoxScope.(isRefreshing: Boolean, willRefresh: Boolean) -> Unit = { isRefreshing, willRefresh ->
        DefaultPullToRefreshIndicator(
            isRefreshing = isRefreshing,
            willRefresh = willRefresh
        )
    },
    refreshDistance: Dp = PullToRefreshDefaults.RefreshDistance,
    refreshingOffset: Dp = PullToRefreshDefaults.RefreshingOffset,
    threshold: Float = 0.75f,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val currentOnRefresh by rememberUpdatedState(newValue = onRefresh)

    val refreshDistancePx = remember(refreshDistance) {
        with(density) { refreshDistance.toPx() }
    }
    val refreshingOffsetPx = remember(refreshingOffset) {
        with(density) { refreshingOffset.toPx() }
    }

    val swipeableState = rememberSwipeableState(initialValue = false) { newValue ->
        if (newValue && !refreshing) {
            currentOnRefresh()
        }
        false
    }

    LaunchedEffect(refreshing) {
        if (!refreshing && swipeableState.currentValue) {
            swipeableState.snapTo(false)
        }
    }

    val showIndicator by remember {
        derivedStateOf {
            swipeableState.offset.value > (-refreshDistancePx + refreshingOffsetPx)
        }
    }
    val layoutOffsetY by remember {
        derivedStateOf {
            swipeableState.offset.value + refreshDistancePx
        }
    }

    val nestedScrollConnection = remember(swipeableState, refreshDistancePx) {
        PullToRefreshDefaults.createNestedScrollConnection(
            state = swipeableState,
            refreshDistancePx = refreshDistancePx
        )
    }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .nestedScroll(nestedScrollConnection)
                .swipeable(
                    state = swipeableState,
                    anchors = mapOf(
                        -refreshDistancePx to false,
                        refreshDistancePx to true,
                    ),
                    thresholds = { _: Boolean, _: Boolean -> FractionalThreshold(threshold) },
                    orientation = Orientation.Vertical,
                )
        ) {
            Box(
                modifier = Modifier.offset {
                    IntOffset(
                        x = 0,
                        y = layoutOffsetY.toInt()
                    )
                }
            ) {
                content()
            }
        }

        AnimatedVisibility(
            visible = showIndicator,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            indicator(refreshing, swipeableState.targetValue)
        }
    }
}

object PullToRefreshDefaults {
    val RefreshingOffset = 36.dp
    val RefreshDistance = 72.dp

    @OptIn(ExperimentalMaterialApi::class)
    fun <T> createNestedScrollConnection(
        state: androidx.compose.material.SwipeableState<T>,
        refreshDistancePx: Float,
    ): NestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val delta = available.toFloat()
            return if (delta < 0 && source == NestedScrollSource.Drag) {
                state.performDrag(delta).toOffset()
            } else {
                Offset.Zero
            }
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource,
        ): Offset {
            return if (source == NestedScrollSource.Drag) {
                state.performDrag(available.toFloat()).toOffset()
            } else {
                Offset.Zero
            }
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            val toFling = Offset(available.x, available.y).toFloat()
            return if (toFling < 0 && state.offset.value > -refreshDistancePx) {
                state.performFling(velocity = toFling)
                available
            } else {
                Velocity.Zero
            }
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            state.performFling(velocity = Offset(available.x, available.y).toFloat())
            return available
        }

        private fun Float.toOffset(): Offset = Offset(0f, this)

        private fun Offset.toFloat(): Float = this.y
    }
}

data class PullToRefreshIndicatorTexts(
    val pullToRefreshText: String,
    val releaseToRefreshText: String,
)

object PullToRefreshIndicatorDefaults {
    @Composable
    fun texts(
        pullToRefreshText: String = "Pull down to refresh",
        releaseToRefreshText: String = "Release to refresh",
    ): PullToRefreshIndicatorTexts = remember(pullToRefreshText, releaseToRefreshText) {
        PullToRefreshIndicatorTexts(
            pullToRefreshText = pullToRefreshText,
            releaseToRefreshText = releaseToRefreshText
        )
    }

    @Composable
    fun textStyle(): TextStyle = MaterialTheme.typography.caption

    @Composable
    fun textColor(): Color = MaterialTheme.colors.onSurface
}

@Composable
fun BoxScope.DefaultPullToRefreshIndicator(
    isRefreshing: Boolean,
    willRefresh: Boolean,
    modifier: Modifier = Modifier,
    texts: PullToRefreshIndicatorTexts = PullToRefreshIndicatorDefaults.texts(),
    textStyle: TextStyle = PullToRefreshIndicatorDefaults.textStyle(),
    textColor: Color = PullToRefreshIndicatorDefaults.textColor(),
) {
    Text(
        text = if (isRefreshing || willRefresh) {
            texts.releaseToRefreshText
        } else {
            texts.pullToRefreshText
        },
        modifier = modifier.align(Alignment.Center),
        style = textStyle,
        color = textColor,
    )
}
