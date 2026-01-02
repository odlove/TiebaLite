package com.huanchengfly.tieba.post.ui.page.main.navigation.scaffold

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.huanchengfly.tieba.core.ui.compose.LazyLoadHorizontalPager
import com.huanchengfly.tieba.post.ui.page.main.navigation.items.NavigationItem
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun MainNavigationPager(
    pagerState: PagerState,
    navigationItems: ImmutableList<NavigationItem>,
    contentPadding: PaddingValues,
) {
    LazyLoadHorizontalPager(
        contentPadding = contentPadding,
        state = pagerState,
        key = { navigationItems[it].id },
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.Top,
        userScrollEnabled = false,
        beyondViewportPageCount = 3,
    ) {
        navigationItems[it].content()
    }
}
