package com.huanchengfly.tieba.post.ui.page.thread

import com.huanchengfly.tieba.core.mvi.PartialChange
import com.huanchengfly.tieba.core.mvi.wrapImmutable
import com.huanchengfly.tieba.core.common.thread.ThreadAnti
import com.huanchengfly.tieba.core.common.thread.ThreadDetail
import com.huanchengfly.tieba.core.common.thread.ThreadForum
import com.huanchengfly.tieba.core.common.thread.ThreadPost
import com.huanchengfly.tieba.core.common.thread.ThreadUser
import com.huanchengfly.tieba.core.common.thread.ThreadMeta
import com.huanchengfly.tieba.post.removeAt
import com.huanchengfly.tieba.post.ui.common.PbContentRender
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

sealed interface ThreadPartialChange : PartialChange<ThreadUiState> {
    sealed class Init : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState = when (this) {
            is Success -> oldState.copy(
                isRefreshing = true,
                isError = false,
                error = null,
                title = title,
                author = if (author != null) wrapImmutable(author) else null,
                threadDetail = threadDetail?.wrapImmutable(),
                firstPost = oldState.firstPost,
                firstPostContentRenders = firstPostContentRenders.toImmutableList(),
                postId = postId,
                seeLz = seeLz,
                sortType = sortType,
                initMeta = sanitizedMeta,
            )

            is Failure -> oldState.copy(
                isError = true,
                error = error.wrapImmutable()
            )
        }

        data class Success(
            val title: String,
            val author: ThreadUser?,
            val threadDetail: ThreadDetail?,
            val firstPostContentRenders: List<PbContentRender>,
            val postId: Long = 0,
            val seeLz: Boolean = false,
            val sortType: Int = 0,
            val sanitizedMeta: ThreadMeta? = null,
        ) : Init()

        data class Failure(
            val error: Throwable
        ) : Init()
    }

    sealed class Load : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState = when (this) {
            is Start -> oldState.copy(isRefreshing = true)

            is Success -> oldState.copy(
                isRefreshing = false,
                isError = false,
                error = null,
                title = title,
                author = wrapImmutable(author),
                user = wrapImmutable(user),
                data = data.toImmutableList(),
                threadDetail = threadDetail.wrapImmutable(),
                firstPost = if (firstPost != null) wrapImmutable(firstPost) else oldState.firstPost,
                forum = wrapImmutable(forum),
                anti = wrapImmutable(anti),
                currentPageMin = currentPage,
                currentPageMax = currentPage,
                totalPage = totalPage,
                hasMore = hasMore,
                nextPagePostId = nextPagePostId,
                hasPrevious = hasPrevious,
                firstPostContentRenders = firstPostContentRenders?.toImmutableList()
                    ?: oldState.firstPostContentRenders,
                latestPosts = persistentListOf(),
                postId = postId,
                seeLz = seeLz,
                sortType = sortType,
                threadId = threadId,  // ✅ 新增
                postIds = postIds,  // ✅ 新增
            )

            is Failure -> oldState.copy(
                isRefreshing = false,
                isError = true,
                error = error.wrapImmutable()
            )
        }

        data object Start : Load()

        data class Success(
            val title: String,
            val author: ThreadUser,
            val user: ThreadUser,
            val firstPost: ThreadPost?,
            val data: List<PostItemData>,
            val threadDetail: ThreadDetail,
            val forum: ThreadForum,
            val anti: ThreadAnti,
            val currentPage: Int,
            val totalPage: Int,
            val hasMore: Boolean,
            val nextPagePostId: Long,
            val hasPrevious: Boolean,
            val firstPostContentRenders: List<PbContentRender>?,
            val postId: Long = 0,
            val seeLz: Boolean = false,
            val sortType: Int = 0,
            val threadId: Long,  // ✅ 新增
            val postIds: ImmutableList<Long>,  // ✅ 新增
        ) : Load()

        data class Failure(
            val error: Throwable,
        ) : Load()
    }

    sealed class LoadFirstPage : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState = when (this) {
            is Start -> oldState.copy(isRefreshing = true)
            is Success -> oldState.copy(
                isRefreshing = false,
                isError = false,
                error = null,
                title = title,
                author = wrapImmutable(author),
                user = wrapImmutable(user),
                data = data.toImmutableList(),
                threadDetail = threadDetail.wrapImmutable(),
                firstPost = firstPost?.wrapImmutable(),
                forum = wrapImmutable(forum),
                anti = wrapImmutable(anti),
                currentPageMin = currentPage,
                currentPageMax = currentPage,
                totalPage = totalPage,
                hasMore = hasMore,
                nextPagePostId = nextPagePostId,
                hasPrevious = hasPrevious,
                firstPostContentRenders = firstPostContentRenders.toImmutableList(),
                latestPosts = persistentListOf(),
                postId = postId,
                seeLz = seeLz,
                sortType = sortType,
                threadId = threadId,  // ✅ 新增
                postIds = postIds,  // ✅ 新增
            )

            is Failure -> oldState.copy(
                isRefreshing = false,
                isError = true,
                error = error.wrapImmutable(),
            )
        }

        data object Start : LoadFirstPage()

        data class Success(
            val title: String,
            val author: ThreadUser,
            val user: ThreadUser,
            val firstPost: ThreadPost?,
            val data: List<PostItemData>,
            val threadDetail: ThreadDetail,
            val forum: ThreadForum,
            val anti: ThreadAnti,
            val currentPage: Int,
            val totalPage: Int,
            val hasMore: Boolean,
            val nextPagePostId: Long,
            val hasPrevious: Boolean,
            val firstPostContentRenders: List<PbContentRender>,
            val postId: Long,
            val seeLz: Boolean,
            val sortType: Int,
            val threadId: Long,  // ✅ 新增
            val postIds: ImmutableList<Long>,  // ✅ 新增
        ) : LoadFirstPage()

        data class Failure(
            val error: Throwable
        ) : LoadFirstPage()
    }

    sealed class LoadMore : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState = when (this) {
            is Start -> oldState.copy(isLoadingMore = true)
            is Success -> {
                val uniqueData = data.filterNot { item ->
                    oldState.data.any { it.post.get { id } == item.post.get { id } }
                }
                oldState.copy(
                    isLoadingMore = false,
                    author = wrapImmutable(author),
                    data = (oldState.data + uniqueData).toImmutableList(),
                    threadDetail = threadDetail.wrapImmutable(),
                    currentPageMax = currentPage,
                    totalPage = totalPage,
                    hasMore = hasMore,
                    nextPagePostId = nextPagePostId,
                    latestPosts = persistentListOf(),
                    postIds = (oldState.postIds + newPostIds).distinct().toImmutableList(),  // ✅ 新增
                )
            }

            is Failure -> oldState.copy(isLoadingMore = false)
        }

        data object Start : LoadMore()

        data class Success(
            val author: ThreadUser,
            val data: List<PostItemData>,
            val threadDetail: ThreadDetail,
            val currentPage: Int,
            val totalPage: Int,
            val hasMore: Boolean,
            val nextPagePostId: Long,
            val newPostIds: ImmutableList<Long>,  // ✅ 新增
        ) : LoadMore()

        data class Failure(
            val errorCode: Int,
            val errorMessage: String
        ) : LoadMore()
    }

    sealed class LoadPrevious : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState = when (this) {
            is Start -> oldState.copy(isRefreshing = true)
            is Success -> oldState.copy(
                isRefreshing = false,
                author = wrapImmutable(author),
                data = (data + oldState.data).toImmutableList(),
                threadDetail = threadDetail.wrapImmutable(),
                currentPageMin = currentPage,
                totalPage = totalPage,
                hasPrevious = hasPrevious,
                postIds = (newPostIds + oldState.postIds).distinct().toImmutableList(),  // ✅ 新增
            )

            is Failure -> oldState.copy(isRefreshing = false)
        }

        data object Start : LoadPrevious()

        data class Success(
            val author: ThreadUser,
            val data: List<PostItemData>,
            val threadDetail: ThreadDetail,
            val currentPage: Int,
            val totalPage: Int,
            val hasPrevious: Boolean,
            val newPostIds: ImmutableList<Long>,  // ✅ 新增
        ) : LoadPrevious()

        data class Failure(
            val errorCode: Int,
            val errorMessage: String,
        ) : LoadPrevious()
    }

    sealed class LoadLatestPosts : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState = when (this) {
            Start -> oldState.copy(isLoadingMore = true)
            is Success -> {
                val uniqueData = data.filterNot { item ->
                    oldState.data.any { it.post.get { id } == item.post.get { id } }
                }
                oldState.copy(
                    isLoadingMore = false,
                    author = wrapImmutable(author),
                    data = (oldState.data + uniqueData).toImmutableList(),
                    threadDetail = threadDetail.wrapImmutable(),
                    currentPageMax = currentPage,
                    totalPage = totalPage,
                    hasMore = hasMore,
                    nextPagePostId = nextPagePostId,
                    latestPosts = persistentListOf(),
                    postIds = (oldState.postIds + newPostIds).distinct().toImmutableList(),  // ✅ 新增
                )
            }

            SuccessWithNoNewPost -> oldState.copy(isLoadingMore = false)
            is Failure -> oldState.copy(isLoadingMore = false)
        }

        data object Start : LoadLatestPosts()

        data class Success(
            val author: ThreadUser,
            val data: List<PostItemData>,
            val threadDetail: ThreadDetail,
            val currentPage: Int,
            val totalPage: Int,
            val hasMore: Boolean,
            val nextPagePostId: Long,
            val newPostIds: ImmutableList<Long>,  // ✅ 新增
        ) : LoadLatestPosts()

        data object SuccessWithNoNewPost : LoadLatestPosts()

        data class Failure(
            val error: Throwable,
        ) : LoadLatestPosts()
    }

    sealed class LoadMyLatestReply : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState =
            when (this) {
                Start -> oldState.copy(isLoadingLatestReply = true)
                is Success -> {
                    val continuous = isContinuous || page == oldState.currentPageMax
                    val replacePostIndexes = oldState.data.mapIndexedNotNull { index, item ->
                        val replaceItemIndex =
                            posts.indexOfFirst { it.post.get { id } == item.post.get { id } }
                        if (replaceItemIndex != -1) index to replaceItemIndex else null
                    }
                    val newPost = oldState.data.mapIndexed { index, oldItem ->
                        val replaceIndex = replacePostIndexes.firstOrNull { it.first == index }
                        if (replaceIndex != null) posts[replaceIndex.second] else oldItem
                    }
                    val addPosts = posts.filter {
                        !newPost.any { item -> item.post.get { id } == it.post.get { id } }
                    }
                    when {
                        hasNewPost && continuous && isDesc -> {
                            val newData = (addPosts.reversed() + newPost).toImmutableList()
                            oldState.copy(
                                isLoadingLatestReply = false,
                                isError = false,
                                error = null,
                                anti = anti.wrapImmutable(),
                                data = newData,
                                latestPosts = persistentListOf(),
                                postIds = newData.map { it.post.get { id } }.distinct().toImmutableList(),  // ✅ 新增
                            )
                        }

                        hasNewPost && continuous && !isDesc -> {
                            val newData = (newPost + addPosts).toImmutableList()
                            oldState.copy(
                                isLoadingLatestReply = false,
                                isError = false,
                                error = null,
                                anti = anti.wrapImmutable(),
                                data = newData,
                                latestPosts = persistentListOf(),
                                postIds = newData.map { it.post.get { id } }.distinct().toImmutableList(),  // ✅ 新增
                            )
                        }

                        hasNewPost -> {
                            oldState.copy(
                                isLoadingLatestReply = false,
                                isError = false,
                                error = null,
                                anti = anti.wrapImmutable(),
                                data = newPost.toImmutableList(),
                                latestPosts = posts.toImmutableList(),
                                postIds = newPost.map { it.post.get { id } }.distinct().toImmutableList(),  // ✅ 新增
                            )
                        }

                        !hasNewPost -> {
                            oldState.copy(
                                isLoadingLatestReply = false,
                                isError = false,
                                error = null,
                                anti = anti.wrapImmutable(),
                                data = newPost.toImmutableList(),
                                latestPosts = persistentListOf(),
                                postIds = newPost.map { it.post.get { id } }.distinct().toImmutableList(),  // ✅ 新增
                            )
                        }

                        else -> {
                            oldState.copy(
                                isLoadingLatestReply = false,
                                isError = false,
                                error = null,
                            )
                        }
                    }
                }

                is Failure -> oldState.copy(
                    isLoadingLatestReply = false,
                    isError = true,
                    error = error.wrapImmutable(),
                )
            }

        object Start : LoadMyLatestReply()

        data class Success(
            val anti: ThreadAnti,
            val posts: List<PostItemData>,
            val page: Int,
            val isContinuous: Boolean,
            val isDesc: Boolean,
            val hasNewPost: Boolean,
            val newPostIds: ImmutableList<Long>,  // ✅ 新增
        ) : LoadMyLatestReply()

        data class Failure(
            val error: Throwable,
        ) : LoadMyLatestReply()
    }

    sealed class ToggleImmersiveMode : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState = when (this) {
            is Success -> oldState.copy(isImmersiveMode = isImmersiveMode)
        }

        data class Success(
            val isImmersiveMode: Boolean
        ) : ToggleImmersiveMode()
    }

    sealed class AddCollect : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState {
            return when (this) {
                Start -> oldState
                is Success -> oldState.copy(
                    threadDetail = oldState.threadDetail?.getImmutable {
                        updateCollectStatus(
                            newStatus = 1,
                            markPostId = markPostId
                        )
                    }
                )

                is Failure -> oldState
            }
        }

        object Start : AddCollect()

        data class Success(
            val markPostId: Long,
            val floor: Int
        ) : AddCollect()

        data class Failure(
            val errorCode: Int,
            val errorMessage: String
        ) : AddCollect()
    }

    sealed class RemoveCollect : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState {
            return when (this) {
                Start -> oldState
                Success -> oldState.copy(
                    threadDetail = oldState.threadDetail?.getImmutable {
                        updateCollectStatus(
                            newStatus = 0,
                            markPostId = 0
                        )
                    }
                )

                is Failure -> oldState
            }
        }

        object Start : RemoveCollect()

        object Success : RemoveCollect()

        data class Failure(
            val errorCode: Int,
            val errorMessage: String
        ) : RemoveCollect()
    }

    sealed class AgreeThread : ThreadPartialChange {
        // ✅ 删除 Proto 更新逻辑，Store 已处理更新
        override fun reduce(oldState: ThreadUiState): ThreadUiState =
            when (this) {
                is Start, is Success, is Failure -> oldState  // ✅ Store 已更新，State 无需变化
            }

        data class Start(
            val hasAgree: Boolean
        ) : AgreeThread()

        data class Success(
            val hasAgree: Boolean
        ) : AgreeThread()

        data class Failure(
            val hasAgree: Boolean,
            val errorCode: Int,
            val errorMessage: String
        ) : AgreeThread()
    }

    sealed class AgreePost : ThreadPartialChange {
        // ✅ 删除 Proto 更新逻辑，Store 已处理更新
        override fun reduce(oldState: ThreadUiState): ThreadUiState =
            when (this) {
                is Start, is Success, is Failure -> oldState  // ✅ Store 已更新，State 无需变化
            }

        data class Start(
            val postId: Long,
            val hasAgree: Boolean
        ) : AgreePost()

        data class Success(
            val postId: Long,
            val hasAgree: Boolean
        ) : AgreePost()

        data class Failure(
            val postId: Long,
            val hasAgree: Boolean,
            val errorCode: Int,
            val errorMessage: String
        ) : AgreePost()
    }

    sealed class DeletePost : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState = when (this) {
            is Success -> {
                val deletedPostIndex = oldState.data.indexOfFirst { it.post.get { id } == postId }
                if (deletedPostIndex == -1) {
                    oldState
                } else {
                    oldState.copy(
                        data = oldState.data.removeAt(deletedPostIndex),
                    )
                }
            }

            is Failure -> oldState
        }

        data class Success(
            val postId: Long
        ) : DeletePost()

        data class Failure(
            val errorCode: Int,
            val errorMessage: String
        ) : DeletePost()
    }

    sealed class DeleteThread : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState = oldState

        object Success : DeleteThread()

        data class Failure(
            val errorCode: Int,
            val errorMessage: String
        ) : DeleteThread()
    }

    sealed class UpdateCollectMark : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState {
            return when (this) {
                Start -> oldState
                is Success -> oldState.copy(
                    threadDetail = oldState.threadDetail?.getImmutable {
                        updateCollectStatus(
                            newStatus = 1,
                            markPostId = markPostId
                        )
                    }
                )

                is Failure -> oldState
            }
        }

        object Start : UpdateCollectMark()

        data class Success(
            val markPostId: Long
        ) : UpdateCollectMark()

        data class Failure(
            val errorCode: Int,
            val errorMessage: String
        ) : UpdateCollectMark()
    }
}

private fun ThreadDetail.updateCollectStatus(
    newStatus: Int,
    markPostId: Long
): ThreadDetail = if (collectStatus != newStatus || collectMarkPid != markPostId) {
    copy(
        collectStatus = newStatus,
        collectMarkPid = markPostId
    )
} else {
    this
}
