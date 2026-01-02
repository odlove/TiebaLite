package com.huanchengfly.tieba.post.ui.page.threadcollect

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.core.common.threadcollect.ThreadCollectItem
import com.huanchengfly.tieba.core.mvi.collectPartialAsState
import com.huanchengfly.tieba.core.mvi.onEvent
import com.huanchengfly.tieba.core.ui.pageViewModel
import com.huanchengfly.tieba.core.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.core.theme.compose.pullRefreshIndicator
import com.huanchengfly.tieba.core.ui.navigation.LocalHomeNavigation
import com.huanchengfly.tieba.core.ui.preferences.LocalAppPreferences
import com.huanchengfly.tieba.post.repository.ThreadPageFrom
import com.huanchengfly.tieba.core.ui.compose.widgets.Avatar
import com.huanchengfly.tieba.post.ui.common.DefaultBackIcon
import com.huanchengfly.tieba.core.ui.compose.widgets.ErrorScreen
import com.huanchengfly.tieba.core.ui.compose.base.LazyLoad
import com.huanchengfly.tieba.core.ui.compose.widgets.LoadMoreLayout
import com.huanchengfly.tieba.core.ui.compose.widgets.LongClickMenu
import com.huanchengfly.tieba.core.ui.compose.base.MyLazyColumn
import com.huanchengfly.tieba.core.ui.compose.base.SnackbarScaffold
import com.huanchengfly.tieba.core.ui.compose.base.rememberSnackbarState
import com.huanchengfly.tieba.core.ui.compose.widgets.Sizes
import com.huanchengfly.tieba.core.theme.compose.scenes.ThemeTopAppBar
import com.huanchengfly.tieba.core.ui.compose.widgets.UserHeader
import com.huanchengfly.tieba.core.ui.compose.widgets.states.StateScreen
import com.huanchengfly.tieba.core.ui.text.StringFormatUtils
import com.huanchengfly.tieba.core.ui.device.dpToPx
import com.huanchengfly.tieba.core.ui.device.pxToSp
import com.huanchengfly.tieba.core.ui.R as CoreUiR
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

private val UpdateTipTextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 10.sp)
private const val SORT_TYPE_DEFAULT = 0
private const val SORT_TYPE_DESC = 1

@OptIn(ExperimentalMaterialApi::class, ExperimentalTextApi::class)
@Destination(
    start = true,
    deepLinks = [
        DeepLink(uriPattern = "tblite://collect")
    ]
)
@Composable
fun ThreadCollectPage(
    navigator: DestinationsNavigator,
    viewModel: ThreadCollectViewModel = pageViewModel()
) {
    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(ThreadCollectUiIntent.Refresh)
        viewModel.initialized = true
    }
    val isRefreshing by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadCollectUiState::isRefreshing,
        initial = false
    )
    val isLoadingMore by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadCollectUiState::isLoadingMore,
        initial = false
    )
    val hasMore by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadCollectUiState::hasMore,
        initial = true
    )
    val currentPage by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadCollectUiState::currentPage,
        initial = 0
    )
    val data by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadCollectUiState::data,
        initial = emptyList()
    )
    val error by viewModel.uiState.collectPartialAsState(
        prop1 = ThreadCollectUiState::error,
        initial = null
    )
    val isError by remember { derivedStateOf { error != null } }

    val context = LocalContext.current
    val homeNavigation = LocalHomeNavigation.current
    val appPreferences = LocalAppPreferences.current
    val snackbarState = rememberSnackbarState()
    viewModel.onEvent<ThreadCollectUiEvent.Delete.Failure> {
        snackbarState.showSnackbar(
            context.getString(
                CoreUiR.string.delete_collect_failure,
                it.errorMsg
            )
        )
    }
    viewModel.onEvent<ThreadCollectUiEvent.Delete.Success> {
        snackbarState.showSnackbar(context.getString(CoreUiR.string.delete_collect_success))
    }
    SnackbarScaffold(
        backgroundColor = Color.Transparent,
        snackbarState = snackbarState,
        topBar = {
            ThemeTopAppBar(
                backgroundColor = ExtendedTheme.colors.topBar,
                contentColor = ExtendedTheme.colors.onTopBar,
                statusBarColor = ExtendedTheme.colors.topBar,
                centerTitle = true,
                title = {
                    Text(
                        text = stringResource(id = CoreUiR.string.title_my_collect),
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.h6
                    )
                },
                navigationIcon = {
                    DefaultBackIcon(onBack = { navigator.navigateUp()  })
                }
            )
        }
    ) { contentPaddings ->
        StateScreen(
            isEmpty = data.isEmpty(),
            isError = isError,
            isLoading = isRefreshing,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPaddings),
            onReload = {
                viewModel.send(ThreadCollectUiIntent.Refresh)
            },
            errorScreen = {
                error?.let {
                    val (e) = it
                    ErrorScreen(error = e)
                }
            }
        ) {
            val pullRefreshState = rememberPullRefreshState(
                refreshing = isRefreshing,
                onRefresh = { viewModel.send(ThreadCollectUiIntent.Refresh) }
            )

            val lazyListState = rememberLazyListState()

            Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
                LoadMoreLayout(
                    isLoading = isLoadingMore,
                    onLoadMore = { viewModel.send(ThreadCollectUiIntent.LoadMore(currentPage + 1)) },
                    loadEnd = !hasMore,
                    lazyListState = lazyListState
                ) {
                    MyLazyColumn(state = lazyListState) {
                        items(
                            items = data,
                            key = { it.threadId }
                        ) { info ->
                            CollectItem(
                                info = info,
                                onUserClick = {
                                    info.author.id?.toLongOrNull()?.let { homeNavigation.openUserProfile(it) }
                                },
                                onClick = {
                                    homeNavigation.openThread(
                                        threadId = info.threadId.toLong(),
                                        postId = info.markPid.toLong(),
                                        seeLz = appPreferences.collectThreadSeeLz,
                                        sortType = if (appPreferences.collectThreadDescSort) SORT_TYPE_DESC else SORT_TYPE_DEFAULT,
                                        from = ThreadPageFrom.FROM_COLLECT
                                    )
                                },
                                onDelete = {
                                    viewModel.send(
                                        ThreadCollectUiIntent.Delete(
                                            info.threadId
                                        )
                                    )
                                }
                            )
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
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun CollectItem(
    info: ThreadCollectItem,
    onUserClick: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val appPreferences = LocalAppPreferences.current
    val hasUpdate = remember(info) { info.count != "0" && info.postNo != "0" }
    val isDeleted = remember(info) { info.isDeleted == "1" }
    val textMeasurer = rememberTextMeasurer()
    LongClickMenu(
        menuContent = {
            DropdownMenuItem(onClick = onDelete) {
                Text(text = stringResource(id = CoreUiR.string.title_collect_on))
            }
        },
        onClick = onClick
    ) {
        Column(
            modifier = modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            UserHeader(
                avatar = {
                    Avatar(
                        data = StringFormatUtils.getAvatarUrl(info.author.portrait),
                        size = Sizes.Small,
                        contentDescription = null
                    )
                },
                name = {
                    Text(
                        text = StringFormatUtils.formatUsernameAnnotated(
                            showBoth = appPreferences.showBothUsernameAndNickname,
                            username = info.author.name ?: "",
                            nickname = info.author.nameShow,
                            color = LocalContentColor.current
                        )
                    )
                },
                onClick = onUserClick,
            )
            val title = remember(info, hasUpdate) {
                buildAnnotatedString {
                    append(info.title)
                    if (hasUpdate) {
                        append(" ")
                        appendInlineContent("Update", info.postNo)
                    }
                }
            }
            val updateTip = stringResource(
                id = CoreUiR.string.tip_thread_collect_update,
                info.postNo
            )
            val result = remember {
                textMeasurer.measure(
                    AnnotatedString(updateTip),
                    style = UpdateTipTextStyle
                ).size
            }
            val width = context.pxToSp(result.width.toFloat() + context.dpToPx(12f) * 2 + 1f)
            val height = context.pxToSp(result.height.toFloat() + context.dpToPx(4f) * 2)
            Text(
                text = title,
                fontSize = 15.sp,
                color = if (isDeleted) ExtendedTheme.colors.textDisabled else ExtendedTheme.colors.text,
                inlineContent = mapOf(
                    "Update" to InlineTextContent(
                        placeholder = Placeholder(
                            width = width.sp,
                            height = height.sp,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                        ),
                        children = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        color = ExtendedTheme.colors.chip,
                                        shape = RoundedCornerShape(3.dp)
                                    )
                                    .padding(vertical = 4.dp, horizontal = 12.dp)
                            ) {
                                Text(
                                    text = updateTip,
                                    style = UpdateTipTextStyle,
                                    color = ExtendedTheme.colors.onChip,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    )
                )
            )
            if (isDeleted) {
                Text(
                    text = stringResource(id = CoreUiR.string.tip_thread_collect_deleted),
                    fontSize = 12.sp,
                    color = ExtendedTheme.colors.textDisabled
                )
            }
        }
    }
}
