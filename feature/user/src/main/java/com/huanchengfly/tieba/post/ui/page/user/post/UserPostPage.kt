package com.huanchengfly.tieba.post.ui.page.user.post

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.huanchengfly.tieba.feature.user.R
import com.huanchengfly.tieba.core.ui.R as CoreUiR
import com.huanchengfly.tieba.core.common.feed.OriginThreadCard
import com.huanchengfly.tieba.core.common.feed.ThreadAuthor
import com.huanchengfly.tieba.core.common.feed.ThreadCard
import com.huanchengfly.tieba.core.common.user.UserPostItem
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.collectPartialAsState
import com.huanchengfly.tieba.core.mvi.getOrNull
import com.huanchengfly.tieba.core.mvi.onGlobalEvent
import com.huanchengfly.tieba.core.ui.pageViewModel
import com.huanchengfly.tieba.core.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.core.theme.compose.pullRefreshIndicator
import com.huanchengfly.tieba.core.ui.navigation.LocalHomeNavigation
import com.huanchengfly.tieba.core.ui.compose.widgets.Button
import com.huanchengfly.tieba.core.ui.compose.base.Container
import com.huanchengfly.tieba.core.ui.compose.widgets.ErrorScreen
import com.huanchengfly.tieba.core.ui.compose.widgets.FeedCard
import com.huanchengfly.tieba.core.ui.compose.widgets.FeedCardPlaceholder
import com.huanchengfly.tieba.core.ui.compose.widgets.FilledCard
import com.huanchengfly.tieba.core.ui.compose.base.LazyLoad
import com.huanchengfly.tieba.core.ui.compose.widgets.LoadMoreLayout
import com.huanchengfly.tieba.core.ui.compose.base.MyLazyColumn
import com.huanchengfly.tieba.core.ui.compose.widgets.TipScreen
import com.huanchengfly.tieba.core.ui.compose.widgets.UserHeader
import com.huanchengfly.tieba.core.ui.compose.widgets.states.StateScreen
import com.huanchengfly.tieba.core.common.utils.DateTimeUtils
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UserPostPage(
    uid: Long,
    isThread: Boolean = true,
    fluid: Boolean = false,
    enablePullRefresh: Boolean = false,
    viewModel: UserPostViewModel = pageViewModel(key = if (isThread) "user_thread_$uid" else "user_post_$uid"),
) {
    val homeNavigation = LocalHomeNavigation.current

    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(UserPostUiIntent.Refresh(uid, isThread))
        viewModel.initialized = true
    }

    val isRefreshing by viewModel.uiState.collectPartialAsState(
        prop1 = UserPostUiState::isRefreshing,
        initial = true
    )
    val isLoadingMore by viewModel.uiState.collectPartialAsState(
        prop1 = UserPostUiState::isLoadingMore,
        initial = false
    )
    val error by viewModel.uiState.collectPartialAsState(
        prop1 = UserPostUiState::error,
        initial = null
    )
    val currentPage by viewModel.uiState.collectPartialAsState(
        prop1 = UserPostUiState::currentPage,
        initial = 1
    )
    val hasMore by viewModel.uiState.collectPartialAsState(
        prop1 = UserPostUiState::hasMore,
        initial = false
    )
    val posts by viewModel.uiState.collectPartialAsState(
        prop1 = UserPostUiState::posts,
        initial = persistentListOf()
    )
    val hidePost by viewModel.uiState.collectPartialAsState(
        prop1 = UserPostUiState::hidePost,
        initial = false
    )

    val isEmpty by remember {
        derivedStateOf { posts.isEmpty() }
    }
    val isError by remember {
        derivedStateOf { error != null }
    }

    onGlobalEvent<CommonUiEvent.Refresh>(
        filter = { it.key == "user_profile" }
    ) {
        viewModel.send(UserPostUiIntent.Refresh(uid, isThread))
    }

    StateScreen(
        modifier = Modifier.fillMaxSize(),
        isEmpty = isEmpty,
        isError = isError,
        isLoading = isRefreshing,
        onReload = {
            viewModel.send(UserPostUiIntent.Refresh(uid, isThread))
        },
        loadingScreen = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column {
                    repeat(4) {
                        FeedCardPlaceholder()
                    }
                }
            }
        },
        errorScreen = { ErrorScreen(error = error.getOrNull()) },
        emptyScreen = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                if (hidePost) {
                    TipScreen(
                        title = { Text(text = stringResource(id = R.string.title_user_hide_post)) },
                        image = {
                            val composition by rememberLottieComposition(
                                LottieCompositionSpec.RawRes(
                                    R.raw.lottie_hide
                                )
                            )
                            LottieAnimation(
                                composition = composition,
                                iterations = LottieConstants.IterateForever,
                                modifier = Modifier
                                    .padding(vertical = 16.dp)
                                    .fillMaxWidth()
                                    .aspectRatio(2.5f)
                            )
                        },
                        scrollable = false,
                    )
                } else {
                    TipScreen(
                        title = { Text(text = stringResource(id = CoreUiR.string.title_empty)) },
                        image = {
                            val composition by rememberLottieComposition(
                                LottieCompositionSpec.RawRes(
                                    CoreUiR.raw.lottie_empty_box
                                )
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
                            if (canReload) {
                                Button(onClick = { reload() }) {
                                    Text(text = stringResource(id = CoreUiR.string.btn_refresh))
                                }
                            }
                        },
                        scrollable = false,
                    )
                }
            }
        },
    ) {
        val pullRefreshState = rememberPullRefreshState(
            refreshing = isRefreshing,
            onRefresh = ::reload
        )

        val lazyListState = rememberLazyListState()

        val pullRefreshModifier =
            if (enablePullRefresh) Modifier.pullRefresh(pullRefreshState) else Modifier

        Box(modifier = pullRefreshModifier) {
            LoadMoreLayout(
                isLoading = isLoadingMore,
                onLoadMore = {
                    viewModel.send(UserPostUiIntent.LoadMore(uid, isThread, currentPage))
                },
                loadEnd = !hasMore,
                lazyListState = lazyListState
            ) {
                UserPostList(
                    data = posts,
                    fluid = fluid,
                    lazyListState = lazyListState,
                    onClickItem = { threadId, postId, isSubPost ->
                        if (postId == null) {
                            homeNavigation.openThread(threadId)
                        } else {
                            if (isSubPost) {
                                homeNavigation.openSubPosts(
                                    threadId = threadId,
                                    subPostId = postId,
                                    loadFromSubPost = true
                                )
                            } else {
                                homeNavigation.openThread(
                                    threadId = threadId,
                                    postId = postId,
                                    scrollToReply = true
                                )
                            }
                        }
                    },
                    onAgreeItem = {
                        viewModel.send(
                            UserPostUiIntent.Agree(
                                it.threadId,
                                it.postId,
                                it.hasAgree
                            )
                        )
                    },
                    onClickReply = {
                        homeNavigation.openThread(
                            threadId = it.threadId,
                            forumId = it.forumId,
                            scrollToReply = true
                        )
                    },
                    onClickUser = {
                        homeNavigation.openUserProfile(it)
                    },
                    onClickForum = {
                        homeNavigation.openForum(it)
                    },
                    onClickOriginThread = {
                        homeNavigation.openThread(it)
                    },
                )
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

@Composable
private fun UserPostList(
    data: ImmutableList<PostListItemData>,
    fluid: Boolean = false,
    lazyListState: LazyListState = rememberLazyListState(),
    onClickItem: (threadId: Long, postId: Long?, isSubPost: Boolean) -> Unit = { _, _, _ -> },
    onAgreeItem: (UserPostItem) -> Unit = {},
    onClickReply: (UserPostItem) -> Unit = {},
    onClickUser: (id: Long) -> Unit = {},
    onClickForum: (name: String) -> Unit = {},
    onClickOriginThread: (threadId: Long) -> Unit = {},
) {
    MyLazyColumn(state = lazyListState) {
        items(
            items = data,
            key = {
                "${it.data.get { threadId }}_${it.data.get { postId }}"
            }
        ) { itemData ->
            Container(fluid = fluid) {
                UserPostItem(
                    post = itemData,
                    onClick = onClickItem,
                    onAgree = onAgreeItem,
                    onClickReply = onClickReply,
                    onClickUser = onClickUser,
                    onClickForum = onClickForum,
                    onClickOriginThread = onClickOriginThread,
                )
            }
        }
    }
}

@Composable
fun UserPostItem(
    post: PostListItemData,
    onAgree: (UserPostItem) -> Unit,
    modifier: Modifier = Modifier,
    onClick: (threadId: Long, postId: Long?, isSubPost: Boolean) -> Unit = { _, _, _ -> },
    onClickReply: (UserPostItem) -> Unit = {},
    onClickUser: (id: Long) -> Unit = {},
    onClickForum: (name: String) -> Unit = {},
    onClickOriginThread: (threadId: Long) -> Unit = {},
) {
    val item = post.data
    if (post.isThread) {
        val postInfo = item.get { this }
        val threadCard = remember(postInfo) { postInfo.toThreadCard() }
        FeedCard(
            item = threadCard,
            onClick = { onClick(it.threadId, null, false) },
            onAgree = { onAgree(postInfo) },
            modifier = modifier,
            onClickReply = { onClickReply(postInfo) },
            onClickUser = onClickUser,
            onClickForum = onClickForum,
            onClickOriginThread = {
                val originThread = threadCard.originThreadPayload as? OriginThreadCard
                if (originThread != null) {
                    onClickOriginThread(originThread.threadId)
                }
            },
        )
    } else {
        FilledCard(
            header = {
                UserHeader(
                    nameProvider = { item.get { userName } },
                    nameShowProvider = { item.get { nameShow }.orEmpty() },
                    portraitProvider = { item.get { userPortrait }.orEmpty() },
                    onClick = {
                        onClickUser(item.get { userId })
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            },
            content = {
                Column {
                    post.contents.fastForEach {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onClick(
                                        item.get { threadId },
                                        it.postId,
                                        it.isSubPost
                                    )
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = it.contentText,
                                style = MaterialTheme.typography.body1,
                                color = ExtendedTheme.colors.text,
                            )

                            Text(
                                text = DateTimeUtils.getRelativeTimeString(
                                    LocalContext.current,
                                    it.createTime
                                ),
                                style = MaterialTheme.typography.caption,
                                color = ExtendedTheme.colors.textSecondary,
                            )
                        }
                    }
                }

                Text(
                    text = item.get { title },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(ExtendedTheme.colors.floorCard)
                        .clickable {
                            onClickOriginThread(item.get { threadId })
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.body2,
                )
            },
            modifier = modifier,
            contentPadding = PaddingValues(0.dp),
        )
    }
}

private fun UserPostItem.toThreadCard(): ThreadCard {
    val author = ThreadAuthor(
        id = userId,
        name = userName,
        nameShow = nameShow,
        portrait = userPortrait
    )
    return ThreadCard(
        threadId = threadId,
        firstPostId = postId,
        forumId = forumId,
        forumName = forumName,
        title = title,
        tabName = "",
        isNoTitle = isNoTitle,
        isGood = isGood,
        isShareThread = isShareThread,
        lastTimeInt = createTime.toInt(),
        shareNum = shareNum,
        replyNum = replyNum,
        hotNum = 0,
        agreeNum = agreeNum,
        hasAgree = hasAgree,
        collectStatus = 0,
        collectMarkPid = 0L,
        author = author,
        forumInfo = null,
        abstractSegments = abstractSegments,
        medias = medias,
        videoInfo = videoInfo,
        hasOriginThreadInfo = originThread != null,
        originThreadPayload = originThread,
        authorId = userId,
    )
}
