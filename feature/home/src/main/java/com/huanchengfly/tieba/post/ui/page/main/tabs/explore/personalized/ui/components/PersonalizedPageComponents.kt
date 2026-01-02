package com.huanchengfly.tieba.post.ui.page.main.tabs.explore.personalized.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.core.common.feed.DislikeReason
import com.huanchengfly.tieba.core.common.feed.PersonalizedInfo
import com.huanchengfly.tieba.core.common.feed.ThreadCard
import com.huanchengfly.tieba.core.ui.R as CoreUiR
import com.huanchengfly.tieba.core.ui.compose.base.Container
import com.huanchengfly.tieba.core.ui.compose.base.MyLazyColumn
import com.huanchengfly.tieba.core.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.compose.widgets.BlockTip
import com.huanchengfly.tieba.core.ui.compose.widgets.BlockableContent
import com.huanchengfly.tieba.core.ui.compose.widgets.FeedCard
import com.huanchengfly.tieba.core.ui.compose.widgets.VerticalDivider
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun BoxScope.RefreshTip(refreshCount: Int) {
    Box(
        modifier = Modifier
            .padding(top = 72.dp)
            .clip(RoundedCornerShape(100))
            .background(
                color = ExtendedTheme.colors.primary,
                shape = RoundedCornerShape(100)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .align(Alignment.TopCenter)
    ) {
        Text(
            text = stringResource(id = CoreUiR.string.toast_feed_refresh, refreshCount),
            color = ExtendedTheme.colors.onAccent
        )
    }
}

@Immutable
internal data class PersonalizedThreadItem(
    val thread: ThreadCard,
    val personalized: PersonalizedInfo? = null,
    val blocked: Boolean = false,
    val hidden: Boolean = false,
)

@Composable
internal fun FeedList(
    state: LazyListState,
    dataProvider: () -> ImmutableList<PersonalizedThreadItem>,
    refreshPositionProvider: () -> Int,
    hiddenThreadIdsProvider: () -> ImmutableList<Long>,
    onItemClick: (ThreadCard) -> Unit,
    onItemReplyClick: (ThreadCard) -> Unit,
    onAgree: (ThreadCard) -> Unit,
    onDislike: (ThreadCard, Long, ImmutableList<DislikeReason>) -> Unit,
    onRefresh: () -> Unit,
    onOpenForum: (forumName: String) -> Unit = {},
    onClickUser: (Long) -> Unit = {},
) {
    val data = dataProvider()
    val refreshPosition = refreshPositionProvider()
    val hiddenThreadIds = hiddenThreadIdsProvider()

    MyLazyColumn(
        state = state,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        itemsIndexed(
            items = data,
            key = { _, threadItem -> "${threadItem.thread.threadId}" },
            contentType = { _, threadItem ->
                val thread = threadItem.thread
                when {
                    thread.videoInfo != null -> "Video"
                    thread.medias.size == 1 -> "SingleMedia"
                    thread.medias.size > 1 -> "MultiMedia"
                    else -> "PlainText"
                }
            }
        ) { index, threadItem ->
            val threadHolder = threadItem.thread
            val isHidden =
                remember(
                    hiddenThreadIds,
                    threadHolder,
                    threadItem.hidden
                ) { hiddenThreadIds.contains(threadHolder.threadId) || threadItem.hidden }
            val isRefreshPosition =
                remember(index, refreshPosition) { index + 1 == refreshPosition }
            val isNotLast = remember(index, data.size) { index < data.size - 1 }
            val showDivider = remember(
                isHidden,
                isRefreshPosition,
                isNotLast
            ) { !isHidden && !isRefreshPosition && isNotLast }
            Container {
                AnimatedVisibility(
                    visible = !isHidden,
                    enter = EnterTransition.None,
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        BlockableContent(
                            blocked = threadItem.blocked,
                            blockedTip = { BlockTip(text = { Text(text = stringResource(id = CoreUiR.string.tip_blocked_thread)) }) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                        ) {
                            Column {
                                FeedCard(
                                    item = threadHolder,
                                    onClick = onItemClick,
                                    onClickReply = onItemReplyClick,
                                    onAgree = onAgree,
                                    onClickForum = remember {
                                        {
                                            onOpenForum(it)
                                        }
                                    },
                                    onClickUser = onClickUser,
                                    agreeEnabled = true,
                                ) {
                                    threadItem.personalized?.let { personalized ->
                                        com.huanchengfly.tieba.post.ui.page.main.tabs.explore.components.Dislike(
                                            personalized = personalized,
                                            onDislike = { clickTime, reasons ->
                                                onDislike(threadHolder, clickTime, reasons)
                                            }
                                        )
                                    }
                                }
                                if (showDivider) {
                                    VerticalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        thickness = 2.dp
                                    )
                                }
                            }
                        }
                        if (isRefreshPosition) {
                            RefreshTip(onRefresh)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun RefreshTip(
    onRefresh: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onRefresh)
            .padding(8.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.Refresh,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(id = CoreUiR.string.tip_refresh),
            style = MaterialTheme.typography.subtitle1
        )
    }
}
