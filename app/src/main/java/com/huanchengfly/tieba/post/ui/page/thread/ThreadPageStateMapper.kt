package com.huanchengfly.tieba.post.ui.page.thread

import com.huanchengfly.tieba.core.common.thread.ThreadDetail
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.core.mvi.wrapImmutable
import com.huanchengfly.tieba.post.models.PostEntity
import com.huanchengfly.tieba.core.common.thread.ThreadMeta

object ThreadPageStateMapper {
    fun map(
        state: ThreadPageState,
        routeThreadId: Long,
        routeForumId: Long?,
        uiState: ThreadUiState,
        threadMetaFromStore: ThreadMeta?,
        postEntities: List<PostEntity>
    ) {
        val effectiveThreadId = uiState.threadId.takeIf { it != 0L } ?: routeThreadId
        val displayThread = uiState.threadDetail ?: state.displayThread
        val threadMeta = threadMetaFromStore
            ?: uiState.initMeta
            ?: buildThreadMetaFromThread(displayThread)

        val enablePullRefresh = uiState.hasPrevious || uiState.sortType == ThreadSortType.SORT_TYPE_DESC
        val loadMoreEnd = !uiState.hasMore && uiState.sortType == ThreadSortType.SORT_TYPE_DESC
        val loadMorePreloadCount = if (uiState.hasMore) 1 else 0
        val isCollected = threadMeta.collectStatus
        val hasThreadAgreed = threadMeta.hasAgree
        val threadAgreeNum = threadMeta.agreeNum
        val threadTitle = displayThread?.get { title } ?: uiState.title
        val forumId = uiState.forum?.get { id }
            ?: displayThread?.get { forumId }
            ?: routeForumId
            ?: state.curForumId
        val forumName = uiState.forum?.get { name }
            ?: displayThread?.get { forumName }
            ?: state.curForumName
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

    private fun buildThreadMetaFromThread(thread: ImmutableHolder<ThreadDetail>?): ThreadMeta {
        if (thread == null) return ThreadMeta()
        val agreeNum = thread.get { agree?.agreeNum?.toInt() ?: 0 } ?: 0
        val hasAgree = thread.get { agree?.hasAgree == 1 } ?: false
        val collectStatus = thread.get { collectStatus == 1 } ?: false
        val collectMarkPid = thread.get { collectMarkPid } ?: 0L
        val replyNum = thread.get { replyNum } ?: 0
        return ThreadMeta(
            hasAgree = hasAgree,
            agreeNum = agreeNum,
            collectStatus = collectStatus,
            collectMarkPid = collectMarkPid,
            replyNum = replyNum
        )
    }
}
