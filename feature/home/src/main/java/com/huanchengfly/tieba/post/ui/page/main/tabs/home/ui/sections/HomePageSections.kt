package com.huanchengfly.tieba.post.ui.page.main.tabs.home.ui.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.core.common.history.HistoryItem
import com.huanchengfly.tieba.core.ui.R as CoreUiR
import com.huanchengfly.tieba.core.ui.compose.MyLazyVerticalGrid
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.scenes.ThemeSearchBox
import com.huanchengfly.tieba.core.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.post.ui.page.main.tabs.home.contract.HomeUiState
import com.huanchengfly.tieba.post.ui.page.main.tabs.home.ui.components.ForumItem
import com.huanchengfly.tieba.post.ui.page.main.tabs.home.ui.components.Header

@Composable
internal fun HomeSearchSection(
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ThemeSearchBox(
        value = "",
        onValueChange = {},
        modifier = modifier
            .padding(bottom = 4.dp, start = 16.dp, end = 16.dp, top = 8.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onSearchClick
            ),
        enabled = false,
        readOnly = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null
            )
        },
        placeholder = {
            Text(
                text = stringResource(id = CoreUiR.string.hint_search),
                fontSize = 14.sp
            )
        }
    )
}

@Composable
internal fun HomeForumGridSection(
    gridCells: GridCells,
    listSingle: Boolean,
    showHistoryForum: Boolean,
    expandHistoryForum: Boolean,
    historyForums: List<HistoryItem>,
    hasTopForum: Boolean,
    topForums: List<HomeUiState.Forum>,
    forums: List<HomeUiState.Forum>,
    onToggleHistory: (Boolean) -> Unit,
    onOpenForum: (String) -> Unit,
    onUnfollow: (HomeUiState.Forum) -> Unit,
    onAddTopForum: (HomeUiState.Forum) -> Unit,
    onDeleteTopForum: (HomeUiState.Forum) -> Unit,
    modifier: Modifier = Modifier,
) {
    MyLazyVerticalGrid(
        columns = gridCells,
        contentPadding = PaddingValues(bottom = 0.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        if (showHistoryForum) {
            item(key = "HistoryForums", span = { GridItemSpan(maxLineSpan) }) {
                val rotate by animateFloatAsState(
                    targetValue = if (expandHistoryForum) 90f else 0f,
                    label = "rotate"
                )
                Column {
                    Row(
                        verticalAlignment = CenterVertically,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onToggleHistory(expandHistoryForum)
                            }
                            .padding(vertical = 8.dp)
                            .padding(end = 16.dp)
                    ) {
                        Header(
                            text = stringResource(id = CoreUiR.string.title_history_forum),
                            invert = false
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = stringResource(id = CoreUiR.string.desc_show),
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(rotate)
                        )
                    }
                    AnimatedVisibility(visible = expandHistoryForum) {
                        LazyRow(
                            contentPadding = PaddingValues(bottom = 8.dp),
                        ) {
                            item(key = "Spacer1") {
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            items(
                                historyForums,
                                key = { it.data }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .height(IntrinsicSize.Min)
                                        .clip(RoundedCornerShape(100))
                                        .background(color = ExtendedTheme.colors.chip)
                                        .clickable {
                                            onOpenForum(it.data)
                                        }
                                        .padding(4.dp),
                                    verticalAlignment = CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Avatar(
                                        data = it.avatar,
                                        contentDescription = null,
                                        size = 24.dp,
                                        shape = CircleShape
                                    )
                                    Text(
                                        text = it.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                }
                            }
                            item(key = "Spacer2") {
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                        }
                    }
                }
            }
        }
        if (hasTopForum) {
            item(key = "TopForumHeader", span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Header(
                        text = stringResource(id = CoreUiR.string.title_top_forum),
                        invert = true
                    )
                }
            }
            gridItems(
                items = topForums,
                key = { "Top${it.forumId}" }
            ) { item ->
                ForumItem(
                    item,
                    listSingle,
                    onClick = { onOpenForum(it.forumName) },
                    onUnfollow = onUnfollow,
                    onAddTopForum = onAddTopForum,
                    onDeleteTopForum = onDeleteTopForum,
                    isTopForum = true
                )
            }
        }
        if (showHistoryForum || hasTopForum) {
            item(key = "ForumHeader", span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Header(text = stringResource(id = CoreUiR.string.forum_list_title))
                }
            }
        }
        gridItems(
            items = forums,
            key = { it.forumId }
        ) { item ->
            ForumItem(
                item,
                listSingle,
                onClick = {
                    onOpenForum(it.forumName)
                },
                onUnfollow = onUnfollow,
                onAddTopForum = onAddTopForum,
                onDeleteTopForum = onDeleteTopForum
            )
        }
    }
}
