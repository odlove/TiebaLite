package com.huanchengfly.tieba.post.ui.page.main.explore.hot

import com.huanchengfly.tieba.core.mvi.bindScrollToTopEvent

import android.graphics.Typeface
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eygraber.compose.placeholder.material.placeholder
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.protos.hasAgree
import com.huanchengfly.tieba.post.arch.GlobalEvent
import com.huanchengfly.tieba.core.mvi.collectPartialAsState
import com.huanchengfly.tieba.core.mvi.onGlobalEvent
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.core.mvi.wrapImmutable
import kotlinx.collections.immutable.toImmutableList
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.theme.compose.OrangeA700
import com.huanchengfly.tieba.post.ui.common.theme.compose.RedA700
import com.huanchengfly.tieba.post.ui.common.theme.compose.White
import com.huanchengfly.tieba.post.ui.common.theme.compose.Yellow
import com.huanchengfly.tieba.post.ui.common.theme.compose.pullRefreshIndicator
import com.huanchengfly.tieba.post.ui.page.LocalNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.ForumPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.HotTopicListPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.ThreadPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.UserProfilePageDestination
import com.huanchengfly.tieba.post.ui.widgets.compose.Container
import com.huanchengfly.tieba.post.ui.widgets.compose.FeedCard
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.MyLazyColumn
import com.huanchengfly.tieba.post.ui.widgets.compose.ProvideContentColor
import com.huanchengfly.tieba.post.ui.widgets.compose.VerticalDivider
import com.huanchengfly.tieba.post.ui.widgets.compose.VerticalGrid
import com.huanchengfly.tieba.post.ui.widgets.compose.items
import com.huanchengfly.tieba.post.ui.widgets.compose.itemsIndexed
import com.huanchengfly.tieba.post.utils.StringUtil.getShortNumString
import com.ramcosta.composedestinations.annotation.Destination

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HotPage(
    viewModel: HotViewModel = pageViewModel()
) {

    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(HotUiIntent.Load)
        viewModel.initialized = true
    }
    val navigator = LocalNavigator.current
    val listState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    viewModel.bindScrollToTopEvent(lazyListState = listState)
    onGlobalEvent<GlobalEvent.Refresh>(
        filter = { it.key == "hot" }
    ) {
        viewModel.send(HotUiIntent.Load)
    }
    val isLoading by viewModel.uiState.collectPartialAsState(
        prop1 = HotUiState::isRefreshing,
        initial = false
    )
    val topicList by viewModel.uiState.collectPartialAsState(
        prop1 = HotUiState::topicList,
        initial = emptyList()
    )
    val tabList by viewModel.uiState.collectPartialAsState(
        prop1 = HotUiState::tabList,
        initial = emptyList()
    )
    val currentTabCode by viewModel.uiState.collectPartialAsState(
        prop1 = HotUiState::currentTabCode,
        initial = "all"
    )
    val isLoadingThreadList by viewModel.uiState.collectPartialAsState(
        prop1 = HotUiState::isLoadingThreadList,
        initial = false
    )
    val threadIds by viewModel.uiState.collectPartialAsState(
        prop1 = HotUiState::threadIds,
        initial = emptyList()
    )

    // ✅ 订阅 Repository 的 threadsFlow，获取最新的 ThreadEntity 列表
    val threadEntities by viewModel.pbPageRepository.threadsFlow(threadIds)
        .collectAsState(initial = emptyList())

    // ✅ O(n) 查找优化：先构建 entityMap
    val entityMap by remember(threadEntities) {
        derivedStateOf {
            threadEntities.associateBy { it.threadId }
        }
    }

    // ✅ 从 Store 构建显示数据（仅包含 Meta 更新，无额外元数据）
    val displayThreadList by remember(threadIds, entityMap) {
        derivedStateOf {
            threadIds.mapNotNull { threadId ->
                val entity = entityMap[threadId] ?: return@mapNotNull null  // Store 中不存在（已被 TTL 清理）

                // 🔍 调试日志：打印关键信息
                Log.d("HotPage_DisplayData", "threadId=$threadId, entity.threadId=${entity.threadId}, proto.id=${entity.proto.id}")
                Log.d("HotPage_DisplayData", "  abstract=${entity.proto._abstract.size}, media=${entity.proto.media.size}")
                Log.d("HotPage_DisplayData", "  title='${entity.proto.title}', agreeNum=${entity.meta.agreeNum}")

                // 从 Store Entity 构建 ThreadInfo，用 Meta 覆盖 Proto
                entity.proto.copy(
                    agreeNum = entity.meta.agreeNum,
                    agree = entity.proto.agree?.copy(
                        hasAgree = entity.meta.hasAgree,
                        agreeNum = entity.meta.agreeNum.toLong()
                    ),
                    // ✅ 新增：同步收藏状态，防止详情页显示旧值
                    collectStatus = entity.meta.collectStatus,
                    collectMarkPid = entity.meta.collectMarkPid.takeIf { it > 0L }?.toString() ?: "0"
                ).wrapImmutable()
            }.toImmutableList()
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = { viewModel.send(HotUiIntent.Load) })
    Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
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
                        ) { ChipHeader(text = stringResource(id = R.string.hot_topic_rank)) }
                    }
                }
                item(key = "TopicList") {
                    Container {
                        VerticalGrid(
                            column = 2,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            itemsIndexed(
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
                                            text = stringResource(id = R.string.topic_tag_hot),
                                            fontSize = 10.sp,
                                            color = White,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(RedA700)
                                                .padding(vertical = 2.dp, horizontal = 4.dp)
                                        )

                                        1 -> Text(
                                            text = stringResource(id = R.string.topic_tag_new),
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
                                                navigator.navigate(HotTopicListPageDestination)
                                            }
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.tip_more_topic),
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
                                        text = stringResource(id = R.string.tab_all_hot_thread),
                                        selected = currentTabCode == "all",
                                        onSelected = {
                                            viewModel.send(
                                                HotUiIntent.RefreshThreadList(
                                                    "all"
                                                )
                                            )
                                        }
                                    )
                                }
                                items(tabList) {
                                    ThreadListTab(
                                        text = it.get { tabName },
                                        selected = currentTabCode == it.get { tabCode },
                                        onSelected = {
                                            viewModel.send(
                                                HotUiIntent.RefreshThreadList(
                                                    it.get { tabCode })
                                            )
                                        }
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
                            text = stringResource(id = R.string.hot_thread_rank_rule),
                            color = ExtendedTheme.colors.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                }
                itemsIndexed(
                    items = displayThreadList,  // ✅ 使用 Store 增强的数据
                    key = { _, item -> "Thread_${item.get { threadId }}" }
                ) { index, item ->
                    Container {
                        // ✅ 订阅是否正在更新中
                        val isUpdating by viewModel.pbPageRepository.isThreadUpdating(item.get { id })
                            .collectAsState(initial = false)

                        FeedCard(
                            item = item,
                            onClick = {
                                navigator.navigate(
                                    ThreadPageDestination(
                                        threadId = it.threadId,
                                        threadInfo = it
                                    )
                                )
                            },
                            onClickReply = {
                                navigator.navigate(
                                    ThreadPageDestination(
                                        threadId = it.threadId,
                                        scrollToReply = true
                                    )
                                )
                            },
                            onAgree = { threadInfo ->
                                viewModel.send(
                                    HotUiIntent.Agree(
                                        threadId = threadInfo.id,
                                        postId = threadInfo.firstPostId,
                                        hasAgree = threadInfo.hasAgree
                                    )
                                )
                            },
                            onClickForum = { navigator.navigate(ForumPageDestination(it.name)) },
                            onClickUser = { navigator.navigate(UserProfilePageDestination(it.id)) },
                            agreeEnabled = !isUpdating,  // ✅ 传递 enabled 状态
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
                                        id = R.string.hot_num,
                                        item.get { hotNum }.getShortNumString()
                                    ),
                                    style = MaterialTheme.typography.caption,
                                    color = color
                                )
                            }
                        }
                    }
//                        ThreadListItem(
//                            index = index,
//                            itemHolder = item,
//                            onClick = {
//                                navigator.navigate(
//                                    ThreadPageDestination(
//                                        threadId = it.id,
//                                        threadInfo = it
//                                    )
//                                )
//                            }
//                        )
                    }
            }
        }

        PullRefreshIndicator(
            refreshing = isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = ExtendedTheme.colors.pullRefreshIndicator,
            contentColor = ExtendedTheme.colors.primary,
        )
    }
}

@Composable
private fun ThreadListItemPlaceholder() {
    Row(modifier = Modifier.padding(all = 16.dp)) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "1",
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = ExtendedTheme.colors.background,
                modifier = Modifier
                    .padding(top = 3.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .wrapContentSize()
                    .placeholder(visible = true, color = MaterialTheme.colors.surface)
                    .padding(vertical = 1.dp, horizontal = 4.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .placeholder(visible = true, color = MaterialTheme.colors.surface)
                )
                Text(
                    text = stringResource(id = R.string.hot_num, "666"),
                    style = MaterialTheme.typography.caption,
                    color = ExtendedTheme.colors.textSecondary,
                    modifier = Modifier.placeholder(visible = true, color = MaterialTheme.colors.surface)
                )
            }
        }
    }
}

@Composable
private fun ThreadListTab(
    text: String,
    selected: Boolean,
    onSelected: () -> Unit
) {
    val textColor by animateColorAsState(targetValue = if (selected) ExtendedTheme.colors.onAccent else ExtendedTheme.colors.onChip)
    val backgroundColor by animateColorAsState(targetValue = if (selected) ExtendedTheme.colors.primary else ExtendedTheme.colors.chip)
    Text(
        text = text,
        textAlign = TextAlign.Center,
        color = textColor,
        maxLines = 1,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(100))
            .background(backgroundColor)
            .clickable(onClick = onSelected)
            .padding(vertical = 4.dp),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
    )
}


@Composable
private fun ChipHeader(
    text: String,
    invert: Boolean = false,
    modifier: Modifier = Modifier
) {
    Text(
        color = if (invert) MaterialTheme.colors.onSecondary else ExtendedTheme.colors.onChip,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(100))
            .then(modifier)
            .background(color = if (invert) MaterialTheme.colors.secondary else ExtendedTheme.colors.chip)
            .padding(horizontal = 16.dp, vertical = 4.dp)
    )
}
