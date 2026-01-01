package com.huanchengfly.tieba.post.ui.page.main.tabs.notifications.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Scaffold
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.huanchengfly.tieba.core.ui.R as CoreUiR
import com.huanchengfly.tieba.core.ui.compose.LazyLoadHorizontalPager
import com.huanchengfly.tieba.core.ui.compose.PagerTabIndicator
import com.huanchengfly.tieba.core.ui.compose.SnackbarScaffold
import com.huanchengfly.tieba.core.ui.compose.rememberSnackbarState
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.tabSelectedColor
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.tabUnselectedColor
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.scenes.ThemeTopAppBar
import com.huanchengfly.tieba.core.ui.widgets.compose.ActionItem
import com.huanchengfly.tieba.core.ui.widgets.compose.accountNavIconIfCompact
import com.huanchengfly.tieba.core.ui.navigation.LocalNavigator
import com.huanchengfly.tieba.core.ui.navigation.ProvideNavigator
import com.huanchengfly.tieba.post.ui.page.main.tabs.notifications.contract.NotificationsType
import com.huanchengfly.tieba.post.ui.page.main.tabs.notifications.list.ui.NotificationsListPage
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Destination(
    start = true,
    deepLinks = [DeepLink(uriPattern = "tblite://notifications/{initialTab}")]
)
@Composable
fun NotificationsPage(
    initialTab: Int = 0,
) {
    val navigator = LocalNavigator.current
    val pages = listOf<Pair<String, (@Composable () -> Unit)>>(
        stringResource(id = CoreUiR.string.title_reply_me) to @Composable {
            NotificationsListPage(type = NotificationsType.ReplyMe)
        },
        stringResource(id = CoreUiR.string.title_at_me) to @Composable {
            NotificationsListPage(type = NotificationsType.AtMe)
        }
    )
    val pagerState = rememberPagerState(
        initialPage = initialTab,
    ) { pages.size }
    val coroutineScope = rememberCoroutineScope()
    ProvideNavigator(navigator = navigator) {
        val snackbarState = rememberSnackbarState()
        val colors = ExtendedTheme.colors
        val tabContentColor = tabSelectedColor()
        val tabUnselectedColor = tabUnselectedColor()
        SnackbarScaffold(
            snackbarState = snackbarState,
            backgroundColor = Color.Transparent,
            topBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ThemeTopAppBar(
                        backgroundColor = colors.topBar,
                        contentColor = colors.onTopBar,
                        statusBarColor = colors.topBar,
                        centerTitle = false,
                        title = {
                            Text(
                                text = stringResource(id = CoreUiR.string.title_notifications),
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = accountNavIconIfCompact(),
                        actions = {
                            ActionItem(
                                icon = Icons.Rounded.Search,
                                contentDescription = stringResource(id = CoreUiR.string.title_search),
                                onClick = { navigator.navigateUp() } // placeholder; search dest在 app 模块
                            )
                        },
                    )
                    TabRow(
                        selectedTabIndex = pagerState.currentPage,
                        backgroundColor = Color.Transparent,
                        contentColor = tabContentColor,
                        indicator = { tabPositions ->
                            PagerTabIndicator(
                                pagerState = pagerState,
                                tabPositions = tabPositions
                            )
                        },
                        divider = {}
                    ) {
                        pages.forEachIndexed { index, page ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
                                },
                                selectedContentColor = tabContentColor,
                                unselectedContentColor = tabUnselectedColor,
                                text = { Text(text = page.first) },
                            )
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
        ) { paddingValues ->
            LazyLoadHorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                pages[it].second()
            }
        }
    }
}
