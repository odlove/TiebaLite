package com.huanchengfly.tieba.post.ui.page.subposts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.huanchengfly.tieba.feature.subposts.R
import com.huanchengfly.tieba.core.common.thread.ThreadPost
import com.huanchengfly.tieba.core.common.thread.ThreadUser
import com.huanchengfly.tieba.core.common.utils.DateTimeUtils
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.core.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.text.StringFormatUtils
import com.huanchengfly.tieba.post.ui.page.subposts.components.SubPostItem
import com.huanchengfly.tieba.post.ui.page.subposts.components.SubPostsBottomBar
import com.huanchengfly.tieba.post.ui.page.subposts.components.SubPostsUserNameText
import com.huanchengfly.tieba.post.ui.common.PbContentRender
import com.huanchengfly.tieba.core.ui.compose.widgets.LoadMoreLayout
import com.huanchengfly.tieba.core.ui.compose.base.MyLazyColumn
import com.huanchengfly.tieba.core.ui.compose.base.SnackbarScaffold
import com.huanchengfly.tieba.core.ui.compose.base.rememberSnackbarState
import com.huanchengfly.tieba.core.ui.compose.widgets.ThemeTopAppBar
import com.huanchengfly.tieba.core.ui.compose.widgets.AgreeButton
import com.huanchengfly.tieba.core.ui.compose.widgets.AgreeButtonVariant
import com.huanchengfly.tieba.core.ui.compose.widgets.Avatar
import com.huanchengfly.tieba.core.ui.compose.widgets.LongClickMenu
import com.huanchengfly.tieba.core.ui.compose.widgets.PlainCard
import com.huanchengfly.tieba.core.ui.compose.widgets.Sizes
import com.huanchengfly.tieba.core.ui.compose.widgets.UserHeader
import com.huanchengfly.tieba.core.ui.compose.widgets.rememberMenuState
import com.huanchengfly.tieba.core.ui.compose.widgets.VerticalDivider
import com.huanchengfly.tieba.core.ui.compose.widgets.states.StateScreen
import com.huanchengfly.tieba.post.preferences.appPreferences
import com.huanchengfly.tieba.post.utils.compose.calcStatusBarColor

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
 * @param onShowReplyDialog 触发回复入口（外层会导航到底部 ReplyPage）
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
        val extendedColors = ExtendedTheme.colors
        val scaffoldBackground = remember(isSheet, extendedColors.background, extendedColors.isTranslucent) {
            if (isSheet && extendedColors.isTranslucent) {
                extendedColors.background.copy(alpha = 0.8f)
            } else {
                extendedColors.background
            }
        }
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
                backgroundColor = scaffoldBackground,
                contentColor = extendedColors.text,
                topBar = {
                    val topBarColor = ExtendedTheme.colors.topBar
                    val statusBarColor = topBarColor.calcStatusBarColor()
                ThemeTopAppBar(
                    backgroundColor = topBarColor,
                    statusBarColor = statusBarColor,
                    centerTitle = true,
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
                    avatarUrl = StringFormatUtils.getAvatarUrl(props.currentAccount?.portrait),
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
                                SubPostsMainPostCard(
                                    postHolder = it,
                                    contentRenders = props.postContentRenders,
                                    threadAuthorId = props.threadAuthorId,
                                    canDelete = { post -> post.authorId == props.currentAccount?.uid?.toLongOrNull() },
                                    onUserClick = { user -> callbacks.onUserClick(user.id) },
                                    onAgree = onAgreeMainPost,
                                    onReplyClick = { onReplyMainPost() },
                                    onMenuCopyClick = onMainPostMenuCopy,
                                    onMenuDeleteClick = { onMainPostMenuDelete() },
                                )
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

@Composable
private fun SubPostsMainPostCard(
    postHolder: ImmutableHolder<ThreadPost>,
    contentRenders: List<PbContentRender>,
    threadAuthorId: Long?,
    canDelete: (ThreadPost) -> Boolean,
    onUserClick: (ThreadUser) -> Unit,
    onAgree: () -> Unit,
    onReplyClick: (ThreadPost) -> Unit,
    onMenuCopyClick: ((String) -> Unit)?,
    onMenuDeleteClick: (() -> Unit)?,
) {
    val context = LocalContext.current
    val post = remember(postHolder) { postHolder.get() }
    val author = post.author ?: return
    val menuState = rememberMenuState()
    val hasAgreed = post.agree?.hasAgree == 1
    val agreeNum = ((post.agree?.diffAgreeNum ?: post.agree?.agreeNum) ?: 0L).toInt()
    val desc = remember(postHolder) {
        val texts = listOfNotNull(
            DateTimeUtils.getRelativeTimeString(context, post.time),
            post.floor.takeIf { it > 0 }?.let { context.getString(R.string.tip_post_floor, it) }
        )
        texts.joinToString(" · ")
    }

    LongClickMenu(
        menuState = menuState,
        indication = null,
        menuContent = {
            if (onMenuCopyClick != null) {
                DropdownMenuItem(
                    onClick = {
                        onMenuCopyClick(contentRenders.joinToString("\n") { it.toString() })
                        menuState.expanded = false
                    },
                ) {
                    Text(text = stringResource(id = R.string.menu_copy))
                }
            }
            if (onMenuDeleteClick != null && canDelete(post)) {
                DropdownMenuItem(
                    onClick = {
                        onMenuDeleteClick()
                        menuState.expanded = false
                    },
                ) {
                    Text(text = stringResource(id = R.string.title_delete))
                }
            }
        },
        onClick = { onReplyClick(post) }.takeUnless { context.appPreferences.hideReply },
    ) {
        PlainCard(
            header = {
                UserHeader(
                    avatar = {
                        Avatar(
                            data = StringFormatUtils.getAvatarUrl(author.portrait),
                            size = Sizes.Small,
                            contentDescription = null,
                        )
                    },
                    name = {
                        SubPostsUserNameText(
                            userName = StringFormatUtils.formatUsernameAnnotated(
                                context.appPreferences.showBothUsernameAndNickname,
                                author.name,
                                author.nameShow,
                            ),
                            userLevel = author.levelId,
                            isLz = author.id == threadAuthorId,
                            bawuType = author.bawuType,
                        )
                    },
                    desc = {
                        Text(text = desc)
                    },
                    onClick = { onUserClick(author) },
                ) {
                    AgreeButton(
                        hasAgreed = hasAgreed,
                        agreeNum = agreeNum,
                        onClick = onAgree,
                        variant = AgreeButtonVariant.PostDetail,
                    )
                }
            },
            content = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .padding(start = Sizes.Small + 8.dp)
                        .fillMaxWidth(),
                ) {
                    contentRenders.fastForEach { it.Render() }
                }
            },
        )
    }
}
