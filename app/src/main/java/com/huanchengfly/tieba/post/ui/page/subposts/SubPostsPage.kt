package com.huanchengfly.tieba.post.ui.page.subposts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.protos.SubPostList
import com.huanchengfly.tieba.core.network.retrofit.doIfFailure
import com.huanchengfly.tieba.core.network.retrofit.doIfSuccess
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.core.mvi.collectPartialAsState
import com.huanchengfly.tieba.core.mvi.onEvent
import com.huanchengfly.tieba.core.ui.pageViewModel
import com.huanchengfly.tieba.core.mvi.wrapImmutable
import com.huanchengfly.tieba.post.components.dialogs.LoadingDialog
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.core.ui.navigation.LocalNavigator
import com.huanchengfly.tieba.core.ui.navigation.ProvideNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.CopyTextDialogPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.ThreadPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.WebViewPageDestination
import com.huanchengfly.tieba.post.ui.page.reply.ReplyArgs
import com.huanchengfly.tieba.post.ui.page.reply.ReplyDialog
import com.huanchengfly.tieba.post.ui.page.subposts.components.SubPostsDeleteDialog
import com.huanchengfly.tieba.core.ui.compose.LazyLoad
import com.huanchengfly.tieba.core.ui.widgets.compose.rememberDialogState
import com.huanchengfly.tieba.post.utils.AccountUtil.LocalAccount
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DestinationStyleBottomSheet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Destination
@Composable
fun SubPostsPage(
    navigator: DestinationsNavigator,
    threadId: Long,
    forumId: Long = 0L,
    postId: Long = 0L,
    subPostId: Long = 0L,
    loadFromSubPost: Boolean = false,
    viewModel: SubPostsViewModel = pageViewModel(),
) {
    ProvideNavigator(navigator) {
        SubPostsContent(
            viewModel = viewModel,
            forumId = forumId,
            threadId = threadId,
            postId = postId,
            subPostId = subPostId,
            loadFromSubPost = loadFromSubPost,
            onNavigateUp = { navigator.navigateUp() },
        )
    }
}

@Destination(
    style = DestinationStyleBottomSheet::class,
)
@Composable
fun SubPostsSheetPage(
    navigator: DestinationsNavigator,
    threadId: Long,
    forumId: Long = 0L,
    postId: Long = 0L,
    subPostId: Long = 0L,
    loadFromSubPost: Boolean = false,
    viewModel: SubPostsViewModel = pageViewModel(),
) {
    ProvideNavigator(navigator) {
        SubPostsContent(
            viewModel = viewModel,
            forumId = forumId,
            threadId = threadId,
            postId = postId,
            subPostId = subPostId,
            loadFromSubPost = loadFromSubPost,
            isSheet = true,
            onNavigateUp = { navigator.navigateUp() },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SubPostsContent(
    viewModel: SubPostsViewModel,
    forumId: Long,
    threadId: Long,
    postId: Long,
    subPostId: Long = 0L,
    loadFromSubPost: Boolean = false,
    isSheet: Boolean = false,
    onNavigateUp: () -> Unit = {},
) {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val account = LocalAccount.current
    val coroutineScope = rememberCoroutineScope()

    LazyLoad(key = viewModel, loaded = viewModel.initialized) {
        viewModel.send(
            SubPostsUiIntent.Load(
                forumId,
                threadId,
                postId,
                subPostId.takeIf { loadFromSubPost } ?: 0L,
            ),
        )
    }

    val isRefreshing by viewModel.uiState.collectPartialAsState(
        prop1 = SubPostsUiState::isRefreshing,
        initial = false,
    )
    val isLoading by viewModel.uiState.collectPartialAsState(
        prop1 = SubPostsUiState::isLoading,
        initial = false,
    )
    val anti by viewModel.uiState.collectPartialAsState(
        prop1 = SubPostsUiState::anti,
        initial = null,
    )
    val forum by viewModel.uiState.collectPartialAsState(
        prop1 = SubPostsUiState::forum,
        initial = null,
    )
    val thread by viewModel.uiState.collectPartialAsState(
        prop1 = SubPostsUiState::thread,
        initial = null,
    )
    val post by viewModel.uiState.collectPartialAsState(
        prop1 = SubPostsUiState::post,
        initial = null,
    )
    val postContentRenders by viewModel.uiState.collectPartialAsState(
        prop1 = SubPostsUiState::postContentRenders,
        initial = persistentListOf(),
    )
    val subPosts by viewModel.uiState.collectPartialAsState(
        prop1 = SubPostsUiState::subPosts,
        initial = persistentListOf(),
    )
    val totalCount by viewModel.uiState.collectPartialAsState(
        prop1 = SubPostsUiState::totalCount,
        initial = 0,
    )
    val hasMore by viewModel.uiState.collectPartialAsState(
        prop1 = SubPostsUiState::hasMore,
        initial = true,
    )

    // 计算有效的 postId：优先使用 API 返回的真实 ID，fallback 到导航参数
    // 这对于从通知跳转的场景至关重要（通知时 postId = 0）
    val effectivePostId = remember(post, postId) {
        post?.get { id } ?: postId
    }

    val lazyListState = rememberLazyListState()

    viewModel.onEvent<SubPostsUiEvent.ScrollToSubPosts> {
        // 仅在请求了特定楼中楼时才滚动
        if (subPostId > 0) {
            delay(20)
            val targetIndex = subPosts.indexOfFirst { it.id == subPostId }
            // 额外检查索引有效性，防止找不到目标
            if (targetIndex >= 0) {
                lazyListState.scrollToItem(2 + targetIndex)
            }
        }
    }

    val confirmDeleteDialogState = rememberDialogState()
    var deleteSubPost by remember { mutableStateOf<ImmutableHolder<SubPostList>?>(null) }

    SubPostsDeleteDialog(
        dialogState = confirmDeleteDialogState,
        postFloor = post?.get { floor } as? Int,
        subPost = deleteSubPost,
        onConfirm = {
            if (deleteSubPost == null) {
                val isSelfPost = post?.get { author_id } == account?.uid?.toLongOrNull()
                viewModel.send(
                    SubPostsUiIntent.DeletePost(
                        forumId = (forum?.get { id } as? Long) ?: forumId,
                        forumName = forum?.get { name }.orEmpty(),
                        threadId = threadId,
                        postId = effectivePostId,
                        deleteMyPost = isSelfPost,
                        tbs = anti?.get { tbs },
                    ),
                )
            } else {
                deleteSubPost?.let { subPost ->
                    val isSelfSubPost = subPost.get { author_id } == account?.uid?.toLongOrNull()
                    viewModel.send(
                        SubPostsUiIntent.DeletePost(
                            forumId = (forum?.get { id } as? Long) ?: forumId,
                            forumName = forum?.get { name }.orEmpty(),
                            threadId = threadId,
                            postId = effectivePostId,
                            subPostId = subPost.get { id },
                            deleteMyPost = isSelfSubPost,
                            tbs = anti?.get { tbs },
                        ),
                    )
                }
            }
        },
    )

    val replyDialogState = rememberDialogState()
    var currentReplyArgs by remember { mutableStateOf<ReplyArgs?>(null) }
    currentReplyArgs?.let { args ->
        ReplyDialog(args = args, state = replyDialogState)
    }

//    onGlobalEvent<GlobalEvent.ReplySuccess>(
//        filter = { it.threadId == threadId && it.postId == postId }
//    ) { event ->
//        viewModel.send(
//            SubPostsUiIntent.Load(
//                forumId,
//                threadId,
//                postId,
//                subPostId.takeIf { loadFromSubPost } ?: 0L
//            )
//        )
//    }

    fun showReplyDialog(args: ReplyArgs) {
        currentReplyArgs = args
        replyDialogState.show()
    }

    // 创建 UI Props 和 Callbacks
    val props =
        rememberSubPostsUiProps(
            forum = forum,
            thread = thread,
            post = post,
            anti = anti,
            postContentRenders = postContentRenders,
            subPosts = subPosts,
            isLoading = isLoading,
            isRefreshing = isRefreshing,
            hasMore = hasMore,
            currentAccount = account,
        )

    val callbacks =
        rememberSubPostsCallbacks(
            viewModel = viewModel,
            navigator = navigator,
            forumId = forumId,
            threadId = threadId,
            postId = effectivePostId,
            onNavigateUp = onNavigateUp,
            onReply = { subPost ->
                handleReplyAction(
                    forum = forum,
                    fallbackForumId = forumId,
                    threadId = threadId,
                    post = post,
                    fallbackPostId = effectivePostId,
                    targetSubPost = subPost,
                    replyUser = subPost?.author,
                    postAuthorIdFallback = post?.get { author_id },
                    onSuccess = ::showReplyDialog,
                    onFailure = { context.toastShort(R.string.toast_forum_info_loading) },
                )
            },
            onShowDeleteDialog = { subPost ->
                deleteSubPost = subPost?.wrapImmutable()
                confirmDeleteDialogState.show()
            },
        )

    // 使用纯 UI 组件 SubPostsScreen
    SubPostsScreen(
        props = props,
        callbacks = callbacks,
        lazyListState = lazyListState,
        isSheet = isSheet,
        postId = effectivePostId,
        totalCount = totalCount,
        onNavigateToThread = {
            navigator.navigate(
                ThreadPageDestination(
                    forumId = forum?.get { id } ?: forumId,
                    threadId = threadId,
                    postId = effectivePostId,
                ),
            )
        },
        onShowReplyDialog = {
            handleReplyAction(
                forum = forum,
                fallbackForumId = forumId,
                threadId = threadId,
                post = post,
                fallbackPostId = effectivePostId,
                postAuthorIdFallback = post?.get { author_id },
                onSuccess = ::showReplyDialog,
                onFailure = { context.toastShort(R.string.toast_forum_info_loading) },
            )
        },
        onReportSubPost = { subPostId ->
            coroutineScope.launch {
                val dialog = LoadingDialog(context).apply { show() }
                viewModel
                    .checkReportPost(subPostId)
                    .doIfSuccess {
                        dialog.dismiss()
                        navigator.navigate(WebViewPageDestination(it.data.url))
                    }.doIfFailure {
                        dialog.dismiss()
                        context.toastShort(R.string.toast_load_failed)
                    }
            }
        },
        onAgreeMainPost = {
            val hasAgreed = post?.get { agree?.hasAgree != 0 } ?: false
            viewModel.send(
                SubPostsUiIntent.Agree(
                    forumId,
                    threadId,
                    effectivePostId,
                    agree = !hasAgreed,
                ),
            )
        },
        onReplyMainPost = {
            handleReplyAction(
                forum = forum,
                fallbackForumId = forumId,
                threadId = threadId,
                post = post,
                fallbackPostId = effectivePostId,
                replyUser = post?.get { author },
                postAuthorIdFallback = post?.get { author_id },
                onSuccess = ::showReplyDialog,
                onFailure = { context.toastShort(R.string.toast_forum_info_loading) },
            )
        },
        onMainPostMenuCopy = { content ->
            navigator.navigate(CopyTextDialogPageDestination(content))
        },
        onMainPostMenuDelete = {
            deleteSubPost = null
            confirmDeleteDialogState.show()
        },
    )
}
