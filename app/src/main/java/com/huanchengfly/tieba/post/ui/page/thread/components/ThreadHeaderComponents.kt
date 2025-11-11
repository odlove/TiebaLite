package com.huanchengfly.tieba.post.ui.page.thread.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.core.ui.compose.Container
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.core.ui.widgets.compose.BackNavigationIcon
import com.huanchengfly.tieba.core.ui.widgets.compose.HorizontalDivider
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.protos.SimpleForum
import com.huanchengfly.tieba.post.ui.page.thread.ThreadPageState
import com.huanchengfly.tieba.core.ui.widgets.compose.TitleCentredToolbar
import com.huanchengfly.tieba.core.common.utils.getShortNumString

@Composable
fun ThreadInfoHeader(
    pageState: ThreadPageState,
    onAllClick: () -> Unit,
    onSeeLzClick: () -> Unit,
) {
    Container {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colors.background)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(
                    R.string.title_thread_header,
                    (pageState.displayThread?.get { replyNum } ?: 0).getShortNumString()
                ),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ExtendedTheme.colors.text,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(IntrinsicSize.Min)
            ) {
                Text(
                    text = stringResource(R.string.text_all),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            if (pageState.isSeeLz) {
                                onAllClick()
                            }
                        },
                    fontSize = 13.sp,
                    fontWeight = if (!pageState.isSeeLz) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (!pageState.isSeeLz) ExtendedTheme.colors.text else ExtendedTheme.colors.textSecondary,
                )
                HorizontalDivider()
                Text(
                    text = stringResource(R.string.title_see_lz),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            if (!pageState.isSeeLz) {
                                onSeeLzClick()
                            }
                        },
                    fontSize = 13.sp,
                    fontWeight = if (pageState.isSeeLz) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (pageState.isSeeLz) ExtendedTheme.colors.text else ExtendedTheme.colors.textSecondary,
                )
            }
        }
    }
}

@Composable
fun ThreadPageTopBar(
    forum: ImmutableHolder<SimpleForum>?,
    onBack: () -> Unit,
    onForumClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TitleCentredToolbar(
        title = {
            forum?.let {
                val forumName = it.get { name }
                if (forumName.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 48.dp)
                            .height(IntrinsicSize.Min)
                            .clip(RoundedCornerShape(100))
                            .background(ExtendedTheme.colors.chip)
                            .clickable(onClick = onForumClick)
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Avatar(
                            data = it.get { avatar },
                            contentDescription = forumName,
                            modifier = Modifier
                                .fillMaxHeight()
                                .aspectRatio(1f)
                        )

                        Text(
                            text = stringResource(id = R.string.title_forum, forumName),
                            fontSize = 14.sp,
                            color = ExtendedTheme.colors.text,
                            modifier = Modifier.padding(horizontal = 8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        },
        navigationIcon = {
            BackNavigationIcon(onBack)
        },
        modifier = modifier
    )
}
