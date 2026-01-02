package com.huanchengfly.tieba.post.ui.page.main.tabs.explore.hot.ui.sections

import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.core.common.feed.HotTab
import com.huanchengfly.tieba.core.common.feed.HotTopic
import com.huanchengfly.tieba.core.common.feed.ThreadCard
import com.huanchengfly.tieba.core.common.utils.getShortNumString
import com.huanchengfly.tieba.core.mvi.ImmutableHolder
import com.huanchengfly.tieba.core.ui.R as CoreUiR
import com.huanchengfly.tieba.core.ui.compose.base.Container
import com.huanchengfly.tieba.core.ui.compose.base.MyLazyColumn
import com.huanchengfly.tieba.core.ui.compose.base.ProvideContentColor
import com.huanchengfly.tieba.core.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.core.theme.compose.OrangeA700
import com.huanchengfly.tieba.core.theme.compose.RedA700
import com.huanchengfly.tieba.core.theme.compose.White
import com.huanchengfly.tieba.core.theme.compose.Yellow
import com.huanchengfly.tieba.core.ui.compose.widgets.FeedCard
import com.huanchengfly.tieba.core.ui.compose.widgets.VerticalDivider
import com.huanchengfly.tieba.core.ui.compose.widgets.VerticalGrid
import com.huanchengfly.tieba.core.ui.compose.widgets.items as gridItems
import com.huanchengfly.tieba.core.ui.compose.widgets.itemsIndexed as gridItemsIndexed
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.hot.ui.components.ChipHeader
import com.huanchengfly.tieba.post.ui.page.main.tabs.explore.hot.ui.components.ThreadListTab
import kotlinx.coroutines.flow.Flow

@Composable
internal fun HotContentSection(
    listState: LazyListState,
    topicList: List<ImmutableHolder<HotTopic>>,
    tabList: List<ImmutableHolder<HotTab>>,
    currentTabCode: String,
    displayThreadList: List<ThreadCard>,
    onOpenTopicList: () -> Unit,
    onTabSelected: (String) -> Unit,
    onThreadClick: (ThreadCard) -> Unit,
    onThreadReplyClick: (ThreadCard) -> Unit,
    onAgree: (ThreadCard) -> Unit,
    onOpenForum: (String) -> Unit,
    onOpenUser: (Long) -> Unit,
    isThreadUpdatingFlow: (Long) -> Flow<Boolean>,
) {
    MyLazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        if (topicList.isNotEmpty()) {
            item(key = "TopicHeader") {
                Container {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .padding(horizontal = 16.dp)
                    ) { ChipHeader(text = stringResource(id = CoreUiR.string.hot_topic_rank)) }
                }
            }
            item(key = "TopicList") {
                Container {
                    VerticalGrid(
                        column = 2,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        gridItemsIndexed(
                            items = topicList,
                        ) { index, item ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontWeight = FontWeight.Bold,
                                    color = when (index) {
                                        0 -> RedA700
                                        1 -> OrangeA700
                                        2 -> Yellow
                                        else -> MaterialTheme.colors.onBackground.copy(
                                            ContentAlpha.medium
                                        )
                                    },
                                    fontFamily = FontFamily(
                                        Typeface.createFromAsset(
                                            LocalContext.current.assets,
                                            "bebas.ttf"
                                        )
                                    ),
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                                Text(
                                    text = item.get { topicName },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                when (item.get { tag }) {
                                    2 -> Text(
                                        text = stringResource(id = CoreUiR.string.topic_tag_hot),
                                        fontSize = 10.sp,
                                        color = White,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(RedA700)
                                            .padding(vertical = 2.dp, horizontal = 4.dp)
                                    )

                                    1 -> Text(
                                        text = stringResource(id = CoreUiR.string.topic_tag_new),
                                        fontSize = 10.sp,
                                        color = White,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(OrangeA700)
                                            .padding(vertical = 2.dp, horizontal = 4.dp)
                                    )
                                }
                            }
                        }
                        item {
                            ProvideContentColor(color = ExtendedTheme.colors.primary) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onOpenTopicList()
                                        }
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = stringResource(id = CoreUiR.string.tip_more_topic),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = Icons.Rounded.KeyboardArrowRight,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item(key = "TopicDivider") {
                Container {
                    VerticalDivider(
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 8.dp)
                            .padding(horizontal = 16.dp),
                        thickness = 2.dp
                    )
                }
            }
        }
        if (displayThreadList.isNotEmpty()) {
            if (tabList.isNotEmpty()) {
                item(key = "ThreadTabs") {
                    Container {
                        VerticalGrid(
                            column = 5,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ExtendedTheme.colors.background)
                                .padding(vertical = 8.dp)
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            item {
                                ThreadListTab(
                                    text = stringResource(id = CoreUiR.string.tab_all_hot_thread),
                                    selected = currentTabCode == "all",
                                    onSelected = { onTabSelected("all") }
                                )
                            }
                            gridItems(tabList) {
                                ThreadListTab(
                                    text = it.get { tabName },
                                    selected = currentTabCode == it.get { tabCode },
                                    onSelected = { onTabSelected(it.get { tabCode }) }
                                )
                            }
                        }
                    }
                }
            }
            item(key = "ThreadListTip") {
                Container(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = stringResource(id = CoreUiR.string.hot_thread_rank_rule),
                        color = ExtendedTheme.colors.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                    )
                }
            }
            itemsIndexed(
                items = displayThreadList,
                key = { _, item -> "Thread_${item.threadId}" }
            ) { index, item ->
                Container {
                    val isUpdating by isThreadUpdatingFlow(item.threadId).collectAsState(initial = false)

                    FeedCard(
                        item = item,
                        onClick = {
                            onThreadClick(it)
                        },
                        onClickReply = {
                            onThreadReplyClick(it)
                        },
                        onAgree = { threadInfo ->
                            onAgree(threadInfo)
                        },
                        onClickForum = { onOpenForum(it) },
                        onClickUser = { onOpenUser(it) },
                        agreeEnabled = !isUpdating,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val color = when (index) {
                                0 -> RedA700
                                1 -> OrangeA700
                                2 -> Yellow
                                else -> MaterialTheme.colors.onBackground.copy(
                                    ContentAlpha.medium
                                )
                            }
                            Text(
                                text = "${index + 1}",
                                color = color,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = stringResource(
                                    id = CoreUiR.string.hot_num,
                                    item.hotNum.getShortNumString()
                                ),
                                style = MaterialTheme.typography.caption,
                                color = color
                            )
                        }
                    }
                }
            }
        }
    }
}
