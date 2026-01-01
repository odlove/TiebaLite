package com.huanchengfly.tieba.post.ui.page.main.tabs.explore.hot.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.huanchengfly.tieba.core.common.thread.toThreadPreview
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.bindScrollToTopEvent
import com.huanchengfly.tieba.core.mvi.collectPartialAsState
import com.huanchengfly.tieba.core.mvi.onGlobalEvent
import com.huanchengfly.tieba.core.ui.compose.LazyLoad
import com.huanchengfly.tieba.core.ui.navigation.LocalHomeNavigation
import com.huanchengfly.tieba.core.ui.pageViewModel
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.pullRefreshIndicator
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.hot.contract.HotUiIntent
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.hot.contract.HotUiState
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.hot.ui.sections.HotContentSection
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.hot.viewmodel.HotViewModel
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HotPage(
    viewModel: HotViewModel = pageViewModel()
) {

    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(HotUiIntent.Load)
        viewModel.initialized = true
    }
    val homeNavigation = LocalHomeNavigation.current
    val listState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    viewModel.bindScrollToTopEvent(lazyListState = listState)
    onGlobalEvent<CommonUiEvent.Refresh>(
        filter = { it.key == "hot" }
    ) {
        viewModel.send(HotUiIntent.Load)
    }
    val isLoading by viewModel.uiState.collectPartialAsState(
        prop1 = HotUiState::isRefreshing,
        initial = false
    )
    val topicList by viewModel.uiState.collectPartialAsState(
        prop1 = HotUiState::topicList,
        initial = emptyList()
    )
    val tabList by viewModel.uiState.collectPartialAsState(
        prop1 = HotUiState::tabList,
        initial = emptyList()
    )
    val currentTabCode by viewModel.uiState.collectPartialAsState(
        prop1 = HotUiState::currentTabCode,
        initial = "all"
    )
    val isLoadingThreadList by viewModel.uiState.collectPartialAsState(
        prop1 = HotUiState::isLoadingThreadList,
        initial = false
    )
    val threadIds by viewModel.uiState.collectPartialAsState(
        prop1 = HotUiState::threadIds,
        initial = emptyList()
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

    // ✅ 从 Store 构建显示数据
    val displayThreadList by remember(threadIds, cardMap) {
        derivedStateOf {
            threadIds.mapNotNull { threadId ->
                cardMap[threadId]
            }.toImmutableList()
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = { viewModel.send(HotUiIntent.Load) })
    Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
        HotContentSection(
            listState = listState,
            topicList = topicList,
            tabList = tabList,
            currentTabCode = currentTabCode,
            displayThreadList = displayThreadList,
            onOpenTopicList = { homeNavigation.openHotTopicList() },
            onTabSelected = { tabCode ->
                viewModel.send(HotUiIntent.RefreshThreadList(tabCode))
            },
            onThreadClick = {
                homeNavigation.openThread(
                    threadId = it.threadId,
                    forumId = it.forumId,
                    threadPreview = it.toThreadPreview(),
                )
            },
            onThreadReplyClick = {
                homeNavigation.openThread(
                    threadId = it.threadId,
                    forumId = it.forumId,
                    threadPreview = it.toThreadPreview(),
                    scrollToReply = true
                )
            },
            onAgree = { threadInfo ->
                viewModel.send(
                    HotUiIntent.Agree(
                        threadId = threadInfo.threadId,
                        postId = threadInfo.firstPostId,
                        hasAgree = threadInfo.hasAgree
                    )
                )
            },
            onOpenForum = { homeNavigation.openForum(it) },
            onOpenUser = { homeNavigation.openUserProfile(it) },
            isThreadUpdatingFlow = { threadId ->
                viewModel.threadCardRepository.isThreadUpdating(threadId)
            }
        )

        PullRefreshIndicator(
            refreshing = isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = ExtendedTheme.colors.pullRefreshIndicator,
            contentColor = ExtendedTheme.colors.primary,
        )
    }
}
