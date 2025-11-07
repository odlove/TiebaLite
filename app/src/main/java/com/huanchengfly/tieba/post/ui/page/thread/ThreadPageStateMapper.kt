package com.huanchengfly.tieba.post.ui.page.thread

import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.core.mvi.wrapImmutable
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.models.PostEntity
import com.huanchengfly.tieba.post.models.ThreadEntity
import com.huanchengfly.tieba.post.models.ThreadMeta

object ThreadPageStateMapper {
    fun map(
        state: ThreadPageState,
        routeThreadId: Long,
        routeForumId: Long?,
        uiState: ThreadUiState,
        repositoryThread: ThreadEntity?,
        postEntities: List<PostEntity>
    ) {
        val effectiveThreadId = uiState.threadId.takeIf { it != 0L } ?: routeThreadId
        val displayThread = repositoryThread?.proto?.wrapImmutable() ?: uiState.threadInfo
        val threadMeta = repositoryThread?.meta
            ?: uiState.initMeta
            ?: buildThreadMetaFromThread(displayThread)

        val enablePullRefresh = uiState.hasPrevious || uiState.sortType == ThreadSortType.SORT_TYPE_DESC
        val loadMoreEnd = !uiState.hasMore && uiState.sortType == ThreadSortType.SORT_TYPE_DESC
        val loadMorePreloadCount = if (uiState.hasMore) 1 else 0
        val isCollected = threadMeta.collectStatus != 0
        val hasThreadAgreed = threadMeta.hasAgree == 1
        val threadAgreeNum = threadMeta.agreeNum
        val threadTitle = displayThread?.get { title } ?: uiState.title
        val forumId = uiState.forum?.get { id } ?: routeForumId ?: state.curForumId
        val forumName = uiState.forum?.get { name } ?: state.curForumName
        val tbs = uiState.anti?.get { tbs }
        val isEmpty = uiState.data.isEmpty() && uiState.firstPost == null

        state.threadId = effectiveThreadId
        state.displayThread = displayThread
        state.firstPost = uiState.firstPost
        state.firstPostContentRenders = uiState.firstPostContentRenders
        state.postItems = uiState.data
        state.latestPosts = uiState.latestPosts
        state.author = uiState.author
        state.forum = uiState.forum
        state.user = uiState.user
        state.anti = uiState.anti
        state.threadMeta = threadMeta
        state.postEntities = postEntities
        state.postIds = uiState.postIds
        state.nextPagePostId = uiState.nextPagePostId
        state.isRefreshing = uiState.isRefreshing
        state.isLoadingMore = uiState.isLoadingMore
        state.isError = uiState.isError
        state.error = uiState.error
        state.hasMore = uiState.hasMore
        state.hasPrevious = uiState.hasPrevious
        state.currentPageMax = uiState.currentPageMax
        state.totalPage = uiState.totalPage
        state.isSeeLz = uiState.seeLz
        state.sortType = uiState.sortType
        state.isImmersiveMode = uiState.isImmersiveMode
        state.isEmpty = isEmpty
        state.enablePullRefresh = enablePullRefresh
        state.loadMoreEnd = loadMoreEnd
        state.loadMorePreloadCount = loadMorePreloadCount
        state.isCollected = isCollected
        state.hasThreadAgreed = hasThreadAgreed
        state.threadAgreeNum = threadAgreeNum
        state.threadTitle = threadTitle
        state.curForumId = forumId
        state.curForumName = forumName
        state.curTbs = tbs
    }

    private fun buildThreadMetaFromThread(thread: ImmutableHolder<ThreadInfo>?): ThreadMeta {
        if (thread == null) return ThreadMeta()
        val agreeNum = thread.get { agreeNum } ?: 0
        val hasAgree = thread.get { agree?.hasAgree } ?: 0
        val collectStatus = thread.get { collectStatus } ?: 0
        val collectMarkPid = thread.get { collectMarkPid.toLongOrNull() ?: 0L } ?: 0L
        return ThreadMeta(
            hasAgree = hasAgree,
            agreeNum = agreeNum,
            collectStatus = collectStatus,
            collectMarkPid = collectMarkPid
        )
    }
}
