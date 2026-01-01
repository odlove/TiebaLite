package com.huanchengfly.tieba.post.ui.page.main.tabs.explore.personalized.ui.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.huanchengfly.tieba.core.common.feed.DislikeReason
import com.huanchengfly.tieba.core.common.feed.ThreadCard
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.pullRefreshIndicator
import com.huanchengfly.tieba.core.ui.widgets.compose.HomeLoadMoreLayout
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.personalized.ui.components.FeedList
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.personalized.ui.components.PersonalizedThreadItem
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.personalized.ui.components.RefreshTip
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun PersonalizedContentSection(
    pullRefreshState: PullRefreshState,
    lazyListState: LazyListState,
    isRefreshing: Boolean,
    isLoadingMore: Boolean,
    isEmpty: Boolean,
    displayData: ImmutableList<PersonalizedThreadItem>,
    refreshPosition: Int,
    hiddenThreadIds: ImmutableList<Long>,
    showRefreshTip: Boolean,
    refreshCount: Int,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit,
    onItemClick: (ThreadCard) -> Unit,
    onItemReplyClick: (ThreadCard) -> Unit,
    onAgree: (ThreadCard) -> Unit,
    onDislike: (ThreadCard, Long, ImmutableList<DislikeReason>) -> Unit,
    onOpenForum: (String) -> Unit,
    onClickUser: (Long) -> Unit,
) {
    Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
        HomeLoadMoreLayout(
            isLoading = isLoadingMore,
            onLoadMore = onLoadMore,
            lazyListState = lazyListState,
            isEmpty = isEmpty
        ) {
            FeedList(
                state = lazyListState,
                dataProvider = { displayData },
                refreshPositionProvider = { refreshPosition },
                hiddenThreadIdsProvider = { hiddenThreadIds },
                onItemClick = onItemClick,
                onItemReplyClick = onItemReplyClick,
                onAgree = onAgree,
                onDislike = onDislike,
                onRefresh = onRefresh,
                onOpenForum = onOpenForum,
                onClickUser = onClickUser
            )
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = ExtendedTheme.colors.pullRefreshIndicator,
            contentColor = ExtendedTheme.colors.primary,
        )

        AnimatedVisibility(
            visible = showRefreshTip,
            enter = fadeIn() + slideInVertically(),
            exit = slideOutVertically() + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            RefreshTip(refreshCount = refreshCount)
        }
    }
}
