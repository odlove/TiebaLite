package com.huanchengfly.tieba.post.ui.page.main.explore.concern

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
import com.huanchengfly.tieba.post.api.models.protos.hasAgree
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.collectPartialAsState
import com.huanchengfly.tieba.core.mvi.onGlobalEvent
import com.huanchengfly.tieba.core.ui.pageViewModel
import com.huanchengfly.tieba.core.mvi.wrapImmutable
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.theme.compose.pullRefreshIndicator
import com.huanchengfly.tieba.post.ui.page.LocalNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.ForumPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.ThreadPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.UserProfilePageDestination
import com.huanchengfly.tieba.core.ui.compose.Container
import com.huanchengfly.tieba.post.ui.widgets.compose.FeedCard
import com.huanchengfly.tieba.core.ui.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.LoadMoreLayout
import com.huanchengfly.tieba.core.ui.compose.MyLazyColumn
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

    // ✅ 订阅 Repository 的 threadsFlow，获取最新的 ThreadEntity 列表
    val threadEntities by viewModel.pbPageRepository.threadsFlow(threadIds)
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

                // 🔍 调试日志：打印关键信息
                Log.d("ConcernPage_DisplayData", "threadId=$threadId, entity.threadId=${entity.threadId}, proto.id=${entity.proto.id}")
                Log.d("ConcernPage_DisplayData", "  abstract=${entity.proto._abstract.size}, media=${entity.proto.media.size}")
                Log.d("ConcernPage_DisplayData", "  title='${entity.proto.title}', agreeNum=${entity.meta.agreeNum}")

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
                        val threadInfo = item.threadList
                        if (item.recommendType == 1 && threadInfo != null) {
                            Column {
                                // ✅ 订阅是否正在更新中
                                val isUpdating by viewModel.pbPageRepository.isThreadUpdating(threadInfo.id)
                                    .collectAsState(initial = false)

                                FeedCard(
                                    item = wrapImmutable(threadInfo),
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
