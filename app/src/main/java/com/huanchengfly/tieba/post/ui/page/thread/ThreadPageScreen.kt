package com.huanchengfly.tieba.post.ui.page.thread

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.huanchengfly.tieba.core.ui.compose.LazyLoad
import com.huanchengfly.tieba.core.ui.compose.rememberSnackbarState
import com.huanchengfly.tieba.core.ui.pageViewModel
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.ui.page.thread.ThreadUiIntent.Load
import com.huanchengfly.tieba.post.ui.page.thread.ThreadUiIntent.Init
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination

@Destination(
    deepLinks = [
        DeepLink(uriPattern = "tblite://thread/{threadId}")
    ]
)
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun ThreadPage(
    threadId: Long,
    navigator: com.ramcosta.composedestinations.navigation.DestinationsNavigator,
    forumId: Long? = null,
    postId: Long = 0,
    seeLz: Boolean = false,
    sortType: Int = 0,
    from: String = "",
    extra: ThreadPageExtra? = null,
    threadInfo: ThreadInfo? = null,
    scrollToReply: Boolean = false,
    viewModel: ThreadViewModel = pageViewModel(),
) {
    ThreadPageScreen(
        threadId = threadId,
        navigator = navigator,
        forumId = forumId,
        postId = postId,
        seeLz = seeLz,
        sortType = sortType,
        from = from,
        extra = extra,
        threadInfo = threadInfo,
        scrollToReply = scrollToReply,
        viewModel = viewModel
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun ThreadPageScreen(
    threadId: Long,
    navigator: com.ramcosta.composedestinations.navigation.DestinationsNavigator,
    forumId: Long?,
    postId: Long,
    seeLz: Boolean,
    sortType: Int,
    from: String,
    extra: ThreadPageExtra?,
    threadInfo: ThreadInfo?,
    scrollToReply: Boolean,
    viewModel: ThreadViewModel = pageViewModel(),
) {
    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(
            Init(
                threadId,
                forumId,
                postId,
                threadInfo,
                seeLz,
                sortType
            )
        )
        viewModel.send(
            Load(
                threadId,
                page = 0,
                postId = postId,
                forumId = forumId,
                seeLz = seeLz,
                sortType = sortType,
                from = from
            )
        )
        viewModel.initialized = true
    }

    val snackbarState = rememberSnackbarState()
    val uiState by viewModel.uiState.collectAsState(initial = ThreadUiState())
    val threadFeeds = rememberThreadFeeds(
        viewModel = viewModel,
        routeThreadId = threadId,
        uiState = uiState
    )

    val pageState = remember { ThreadPageState() }
    LaunchedEffect(uiState, threadFeeds.threadEntity, threadFeeds.postEntities, threadId, forumId) {
        ThreadPageStateMapper.map(
            state = pageState,
            routeThreadId = threadId,
            routeForumId = forumId,
            uiState = uiState,
            repositoryThread = threadFeeds.threadEntity,
            postEntities = threadFeeds.postEntities
        )
    }

    val actions = remember(viewModel) { ThreadPageActionsImpl(viewModel) }
    val dialogs = rememberThreadPageDialogs()

    ThreadPageLayout(
        navigator = navigator,
        threadId = threadId,
        forumId = forumId,
        postId = postId,
        seeLz = seeLz,
        sortType = sortType,
        from = from,
        extra = extra,
        scrollToReply = scrollToReply,
        pageState = pageState,
        dialogs = dialogs,
        snackbarState = snackbarState,
        actions = actions,
        viewModel = viewModel
    )
}
