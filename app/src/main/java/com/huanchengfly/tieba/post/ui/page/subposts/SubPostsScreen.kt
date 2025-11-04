package com.huanchengfly.tieba.post.ui.page.subposts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.page.subposts.components.SubPostItem
import com.huanchengfly.tieba.post.ui.page.subposts.components.SubPostsBottomBar
import com.huanchengfly.tieba.post.ui.page.thread.PostCard
import com.huanchengfly.tieba.post.ui.widgets.compose.LoadMoreLayout
import com.huanchengfly.tieba.core.ui.compose.MyLazyColumn
import com.huanchengfly.tieba.core.ui.compose.SnackbarScaffold
import com.huanchengfly.tieba.core.ui.compose.rememberSnackbarState
import com.huanchengfly.tieba.post.ui.widgets.compose.TitleCentredToolbar
import com.huanchengfly.tieba.post.ui.widgets.compose.VerticalDivider
import com.huanchengfly.tieba.post.ui.widgets.compose.states.StateScreen
import com.huanchengfly.tieba.post.utils.StringUtil

/**
 * SubPostsScreen - 纯 UI 组件
 *
 * 完全不依赖 ViewModel，只通过 Props 和 Callbacks 接收数据和处理交互
 *
 * @param props UI 所需的所有状态数据
 * @param callbacks 所有用户交互回调
 * @param lazyListState 列表滚动状态
 * @param isSheet 是否为底部弹窗模式
 * @param postId 主楼 ID（用于 item key）
 * @param totalCount 楼中楼总数（用于显示）
 * @param onNavigateToThread 点击"在原贴查看"按钮的回调
 * @param onShowReplyDialog 显示回复对话框（需要传入构建好的 ReplyArgs）
 * @param onReportSubPost 举报楼中楼（传入 subPostId）
 * @param onAgreeMainPost 主楼点赞回调
 * @param onReplyMainPost 回复主楼回调
 * @param onMainPostMenuCopy 主楼长按菜单-复制
 * @param onMainPostMenuDelete 主楼长按菜单-删除
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SubPostsScreen(
    props: SubPostsUiProps,
    callbacks: SubPostsCallbacks,
    lazyListState: LazyListState,
    isSheet: Boolean,
    postId: Long,
    totalCount: Int,
    onNavigateToThread: () -> Unit,
    onShowReplyDialog: () -> Unit,
    onReportSubPost: (String) -> Unit,
    onAgreeMainPost: () -> Unit,
    onReplyMainPost: () -> Unit,
    onMainPostMenuCopy: (String) -> Unit,
    onMainPostMenuDelete: () -> Unit,
) {
    StateScreen(
        modifier = Modifier.fillMaxSize(),
        isEmpty = props.subPosts.isEmpty(),
        isError = false,
        isLoading = props.isRefreshing,
    ) {
        val snackbarState = rememberSnackbarState()
        SnackbarScaffold(
            snackbarState = snackbarState,
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TitleCentredToolbar(
                    title = {
                        Text(
                            text =
                                props.post?.let {
                                    stringResource(
                                        id = R.string.title_sub_posts,
                                        it.get { floor },
                                    )
                                } ?: stringResource(id = R.string.title_sub_posts_default),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.h6,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = callbacks.onBack) {
                            Icon(
                                imageVector = if (isSheet) Icons.Rounded.Close else Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(id = R.string.btn_close),
                            )
                        }
                    },
                    actions = {
                        if (!isSheet) {
                            IconButton(onClick = onNavigateToThread) {
                                Icon(
                                    imageVector = Icons.Rounded.OpenInBrowser,
                                    contentDescription = stringResource(id = R.string.btn_open_origin_thread),
                                )
                            }
                        }
                    },
                )
            },
            bottomBar = {
                SubPostsBottomBar(
                    visible = props.bottomBarVisible,
                    avatarUrl = StringUtil.getAvatarUrl(props.currentAccount?.portrait),
                    accountName = props.currentAccount?.name.orEmpty(),
                    onReplyClick = onShowReplyDialog,
                )
            },
        ) { paddingValues ->
            LoadMoreLayout(
                modifier = Modifier.padding(paddingValues),
                isLoading = props.isLoading,
                onLoadMore = callbacks.onLoadMore,
                loadEnd = !props.hasMore,
                lazyListState = lazyListState,
                isEmpty = props.post == null && props.subPosts.isEmpty(),
            ) {
                MyLazyColumn(state = lazyListState) {
                    item(key = "Post$postId") {
                        props.post?.let {
                            Column {
                                PostCard(
                                    postHolder = it,
                                    contentRenders = props.postContentRenders,
                                    canDelete = { post -> post.author_id == props.currentAccount?.uid?.toLongOrNull() },
                                    showSubPosts = false,
                                    onUserClick = callbacks.onUserClick,
                                    onAgree = onAgreeMainPost,
                                    onReplyClick = { onReplyMainPost() },
                                    onMenuCopyClick = onMainPostMenuCopy,
                                ) {
                                    onMainPostMenuDelete()
                                }
                                VerticalDivider(thickness = 2.dp)
                            }
                        }
                    }
                    stickyHeader(key = "SubPostsHeader") {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .background(ExtendedTheme.colors.background)
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text =
                                    stringResource(
                                        id = R.string.title_sub_posts_header,
                                        totalCount,
                                    ),
                                style = MaterialTheme.typography.subtitle1,
                            )
                        }
                    }
                    itemsIndexed(
                        items = props.subPosts,
                        key = { _, subPost -> subPost.id },
                    ) { index, item ->
                        SubPostItem(
                            item = item,
                            threadAuthorId = props.threadAuthorId,
                            canDelete = props.canDelete,
                            onUserClick = callbacks.onUserClick,
                            onAgree = callbacks.onAgree,
                            onReplyClick = callbacks.onReplyClick,
                            onMenuCopyClick = callbacks.onMenuCopy,
                            onReportClick = onReportSubPost,
                            onMenuDeleteClick = callbacks.onMenuDelete,
                        )
                    }
                }
            }
        }
    }
}
