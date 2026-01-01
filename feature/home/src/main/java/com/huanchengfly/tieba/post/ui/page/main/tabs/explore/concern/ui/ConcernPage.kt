package com.huanchengfly.tieba.post.ui.page.main.tabs.explore.concern.ui

import com.huanchengfly.tieba.core.mvi.bindScrollToTopEvent

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.collectPartialAsState
import com.huanchengfly.tieba.core.mvi.onGlobalEvent
import com.huanchengfly.tieba.core.ui.pageViewModel
import com.huanchengfly.tieba.core.common.feed.ThreadCard
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.pullRefreshIndicator
import com.huanchengfly.tieba.core.ui.navigation.LocalHomeNavigation
import com.huanchengfly.tieba.core.ui.compose.Container
import com.huanchengfly.tieba.core.ui.widgets.compose.FeedCard
import com.huanchengfly.tieba.core.common.thread.toThreadPreview
import com.huanchengfly.tieba.core.ui.compose.LazyLoad
import com.huanchengfly.tieba.core.ui.compose.MyLazyColumn
import com.huanchengfly.tieba.core.ui.widgets.compose.VerticalDivider
import com.huanchengfly.tieba.core.ui.widgets.compose.HomeLoadMoreLayout
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.concern.contract.ConcernUiIntent
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.concern.contract.ConcernUiState
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.concern.viewmodel.ConcernViewModel
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ConcernPage(
    viewModel: ConcernViewModel = pageViewModel()
) {

    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(ConcernUiIntent.Refresh)
        viewModel.initialized = true
    }
    val homeNavigation = LocalHomeNavigation.current
    val isRefreshing by viewModel.uiState.collectPartialAsState(
        prop1 = ConcernUiState::isRefreshing,
        initial = false
    )
    val isLoadingMore by viewModel.uiState.collectPartialAsState(
        prop1 = ConcernUiState::isLoadingMore,
        initial = false
    )
    val nextPageTag by viewModel.uiState.collectPartialAsState(
        prop1 = ConcernUiState::nextPageTag,
        initial = ""
    )
    val threadIds by viewModel.uiState.collectPartialAsState(
        prop1 = ConcernUiState::threadIds,
        initial = persistentListOf()
    )
    val metadata by viewModel.uiState.collectPartialAsState(
        prop1 = ConcernUiState::metadata,
        initial = persistentMapOf()
    )

    // ✅ 订阅 Repository 的 threadsFlow，获取最新的 ThreadCard 列表
    val threadCards by viewModel.threadCardRepository.threadCardsFlow(threadIds)
        .collectAsState(initial = emptyList())

    // ✅ O(n) 查找优化：先构建 cardMap
    val cardMap by remember(threadCards) {
        derivedStateOf {
            threadCards.associateBy { it.threadId }
        }
    }

    // ✅ 从 Store 和 metadata 构建显示数据
    val displayData by remember(threadIds, metadata, cardMap) {
        derivedStateOf {
            threadIds.mapNotNull { threadId ->
                val meta = metadata[threadId] ?: return@mapNotNull null
                val card = cardMap[threadId] ?: return@mapNotNull null
                ConcernThreadItem(
                    recommendType = meta.recommendType,
                    thread = card
                )
            }.toImmutableList()
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.send(ConcernUiIntent.Refresh) })

    onGlobalEvent<CommonUiEvent.Refresh>(
        filter = { it.key == "concern" }
    ) {
        viewModel.send(ConcernUiIntent.Refresh)
    }

    val lazyListState = rememberLazyListState()


    viewModel.bindScrollToTopEvent(lazyListState = lazyListState)

    Box(
        modifier = Modifier.pullRefresh(pullRefreshState)
    ) {
        HomeLoadMoreLayout(
            isLoading = isLoadingMore,
            onLoadMore = { viewModel.send(ConcernUiIntent.LoadMore(nextPageTag)) },
            lazyListState = lazyListState,
        ) {
            MyLazyColumn(
                state = lazyListState,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                itemsIndexed(
                    items = displayData,
                    key = { _, item -> "${item.recommendType}_${item.thread.threadId}" },
                    contentType = { _, item -> item.recommendType }
                ) { index, item ->
                    Container {
                        val threadInfo = item.thread
                        if (item.recommendType == 1) {
                            Column {
                                // ✅ 订阅是否正在更新中
                                val isUpdating by viewModel.threadCardRepository.isThreadUpdating(threadInfo.threadId)
                                    .collectAsState(initial = false)

                                FeedCard(
                                    item = threadInfo,
                                    onClick = {
                                        homeNavigation.openThread(
                                            threadId = it.threadId,
                                            forumId = it.forumId,
                                            threadPreview = it.toThreadPreview(),
                                        )
                                    },
                                    onClickReply = {
                                        homeNavigation.openThread(
                                            threadId = it.threadId,
                                            forumId = it.forumId,
                                            threadPreview = it.toThreadPreview(),
                                            scrollToReply = true
                                        )
                                    },
                                    onAgree = { threadCard ->
                                        viewModel.send(
                                            ConcernUiIntent.Agree(
                                                threadCard.threadId,
                                                threadCard.firstPostId,
                                                threadCard.hasAgree
                                            )
                                        )
                                    },
                                    onClickForum = { homeNavigation.openForum(it) },
                                    onClickUser = { homeNavigation.openUserProfile(it) },
                                    agreeEnabled = !isUpdating,
                                )
                                if (index < displayData.size - 1) {
                                    VerticalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        thickness = 2.dp
                                    )
                                }
                            }
                        } else {
                            Box {}
                        }
                    }
                }
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = ExtendedTheme.colors.pullRefreshIndicator,
            contentColor = ExtendedTheme.colors.primary,
        )
    }
}

@Immutable
private data class ConcernThreadItem(
    val recommendType: Int,
    val thread: ThreadCard,
)
