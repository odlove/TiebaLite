package com.huanchengfly.tieba.post.ui.page.subposts.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import com.huanchengfly.tieba.core.theme.compose.dialogPrimaryButtonColors
import com.huanchengfly.tieba.core.theme.compose.dialogSecondaryButtonColors
import com.huanchengfly.tieba.core.theme.compose.scenes.ThemeDialog
import com.huanchengfly.tieba.feature.subposts.R
import com.huanchengfly.tieba.core.ui.R as CoreUiR
import com.huanchengfly.tieba.core.common.thread.ThreadSubPost
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.core.ui.compose.widgets.DialogState
import com.huanchengfly.tieba.core.ui.compose.widgets.TextButton

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
    subPost: ImmutableHolder<ThreadSubPost>?,
    onConfirm: () -> Unit,
) {
    if (!dialogState.show) return

    val message =
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
        )

    ThemeDialog(
        onDismissRequest = { dialogState.show = false },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(
                onClick = {
                    dialogState.show = false
                    onConfirm()
                },
                shape = RoundedCornerShape(100),
                colors = dialogPrimaryButtonColors(),
            ) {
                Text(text = stringResource(id = CoreUiR.string.button_sure_default))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { dialogState.show = false },
                shape = RoundedCornerShape(100),
                colors = dialogSecondaryButtonColors(),
            ) {
                Text(text = stringResource(id = CoreUiR.string.button_cancel))
            }
        },
    )
}
