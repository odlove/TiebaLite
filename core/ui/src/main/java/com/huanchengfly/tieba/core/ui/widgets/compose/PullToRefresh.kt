package com.huanchengfly.tieba.core.ui.widgets.compose

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.huanchengfly.tieba.core.ui.R as CoreUiR
import com.huanchengfly.tieba.core.ui.compose.DefaultPullToRefreshIndicator
import com.huanchengfly.tieba.core.ui.compose.PullToRefreshDefaults
import com.huanchengfly.tieba.core.ui.compose.PullToRefreshIndicatorDefaults
import com.huanchengfly.tieba.core.ui.compose.PullToRefreshIndicatorTexts
import com.huanchengfly.tieba.core.ui.compose.PullToRefreshLayout as CorePullToRefreshLayout

@Composable
fun PullToRefreshLayout(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    indicator: @Composable BoxScope.(isRefreshing: Boolean, willRefresh: Boolean) -> Unit = { isRefreshing, willRefresh ->
        DefaultIndicator(
            isRefreshing = isRefreshing,
            willRefresh = willRefresh
        )
    },
    refreshDistance: Dp = PullToRefreshDefaults.RefreshDistance,
    refreshingOffset: Dp = PullToRefreshDefaults.RefreshingOffset,
    threshold: Float = 0.75f,
    content: @Composable () -> Unit,
) {
    CorePullToRefreshLayout(
        refreshing = refreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        indicator = indicator,
        refreshDistance = refreshDistance,
        refreshingOffset = refreshingOffset,
        threshold = threshold,
        content = content
    )
}

@Composable
fun BoxScope.DefaultIndicator(
    isRefreshing: Boolean,
    willRefresh: Boolean,
    modifier: Modifier = Modifier,
    texts: PullToRefreshIndicatorTexts = PullToRefreshIndicatorDefaults.texts(
        pullToRefreshText = stringResource(id = CoreUiR.string.pull_down_to_refresh),
        releaseToRefreshText = stringResource(id = CoreUiR.string.release_to_refresh),
    ),
    textStyle: TextStyle = PullToRefreshIndicatorDefaults.textStyle(),
    textColor: Color = PullToRefreshIndicatorDefaults.textColor(),
) {
    DefaultPullToRefreshIndicator(
        isRefreshing = isRefreshing,
        willRefresh = willRefresh,
        modifier = modifier,
        texts = texts,
        textStyle = textStyle,
        textColor = textColor
    )
}
