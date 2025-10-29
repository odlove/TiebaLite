package com.huanchengfly.tieba.core.ui.compose

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.SwipeableState
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private val LoadDistance = 70.dp

@OptIn(ExperimentalMaterialApi::class, FlowPreview::class)
@Composable
fun LoadMoreLayout(
    isLoading: Boolean,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    enableLoadMore: Boolean = true,
    loadEnd: Boolean = false,
    indicator: @Composable (Boolean, Boolean, Boolean) -> Unit,
    lazyListState: LazyListState? = null,
    isEmpty: Boolean = lazyListState?.layoutInfo?.totalItemsCount == 0,
    preloadCount: Int = 1,
    content: @Composable () -> Unit,
) {
    val loadDistance = with(LocalDensity.current) { LoadDistance.toPx() }

    val curOnLoadMore by rememberUpdatedState(newValue = onLoadMore)
    var lastTriggerTime by remember { mutableLongStateOf(0L) }
    var waitingStateReset by remember { mutableStateOf(false) }
    val loadMoreFlow = remember {
        MutableSharedFlow<Long>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_LATEST
        )
    }
    val coroutineScope = rememberCoroutineScope()
    DisposableEffect(Unit) {
        val job = coroutineScope.launch {
            loadMoreFlow
                .sample(500)
                .collect {
                    curOnLoadMore()
                    waitingStateReset = true
                    lastTriggerTime = it
                }
        }

        onDispose { job.cancel() }
    }

    val canLoadMore = remember(enableLoadMore, loadEnd) { enableLoadMore && !loadEnd }
    val curIsEmpty by rememberUpdatedState(newValue = isEmpty)
    val curIsLoading by rememberUpdatedState(newValue = isLoading)
    val curCanLoadMore by rememberUpdatedState(newValue = canLoadMore)
    val curPreloadCount by rememberUpdatedState(newValue = preloadCount)

    val curLazyListState by rememberUpdatedState(newValue = lazyListState)
    LaunchedEffect(curLazyListState) {
        curLazyListState?.let { state ->
            snapshotFlow {
                val shouldPreload =
                    !curIsEmpty && curCanLoadMore && !curIsLoading && curPreloadCount > 0
                val isInPreloadRange =
                    state.firstVisibleItemIndex + state.layoutInfo.visibleItemsInfo.size - 1 >= state.layoutInfo.totalItemsCount - curPreloadCount
                shouldPreload && isInPreloadRange
            }
                .distinctUntilChanged()
                .collect {
                    if (it) {
                        val curTime = System.currentTimeMillis()
                        coroutineScope.launch {
                            loadMoreFlow.emit(curTime)
                        }
                        curTime - lastTriggerTime >= 500
                    }
                }
        }
    }

    val swipeableState = rememberSwipeableState(false) { newValue ->
        if (newValue && !curIsLoading && curCanLoadMore) {
            val curTime = System.currentTimeMillis()
            coroutineScope.launch {
                loadMoreFlow.emit(curTime)
            }
            curTime - lastTriggerTime >= 500
        } else !newValue
    }

    val isStateReset by remember { derivedStateOf { abs(swipeableState.offset.value - loadDistance) < 1f } }
    LaunchedEffect(waitingStateReset, isStateReset) {
        if (waitingStateReset && isStateReset) {
            waitingStateReset = false
        }
    }

    LaunchedEffect(isLoading) { if (!isLoading) swipeableState.animateTo(isLoading) }

    val loadMoreNestedScrollConnection = remember(swipeableState, loadDistance) {
        createLoadMoreNestedScrollConnection(
            state = swipeableState,
            loadDistance = loadDistance
        )
    }

    Box(
        modifier = Modifier
            .nestedScroll(loadMoreNestedScrollConnection)
            .swipeable(
                state = swipeableState,
                anchors = mapOf(
                    loadDistance to false,
                    -loadDistance to true,
                ),
                thresholds = { _: Boolean, _: Boolean -> FractionalThreshold(0.5f) },
                orientation = Orientation.Vertical,
                enabled = enableLoadMore && !waitingStateReset,
            )
            .fillMaxSize()
            .then(modifier)
    ) {
        content()

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset { IntOffset(0, swipeableState.offset.value.roundToInt()) }
        ) {
            if (enableLoadMore && swipeableState.offset.value != loadDistance) {
                indicator(isLoading, loadEnd, swipeableState.targetValue)
            }
        }
    }
}

@Composable
fun DefaultLoadMoreIndicator(
    isLoading: Boolean,
    loadEnd: Boolean,
    willLoad: Boolean,
    modifier: Modifier = Modifier,
    colors: LoadMoreIndicatorColors = LoadMoreIndicatorDefaults.colors(),
    texts: LoadMoreIndicatorTexts,
) {
    Surface(
        modifier = modifier,
        elevation = 8.dp,
        shape = RoundedCornerShape(100),
        color = colors.containerColor,
        contentColor = colors.contentColor
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(10.dp)
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 3.dp,
                    color = colors.progressColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = texts.loadingText,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else if (loadEnd) {
                Text(
                    text = texts.loadEndText,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = if (willLoad) texts.releaseToLoadText else texts.pullToLoadText,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

data class LoadMoreIndicatorColors(
    val containerColor: Color,
    val contentColor: Color,
    val progressColor: Color,
)

data class LoadMoreIndicatorTexts(
    val loadingText: String,
    val loadEndText: String,
    val pullToLoadText: String,
    val releaseToLoadText: String,
)

object LoadMoreIndicatorDefaults {
    @Composable
    fun colors(
        containerColor: Color = MaterialTheme.colors.surface,
        contentColor: Color = MaterialTheme.colors.onSurface,
        progressColor: Color = MaterialTheme.colors.primary,
    ): LoadMoreIndicatorColors =
        remember(containerColor, contentColor, progressColor) {
            LoadMoreIndicatorColors(
                containerColor = containerColor,
                contentColor = contentColor,
                progressColor = progressColor
            )
        }

    @Composable
    fun texts(
        loadingText: String,
        loadEndText: String,
        pullToLoadText: String,
        releaseToLoadText: String,
    ): LoadMoreIndicatorTexts =
        remember(loadingText, loadEndText, pullToLoadText, releaseToLoadText) {
            LoadMoreIndicatorTexts(
                loadingText = loadingText,
                loadEndText = loadEndText,
                pullToLoadText = pullToLoadText,
                releaseToLoadText = releaseToLoadText
            )
        }
}

@OptIn(ExperimentalMaterialApi::class)
private fun <T> createLoadMoreNestedScrollConnection(
    state: SwipeableState<T>,
    loadDistance: Float
): NestedScrollConnection = object : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val delta = available.toFloat()
        return if (delta > 0 && source == NestedScrollSource.Drag) {
            state.performDrag(delta).toOffset()
        } else {
            Offset.Zero
        }
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        return if (source == NestedScrollSource.Drag) {
            state.performDrag(available.toFloat()).toOffset()
        } else {
            Offset.Zero
        }
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        val toFling = Offset(available.x, available.y).toFloat()
        return if (toFling > 0 && state.offset.value < loadDistance) {
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
