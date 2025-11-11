package com.huanchengfly.tieba.post.ui.page.thread.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.ui.page.thread.ThreadPageActions
import com.huanchengfly.tieba.post.ui.page.thread.ThreadPageState
import com.huanchengfly.tieba.core.ui.widgets.compose.ConfirmDialog
import com.huanchengfly.tieba.core.ui.widgets.compose.DialogState
import com.huanchengfly.tieba.core.ui.widgets.compose.PromptDialog
import com.huanchengfly.tieba.post.toastShort
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Composable
fun ThreadCollectMarkDialog(
    dialogState: DialogState,
    readFloorBeforeBack: Int,
    lastVisibilityPostId: Long,
    pageState: ThreadPageState,
    actions: ThreadPageActions,
    navigator: DestinationsNavigator
) {
    ConfirmDialog(
        dialogState = dialogState,
        onConfirm = {
            if (lastVisibilityPostId != 0L) {
                actions.updateFavoriteMark(
                    threadId = pageState.threadId,
                    postId = lastVisibilityPostId
                )
            } else {
                navigator.navigateUp()
            }
        },
        onCancel = { navigator.navigateUp() }
    ) {
        Text(text = stringResource(R.string.message_update_collect_mark, readFloorBeforeBack))
    }
}

@Composable
fun ThreadDeleteDialog(
    dialogState: DialogState,
    pageState: ThreadPageState,
    deletePostState: MutableState<ImmutableHolder<Post>?>,
    actions: ThreadPageActions
) {
    ConfirmDialog(
        dialogState = dialogState,
        onConfirm = {
            val forumIdValue = pageState.curForumId ?: return@ConfirmDialog
            val deletePost = deletePostState.value
            if (deletePost == null) {
                val isSelfThread = pageState.author?.get { id } == pageState.user.get { id }
                actions.deleteThread(
                    forumId = forumIdValue,
                    forumName = pageState.curForumName.orEmpty(),
                    threadId = pageState.threadId,
                    deleteMyThread = isSelfThread,
                    tbs = pageState.curTbs
                )
            } else {
                val isSelfPost = deletePost.get { author_id } == pageState.user.get { id }
                actions.deletePost(
                    forumId = forumIdValue,
                    forumName = pageState.curForumName.orEmpty(),
                    threadId = pageState.threadId,
                    postId = deletePost.get { id },
                    deleteMyPost = isSelfPost,
                    tbs = pageState.curTbs
                )
            }
        }
    ) {
        val deletePost = deletePostState.value
        val message = if (deletePost == null) {
            stringResource(id = R.string.this_thread)
        } else {
            stringResource(id = R.string.tip_post_floor, deletePost.get { floor })
        }
        Text(text = stringResource(id = R.string.message_confirm_delete, message))
    }
}

@Composable
fun ThreadJumpToPageDialog(
    dialogState: DialogState,
    pageState: ThreadPageState,
    actions: ThreadPageActions,
    forumId: Long?,
    postId: Long
) {
    val context = LocalContext.current
    PromptDialog(
        onConfirm = { targetPage ->
            val maxPage = pageState.totalPage.coerceAtLeast(1)
            val targetPageNumber = targetPage.toIntOrNull()?.takeIf { it > 0 }
            val finalPage = when {
                targetPageNumber == null -> {
                    context.toastShort(R.string.toast_jump_page_invalid_input)
                    return@PromptDialog
                }
                targetPageNumber > maxPage -> {
                    context.toastShort(
                        context.getString(R.string.toast_jump_page_clamped, maxPage)
                    )
                    maxPage
                }
                else -> targetPageNumber
            }
            actions.load(
                threadId = pageState.threadId,
                page = finalPage,
                postId = postId,
                forumId = pageState.curForumId ?: forumId,
                seeLz = pageState.isSeeLz,
                sortType = pageState.sortType
            )
        },
        dialogState = dialogState,
        onValueChange = { newVal, _ -> "^[0-9]*$".toRegex().matches(newVal) },
        title = { Text(text = stringResource(id = R.string.title_jump_page)) },
        content = {
            Text(
                text = stringResource(
                    id = R.string.tip_jump_page,
                    pageState.currentPageMax,
                    pageState.totalPage
                )
            )
        }
    )
}
