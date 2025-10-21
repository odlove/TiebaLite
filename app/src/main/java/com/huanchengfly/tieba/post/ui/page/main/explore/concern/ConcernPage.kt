package com.huanchengfly.tieba.post.ui.page.main.explore.concern

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
import com.huanchengfly.tieba.post.api.models.protos.hasAgree
import com.huanchengfly.tieba.post.arch.CommonUiEvent.ScrollToTop.bindScrollToTopEvent
import com.huanchengfly.tieba.post.arch.GlobalEvent
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.onGlobalEvent
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.arch.wrapImmutable
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.theme.compose.pullRefreshIndicator
import com.huanchengfly.tieba.post.ui.page.LocalNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.ForumPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.ThreadPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.UserProfilePageDestination
import com.huanchengfly.tieba.post.ui.widgets.compose.Container
import com.huanchengfly.tieba.post.ui.widgets.compose.FeedCard
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.LoadMoreLayout
import com.huanchengfly.tieba.post.ui.widgets.compose.MyLazyColumn
import com.huanchengfly.tieba.post.ui.widgets.compose.VerticalDivider
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
    val navigator = LocalNavigator.current
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

    // ✅ 订阅 Store 的 threadsFlow，获取最新的 ThreadEntity 列表
    val threadEntities by viewModel.threadStore.threadsFlow(threadIds)
        .collectAsState(initial = emptyList())

    // ✅ O(n) 查找优化：先构建 entityMap
    val entityMap by remember(threadEntities) {
        derivedStateOf {
            threadEntities.associateBy { it.threadId }
        }
    }

    // ✅ 从 Store 和 metadata 构建显示数据
    val displayData by remember(threadIds, metadata, entityMap) {
        derivedStateOf {
            threadIds.mapNotNull { threadId ->
                val meta = metadata[threadId] ?: return@mapNotNull null  // 元数据缺失则跳过
                val entity = entityMap[threadId] ?: return@mapNotNull null  // Store 中不存在（已被 TTL 清理）

                // 从 Store Entity 构建 ConcernData（兼容旧 UI）
                com.huanchengfly.tieba.post.api.models.protos.userLike.ConcernData(
                    recommendType = meta.recommendType,  // ✅ 使用 metadata 中的 recommendType
                    threadList = entity.proto.copy(
                        agreeNum = entity.meta.agreeNum,
                        agree = entity.proto.agree?.copy(
                            hasAgree = entity.meta.hasAgree,
                            agreeNum = entity.meta.agreeNum.toLong()
                        ),
                        // ✅ 新增：同步收藏状态，防止详情页显示旧值
                        collectStatus = entity.meta.collectStatus,
                        collectMarkPid = entity.meta.collectMarkPid.takeIf { it > 0L }?.toString() ?: "0"
                    )
                )
            }.toImmutableList()
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.send(ConcernUiIntent.Refresh) })

    onGlobalEvent<GlobalEvent.Refresh>(
        filter = { it.key == "concern" }
    ) {
        viewModel.send(ConcernUiIntent.Refresh)
    }

    val lazyListState = rememberLazyListState()
    viewModel.bindScrollToTopEvent(lazyListState = lazyListState)

    Box(
        modifier = Modifier.pullRefresh(pullRefreshState)
    ) {
        LoadMoreLayout(
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
                    items = displayData,  // ✅ 使用 Store 增强的数据
                    key = { _, item -> "${item.recommendType}_${item.threadList?.id}" },
                    contentType = { _, item -> item.recommendType }
                ) { index, item ->
                    Container {
                        if (item.recommendType == 1 && item.threadList != null) {
                            Column {
                                // ✅ 订阅是否正在更新中
                                val isUpdating by viewModel.threadStore.isThreadUpdating(item.threadList.id)
                                    .collectAsState(initial = false)

                                FeedCard(
                                    item = wrapImmutable(item.threadList),
                                    onClick = {
                                        navigator.navigate(
                                            ThreadPageDestination(
                                                it.threadId,
                                                it.forumId,
                                                threadInfo = it
                                            )
                                        )
                                    },
                                    onClickReply = {
                                        navigator.navigate(
                                            ThreadPageDestination(
                                                it.threadId,
                                                it.forumId,
                                                scrollToReply = true
                                            )
                                        )
                                    },
                                    onAgree = { threadInfo ->
                                        viewModel.send(
                                            ConcernUiIntent.Agree(
                                                threadInfo.id,
                                                threadInfo.firstPostId,
                                                threadInfo.hasAgree
                                            )
                                        )
                                    },
                                    onClickForum = { navigator.navigate(ForumPageDestination(it.name)) },
                                    onClickUser = { navigator.navigate(UserProfilePageDestination(it.id)) },
                                    agreeEnabled = !isUpdating,  // ✅ 传递 enabled 状态
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