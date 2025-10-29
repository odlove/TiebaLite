package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.huanchengfly.tieba.core.ui.compose.DefaultLoadMoreIndicator
import com.huanchengfly.tieba.core.ui.compose.LoadMoreIndicatorColors
import com.huanchengfly.tieba.core.ui.compose.LoadMoreIndicatorDefaults
import com.huanchengfly.tieba.core.ui.compose.LoadMoreIndicatorTexts
import com.huanchengfly.tieba.core.ui.compose.LoadMoreLayout as CoreLoadMoreLayout
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.theme.compose.loadMoreIndicator

@Composable
fun LoadMoreLayout(
    isLoading: Boolean,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    enableLoadMore: Boolean = true,
    loadEnd: Boolean = false,
    indicator: @Composable (Boolean, Boolean, Boolean) -> Unit = { loading, end, willLoad ->
        DefaultIndicator(
            isLoading = loading,
            loadEnd = end,
            willLoad = willLoad
        )
    },
    lazyListState: LazyListState? = null,
    isEmpty: Boolean = lazyListState?.layoutInfo?.totalItemsCount == 0,
    preloadCount: Int = 1,
    content: @Composable () -> Unit,
) {
    CoreLoadMoreLayout(
        isLoading = isLoading,
        onLoadMore = onLoadMore,
        modifier = modifier,
        enableLoadMore = enableLoadMore,
        loadEnd = loadEnd,
        indicator = indicator,
        lazyListState = lazyListState,
        isEmpty = isEmpty,
        preloadCount = preloadCount,
        content = content
    )
}

@Composable
fun DefaultIndicator(
    isLoading: Boolean,
    loadEnd: Boolean,
    willLoad: Boolean,
    loadingText: String = stringResource(id = R.string.text_loading),
    loadEndText: String = stringResource(id = R.string.no_more),
    pullToLoadText: String = stringResource(id = R.string.pull_to_load),
    releaseToLoadText: String = stringResource(id = R.string.release_to_load),
    colors: LoadMoreIndicatorColors = LoadMoreIndicatorDefaults.colors(
        containerColor = ExtendedTheme.colors.loadMoreIndicator,
        contentColor = ExtendedTheme.colors.text,
        progressColor = ExtendedTheme.colors.primary
    ),
    texts: LoadMoreIndicatorTexts = LoadMoreIndicatorDefaults.texts(
        loadingText = loadingText,
        loadEndText = loadEndText,
        pullToLoadText = pullToLoadText,
        releaseToLoadText = releaseToLoadText
    ),
) {
    DefaultLoadMoreIndicator(
        isLoading = isLoading,
        loadEnd = loadEnd,
        willLoad = willLoad,
        colors = colors,
        texts = texts
    )
}
