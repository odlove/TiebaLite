package com.huanchengfly.tieba.post.ui.page.hottopic.list

import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.Scaffold
import com.huanchengfly.tieba.core.common.hottopic.HotTopicItem
import com.huanchengfly.tieba.core.mvi.collectPartialAsState
import com.huanchengfly.tieba.core.ui.pageViewModel
import com.huanchengfly.tieba.core.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.core.theme.compose.OrangeA700
import com.huanchengfly.tieba.core.theme.compose.RedA700
import com.huanchengfly.tieba.core.theme.compose.Shapes
import com.huanchengfly.tieba.core.theme.compose.White
import com.huanchengfly.tieba.core.theme.compose.Yellow
import com.huanchengfly.tieba.core.theme.compose.pullRefreshIndicator
import com.huanchengfly.tieba.post.ui.common.DefaultBackIcon
import com.huanchengfly.tieba.core.ui.compose.base.MyLazyColumn
import com.huanchengfly.tieba.core.ui.compose.widgets.NetworkImage
import com.huanchengfly.tieba.core.ui.compose.widgets.Sizes
import com.huanchengfly.tieba.core.ui.compose.widgets.ThemeTopAppBar
import com.huanchengfly.tieba.core.common.utils.getShortNumString
import com.huanchengfly.tieba.feature.hottopic.R
import com.huanchengfly.tieba.core.ui.R as CoreUiR
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Composable
private fun TopicImage(
    index: Int,
    imageUri: String
) {
    val boxModifier = if (index < 3) {
        Modifier
            .fillMaxWidth()
            .aspectRatio(2.39f)
            .clip(Shapes.medium)
    } else {
        Modifier
            .size(Sizes.Medium)
            .aspectRatio(1f)
            .clip(Shapes.small)
    }
    Box(
        modifier = boxModifier
    ) {
        NetworkImage(
            imageUri = imageUri,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Text(
            text = "${index + 1}",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.background,
            fontFamily = FontFamily(
                Typeface.createFromAsset(
                    LocalContext.current.assets,
                    "bebas.ttf"
                )
            ),
            modifier = Modifier
                .background(
                    when (index) {
                        0 -> RedA700
                        1 -> OrangeA700
                        2 -> Yellow
                        else -> MaterialTheme.colors.onBackground.copy(ContentAlpha.medium)
                    }
                )
                .padding(4.dp)
        )
    }
}

@Composable
private fun TopicBody(
    index: Int,
    item: HotTopicItem
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = item.topicName, style = MaterialTheme.typography.subtitle1)
            when (item.topicTag) {
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
        Text(
            text = item.topicDesc,
            maxLines = if (index < 3) 3 else 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.body2
        )
        Text(
            text = stringResource(id = CoreUiR.string.hot_num, item.discussNum.getShortNumString()),
            style = MaterialTheme.typography.caption,
            color = ExtendedTheme.colors.textSecondary
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Destination
@Composable
fun HotTopicListPage(
    viewModel: HotTopicListViewModel = pageViewModel<HotTopicListUiIntent, HotTopicListViewModel>(
        listOf(HotTopicListUiIntent.Load)
    ),
    navigator: DestinationsNavigator,
) {
    val isRefreshing by viewModel.uiState.collectPartialAsState(
        prop1 = HotTopicListUiState::isRefreshing,
        initial = false
    )
    val topicList by viewModel.uiState.collectPartialAsState(
        prop1 = HotTopicListUiState::topicList,
        initial = emptyList()
    )
    Scaffold(
        backgroundColor = Color.Transparent,
        topBar = {
            ThemeTopAppBar(
                backgroundColor = ExtendedTheme.colors.topBar,
                contentColor = ExtendedTheme.colors.onTopBar,
                statusBarColor = ExtendedTheme.colors.topBar,
                centerTitle = true,
                title = {
                    Text(
                        text = stringResource(id = R.string.title_hot_message_list),
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.h6
                    )
                },
                navigationIcon = {
                    DefaultBackIcon(onBack = { navigator.navigateUp()  })
                }
            )
        },
    ) { contentPaddings ->
        val pullRefreshState = rememberPullRefreshState(
            refreshing = isRefreshing,
            onRefresh = { viewModel.send(HotTopicListUiIntent.Load) }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPaddings)
                .pullRefresh(pullRefreshState)
        ) {
            MyLazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                itemsIndexed(
                    items = topicList,
                    key = { _, item -> item.topicId },
                ) { index, item ->
                    if (index < 3) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TopicImage(index = index, imageUri = item.topicImage)
                            TopicBody(index = index, item = item)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            TopicImage(index = index, imageUri = item.topicImage)
                            TopicBody(index = index, item = item)
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = ExtendedTheme.colors.pullRefreshIndicator,
                contentColor = ExtendedTheme.colors.primary,
            )
        }
    }
}
