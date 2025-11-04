package com.huanchengfly.tieba.post.ui.page.main.explore

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.LocalGlobalEventBus
import com.huanchengfly.tieba.core.mvi.emitGlobalEvent
import com.huanchengfly.tieba.core.mvi.onGlobalEvent
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.page.LocalNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.SearchPageDestination
import com.huanchengfly.tieba.post.ui.page.main.explore.concern.ConcernPage
import com.huanchengfly.tieba.post.ui.page.main.explore.hot.HotPage
import com.huanchengfly.tieba.post.ui.page.main.explore.personalized.PersonalizedPage
import com.huanchengfly.tieba.post.ui.widgets.compose.ActionItem
import com.huanchengfly.tieba.core.ui.compose.LazyLoadHorizontalPager
import com.huanchengfly.tieba.core.ui.compose.PagerTabIndicator
import com.huanchengfly.tieba.core.ui.compose.TabRow
import com.huanchengfly.tieba.post.ui.widgets.compose.Toolbar
import com.huanchengfly.tieba.post.ui.widgets.compose.accountNavIconIfCompact
import com.huanchengfly.tieba.post.utils.AccountUtil.LocalAccount
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch


@Immutable
data class ExplorePageItem(
    val id: String,
    val name: @Composable (selected: Boolean) -> Unit,
    val content: @Composable () -> Unit,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ColumnScope.ExplorePageTab(
    pagerState: PagerState,
    pages: ImmutableList<ExplorePageItem>
) {
    val coroutineScope = rememberCoroutineScope()
    val globalEventBus = LocalGlobalEventBus.current

    TabRow(
        selectedTabIndex = pagerState.currentPage,
        indicator = { tabPositions ->
            PagerTabIndicator(
                pagerState = pagerState,
                tabPositions = tabPositions
            )
        },
        divider = {},
        backgroundColor = Color.Transparent,
        contentColor = ExtendedTheme.colors.onTopBar,
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .width(76.dp * pages.size),
    ) {
        pages.fastForEachIndexed { index, item ->
            Tab(
                text = { item.name(pagerState.currentPage == index) },
                selected = pagerState.currentPage == index,
                onClick = {
                    coroutineScope.launch {
                        if (pagerState.currentPage == index) {
                            coroutineScope.emitGlobalEvent(
                                globalEventBus,
                                CommonUiEvent.Refresh(item.id)
                            )
                        } else {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun TabText(
    text: String,
    selected: Boolean
) {
    val style = MaterialTheme.typography.button.copy(
        letterSpacing = 0.75.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        textAlign = TextAlign.Center
    )
    Text(text = text, style = style)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExplorePage() {
    val account = LocalAccount.current
    val navigator = LocalNavigator.current
    val globalEventBus = LocalGlobalEventBus.current

    val loggedIn = remember(account) { account != null }

    val pages = remember(loggedIn) {
        listOfNotNull(
            if (loggedIn) ExplorePageItem(
                "concern",
                { TabText(text = stringResource(id = R.string.title_concern), selected = it) },
                { ConcernPage() }
            ) else null,
            ExplorePageItem(
                "personalized",
                { TabText(text = stringResource(id = R.string.title_personalized), selected = it) },
                { PersonalizedPage() }
            ),
            ExplorePageItem(
                "hot",
                { TabText(text = stringResource(id = R.string.title_hot), selected = it) },
                { HotPage() }
            ),
        ).toImmutableList()
    }

    val pagerState = key(pages.size) {
        rememberPagerState(
            initialPage = if (loggedIn) 1 else 0
        ) { pages.size }
    }


    val coroutineScope = rememberCoroutineScope()

    onGlobalEvent<CommonUiEvent.Refresh>(
        filter = { it.key == "explore" }
    ) {
        if (pagerState.currentPage < pages.size) {
            coroutineScope.emitGlobalEvent(
                globalEventBus,
                CommonUiEvent.Refresh(pages[pagerState.currentPage].id)
            )
        }
    }

    Scaffold(
        backgroundColor = Color.Transparent,
        topBar = {
            Toolbar(
                title = stringResource(id = R.string.title_explore),
                navigationIcon = accountNavIconIfCompact(),
                actions = {
                    ActionItem(
                        icon = Icons.Rounded.Search,
                        contentDescription = stringResource(id = R.string.title_search)
                    ) {
                        navigator.navigate(SearchPageDestination)
                    }
                },
            ) {
                ExplorePageTab(pagerState = pagerState, pages = pages)
            }
        },
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        LazyLoadHorizontalPager(
            contentPadding = paddingValues,
            state = pagerState,
            key = { pages[it].id },
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top,
            userScrollEnabled = true,
            beyondViewportPageCount = 2,
        ) {
            pages[it].content()
        }
    }
}
