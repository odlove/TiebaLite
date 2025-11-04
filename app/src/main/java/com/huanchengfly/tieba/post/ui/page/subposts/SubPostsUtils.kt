package com.huanchengfly.tieba.post.ui.page.subposts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.huanchengfly.tieba.post.api.models.protos.Anti
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.api.models.protos.SimpleForum
import com.huanchengfly.tieba.post.api.models.protos.SubPostList
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.User
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.post.models.database.Account
import com.huanchengfly.tieba.post.ui.common.PbContentRender
import com.huanchengfly.tieba.post.ui.page.destinations.CopyTextDialogPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.UserProfilePageDestination
import com.huanchengfly.tieba.post.ui.page.reply.ReplyArgs
import com.huanchengfly.tieba.post.preferences.appPreferences
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.collections.immutable.ImmutableList

/**
 * 构建回复参数，统一校验逻辑
 *
 * 校验规则：
 * 1. forumId 必须非零
 * 2. forumName 必须非空
 * 3. 优先使用 API 返回的数据，其次使用 fallback 参数
 *
 * @param forumIdFromApi API 返回的论坛 ID（可能为 null）
 * @param fallbackForumId 初始传入的 forumId（可能为 0）
 * @param forumNameFromApi API 返回的论坛名称（可能为 null）
 * @param threadId 主题 ID
 * @param postIdFromApi API 返回的主楼 ID（可能为 null）
 * @param fallbackPostId 初始传入的 postId（可能为 0）
 * @param targetSubPost 回复的目标楼中楼（普通回复时为 null）
 * @param replyUser 被回复的用户信息
 * @param postAuthorIdFallback 主楼作者 ID（回复主楼时的 fallback）
 *
 * @return ReplyArgs 或 null（不满足回复条件时）
 */
fun buildReplyArgs(
    forumIdFromApi: Long?,
    fallbackForumId: Long,
    forumNameFromApi: String?,
    threadId: Long,
    postIdFromApi: Long?,
    fallbackPostId: Long,
    targetSubPost: SubPostList? = null,
    replyUser: User? = null,
    postAuthorIdFallback: Long? = null,
): ReplyArgs? {
    // 1. 获取 forumId，优先使用 API 数据
    val fid = forumIdFromApi ?: fallbackForumId

    // 2. 获取 forumName
    val forumName = forumNameFromApi

    // 3. 获取 postId，优先使用 API 数据
    val pid = postIdFromApi ?: fallbackPostId

    // 4. 统一校验
    if (fid == 0L || forumName.isNullOrEmpty()) {
        return null
    }

    // 5. 构建 ReplyArgs
    return ReplyArgs(
        forumId = fid,
        forumName = forumName,
        threadId = threadId,
        postId = pid,
        subPostId = targetSubPost?.id,
        replyUserId =
            replyUser?.id
                ?: postAuthorIdFallback
                ?: targetSubPost?.author_id
                ?: targetSubPost?.author?.id,
        replyUserName =
            replyUser?.nameShow?.takeIf { it.isNotEmpty() }
                ?: replyUser?.name
                ?: targetSubPost?.author?.nameShow?.takeIf { !it.isNullOrEmpty() }
                ?: targetSubPost?.author?.name,
        replyUserPortrait = replyUser?.portrait ?: targetSubPost?.author?.portrait,
    )
}

/**
 * 统一处理回复操作
 *
 * 封装 buildReplyArgs 调用 + 成功/失败处理，消除重复代码
 *
 * @param forum 论坛信息
 * @param fallbackForumId 初始传入的 forumId
 * @param threadId 主题 ID
 * @param post 主楼信息
 * @param fallbackPostId 初始传入的 postId
 * @param targetSubPost 回复的目标楼中楼
 * @param replyUser 被回复的用户信息
 * @param postAuthorIdFallback 主楼作者 ID（回复主楼时的 fallback）
 * @param onSuccess 构建成功回调（接收 ReplyArgs）
 * @param onFailure 构建失败回调（论坛信息未加载完成）
 */
fun handleReplyAction(
    forum: ImmutableHolder<SimpleForum>?,
    fallbackForumId: Long,
    threadId: Long,
    post: ImmutableHolder<Post>?,
    fallbackPostId: Long,
    targetSubPost: SubPostList? = null,
    replyUser: User? = null,
    postAuthorIdFallback: Long? = null,
    onSuccess: (ReplyArgs) -> Unit,
    onFailure: () -> Unit,
) {
    buildReplyArgs(
        forumIdFromApi = forum?.get { id },
        fallbackForumId = fallbackForumId,
        forumNameFromApi = forum?.get { name },
        threadId = threadId,
        postIdFromApi = post?.get { id },
        fallbackPostId = fallbackPostId,
        targetSubPost = targetSubPost,
        replyUser = replyUser,
        postAuthorIdFallback = postAuthorIdFallback,
    )?.let(onSuccess) ?: onFailure()
}

/**
 * 将 ViewModel 的状态转换为 UI Props
 *
 * 这个函数将 ViewModel 的原始状态和一些计算属性封装到 SubPostsUiProps 中
 *
 * @param forum 论坛信息
 * @param thread 主题信息
 * @param post 主楼帖子信息
 * @param anti 反作弊信息
 * @param postContentRenders 主楼内容渲染器列表
 * @param subPosts 楼中楼列表数据
 * @param isLoading 是否正在加载
 * @param isRefreshing 是否正在刷新
 * @param hasMore 是否有更多数据
 * @param currentAccount 当前登录账号
 * @return SubPostsUiProps UI 状态封装对象
 */
@Composable
fun rememberSubPostsUiProps(
    forum: ImmutableHolder<SimpleForum>?,
    thread: ImmutableHolder<ThreadInfo>?,
    post: ImmutableHolder<Post>?,
    anti: ImmutableHolder<Anti>?,
    postContentRenders: ImmutableList<PbContentRender>,
    subPosts: ImmutableList<SubPostItemData>,
    isLoading: Boolean,
    isRefreshing: Boolean,
    hasMore: Boolean,
    currentAccount: Account?,
): SubPostsUiProps {
    val context = LocalContext.current

    val threadAuthorId =
        remember(thread) {
            thread?.get { author?.id }
        }

    val canDelete: (SubPostList) -> Boolean =
        remember(currentAccount) {
            { subPost: SubPostList ->
                subPost.author_id == currentAccount?.uid?.toLongOrNull()
            }
        }

    val bottomBarVisible =
        remember(currentAccount) {
            currentAccount != null && !context.appPreferences.hideReply
        }

    return SubPostsUiProps(
        forum = forum,
        thread = thread,
        post = post,
        anti = anti,
        postContentRenders = postContentRenders,
        subPosts = subPosts,
        isLoading = isLoading,
        isRefreshing = isRefreshing,
        hasMore = hasMore,
        currentAccount = currentAccount,
        threadAuthorId = threadAuthorId,
        canDelete = canDelete,
        bottomBarVisible = bottomBarVisible,
    )
}

/**
 * 创建回调函数集合
 *
 * 这个函数将所有用户交互回调封装到 SubPostsCallbacks 中
 * 注意：部分回调（如 onReplyClick）需要复杂的上下文处理，由调用者传入
 *
 * @param viewModel ViewModel 实例（用于发送 Intent）
 * @param navigator 导航器
 * @param forumId 论坛 ID
 * @param threadId 主题 ID
 * @param postId 主楼 ID
 * @param onNavigateUp 返回上一页回调
 * @param onReply 回复回调（null 表示回复主楼）
 * @param onShowDeleteDialog 显示删除确认对话框回调
 * @return SubPostsCallbacks 回调函数集合
 */
@Composable
fun rememberSubPostsCallbacks(
    viewModel: SubPostsViewModel,
    navigator: DestinationsNavigator,
    forumId: Long,
    threadId: Long,
    postId: Long,
    onNavigateUp: () -> Unit,
    onReply: (SubPostList?) -> Unit,
    onShowDeleteDialog: (SubPostList?) -> Unit,
): SubPostsCallbacks {
    return remember(viewModel, navigator, forumId, threadId, postId, onNavigateUp, onReply, onShowDeleteDialog) {
        SubPostsCallbacks(
            onBack = onNavigateUp,
            onUserClick = { user ->
                navigator.navigate(UserProfilePageDestination(user.id))
            },
            onReplyClick = onReply,
            onAgree = { subPost ->
                val hasAgreed = subPost.agree?.hasAgree != 0
                viewModel.send(
                    SubPostsUiIntent.Agree(
                        forumId,
                        threadId,
                        postId,
                        subPostId = subPost.id,
                        agree = !hasAgreed,
                    ),
                )
            },
            onMenuCopy = { content ->
                navigator.navigate(CopyTextDialogPageDestination(content))
            },
            onMenuDelete = onShowDeleteDialog,
            onRefresh = {
                viewModel.send(SubPostsUiIntent.Load(forumId, threadId, postId))
            },
            onLoadMore = {
                viewModel.send(
                    SubPostsUiIntent.LoadMore(
                        forumId,
                        threadId,
                        postId,
                        page = viewModel.uiState.value.currentPage + 1,
                    ),
                )
            },
            onRetry = {
                viewModel.send(SubPostsUiIntent.Load(forumId, threadId, postId))
            },
        )
    }
}
