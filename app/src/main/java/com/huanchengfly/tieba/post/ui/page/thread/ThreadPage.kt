package com.huanchengfly.tieba.post.ui.page.thread

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import com.huanchengfly.tieba.core.mvi.GlobalEvent
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.core.mvi.onEvent
import com.huanchengfly.tieba.core.mvi.onGlobalEvent
import com.huanchengfly.tieba.core.mvi.wrapImmutable
import com.huanchengfly.tieba.core.ui.compose.Container
import com.huanchengfly.tieba.core.ui.compose.LazyLoad
import com.huanchengfly.tieba.core.ui.compose.MyPredictiveBackHandler
import com.huanchengfly.tieba.core.ui.compose.SnackbarScaffold
import com.huanchengfly.tieba.core.ui.compose.rememberSnackbarState
import com.huanchengfly.tieba.core.ui.pageViewModel
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.loadMoreIndicator
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.pullRefreshIndicator
import com.huanchengfly.tieba.post.ui.widgets.compose.BlockTip
import com.huanchengfly.tieba.post.ui.widgets.compose.BlockableContent
import com.huanchengfly.tieba.core.ui.widgets.compose.ErrorScreen
import com.huanchengfly.tieba.core.ui.widgets.compose.states.StateScreen
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.models.PostEntity
import com.huanchengfly.tieba.post.models.ThreadHistoryInfoBean
import com.huanchengfly.tieba.post.models.ThreadMeta
import com.huanchengfly.tieba.post.models.database.History
import com.huanchengfly.tieba.post.repository.ThreadPageFrom
import com.huanchengfly.tieba.post.toJson
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.ui.common.PbContentRender
import com.huanchengfly.tieba.post.ui.page.ProvideNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.CopyTextDialogPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.ForumPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.ReplyPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.SubPostsSheetPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.ThreadPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.WebViewPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.UserProfilePageDestination
import com.huanchengfly.tieba.post.ui.page.thread.components.ThreadCollectMarkDialog
import com.huanchengfly.tieba.post.ui.page.thread.components.ThreadDeleteDialog
import com.huanchengfly.tieba.post.ui.page.thread.components.ThreadInfoHeader
import com.huanchengfly.tieba.post.ui.page.thread.components.ThreadJumpToPageDialog
import com.huanchengfly.tieba.post.ui.page.thread.components.ThreadMenuSheetContent
import com.huanchengfly.tieba.post.ui.page.thread.components.ThreadPageTopBar
import com.huanchengfly.tieba.post.ui.page.thread.components.ThreadPostList
import com.huanchengfly.tieba.post.utils.HistoryUtil
import com.huanchengfly.tieba.post.utils.StringUtil
import com.huanchengfly.tieba.post.utils.StringUtil.getShortNumString
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.math.max

@Serializable
sealed interface ThreadPageExtra

@Serializable
object ThreadPageNoExtra : ThreadPageExtra

@Serializable
data class ThreadPageFromStoreExtra(
    val maxPid: Long,
    val maxFloor: Int,
) : ThreadPageExtra

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun ThreadPageLayout(
    navigator: com.ramcosta.composedestinations.navigation.DestinationsNavigator,
    threadId: Long,
    forumId: Long?,
    postId: Long,
    seeLz: Boolean,
    sortType: Int,
    from: String,
    extra: ThreadPageExtra?,
    scrollToReply: Boolean,
    pageState: ThreadPageState,
    dialogs: ThreadPageDialogs,
    snackbarState: com.huanchengfly.tieba.core.ui.compose.SnackbarState,
    actions: ThreadPageActions,
    viewModel: ThreadViewModel
) {
    val updateCollectMarkDialogState = dialogs.updateCollectMarkDialogState
    var readFloorBeforeBack by dialogs.readFloorBeforeBack
    val confirmDeleteDialogState = dialogs.confirmDeleteDialogState
    val jumpToPageDialogState = dialogs.jumpToPageDialogState
    var deletePost by dialogs.deletePost
    var waitLoadSuccessAndScrollToFirstReply by remember { mutableStateOf(scrollToReply) }
    val effectiveThreadId = pageState.threadId.takeIf { it != 0L } ?: threadId

    val lazyListState = rememberLazyListState()
    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val lastVisibilityPost by remember(pageState.postItems, pageState.firstPost, lazyListState) {
        derivedStateOf {
            pageState.postItems.firstOrNull { (post) ->
                val lastPostKey = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull { info ->
                    info.key is String && (info.key as String).startsWith("Post_")
                }?.key as String?
                lastPostKey?.endsWith(post.get { id }.toString()) == true
            }?.post ?: pageState.firstPost
        }
    }
    val lastVisibilityPostId by remember(lastVisibilityPost) {
        derivedStateOf { lastVisibilityPost?.get { id } ?: 0L }
    }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val openBottomSheet = {
        coroutineScope.launch {
            bottomSheetState.show()
        }
    }
    val closeBottomSheet = {
        coroutineScope.launch {
            bottomSheetState.hide()
        }
    }

    MyPredictiveBackHandler(
        enabled = bottomSheetState.isVisible,
        currentScreen = ThreadPageDestination,
        onBack = { closeBottomSheet() }
    )

    viewModel.onEvent<ThreadPageEffect> { effect ->
        when (effect) {
            ThreadPageEffect.ScrollToFirstReply -> {
                lazyListState.animateScrollToItem(1)
            }

            ThreadPageEffect.ScrollToLatestReply -> {
                if (pageState.sortType != ThreadSortType.SORT_TYPE_DESC) {
                    lazyListState.animateScrollToItem(2 + pageState.postItems.size)
                } else {
                    lazyListState.animateScrollToItem(1)
                }
            }

            is ThreadPageEffect.LoadSuccess -> {
                if (effect.page > 1 || waitLoadSuccessAndScrollToFirstReply) {
                    waitLoadSuccessAndScrollToFirstReply = false
                    lazyListState.animateScrollToItem(1)
                }
            }

            is ThreadPageEffect.ShowSnackbar -> {
                snackbarState.showSnackbar(effect.message)
            }

            is ThreadPageEffect.ShowToast -> {
                context.toastShort(effect.message)
                if (effect.navigateUpAfter) {
                    navigator.navigateUp()
                }
            }

            ThreadPageEffect.NavigateUp -> navigator.navigateUp()
        }
    }

    onGlobalEvent<GlobalEvent.ReplySuccess>(
        filter = { it.threadId == effectiveThreadId }
    ) { event ->
        actions.loadMyLatestReply(
            threadId = effectiveThreadId,
            postId = event.newPostId,
            forumId = pageState.curForumId,
            isDesc = pageState.sortType == ThreadSortType.SORT_TYPE_DESC,
            curLatestPostFloor = if (pageState.sortType == ThreadSortType.SORT_TYPE_DESC) {
                pageState.postItems.firstOrNull()?.post?.get { floor } ?: 1
            } else {
                pageState.postItems.lastOrNull()?.post?.get { floor } ?: 1
            },
            curPostIds = pageState.postItems.map { it.post.get { id } }
        )
    }

    ThreadCollectMarkDialog(
        dialogState = updateCollectMarkDialogState,
        readFloorBeforeBack = readFloorBeforeBack,
        lastVisibilityPostId = lastVisibilityPostId,
        pageState = pageState,
        actions = actions,
        navigator = navigator
    )

    MyPredictiveBackHandler(
        enabled = pageState.isCollected && !bottomSheetState.isVisible,
        currentScreen = ThreadPageDestination,
        onBack = {
            readFloorBeforeBack = lastVisibilityPost?.get { floor } ?: 0
            if (readFloorBeforeBack != 0) {
                updateCollectMarkDialogState.show()
            } else {
                navigator.navigateUp()
            }
        }
    )

    ThreadDeleteDialog(
        dialogState = confirmDeleteDialogState,
        pageState = pageState,
        deletePostState = dialogs.deletePost,
        actions = actions
    )

    ThreadJumpToPageDialog(
        dialogState = jumpToPageDialogState,
        pageState = pageState,
        actions = actions,
        forumId = forumId,
        postId = postId
    )

    LaunchedEffect(Unit) {
        val snackbarThreadId = pageState.threadId.takeIf { it != 0L } ?: threadId
        if (from == ThreadPageFrom.FROM_STORE && extra is ThreadPageFromStoreExtra && extra.maxPid != postId) {
            val result = snackbarState.showSnackbarSuspending(
                context.getString(R.string.message_store_thread_update, extra.maxFloor),
                context.getString(R.string.button_load_new),
                SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                actions.load(
                    threadId = snackbarThreadId,
                    page = 0,
                    postId = extra.maxPid,
                    forumId = forumId,
                    seeLz = seeLz,
                    sortType = sortType
                )
            }
        }
    }

    rememberThreadHistorySaver(
        routeThreadId = threadId,
        pageState = pageState,
        lastVisibilityPostId = lastVisibilityPostId,
        lastVisibilityPost = lastVisibilityPost
    )

    val pullRefreshState = rememberPullRefreshState(
        refreshing = pageState.isRefreshing,
        onRefresh = {
            actions.loadFirstPage(
                threadId = effectiveThreadId,
                forumId = pageState.curForumId ?: forumId,
                seeLz = pageState.isSeeLz,
                sortType = pageState.sortType
            )
        }
    )

    ProvideNavigator(navigator = navigator) {
        StateScreen(
            modifier = Modifier.fillMaxSize(),
            isEmpty = pageState.isEmpty,
            isError = pageState.isError,
            isLoading = pageState.isRefreshing,
            errorScreen = {
                pageState.error?.let { (throwable) ->
                    ErrorScreen(error = throwable)
                }
            },
            onReload = {
                actions.load(
                    threadId = effectiveThreadId,
                    page = 0,
                    postId = postId,
                    forumId = forumId,
                    seeLz = pageState.isSeeLz,
                    sortType = pageState.sortType
                )
            }
        ) {
            SnackbarScaffold(
                snackbarState = snackbarState,
                topBar = {
                    ThreadPageTopBar(
                        forum = pageState.forum,
                        onBack = { navigator.navigateUp() },
                        onForumClick = {
                            val forumName = pageState.forum?.get { name }
                            if (!forumName.isNullOrBlank()) {
                                navigator.navigate(ForumPageDestination(forumName))
                            }
                        }
                    )
                },
                bottomBar = {
                    BottomBar(
                        user = pageState.user,
                        pbPageRepository = viewModel.pbPageRepository,
                        threadId = pageState.threadId,
                        onClickReply = {
                            navigator.navigate(
                                ReplyPageDestination(
                                    forumId = pageState.curForumId ?: 0,
                                    forumName = pageState.curForumName.orEmpty(),
                                    threadId = pageState.threadId,
                                )
                            )
                        },
                        onAgree = {
                            val firstPostId =
                                pageState.displayThread?.get { firstPostId }.takeIf { it != 0L }
                                    ?: pageState.firstPost?.get { id }
                                    ?: 0L
                            if (firstPostId != 0L) {
                                actions.agreeThread(
                                    threadId = pageState.threadId,
                                    postId = firstPostId,
                                    agree = !pageState.hasThreadAgreed
                                )
                            }
                        },
                        onClickMore = {
                            if (bottomSheetState.isVisible) {
                                closeBottomSheet()
                            } else {
                                openBottomSheet()
                            }
                        },
                        hasAgreed = pageState.hasThreadAgreed,
                        agreeNum = pageState.threadAgreeNum,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {}
                            )
                    )
                }
            ) { paddingValues ->
                ModalBottomSheetLayout(
                    sheetState = bottomSheetState,
                    sheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                    sheetBackgroundColor = ExtendedTheme.colors.windowBackground,
                    sheetContent = {
                        ThreadMenuSheetContent(
                            pageState = pageState,
                            forumId = forumId,
                            lastVisibilityPost = lastVisibilityPost,
                            bottomSheetState = bottomSheetState,
                            jumpToPageDialogState = jumpToPageDialogState,
                            confirmDeleteDialogState = confirmDeleteDialogState,
                            deletePostState = dialogs.deletePost,
                            actions = actions,
                            navigator = navigator,
                            viewModel = viewModel,
                            coroutineScope = coroutineScope,
                            context = context,
                            closeBottomSheet = { closeBottomSheet() }
                        )
                    },
                    scrimColor = Color.Transparent,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    ThreadPostList(
                        pageState = pageState,
                        actions = actions,
                        viewModel = viewModel,
                        navigator = navigator,
                        forumId = forumId,
                        lazyListState = lazyListState,
                        pullRefreshState = pullRefreshState,
                        deletePostState = dialogs.deletePost,
                        confirmDeleteDialogState = confirmDeleteDialogState,
                        modifier = Modifier.pullRefresh(
                            state = pullRefreshState,
                            enabled = pageState.enablePullRefresh
                        )
                    )
                }
            }
        }
    }
}
