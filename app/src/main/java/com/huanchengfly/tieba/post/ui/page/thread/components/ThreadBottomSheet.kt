package com.huanchengfly.tieba.post.ui.page.thread.components

import android.content.Context
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.core.network.retrofit.doIfFailure
import com.huanchengfly.tieba.core.network.retrofit.doIfSuccess
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.booleanToString
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.components.dialogs.LoadingDialog
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.ui.page.destinations.WebViewPageDestination
import com.huanchengfly.tieba.post.ui.page.thread.ThreadMenu
import com.huanchengfly.tieba.post.ui.page.thread.ThreadPageActions
import com.huanchengfly.tieba.post.ui.page.thread.ThreadPageState
import com.huanchengfly.tieba.post.ui.page.thread.ThreadSortType
import com.huanchengfly.tieba.post.ui.page.thread.ThreadViewModel
import com.huanchengfly.tieba.core.ui.widgets.compose.DialogState
import com.huanchengfly.tieba.post.utils.TiebaUtil
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ThreadMenuSheetContent(
    pageState: ThreadPageState,
    forumId: Long?,
    lastVisibilityPost: ImmutableHolder<Post>?,
    bottomSheetState: ModalBottomSheetState,
    jumpToPageDialogState: DialogState,
    confirmDeleteDialogState: DialogState,
    deletePostState: MutableState<ImmutableHolder<Post>?>,
    actions: ThreadPageActions,
    navigator: DestinationsNavigator,
    viewModel: ThreadViewModel,
    coroutineScope: CoroutineScope,
    context: Context,
    closeBottomSheet: () -> Unit
) {
    ThreadMenu(
        isSeeLz = pageState.isSeeLz,
        isCollected = pageState.isCollected,
        isImmersiveMode = pageState.isImmersiveMode,
        isDesc = pageState.sortType == ThreadSortType.SORT_TYPE_DESC,
        canDelete = { pageState.author?.get { id } == pageState.user.get { id } },
        onSeeLzClick = {
            if (!bottomSheetState.isVisible) return@ThreadMenu
            actions.loadFirstPage(
                threadId = pageState.threadId,
                forumId = pageState.curForumId ?: forumId,
                seeLz = !pageState.isSeeLz,
                sortType = pageState.sortType
            )
            closeBottomSheet()
        },
        onCollectClick = {
            if (!bottomSheetState.isVisible) return@ThreadMenu
            val fid = pageState.curForumId ?: forumId
            val tbs = pageState.curTbs
            if (fid != null) {
                if (pageState.isCollected) {
                    actions.removeFavorite(
                        threadId = pageState.threadId,
                        forumId = fid,
                        tbs = tbs
                    )
                } else {
                    val readItem = lastVisibilityPost
                    if (readItem != null) {
                        actions.addFavorite(
                            threadId = pageState.threadId,
                            postId = readItem.get { id },
                            floor = readItem.get { floor }
                        )
                    }
                }
            }
            closeBottomSheet()
        },
        onImmersiveModeClick = {
            if (!bottomSheetState.isVisible) return@ThreadMenu
            if (!pageState.isImmersiveMode && !pageState.isSeeLz) {
                actions.loadFirstPage(
                    threadId = pageState.threadId,
                    forumId = pageState.curForumId ?: forumId,
                    seeLz = true,
                    sortType = pageState.sortType
                )
            }
            actions.toggleImmersiveMode(!pageState.isImmersiveMode)
            closeBottomSheet()
        },
        onDescClick = {
            if (!bottomSheetState.isVisible) return@ThreadMenu
            actions.loadFirstPage(
                threadId = pageState.threadId,
                forumId = pageState.curForumId ?: forumId,
                seeLz = pageState.isSeeLz,
                sortType = if (pageState.sortType != ThreadSortType.SORT_TYPE_DESC) {
                    ThreadSortType.SORT_TYPE_DESC
                } else {
                    ThreadSortType.SORT_TYPE_DEFAULT
                }
            )
            closeBottomSheet()
        },
        onJumpPageClick = {
            closeBottomSheet()
            jumpToPageDialogState.show()
        },
        onShareClick = {
            TiebaUtil.shareText(
                context,
                "https://tieba.baidu.com/p/${pageState.threadId}",
                pageState.threadTitle
            )
        },
        onCopyLinkClick = {
            TiebaUtil.copyText(
                context,
                "https://tieba.baidu.com/p/${pageState.threadId}?see_lz=${pageState.isSeeLz.booleanToString()}"
            )
        },
        onReportClick = {
            val firstPostId =
                pageState.displayThread?.get { firstPostId }.takeIf { it != 0L }
                    ?: pageState.firstPost?.get { id }
                    ?: 0L
            if (firstPostId == 0L) return@ThreadMenu
            coroutineScope.launch {
                val dialog = LoadingDialog(context).apply { show() }
                viewModel.checkReportPost(firstPostId.toString())
                    .doIfSuccess {
                        dialog.dismiss()
                        navigator.navigate(WebViewPageDestination(it.data.url))
                    }
                    .doIfFailure {
                        dialog.dismiss()
                        context.toastShort(R.string.toast_load_failed)
                    }
            }
        },
        onDeleteClick = {
            deletePostState.value = null
            confirmDeleteDialogState.show()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .defaultMinSize(minHeight = 1.dp)
    )
}
