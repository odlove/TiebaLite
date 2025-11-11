package com.huanchengfly.tieba.core.ui.widgets.compose

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HomeLoadMoreLayout(
    isLoading: Boolean,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    loadEnd: Boolean = false,
    lazyListState: LazyListState,
    isEmpty: Boolean = lazyListState.layoutInfo.totalItemsCount == 0,
    preloadCount: Int = 1,
    content: @Composable () -> Unit,
) {
    LoadMoreLayout(
        isLoading = isLoading,
        onLoadMore = onLoadMore,
        modifier = modifier,
        loadEnd = loadEnd,
        lazyListState = lazyListState,
        isEmpty = isEmpty,
        preloadCount = preloadCount,
        content = content
    )
}
