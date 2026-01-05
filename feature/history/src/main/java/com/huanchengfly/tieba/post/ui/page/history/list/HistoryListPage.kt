package com.huanchengfly.tieba.post.ui.page.history.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.feature.history.R
import com.huanchengfly.tieba.core.mvi.collectPartialAsState
import com.huanchengfly.tieba.core.mvi.onEvent
import com.huanchengfly.tieba.core.mvi.onGlobalEvent
import com.huanchengfly.tieba.core.ui.pageViewModel
import com.huanchengfly.tieba.post.fromJson
import com.huanchengfly.tieba.core.common.history.ThreadHistoryInfoBean
import com.huanchengfly.tieba.core.common.history.HistoryItem
import com.huanchengfly.tieba.core.common.history.HistoryRepository
import com.huanchengfly.tieba.core.ui.compose.widgets.CardSurface
import com.huanchengfly.tieba.core.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.compose.widgets.Avatar
import com.huanchengfly.tieba.core.ui.compose.widgets.Chip
import com.huanchengfly.tieba.core.ui.compose.base.LazyLoad
import com.huanchengfly.tieba.core.ui.compose.widgets.LoadMoreLayout
import com.huanchengfly.tieba.core.ui.compose.base.LocalSnackbarState
import com.huanchengfly.tieba.core.ui.compose.widgets.LongClickMenu
import com.huanchengfly.tieba.core.ui.compose.base.MyLazyColumn
import com.huanchengfly.tieba.core.ui.compose.widgets.Sizes
import com.huanchengfly.tieba.core.ui.compose.widgets.UserHeader
import com.huanchengfly.tieba.core.ui.compose.widgets.rememberMenuState
import com.huanchengfly.tieba.core.common.utils.DateTimeUtils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryListPage(
    type: Int,
    viewModel: HistoryListViewModel = if (type == HistoryRepository.TYPE_THREAD) pageViewModel<ThreadHistoryListViewModel>() else pageViewModel<ForumHistoryListViewModel>(),
    onOpenForum: (String) -> Unit = {},
    onOpenThread: (threadId: Long, postId: Long, seeLz: Boolean) -> Unit = { _, _, _ -> }
) {
    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(HistoryListUiIntent.Refresh)
        viewModel.initialized = true
    }
    onGlobalEvent<HistoryListUiEvent.DeleteAll> {
        viewModel.send(HistoryListUiIntent.DeleteAll)
    }
    val isLoadingMore by viewModel.uiState.collectPartialAsState(
        prop1 = HistoryListUiState::isLoadingMore,
        initial = false
    )
    val hasMore by viewModel.uiState.collectPartialAsState(
        prop1 = HistoryListUiState::hasMore,
        initial = true
    )
    val currentPage by viewModel.uiState.collectPartialAsState(
        prop1 = HistoryListUiState::currentPage,
        initial = 0
    )
    val todayHistoryData by viewModel.uiState.collectPartialAsState(
        prop1 = HistoryListUiState::todayHistoryData,
        initial = emptyList()
    )
    val beforeHistoryData by viewModel.uiState.collectPartialAsState(
        prop1 = HistoryListUiState::beforeHistoryData,
        initial = emptyList()
    )

    val context = LocalContext.current
    val snackbarState = LocalSnackbarState.current
    viewModel.onEvent<HistoryListUiEvent.Delete.Failure> {
        snackbarState.showSnackbar(
            context.getString(
                R.string.delete_history_failure,
                it.errorMsg
            )
        )
    }
    viewModel.onEvent<HistoryListUiEvent.Delete.Success> {
        snackbarState.showSnackbar(context.getString(R.string.delete_history_success))
    }
    val lazyListState = rememberLazyListState()
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LoadMoreLayout(
            isLoading = isLoadingMore,
            onLoadMore = { viewModel.send(HistoryListUiIntent.LoadMore(currentPage + 1)) },
            loadEnd = !hasMore,
            lazyListState = lazyListState,
            isEmpty = todayHistoryData.isEmpty() && beforeHistoryData.isEmpty()
        ) {
            MyLazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                state = lazyListState
            ) {
                if (todayHistoryData.isNotEmpty()) {
                    stickyHeader(key = "TodayHistoryHeader") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ExtendedTheme.colors.background)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Chip(
                                text = stringResource(id = R.string.title_history_today),
                                invertColor = true
                            )
                        }
                    }
                    items(
                        items = todayHistoryData,
                        key = { it.id }
                    ) { info ->
                        HistoryListItem(
                            info,
                            onDelete = {
                                viewModel.send(HistoryListUiIntent.Delete(it.id))
                            },
                            onClick = {
                                when (it.type) {
                                    HistoryRepository.TYPE_FORUM -> {
                                        onOpenForum(it.data)
                                    }

                                    HistoryRepository.TYPE_THREAD -> {
                                        val extrasJson = it.extras
                                        val extra = extrasJson?.fromJson<ThreadHistoryInfoBean>()
                                        onOpenThread(
                                            it.data.toLong(),
                                            extra?.pid?.toLongOrNull() ?: 0L,
                                            extra?.isSeeLz ?: false
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
                if (beforeHistoryData.isNotEmpty()) {
                    stickyHeader(key = "BeforeHistoryHeader") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ExtendedTheme.colors.background)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Chip(text = stringResource(id = R.string.title_history_before))
                        }
                    }
                    items(
                        items = beforeHistoryData,
                        key = { it.id }
                    ) { info ->
                        HistoryListItem(
                            info,
                            onDelete = {
                                viewModel.send(HistoryListUiIntent.Delete(it.id))
                            },
                            onClick = {
                                when (it.type) {
                                    HistoryRepository.TYPE_FORUM -> {
                                        onOpenForum(it.data)
                                    }

                                    HistoryRepository.TYPE_THREAD -> {
                                        val extrasJson = it.extras
                                        val extra = extrasJson?.fromJson<ThreadHistoryInfoBean>()
                                        onOpenThread(
                                            it.data.toLong(),
                                            extra?.pid?.toLongOrNull() ?: 0L,
                                            extra?.isSeeLz ?: false
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryListItem(
    info: HistoryItem,
    modifier: Modifier = Modifier,
    onClick: (HistoryItem) -> Unit = {},
    onDelete: (HistoryItem) -> Unit = {},
) {
    val menuState = rememberMenuState()
    LongClickMenu(
        menuContent = {
            DropdownMenuItem(onClick = {
                onDelete(info)
                menuState.expanded = false
            }) {
                Text(text = stringResource(id = R.string.title_delete))
            }
        },
        menuState = menuState,
        onClick = { onClick(info) }
    ) {
        CardSurface(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            plain = true
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                UserHeader(
                    avatar = {
                        Avatar(
                            data = info.avatar,
                            size = Sizes.Small,
                            contentDescription = null
                        )
                    },
                    name = {
                        Text(
                            text = (if (info.type == HistoryRepository.TYPE_THREAD) info.username else info.title)
                                ?: ""
                        )
                    },
                ) {
                    Text(
                        text = DateTimeUtils.getRelativeTimeString(
                            LocalContext.current,
                            info.timestamp
                        ),
                        fontSize = 15.sp,
                        color = ExtendedTheme.colors.text,
                    )
                }
                if (info.type == HistoryRepository.TYPE_THREAD) {
                    Text(
                        text = info.title,
                        fontSize = 15.sp,
                        color = ExtendedTheme.colors.text,
                    )
                }
            }
        }
    }
}
