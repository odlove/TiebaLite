package com.huanchengfly.tieba.post.ui.page.thread

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.AnnotatedString
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.core.mvi.UiEvent
import com.huanchengfly.tieba.core.mvi.UiState
import com.huanchengfly.tieba.core.mvi.wrapImmutable
import com.huanchengfly.tieba.post.api.models.protos.Anti
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.api.models.protos.SimpleForum
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.User
import com.huanchengfly.tieba.post.api.models.protos.contentRenders
import com.huanchengfly.tieba.post.api.models.protos.subPosts
import com.huanchengfly.tieba.post.api.models.protos.SubPostList
import com.huanchengfly.tieba.post.api.models.protos.updateAgreeStatus
import com.huanchengfly.tieba.post.api.models.protos.updateCollectStatus
import com.huanchengfly.tieba.post.models.PostEntity
import com.huanchengfly.tieba.post.models.ThreadMeta
import com.huanchengfly.tieba.post.ui.common.PbContentRender
import com.huanchengfly.tieba.post.utils.BlockManager.shouldBlock
import com.huanchengfly.tieba.post.ui.widgets.compose.DialogState
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberDialogState
import com.huanchengfly.tieba.post.ui.page.thread.ThreadUiIntent.AddFavorite
import com.huanchengfly.tieba.post.ui.page.thread.ThreadUiIntent.AgreePost
import com.huanchengfly.tieba.post.ui.page.thread.ThreadUiIntent.AgreeThread
import com.huanchengfly.tieba.post.ui.page.thread.ThreadUiIntent.DeletePost
import com.huanchengfly.tieba.post.ui.page.thread.ThreadUiIntent.DeleteThread
import com.huanchengfly.tieba.post.ui.page.thread.ThreadUiIntent.Load
import com.huanchengfly.tieba.post.ui.page.thread.ThreadUiIntent.LoadFirstPage
import com.huanchengfly.tieba.post.ui.page.thread.ThreadUiIntent.LoadLatestPosts
import com.huanchengfly.tieba.post.ui.page.thread.ThreadUiIntent.LoadMore
import com.huanchengfly.tieba.post.ui.page.thread.ThreadUiIntent.LoadMyLatestReply
import com.huanchengfly.tieba.post.ui.page.thread.ThreadUiIntent.LoadPrevious
import com.huanchengfly.tieba.post.ui.page.thread.ThreadUiIntent.RemoveFavorite
import com.huanchengfly.tieba.post.ui.page.thread.ThreadUiIntent.ToggleImmersiveMode
import com.huanchengfly.tieba.post.ui.page.thread.ThreadUiIntent.UpdateFavoriteMark
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Stable
class ThreadPageState(
    threadId: Long = 0L,
    displayThread: ImmutableHolder<ThreadInfo>? = null,
    firstPost: ImmutableHolder<Post>? = null,
    firstPostContentRenders: ImmutableList<PbContentRender> = persistentListOf(),
    postItems: ImmutableList<PostItemData> = persistentListOf(),
    latestPosts: ImmutableList<PostItemData> = persistentListOf(),
    author: ImmutableHolder<User>? = null,
    forum: ImmutableHolder<SimpleForum>? = null,
    user: ImmutableHolder<User> = com.huanchengfly.tieba.core.mvi.wrapImmutable(User()),
    anti: ImmutableHolder<Anti>? = null,
    threadMeta: ThreadMeta = ThreadMeta(),
    postEntities: List<PostEntity> = emptyList(),
    postIds: ImmutableList<Long> = persistentListOf(),
    nextPagePostId: Long = 0L,
    isRefreshing: Boolean = false,
    isLoadingMore: Boolean = false,
    isError: Boolean = false,
    error: ImmutableHolder<Throwable>? = null,
    hasMore: Boolean = true,
    hasPrevious: Boolean = false,
    currentPageMax: Int = 0,
    totalPage: Int = 0,
    isSeeLz: Boolean = false,
    sortType: Int = ThreadSortType.SORT_TYPE_DEFAULT,
    isImmersiveMode: Boolean = false,
    isEmpty: Boolean = true,
    enablePullRefresh: Boolean = false,
    loadMoreEnd: Boolean = false,
    loadMorePreloadCount: Int = 0,
    isCollected: Boolean = false,
    hasThreadAgreed: Boolean = false,
    threadAgreeNum: Int = 0,
    threadTitle: String = "",
    curForumId: Long? = null,
    curForumName: String? = null,
    curTbs: String? = null
) {
    var threadId by mutableLongStateOf(threadId)
        internal set
    var displayThread by mutableStateOf(displayThread)
        internal set
    var firstPost by mutableStateOf(firstPost)
        internal set
    var firstPostContentRenders by mutableStateOf(firstPostContentRenders)
        internal set
    var postItems by mutableStateOf(postItems)
        internal set
    var latestPosts by mutableStateOf(latestPosts)
        internal set
    var author by mutableStateOf(author)
        internal set
    var forum by mutableStateOf(forum)
        internal set
    var user by mutableStateOf(user)
        internal set
    var anti by mutableStateOf(anti)
        internal set
    var threadMeta by mutableStateOf(threadMeta)
        internal set
    var postEntities by mutableStateOf(postEntities)
        internal set
    var postIds by mutableStateOf(postIds)
        internal set
    var nextPagePostId by mutableLongStateOf(nextPagePostId)
        internal set
    var isRefreshing by mutableStateOf(isRefreshing)
        internal set
    var isLoadingMore by mutableStateOf(isLoadingMore)
        internal set
    var isError by mutableStateOf(isError)
        internal set
    var error by mutableStateOf(error)
        internal set
    var hasMore by mutableStateOf(hasMore)
        internal set
    var hasPrevious by mutableStateOf(hasPrevious)
        internal set
    var currentPageMax by mutableIntStateOf(currentPageMax)
        internal set
    var totalPage by mutableIntStateOf(totalPage)
        internal set
    var isSeeLz by mutableStateOf(isSeeLz)
        internal set
    var sortType by mutableIntStateOf(sortType)
        internal set
    var isImmersiveMode by mutableStateOf(isImmersiveMode)
        internal set
    var isEmpty by mutableStateOf(isEmpty)
        internal set
    var enablePullRefresh by mutableStateOf(enablePullRefresh)
        internal set
    var loadMoreEnd by mutableStateOf(loadMoreEnd)
        internal set
    var loadMorePreloadCount by mutableIntStateOf(loadMorePreloadCount)
        internal set
    var isCollected by mutableStateOf(isCollected)
        internal set
    var hasThreadAgreed by mutableStateOf(hasThreadAgreed)
        internal set
    var threadAgreeNum by mutableIntStateOf(threadAgreeNum)
        internal set
    var threadTitle by mutableStateOf(threadTitle)
        internal set
    var curForumId by mutableStateOf(curForumId)
        internal set
    var curForumName by mutableStateOf(curForumName)
        internal set
    var curTbs by mutableStateOf(curTbs)
        internal set
}

interface ThreadPageActions {
    fun load(
        threadId: Long,
        page: Int,
        postId: Long,
        forumId: Long?,
        seeLz: Boolean,
        sortType: Int,
        from: String = ""
    )

    fun loadFirstPage(
        threadId: Long,
        forumId: Long?,
        seeLz: Boolean,
        sortType: Int
    )

    fun loadMore(
        threadId: Long,
        page: Int,
        forumId: Long?,
        postId: Long,
        seeLz: Boolean,
        sortType: Int,
        postIds: List<Long>
    )

    fun loadPrevious(
        threadId: Long,
        page: Int,
        forumId: Long?,
        postId: Long,
        seeLz: Boolean,
        sortType: Int,
        postIds: List<Long>
    )

    fun loadLatestPosts(
        threadId: Long,
        curLatestPostId: Long,
        forumId: Long?,
        seeLz: Boolean,
        sortType: Int
    )

    fun loadMyLatestReply(
        threadId: Long,
        postId: Long,
        forumId: Long?,
        isDesc: Boolean,
        curLatestPostFloor: Int,
        curPostIds: List<Long>
    )

    fun toggleImmersiveMode(isImmersiveMode: Boolean)

    fun addFavorite(threadId: Long, postId: Long, floor: Int)

    fun removeFavorite(threadId: Long, forumId: Long, tbs: String?)

    fun updateFavoriteMark(threadId: Long, postId: Long)

    fun agreeThread(threadId: Long, postId: Long, agree: Boolean)

    fun agreePost(threadId: Long, postId: Long, agree: Boolean)

    fun deleteThread(forumId: Long, forumName: String, threadId: Long, deleteMyThread: Boolean, tbs: String?)

    fun deletePost(forumId: Long, forumName: String, threadId: Long, postId: Long, deleteMyPost: Boolean, tbs: String?)
}

class ThreadPageActionsImpl(
    private val viewModel: ThreadViewModel
) : ThreadPageActions {
    override fun load(
        threadId: Long,
        page: Int,
        postId: Long,
        forumId: Long?,
        seeLz: Boolean,
        sortType: Int,
        from: String
    ) {
        viewModel.send(
            Load(
                threadId = threadId,
                page = page,
                postId = postId,
                forumId = forumId,
                seeLz = seeLz,
                sortType = sortType,
                from = from
            )
        )
    }

    override fun loadFirstPage(threadId: Long, forumId: Long?, seeLz: Boolean, sortType: Int) {
        viewModel.send(
            LoadFirstPage(
                threadId = threadId,
                forumId = forumId,
                seeLz = seeLz,
                sortType = sortType
            )
        )
    }

    override fun loadMore(
        threadId: Long,
        page: Int,
        forumId: Long?,
        postId: Long,
        seeLz: Boolean,
        sortType: Int,
        postIds: List<Long>
    ) {
        viewModel.send(
            LoadMore(
                threadId = threadId,
                page = page,
                forumId = forumId,
                postId = postId,
                seeLz = seeLz,
                sortType = sortType,
                postIds = postIds
            )
        )
    }

    override fun loadPrevious(
        threadId: Long,
        page: Int,
        forumId: Long?,
        postId: Long,
        seeLz: Boolean,
        sortType: Int,
        postIds: List<Long>
    ) {
        viewModel.send(
            LoadPrevious(
                threadId = threadId,
                page = page,
                forumId = forumId,
                postId = postId,
                seeLz = seeLz,
                sortType = sortType,
                postIds = postIds
            )
        )
    }

    override fun loadLatestPosts(
        threadId: Long,
        curLatestPostId: Long,
        forumId: Long?,
        seeLz: Boolean,
        sortType: Int
    ) {
        viewModel.send(
            LoadLatestPosts(
                threadId = threadId,
                curLatestPostId = curLatestPostId,
                forumId = forumId,
                seeLz = seeLz,
                sortType = sortType
            )
        )
    }

    override fun loadMyLatestReply(
        threadId: Long,
        postId: Long,
        forumId: Long?,
        isDesc: Boolean,
        curLatestPostFloor: Int,
        curPostIds: List<Long>
    ) {
        viewModel.send(
            LoadMyLatestReply(
                threadId = threadId,
                postId = postId,
                forumId = forumId,
                isDesc = isDesc,
                curLatestPostFloor = curLatestPostFloor,
                curPostIds = curPostIds
            )
        )
    }

    override fun toggleImmersiveMode(isImmersiveMode: Boolean) {
        viewModel.send(ToggleImmersiveMode(isImmersiveMode))
    }

    override fun addFavorite(threadId: Long, postId: Long, floor: Int) {
        viewModel.send(AddFavorite(threadId = threadId, postId = postId, floor = floor))
    }

    override fun removeFavorite(threadId: Long, forumId: Long, tbs: String?) {
        viewModel.send(RemoveFavorite(threadId = threadId, forumId = forumId, tbs = tbs))
    }

    override fun updateFavoriteMark(threadId: Long, postId: Long) {
        viewModel.send(UpdateFavoriteMark(threadId = threadId, postId = postId))
    }

    override fun agreeThread(threadId: Long, postId: Long, agree: Boolean) {
        viewModel.send(AgreeThread(threadId = threadId, postId = postId, agree = agree))
    }

    override fun agreePost(threadId: Long, postId: Long, agree: Boolean) {
        viewModel.send(AgreePost(threadId = threadId, postId = postId, agree = agree))
    }

    override fun deleteThread(
        forumId: Long,
        forumName: String,
        threadId: Long,
        deleteMyThread: Boolean,
        tbs: String?
    ) {
        viewModel.send(
            DeleteThread(
                forumId = forumId,
                forumName = forumName,
                threadId = threadId,
                deleteMyThread = deleteMyThread,
                tbs = tbs
            )
        )
    }

    override fun deletePost(
        forumId: Long,
        forumName: String,
        threadId: Long,
        postId: Long,
        deleteMyPost: Boolean,
        tbs: String?
    ) {
        viewModel.send(
            DeletePost(
                forumId = forumId,
                forumName = forumName,
                threadId = threadId,
                postId = postId,
                deleteMyPost = deleteMyPost,
                tbs = tbs
            )
        )
    }
}

@Immutable
data class ThreadPageDialogs(
    val updateCollectMarkDialogState: DialogState,
    val confirmDeleteDialogState: DialogState,
    val jumpToPageDialogState: DialogState,
    val deletePost: MutableState<ImmutableHolder<Post>?>,
    val readFloorBeforeBack: MutableIntState,
)

@Composable
fun rememberThreadPageDialogs(): ThreadPageDialogs {
    val updateCollectMark = rememberDialogState()
    val confirmDelete = rememberDialogState()
    val jumpToPage = rememberDialogState()
    val deletePost = remember { mutableStateOf<ImmutableHolder<Post>?>(null) }
    val readFloorBeforeBack = remember { mutableIntStateOf(1) }
    return remember(updateCollectMark, confirmDelete, jumpToPage, deletePost, readFloorBeforeBack) {
        ThreadPageDialogs(
            updateCollectMarkDialogState = updateCollectMark,
            confirmDeleteDialogState = confirmDelete,
            jumpToPageDialogState = jumpToPage,
            deletePost = deletePost,
            readFloorBeforeBack = readFloorBeforeBack
        )
    }
}

sealed interface ThreadPageEffect : UiEvent {
    data object ScrollToFirstReply : ThreadPageEffect
    data object ScrollToLatestReply : ThreadPageEffect
    data class LoadSuccess(val page: Int) : ThreadPageEffect
    data class ShowSnackbar(val message: String) : ThreadPageEffect
    data class ShowToast(
        val message: String,
        val navigateUpAfter: Boolean = false
    ) : ThreadPageEffect
    data object NavigateUp : ThreadPageEffect
}

data class ThreadUiState(
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isLoadingLatestReply: Boolean = false,
    val isError: Boolean = false,
    val error: ImmutableHolder<Throwable>? = null,

    val hasMore: Boolean = true,
    val nextPagePostId: Long = 0,
    val hasPrevious: Boolean = false,
    val currentPageMin: Int = 0,
    val currentPageMax: Int = 0,
    val totalPage: Int = 0,

    val seeLz: Boolean = false,
    val sortType: Int = ThreadSortType.SORT_TYPE_DEFAULT,
    val postId: Long = 0,
    val threadId: Long = 0,

    val title: String = "",
    val author: ImmutableHolder<User>? = null,
    val user: ImmutableHolder<User> = wrapImmutable(User()),
    val threadInfo: ImmutableHolder<ThreadInfo>? = null,
    val firstPost: ImmutableHolder<Post>? = null,
    val forum: ImmutableHolder<SimpleForum>? = null,
    val anti: ImmutableHolder<Anti>? = null,

    val firstPostContentRenders: ImmutableList<PbContentRender> = persistentListOf(),
    val data: ImmutableList<PostItemData> = persistentListOf(),
    val latestPosts: ImmutableList<PostItemData> = persistentListOf(),
    val postIds: ImmutableList<Long> = persistentListOf(),

    val initMeta: ThreadMeta? = null,
    val isImmersiveMode: Boolean = false,
) : UiState

object ThreadSortType {
    const val SORT_TYPE_ASC = 0
    const val SORT_TYPE_DESC = 1
    const val SORT_TYPE_HOT = 2
    const val SORT_TYPE_DEFAULT = SORT_TYPE_ASC
}

@Immutable
data class PostItemData(
    val post: ImmutableHolder<Post>,
    val blocked: Boolean = post.get { shouldBlock() },
    val contentRenders: ImmutableList<PbContentRender> = post.get { this.contentRenders },
    val subPosts: ImmutableList<SubPostItemData> = post.get { this.subPosts },
)

@Immutable
data class SubPostItemData(
    val subPost: ImmutableHolder<SubPostList>,
    val subPostContent: AnnotatedString,
    val blocked: Boolean = subPost.get { shouldBlock() },
) {
    val id: Long
        get() = subPost.get { id }

    val author: ImmutableHolder<User>?
        get() = subPost.get { author }?.wrapImmutable()
}
