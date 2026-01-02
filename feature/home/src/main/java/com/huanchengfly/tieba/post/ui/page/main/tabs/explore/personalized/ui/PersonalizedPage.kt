package com.huanchengfly.tieba.post.ui.page.main.tabs.explore.personalized.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.huanchengfly.tieba.core.common.thread.toThreadPreview
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.bindScrollToTopEvent
import com.huanchengfly.tieba.core.mvi.collectPartialAsState
import com.huanchengfly.tieba.core.mvi.onEvent
import com.huanchengfly.tieba.core.mvi.onGlobalEvent
import com.huanchengfly.tieba.core.ui.compose.base.LazyLoad
import com.huanchengfly.tieba.core.ui.navigation.LocalHomeNavigation
import com.huanchengfly.tieba.core.ui.pageViewModel
import com.huanchengfly.tieba.core.ui.preferences.LocalAppPreferences
import com.huanchengfly.tieba.core.ui.compose.widgets.ErrorScreen
import com.huanchengfly.tieba.core.ui.compose.widgets.states.StateScreen
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.personalized.contract.PersonalizedUiEvent
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.personalized.contract.PersonalizedUiIntent
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.personalized.contract.PersonalizedUiState
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.personalized.ui.components.PersonalizedThreadItem
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.personalized.ui.sections.PersonalizedContentSection
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.personalized.viewmodel.PersonalizedViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay

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
        PersonalizedContentSection(
            pullRefreshState = pullRefreshState,
            lazyListState = lazyListState,
            isRefreshing = isRefreshing,
            isLoadingMore = isLoadingMore,
            isEmpty = displayData.isEmpty(),
            displayData = displayData,
            refreshPosition = refreshPosition,
            hiddenThreadIds = hiddenThreadIds,
            showRefreshTip = showRefreshTip,
            refreshCount = refreshCount,
            onLoadMore = { viewModel.send(PersonalizedUiIntent.LoadMore(currentPage + 1)) },
            onRefresh = { viewModel.send(PersonalizedUiIntent.Refresh) },
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
            onOpenForum = { homeNavigation.openForum(it) },
            onClickUser = { homeNavigation.openUserProfile(it) }
        )
    }
}
