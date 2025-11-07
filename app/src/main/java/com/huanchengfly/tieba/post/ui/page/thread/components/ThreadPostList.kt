package com.huanchengfly.tieba.post.ui.page.thread.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.core.mvi.wrapImmutable
import com.huanchengfly.tieba.core.ui.R as CoreUiR
import com.huanchengfly.tieba.core.ui.compose.Container
import com.huanchengfly.tieba.core.ui.compose.MyLazyColumn
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.loadMoreIndicator
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.pullRefreshIndicator
import com.huanchengfly.tieba.core.ui.widgets.compose.Button
import com.huanchengfly.tieba.core.ui.widgets.compose.TipScreen
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.ui.page.destinations.CopyTextDialogPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.ThreadPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.ReplyPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.SubPostsSheetPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.UserProfilePageDestination
import com.huanchengfly.tieba.post.ui.page.thread.ThreadPageActions
import com.huanchengfly.tieba.post.ui.page.thread.ThreadPageState
import com.huanchengfly.tieba.post.ui.page.thread.ThreadSortType
import com.huanchengfly.tieba.post.ui.page.thread.ThreadViewModel
import com.huanchengfly.tieba.post.ui.page.thread.PostCard
import com.huanchengfly.tieba.post.ui.page.thread.components.ThreadInfoHeader
import com.huanchengfly.tieba.post.ui.widgets.compose.DialogState
import com.huanchengfly.tieba.post.ui.widgets.compose.LoadMoreLayout
import com.huanchengfly.tieba.post.ui.widgets.compose.OriginThreadCard
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlin.math.max

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ThreadPostList(
    pageState: ThreadPageState,
    actions: ThreadPageActions,
    viewModel: ThreadViewModel,
    navigator: DestinationsNavigator,
    forumId: Long?,
    lazyListState: LazyListState,
    pullRefreshState: PullRefreshState,
    deletePostState: MutableState<ImmutableHolder<Post>?>,
    confirmDeleteDialogState: DialogState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        LoadMoreLayout(
            isLoading = pageState.isLoadingMore,
            onLoadMore = {
                if (pageState.hasMore) {
                    val nextPage = if (pageState.sortType == ThreadSortType.SORT_TYPE_DESC) {
                        pageState.totalPage - pageState.currentPageMax
                    } else {
                        pageState.currentPageMax + 1
                    }
                    actions.loadMore(
                        threadId = pageState.threadId,
                        page = nextPage,
                        forumId = pageState.curForumId ?: forumId,
                        postId = pageState.nextPagePostId,
                        seeLz = pageState.isSeeLz,
                        sortType = pageState.sortType,
                        postIds = pageState.postItems.map { it.post.get { id } }
                    )
                } else if (pageState.postItems.isNotEmpty() && pageState.sortType != ThreadSortType.SORT_TYPE_DESC) {
                    actions.loadLatestPosts(
                        threadId = pageState.threadId,
                        curLatestPostId = pageState.postItems.last().post.get { id },
                        forumId = pageState.curForumId,
                        seeLz = pageState.isSeeLz,
                        sortType = pageState.sortType
                    )
                }
            },
            loadEnd = pageState.loadMoreEnd,
            indicator = { isLoading, loadMoreEnd, willLoad ->
                ThreadLoadMoreIndicator(
                    isLoading = isLoading,
                    loadMoreEnd = loadMoreEnd,
                    willLoad = willLoad,
                    hasMore = pageState.hasMore
                )
            },
            lazyListState = lazyListState,
            isEmpty = pageState.postItems.isEmpty(),
            preloadCount = pageState.loadMorePreloadCount,
        ) {
            MyLazyColumn(state = lazyListState) {
                threadListContent(
                    pageState = pageState,
                    navigator = navigator,
                    actions = actions,
                    viewModel = viewModel,
                    forumId = forumId,
                    deletePostState = deletePostState,
                    confirmDeleteDialogState = confirmDeleteDialogState,
                    lazyListState = lazyListState,
                )
            }
        }

        PullRefreshIndicator(
            refreshing = pageState.isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = ExtendedTheme.colors.pullRefreshIndicator,
            contentColor = ExtendedTheme.colors.primary,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.threadListContent(
    pageState: ThreadPageState,
    navigator: DestinationsNavigator,
    actions: ThreadPageActions,
    viewModel: ThreadViewModel,
    forumId: Long?,
    deletePostState: MutableState<ImmutableHolder<Post>?>,
    confirmDeleteDialogState: DialogState,
    lazyListState: LazyListState,
) {
    val navigatorInstance = navigator
    val authorId = pageState.author?.get { id } ?: 0L
    val curUserId = pageState.user.get { id }

    fun onDeleteRequest(post: ImmutableHolder<Post>) {
        deletePostState.value = post
        confirmDeleteDialogState.show()
    }

    pageState.firstPost?.let { firstPost ->
        item(key = "FirstPost") {
            Container {
                Column {
                    PostCard(
                        postHolder = firstPost,
                        contentRenders = pageState.firstPostContentRenders,
                        viewModel = viewModel,
                        threadId = pageState.threadId,
                        threadAuthorId = authorId,
                        postEntities = pageState.postEntities,
                        immersiveMode = pageState.isImmersiveMode,
                        canDelete = { it.author_id == curUserId },
                        isCollected = { pageState.threadMeta.collectStatus == 1 && it.id == pageState.threadMeta.collectMarkPid },
                        showSubPosts = false,
                        onUserClick = { navigatorInstance.navigate(UserProfilePageDestination(it.id)) },
                        onAgree = {
                            val firstPostId = firstPost.get { id }
                            actions.agreeThread(pageState.threadId, firstPostId, !pageState.hasThreadAgreed)
                        },
                        onReplyClick = {
                            navigatorInstance.navigate(
                                ReplyPageDestination(
                                    forumId = pageState.curForumId ?: 0,
                                    forumName = pageState.curForumName.orEmpty(),
                                    threadId = pageState.threadId,
                                )
                            )
                        },
                        onMenuCopyClick = { text ->
                            navigatorInstance.navigate(CopyTextDialogPageDestination(text))
                        },
                        onMenuFavoriteClick = {
                            actions.addFavorite(
                                threadId = pageState.threadId,
                                postId = it.id,
                                floor = it.floor
                            )
                        },
                        onMenuDeleteClick = { post ->
                            onDeleteRequest(post.wrapImmutable())
                        }
                    )

                    pageState.displayThread?.getNullableImmutable { origin_thread_info }
                        .takeIf { pageState.displayThread?.get { is_share_thread } == 1 }
                        ?.let { originThreadInfo ->
                            val originThreadId = originThreadInfo.get { tid }.toLongOrNull()
                            var originThreadModifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ExtendedTheme.colors.floorCard)
                            if (originThreadId != null) {
                                originThreadModifier = originThreadModifier.clickable {
                                    navigatorInstance.navigate(
                                        ThreadPageDestination(
                                            originThreadId,
                                            forumId = originThreadInfo.get { fid }
                                        )
                                    )
                                }
                            }

                            OriginThreadCard(
                                originThreadInfo = originThreadInfo,
                                modifier = originThreadModifier.padding(16.dp)
                            )
                        }
                }
            }
        }
    }

    stickyHeader(key = "ThreadInfoHeader") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.background)
        ) {
            ThreadInfoHeader(
                pageState = pageState,
                onAllClick = {
                    actions.loadFirstPage(
                        threadId = pageState.threadId,
                        forumId = pageState.curForumId ?: forumId,
                        seeLz = false,
                        sortType = pageState.sortType
                    )
                },
                onSeeLzClick = {
                    actions.loadFirstPage(
                        threadId = pageState.threadId,
                        forumId = pageState.curForumId ?: forumId,
                        seeLz = true,
                        sortType = pageState.sortType
                    )
                }
            )
        }
    }

    if (pageState.sortType == ThreadSortType.SORT_TYPE_DESC) {
        latestPostsSection(
            desc = true,
            pageState = pageState,
            actions = actions,
            navigator = navigatorInstance,
            viewModel = viewModel,
            deletePostState = deletePostState,
            confirmDeleteDialogState = confirmDeleteDialogState
        )
    }

    if (pageState.hasPrevious) {
        item("LoadPrevious") {
            Container {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            actions.loadPrevious(
                                threadId = pageState.threadId,
                                page = max(pageState.currentPageMax - 1, 1),
                                forumId = pageState.curForumId ?: forumId,
                                postId = pageState.postItems.firstOrNull()?.post?.get { id } ?: 0L,
                                seeLz = pageState.isSeeLz,
                                sortType = pageState.sortType,
                                postIds = pageState.postItems.map { it.post.get { id } }
                            )
                        }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.btn_load_previous),
                        style = MaterialTheme.typography.body2.copy(color = ExtendedTheme.colors.accent)
                    )
                }
            }
        }
    }

    val shouldShowEmptyRepliesTip =
        pageState.firstPost != null &&
            pageState.postItems.isEmpty() &&
            !pageState.isRefreshing &&
            !pageState.isLoadingMore

    if (shouldShowEmptyRepliesTip) {
        item(key = "ThreadEmptyRepliesTip") {
            TipScreen(
                title = { Text(text = stringResource(id = R.string.tip_thread_no_replies)) },
                image = {
                    val composition by rememberLottieComposition(
                        LottieCompositionSpec.RawRes(CoreUiR.raw.lottie_empty_box)
                    )
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f)
                    )
                },
                actions = {
                    Button(
                        onClick = {
                            actions.loadFirstPage(
                                threadId = pageState.threadId,
                                forumId = pageState.curForumId ?: forumId,
                                seeLz = pageState.isSeeLz,
                                sortType = pageState.sortType
                            )
                        }
                    ) {
                        Text(text = stringResource(id = CoreUiR.string.btn_refresh))
                    }
                },
                scrollable = false,
            )
        }
    } else {
        items(
            items = pageState.postItems,
            key = { data -> "Post_${data.post.get { id }}" }
        ) { item ->
            Container {
                PostCard(
                    postHolder = item.post,
                    contentRenders = item.contentRenders,
                    viewModel = viewModel,
                    threadId = pageState.threadId,
                    subPosts = item.subPosts,
                    threadAuthorId = authorId,
                    blocked = item.blocked,
                    postEntities = pageState.postEntities,
                    immersiveMode = pageState.isImmersiveMode,
                    canDelete = { it.author_id == curUserId },
                    isCollected = { pageState.threadMeta.collectStatus == 1 && it.id == pageState.threadMeta.collectMarkPid },
                    onUserClick = { navigatorInstance.navigate(UserProfilePageDestination(it.id)) },
                    onAgree = {
                        val meta = pageState.postEntities.find { entity -> entity.id == item.post.get { id } }?.meta
                        val postHasAgreed = meta?.hasAgree == 1 || item.post.get { agree?.hasAgree == 1 }
                        actions.agreePost(
                            threadId = pageState.threadId,
                            postId = item.post.get { id },
                            agree = !postHasAgreed
                        )
                    },
                    onReplyClick = { post ->
                        navigatorInstance.navigate(
                            ReplyPageDestination(
                                forumId = pageState.curForumId ?: 0,
                                forumName = pageState.curForumName.orEmpty(),
                                threadId = pageState.threadId,
                                postId = post.id,
                                replyUserId = post.author?.id ?: post.author_id,
                                replyUserName = post.author?.nameShow.takeIf { !it.isNullOrEmpty() } ?: post.author?.name,
                                replyUserPortrait = post.author?.portrait,
                            )
                        )
                    },
                    onSubPostReplyClick = { post, subPost ->
                        navigatorInstance.navigate(
                            ReplyPageDestination(
                                forumId = pageState.curForumId ?: 0,
                                forumName = pageState.curForumName.orEmpty(),
                                threadId = pageState.threadId,
                                postId = post.id,
                                subPostId = subPost.id,
                                replyUserId = subPost.author?.id ?: subPost.author_id,
                                replyUserName = subPost.author?.nameShow.takeIf { !it.isNullOrEmpty() } ?: subPost.author?.name,
                                replyUserPortrait = subPost.author?.portrait,
                            )
                        )
                    },
                    onOpenSubPosts = { subPostId ->
                        val forumValue = pageState.curForumId ?: forumId
                        if (forumValue != null) {
                            navigatorInstance.navigate(
                                SubPostsSheetPageDestination(
                                    forumId = forumValue,
                                    threadId = pageState.threadId,
                                    postId = item.post.get { id },
                                    subPostId = subPostId,
                                    loadFromSubPost = false
                                )
                            )
                        }
                    },
                    onMenuCopyClick = { text ->
                        navigatorInstance.navigate(CopyTextDialogPageDestination(text))
                    },
                    onMenuFavoriteClick = { post ->
                        val isPostCollected = pageState.threadMeta.collectStatus == 1 && post.id == pageState.threadMeta.collectMarkPid
                        val fid = pageState.curForumId ?: forumId
                        val tbs = pageState.curTbs
                        if (fid != null) {
                            if (isPostCollected) {
                                actions.removeFavorite(
                                    threadId = pageState.threadId,
                                    forumId = fid,
                                    tbs = tbs
                                )
                            } else {
                                actions.addFavorite(
                                    threadId = pageState.threadId,
                                    postId = post.id,
                                    floor = post.floor
                                )
                            }
                        }
                    },
                    onMenuDeleteClick = { post -> onDeleteRequest(post.wrapImmutable()) }
                )
            }
        }
    }

    if (pageState.sortType != ThreadSortType.SORT_TYPE_DESC) {
        latestPostsSection(
            desc = false,
            pageState = pageState,
            actions = actions,
            navigator = navigatorInstance,
            viewModel = viewModel,
            deletePostState = deletePostState,
            confirmDeleteDialogState = confirmDeleteDialogState
        )
    }
}

private fun LazyListScope.latestPostsSection(
    desc: Boolean,
    pageState: ThreadPageState,
    actions: ThreadPageActions,
    navigator: DestinationsNavigator,
    viewModel: ThreadViewModel,
    deletePostState: MutableState<ImmutableHolder<Post>?>,
    confirmDeleteDialogState: DialogState,
) {
    if (pageState.latestPosts.isEmpty()) return

    val headerKey = if (desc) "LatestPostsTipTop" else "LatestPostsTipBottom"
    val footerKey = if (desc) "LatestPostsTipBottom" else "LatestPostsTipTop"

    if (!desc) {
        item(headerKey) {
            LatestPostDivider(text = R.string.below_is_latest_post)
        }
    }

    items(
        items = pageState.latestPosts,
        key = { data -> "LatestPost_${data.post.get { id }}" }
    ) { item ->
        val postHolder = item.post
        Container {
            PostCard(
                postHolder = postHolder,
                contentRenders = item.contentRenders,
                viewModel = viewModel,
                threadId = pageState.threadId,
                subPosts = item.subPosts,
                threadAuthorId = pageState.author?.get { id } ?: 0L,
                blocked = item.blocked,
                postEntities = pageState.postEntities,
                immersiveMode = pageState.isImmersiveMode,
                canDelete = { it.author_id == pageState.user.get { id } },
                isCollected = { pageState.threadMeta.collectStatus == 1 && it.id == pageState.threadMeta.collectMarkPid },
                onUserClick = { navigator.navigate(UserProfilePageDestination(it.id)) },
                onAgree = {
                    val meta = pageState.postEntities.find { entity -> entity.id == postHolder.get { id } }?.meta
                    val postHasAgreed = meta?.hasAgree == 1 || postHolder.get { agree?.hasAgree == 1 }
                    actions.agreePost(
                        threadId = pageState.threadId,
                        postId = postHolder.get { id },
                        agree = !postHasAgreed
                    )
                },
                onReplyClick = { targetPost ->
                    navigator.navigate(
                        ReplyPageDestination(
                            forumId = pageState.curForumId ?: 0,
                            forumName = pageState.curForumName.orEmpty(),
                            threadId = pageState.threadId,
                            postId = targetPost.id,
                            replyUserId = targetPost.author?.id ?: targetPost.author_id,
                            replyUserName = targetPost.author?.nameShow.takeIf { !it.isNullOrEmpty() } ?: targetPost.author?.name,
                            replyUserPortrait = targetPost.author?.portrait,
                        )
                    )
                },
                onSubPostReplyClick = { parent, subPost ->
                    navigator.navigate(
                        ReplyPageDestination(
                            forumId = pageState.curForumId ?: 0,
                            forumName = pageState.curForumName.orEmpty(),
                            threadId = pageState.threadId,
                            postId = parent.id,
                            subPostId = subPost.id,
                            replyUserId = subPost.author?.id ?: subPost.author_id,
                            replyUserName = subPost.author?.nameShow.takeIf { !it.isNullOrEmpty() } ?: subPost.author?.name,
                            replyUserPortrait = subPost.author?.portrait,
                        )
                    )
                },
                onOpenSubPosts = { subPostId ->
                    val forumValue = pageState.curForumId
                    if (forumValue != null) {
                        navigator.navigate(
                            SubPostsSheetPageDestination(
                                forumId = forumValue,
                                threadId = pageState.threadId,
                                postId = postHolder.get { id },
                                subPostId = subPostId,
                                loadFromSubPost = false
                            )
                        )
                    }
                },
                onMenuCopyClick = { text -> navigator.navigate(CopyTextDialogPageDestination(text)) },
                onMenuFavoriteClick = { favouritePost ->
                    val isPostCollected = pageState.threadMeta.collectStatus == 1 && favouritePost.id == pageState.threadMeta.collectMarkPid
                    val fid = pageState.curForumId
                    val tbs = pageState.curTbs
                    if (fid != null) {
                        if (isPostCollected) {
                            actions.removeFavorite(pageState.threadId, fid, tbs)
                        } else {
                            actions.addFavorite(pageState.threadId, favouritePost.id, favouritePost.floor)
                        }
                    }
                },
                onMenuDeleteClick = { post ->
                    deletePostState.value = post.wrapImmutable()
                    confirmDeleteDialogState.show()
                }
            )
        }
    }

    if (desc) {
        item(footerKey) {
            LatestPostDivider(text = R.string.above_is_latest_post)
        }
    }
}

@Composable
private fun LatestPostDivider(text: Int) {
    Container {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material.Divider(
                modifier = Modifier.weight(1f),
                color = ExtendedTheme.colors.divider
            )
            Text(
                text = stringResource(id = text),
                color = ExtendedTheme.colors.textSecondary,
                style = MaterialTheme.typography.caption,
            )
            androidx.compose.material.Divider(
                modifier = Modifier.weight(1f),
                color = ExtendedTheme.colors.divider
            )
        }
    }
}

@Composable
private fun ThreadLoadMoreIndicator(
    isLoading: Boolean,
    loadMoreEnd: Boolean,
    willLoad: Boolean,
    hasMore: Boolean,
) {
    Surface(
        elevation = 8.dp,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(100),
        color = ExtendedTheme.colors.loadMoreIndicator,
        contentColor = ExtendedTheme.colors.text
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(10.dp)
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProvideTextStyle(value = MaterialTheme.typography.body2.copy(fontSize = 13.sp)) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp,
                            color = ExtendedTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.text_loading),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    loadMoreEnd -> {
                        Text(
                            text = stringResource(id = R.string.no_more),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    hasMore -> {
                        Text(
                            text = if (willLoad) stringResource(id = R.string.release_to_load) else stringResource(
                                id = R.string.pull_to_load
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    else -> {
                        Text(
                            text = if (willLoad) stringResource(id = R.string.release_to_load_latest_posts) else stringResource(
                                id = R.string.pull_to_load_latest_posts
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
