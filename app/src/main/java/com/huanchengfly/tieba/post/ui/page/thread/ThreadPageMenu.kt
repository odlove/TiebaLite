package com.huanchengfly.tieba.post.ui.page.thread

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ChromeReaderMode
import androidx.compose.material.icons.automirrored.rounded.ChromeReaderMode
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Face6
import androidx.compose.material.icons.rounded.FaceRetouchingOff
import androidx.compose.material.icons.rounded.Report
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.invertChipBackground
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.invertChipContent
import com.huanchengfly.tieba.core.ui.widgets.compose.ListMenuItem
import com.huanchengfly.tieba.core.ui.widgets.compose.VerticalGrid
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.widgets.compose.TextWithMinWidth

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ToggleButton(
    text: @Composable () -> Unit,
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    backgroundColor: Color = ExtendedTheme.colors.chip,
    contentColor: Color = ExtendedTheme.colors.text,
    selectedBackgroundColor: Color = ExtendedTheme.colors.invertChipBackground,
    selectedContentColor: Color = ExtendedTheme.colors.invertChipContent,
) {
    val animatedColor by animateColorAsState(
        if (checked) selectedContentColor else contentColor,
        label = "toggleBtnColor"
    )
    val animatedBackgroundColor by animateColorAsState(
        if (checked) selectedBackgroundColor else backgroundColor,
        label = "toggleBtnBackgroundColor"
    )

    Surface(
        onClick = onClick,
        modifier = modifier,
        enabled = true,
        shape = RoundedCornerShape(6.dp),
        color = animatedBackgroundColor,
        contentColor = animatedColor,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (icon != null) {
                icon()
            }
            ProvideTextStyle(
                value = MaterialTheme.typography.subtitle1.merge(
                    TextStyle(fontSize = 14.sp, textAlign = TextAlign.Center)
                )
            ) {
                text()
            }
        }
    }
}

@Composable
fun ThreadMenu(
    isSeeLz: Boolean,
    isCollected: Boolean,
    isImmersiveMode: Boolean,
    isDesc: Boolean,
    canDelete: () -> Boolean,
    onSeeLzClick: () -> Unit,
    onCollectClick: () -> Unit,
    onImmersiveModeClick: () -> Unit,
    onDescClick: () -> Unit,
    onJumpPageClick: () -> Unit,
    onShareClick: () -> Unit,
    onCopyLinkClick: () -> Unit,
    onReportClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .height(4.dp)
                .fillMaxWidth(0.25f)
                .clip(RoundedCornerShape(100))
                .background(ExtendedTheme.colors.chip)
        )
        VerticalGrid(
            column = 2,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            rowModifier = Modifier.height(IntrinsicSize.Min),
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            item {
                ToggleButton(
                    text = {
                        TextWithMinWidth(
                            text = stringResource(id = R.string.title_see_lz),
                            minLength = 4
                        )
                    },
                    checked = isSeeLz,
                    onClick = onSeeLzClick,
                    icon = {
                        Icon(
                            imageVector = if (isSeeLz) Icons.Rounded.Face6 else Icons.Rounded.FaceRetouchingOff,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            item {
                ToggleButton(
                    text = {
                        TextWithMinWidth(
                            text = stringResource(
                                id = if (isCollected) R.string.title_collected else R.string.title_uncollected
                            ),
                            minLength = 4
                        )
                    },
                    checked = isCollected,
                    onClick = onCollectClick,
                    icon = {
                        Icon(
                            imageVector = if (isCollected) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            item {
                ToggleButton(
                    text = {
                        TextWithMinWidth(
                            text = stringResource(id = R.string.title_pure_read),
                            minLength = 4
                        )
                    },
                    checked = isImmersiveMode,
                    onClick = onImmersiveModeClick,
                    icon = {
                        Icon(
                            imageVector = if (isImmersiveMode) Icons.AutoMirrored.Rounded.ChromeReaderMode else Icons.AutoMirrored.Outlined.ChromeReaderMode,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            item {
                ToggleButton(
                    text = {
                        TextWithMinWidth(
                            text = stringResource(id = R.string.title_sort),
                            minLength = 4
                        )
                    },
                    checked = isDesc,
                    onClick = onDescClick,
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Sort,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Column {
            ListMenuItem(
                icon = Icons.Rounded.RocketLaunch,
                text = stringResource(id = R.string.title_jump_page),
                iconColor = ExtendedTheme.colors.text,
                onClick = onJumpPageClick,
                modifier = Modifier.fillMaxWidth(),
            )
            ListMenuItem(
                icon = Icons.Rounded.Share,
                text = stringResource(id = R.string.title_share),
                iconColor = ExtendedTheme.colors.text,
                onClick = onShareClick,
                modifier = Modifier.fillMaxWidth(),
            )
            ListMenuItem(
                icon = Icons.Rounded.ContentCopy,
                text = stringResource(id = R.string.title_copy_link),
                iconColor = ExtendedTheme.colors.text,
                onClick = onCopyLinkClick,
                modifier = Modifier.fillMaxWidth(),
            )
            ListMenuItem(
                icon = Icons.Rounded.Report,
                text = stringResource(id = R.string.title_report),
                iconColor = ExtendedTheme.colors.text,
                onClick = onReportClick,
                modifier = Modifier.fillMaxWidth(),
            )
            if (canDelete()) {
                ListMenuItem(
                    icon = Icons.Rounded.Delete,
                    text = stringResource(id = R.string.title_delete),
                    iconColor = ExtendedTheme.colors.text,
                    onClick = onDeleteClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
