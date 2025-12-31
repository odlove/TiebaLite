package com.huanchengfly.tieba.post.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.runtime.preview.ClipBoardLink
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.core.ui.widgets.compose.AvatarIcon
import com.huanchengfly.tieba.core.ui.widgets.compose.Dialog
import com.huanchengfly.tieba.core.ui.widgets.compose.DialogNegativeButton
import com.huanchengfly.tieba.core.ui.widgets.compose.DialogPositiveButton
import com.huanchengfly.tieba.core.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.core.ui.widgets.compose.rememberDialogState
import com.huanchengfly.tieba.feature.clipboard.R

@Composable
fun ClipBoardDetectDialog(
    clipBoardLinkDetector: ClipBoardLinkDetector,
    onOpenClipBoardLink: (ClipBoardLink) -> Unit,
) {
    val previewInfo by clipBoardLinkDetector.previewInfoStateFlow.collectAsState()
    val dialogState = rememberDialogState()

    LaunchedEffect(previewInfo) {
        if (previewInfo != null && !dialogState.show) {
            dialogState.show()
        }
    }

    Dialog(
        onDismiss = { clipBoardLinkDetector.clearPreview() },
        dialogState = dialogState,
        title = {
            Text(text = stringResource(id = R.string.title_dialog_clip_board_tieba_url))
        },
        buttons = {
            DialogPositiveButton(text = stringResource(id = R.string.button_open)) {
                previewInfo?.let {
                    onOpenClipBoardLink(it.clipBoardLink)
                }
            }
            DialogNegativeButton(text = stringResource(id = R.string.btn_close))
        },
    ) {
        previewInfo?.let {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                border = BorderStroke(1.dp, ExtendedTheme.colors.divider),
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    val icon = it.icon
                    val iconResId = icon.resId
                    val iconUrl = icon.url
                    when {
                        iconResId != null -> {
                            AvatarIcon(
                                resId = iconResId,
                                size = Sizes.Medium,
                                contentDescription = null
                            )
                        }

                        iconUrl != null -> {
                            Avatar(
                                data = iconUrl,
                                size = Sizes.Medium,
                                contentDescription = null
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = it.title, style = MaterialTheme.typography.subtitle1)
                        Text(text = it.subtitle, style = MaterialTheme.typography.body2)
                    }
                }
            }
        }
    }
}
