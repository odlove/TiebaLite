package com.huanchengfly.tieba.post.ui.page.subposts.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.protos.SubPostList
import com.huanchengfly.tieba.post.arch.ImmutableHolder
import com.huanchengfly.tieba.post.ui.widgets.compose.ConfirmDialog
import com.huanchengfly.tieba.post.ui.widgets.compose.DialogState

/**
 * 删除确认对话框
 *
 * @param dialogState 对话框状态（由父组件管理）
 * @param postFloor 主楼的楼层号（用于显示提示信息）
 * @param subPost 要删除的楼中楼数据（为 null 时表示删除主楼）
 * @param onConfirm 确认删除的回调
 */
@Composable
fun SubPostsDeleteDialog(
    dialogState: DialogState,
    postFloor: Int?,
    subPost: ImmutableHolder<SubPostList>?,
    onConfirm: () -> Unit,
) {
    ConfirmDialog(
        dialogState = dialogState,
        onConfirm = onConfirm,
    ) {
        Text(
            text =
                stringResource(
                    id = R.string.message_confirm_delete,
                    if (subPost == null) {
                        // 删除主楼：显示楼层号
                        postFloor?.let {
                            stringResource(id = R.string.tip_post_floor, it)
                        } ?: stringResource(id = R.string.this_reply)
                    } else {
                        // 删除楼中楼：显示"这条回复"
                        stringResource(id = R.string.this_reply)
                    },
                ),
        )
    }
}
