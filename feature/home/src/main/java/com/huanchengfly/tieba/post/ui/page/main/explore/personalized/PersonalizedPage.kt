package com.huanchengfly.tieba.post.ui.page.main.explore.personalized

import com.huanchengfly.tieba.core.mvi.bindScrollToTopEvent

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.core.mvi.wrapImmutable
import com.huanchengfly.tieba.core.common.feed.DislikeReason
import com.huanchengfly.tieba.core.common.feed.PersonalizedInfo
import com.huanchengfly.tieba.core.common.feed.ThreadCard
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.collectPartialAsState
import com.huanchengfly.tieba.core.mvi.onEvent
import com.huanchengfly.tieba.core.mvi.onGlobalEvent
import com.huanchengfly.tieba.core.ui.pageViewModel
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.pullRefreshIndicator
import com.huanchengfly.tieba.core.ui.navigation.LocalHomeNavigation
import com.huanchengfly.tieba.core.ui.preferences.LocalAppPreferences
import com.huanchengfly.tieba.core.ui.widgets.compose.BlockTip
import com.huanchengfly.tieba.core.ui.widgets.compose.BlockableContent
import com.huanchengfly.tieba.core.ui.compose.Container
import com.huanchengfly.tieba.core.ui.widgets.compose.ErrorScreen
import com.huanchengfly.tieba.core.ui.widgets.compose.FeedCard
import com.huanchengfly.tieba.core.ui.compose.LazyLoad
import com.huanchengfly.tieba.core.ui.compose.MyLazyColumn
import com.huanchengfly.tieba.core.ui.widgets.compose.VerticalDivider
import com.huanchengfly.tieba.core.ui.widgets.compose.states.StateScreen
import com.huanchengfly.tieba.core.ui.widgets.compose.HomeLoadMoreLayout
import com.huanchengfly.tieba.core.common.thread.toThreadPreview
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.huanchengfly.tieba.core.ui.R as CoreUiR

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PersonalizedPage(
    viewModel: PersonalizedViewModel = pageViewModel()
) {

    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(PersonalizedUiIntent.Refresh)
        viewModel.initialized = true
    }
    val homeNavigation = LocalHomeNavigation.current
    val isRefreshing by viewModel.uiState.collectPartialAsState(
        prop1 = PersonalizedUiState::isRefreshing,
        initial = false
    )
    val isLoadingMore by viewModel.uiState.collectPartialAsState(
        prop1 = PersonalizedUiState::isLoadingMore,
        initial = false
    )
    val currentPage by viewModel.uiState.collectPartialAsState(
        prop1 = PersonalizedUiState::currentPage,
        initial = 1
    )
    val threadIds by viewModel.uiState.collectPartialAsState(
        prop1 = PersonalizedUiState::threadIds,
        initial = persistentListOf()
    )
    val metadata by viewModel.uiState.collectPartialAsState(
        prop1 = PersonalizedUiState::metadata,
        initial = persistentMapOf()
    )
    val error by viewModel.uiState.collectPartialAsState(
        prop1 = PersonalizedUiState::error,
        initial = null
    )
    val refreshPosition by viewModel.uiState.collectPartialAsState(
        prop1 = PersonalizedUiState::refreshPosition,
        initial = 0
    )
    val hiddenThreadIds by viewModel.uiState.collectPartialAsState(
        prop1 = PersonalizedUiState::hiddenThreadIds,
        initial = persistentListOf()
    )

    // ✅ 订阅 Repository 的 threadsFlow，获取最新的 ThreadCard 列表
    val threadCards by viewModel.threadCardRepository.threadCardsFlow(threadIds)
        .collectAsState(initial = emptyList())
    val appPreferences = LocalAppPreferences.current

    // ✅ O(n) 查找优化：先构建 cardMap
    val cardMap by remember(threadCards) {
        derivedStateOf {
            threadCards.associateBy { it.threadId }
        }
    }

    // ✅ 构建显示数据：从 ThreadCard 和 metadata 组合
    val displayData by remember(threadIds, metadata, cardMap, appPreferences.hideBlockedContent) {
        derivedStateOf<ImmutableList<PersonalizedThreadItem>> {
            threadIds.mapNotNull { threadId ->
                val card = cardMap[threadId] ?: return@mapNotNull null
                val meta = metadata[threadId] ?: return@mapNotNull null
                PersonalizedThreadItem(
                    thread = card,
                    personalized = meta.personalized,
                    blocked = meta.blocked,
                    hidden = meta.blocked && appPreferences.hideBlockedContent
                )
            }.toImmutableList()
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.send(PersonalizedUiIntent.Refresh) }
    )
    val lazyListState = rememberLazyListState()


    viewModel.bindScrollToTopEvent(lazyListState = lazyListState)
    val isEmpty by remember(threadIds, isRefreshing) {
        derivedStateOf<Boolean> {
            // 只使用 ViewModel 的 threadIds 判断是否真正为空，避免 displayData 重建时的闪现
            threadIds.isEmpty() && !isRefreshing
        }
    }
    val isError by remember(error) {
        derivedStateOf<Boolean> {
            error != null
        }
    }
    var refreshCount by remember {
        mutableIntStateOf(0)
    }
    var showRefreshTip by remember {
        mutableStateOf(false)
    }

    onGlobalEvent<CommonUiEvent.Refresh>(
        filter = { it.key == "personalized" }
    ) {
        if(!isRefreshing) viewModel.send(PersonalizedUiIntent.Refresh)
    }
    viewModel.onEvent<PersonalizedUiEvent.RefreshSuccess> {
        refreshCount = it.count
        showRefreshTip = true
    }

    if (showRefreshTip) {
        LaunchedEffect(displayData) {  // ✅ 改用 displayData 而非 data
            lazyListState.scrollToItem(0, 0)
            delay(2000)
            showRefreshTip = false
        }
    }
//    if (lazyListState.isScrollInProgress) {
//        DisposableEffect(Unit) {
//            PauseLoadWhenScrollingDrawableDecodeInterceptor.scrolling = true
//            onDispose {
//                PauseLoadWhenScrollingDrawableDecodeInterceptor.scrolling = false
//            }
//        }
//    }
    StateScreen(
        modifier = Modifier.fillMaxSize(),
        isEmpty = isEmpty,
        isError = isError,
        isLoading = isRefreshing,
        onReload = { viewModel.send(PersonalizedUiIntent.Refresh) },
        errorScreen = {
            error?.let {
                ErrorScreen(
                    error = it.get()
                )
            }
        }
    ) {
        Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
            HomeLoadMoreLayout(
                isLoading = isLoadingMore,
                onLoadMore = { viewModel.send(PersonalizedUiIntent.LoadMore(currentPage + 1)) },
                lazyListState = lazyListState,
                isEmpty = displayData.isEmpty()
            ) {
                FeedList(
                    state = lazyListState,
                    dataProvider = { displayData },  // ✅ 使用展示数据
                    refreshPositionProvider = { refreshPosition },
                    hiddenThreadIdsProvider = { hiddenThreadIds },
                    onItemClick = {
                        homeNavigation.openThread(
                            threadId = it.threadId,
                            forumId = it.forumId,
                            threadPreview = it.toThreadPreview(),
                        )
                    },
                    onItemReplyClick = {
                        homeNavigation.openThread(
                            threadId = it.threadId,
                            forumId = it.forumId,
                            threadPreview = it.toThreadPreview(),
                            scrollToReply = true
                        )
                    },
                    onAgree = { threadInfo ->
                        viewModel.send(
                            PersonalizedUiIntent.Agree(
                                threadInfo.threadId,
                                threadInfo.firstPostId,
                                threadInfo.hasAgree
                            )
                        )
                    },
                    onDislike = { item, clickTime, reasons ->
                        viewModel.send(
                            PersonalizedUiIntent.Dislike(
                                item.forumId,
                                item.threadId,
                                reasons,
                                clickTime
                            )
                        )
                    },
                    onRefresh = { viewModel.send(PersonalizedUiIntent.Refresh) },
                    onOpenForum = { homeNavigation.openForum(it) },
                    onClickUser = { homeNavigation.openUserProfile(it) }
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
}

@Composable
private fun BoxScope.RefreshTip(refreshCount: Int) {
    Box(
        modifier = Modifier
            .padding(top = 72.dp)
            .clip(RoundedCornerShape(100))
            .background(
                color = ExtendedTheme.colors.primary,
                shape = RoundedCornerShape(100)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .align(Alignment.TopCenter)
    ) {
        Text(
            text = stringResource(id = CoreUiR.string.toast_feed_refresh, refreshCount),
            color = ExtendedTheme.colors.onAccent
        )
    }
}

@Immutable
private data class PersonalizedThreadItem(
    val thread: ThreadCard,
    val personalized: PersonalizedInfo? = null,
    val blocked: Boolean = false,
    val hidden: Boolean = false,
)

@Composable
private fun FeedList(
    state: LazyListState,
    dataProvider: () -> ImmutableList<PersonalizedThreadItem>,
    refreshPositionProvider: () -> Int,
    hiddenThreadIdsProvider: () -> ImmutableList<Long>,
    onItemClick: (ThreadCard) -> Unit,
    onItemReplyClick: (ThreadCard) -> Unit,
    onAgree: (ThreadCard) -> Unit,
    onDislike: (ThreadCard, Long, ImmutableList<DislikeReason>) -> Unit,
    onRefresh: () -> Unit,
    onOpenForum: (forumName: String) -> Unit = {},
    onClickUser: (Long) -> Unit = {},
) {
    val data = dataProvider()
    val refreshPosition = refreshPositionProvider()
    val hiddenThreadIds = hiddenThreadIdsProvider()

    MyLazyColumn(
        state = state,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        itemsIndexed(
            items = data,
            key = { _, threadItem -> "${threadItem.thread.threadId}" },
            contentType = { _, threadItem ->
                val thread = threadItem.thread
                when {
                    thread.videoInfo != null -> "Video"
                    thread.medias.size == 1 -> "SingleMedia"
                    thread.medias.size > 1 -> "MultiMedia"
                    else -> "PlainText"
                }
            }
        ) { index, threadItem ->
            val threadHolder = threadItem.thread
            val isHidden =
                remember(
                    hiddenThreadIds,
                    threadHolder,
                    threadItem.hidden
                ) { hiddenThreadIds.contains(threadHolder.threadId) || threadItem.hidden }
            val isRefreshPosition =
                remember(index, refreshPosition) { index + 1 == refreshPosition }
            val isNotLast = remember(index, data.size) { index < data.size - 1 }
            val showDivider = remember(
                isHidden,
                isRefreshPosition,
                isNotLast
            ) { !isHidden && !isRefreshPosition && isNotLast }
            Container {
                AnimatedVisibility(
                    visible = !isHidden,
                    enter = EnterTransition.None,
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        BlockableContent(
                            blocked = threadItem.blocked,
                            blockedTip = { BlockTip(text = { Text(text = stringResource(id = CoreUiR.string.tip_blocked_thread)) }) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                        ) {
                            Column {
                                // TODO: 实现 Repository 的 StateFlow 支持用于更新状态订阅
                                FeedCard(
                                    item = threadHolder,
                                    onClick = onItemClick,
                                    onClickReply = onItemReplyClick,
                                    onAgree = onAgree,
                                    onClickForum = remember {
                                        {
                                            onOpenForum(it)
                                        }
                                    },
                                    onClickUser = onClickUser,
                                    agreeEnabled = true,  // 暂时始终启用，待 Repository 实现
                                ) {
                                    threadItem.personalized?.let { personalized ->
                                        Dislike(
                                            personalized = personalized,
                                            onDislike = { clickTime, reasons ->
                                                onDislike(threadHolder, clickTime, reasons)
                                            }
                                        )
                                    }
                                }
                                if (showDivider) {
                                    VerticalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        thickness = 2.dp
                                    )
                                }
                            }
                        }
                        if (isRefreshPosition) {
                            RefreshTip(onRefresh)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RefreshTip(
    onRefresh: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onRefresh)
            .padding(8.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.Refresh,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(id = CoreUiR.string.tip_refresh),
            style = MaterialTheme.typography.subtitle1
        )
    }
}
