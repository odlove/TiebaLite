package com.huanchengfly.tieba.post.ui.page.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.core.mvi.LocalGlobalEventBus
import com.huanchengfly.tieba.core.mvi.emitGlobalEvent
import com.huanchengfly.tieba.core.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.core.theme.compose.tabSelectedColor
import com.huanchengfly.tieba.core.theme.compose.tabUnselectedColor
import com.huanchengfly.tieba.post.ui.page.history.list.HistoryListPage
import com.huanchengfly.tieba.post.ui.page.history.list.HistoryListUiEvent
import com.huanchengfly.tieba.post.ui.common.DefaultBackIcon
import com.huanchengfly.tieba.core.ui.compose.base.SnackbarScaffold
import com.huanchengfly.tieba.core.ui.compose.base.rememberSnackbarState
import com.huanchengfly.tieba.core.theme.compose.PagerTabIndicator
import com.huanchengfly.tieba.core.theme.compose.TabRow
import com.huanchengfly.tieba.core.ui.navigation.LocalHomeNavigation
import com.huanchengfly.tieba.core.theme.compose.scenes.ThemeTopAppBar
import com.huanchengfly.tieba.core.common.history.HistoryRepository
import com.huanchengfly.tieba.post.di.entrypoints.HistoryRepositoryEntryPoint
import com.huanchengfly.tieba.post.repository.ThreadPageFrom
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import com.huanchengfly.tieba.feature.history.R
import com.huanchengfly.tieba.core.ui.R as CoreUiR

@OptIn(ExperimentalFoundationApi::class)
@Destination(
    start = true,
    deepLinks = [
        DeepLink(uriPattern = "tblite://history")
    ]
)
@Composable
fun HistoryPage(
    navigator: DestinationsNavigator
) {
    val pagerState = rememberPagerState { 2 }
    val coroutineScope = rememberCoroutineScope()
    val snackbarState = rememberSnackbarState()
    val globalEventBus = LocalGlobalEventBus.current
    val homeNavigation = LocalHomeNavigation.current

    val context = LocalContext.current
    val historyRepository = remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            HistoryRepositoryEntryPoint::class.java
        ).historyRepository()
    }

    SnackbarScaffold(
        backgroundColor = Color.Transparent,
        snackbarState = snackbarState,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ExtendedTheme.colors.topBar)
            ) {
                ThemeTopAppBar(
                    backgroundColor = ExtendedTheme.colors.topBar,
                    contentColor = ExtendedTheme.colors.onTopBar,
                    statusBarColor = ExtendedTheme.colors.topBar,
                    centerTitle = true,
                    title = {
                        Text(
                            text = stringResource(id = CoreUiR.string.title_history),
                            fontWeight = FontWeight.Bold, style = MaterialTheme.typography.h6
                        )
                    },
                    navigationIcon = {
                        DefaultBackIcon(onBack = { navigator.navigateUp()  })
                    },
                    actions = {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                historyRepository.deleteAll()
                                globalEventBus.emitGlobalEvent(HistoryListUiEvent.DeleteAll)
                                snackbarState.showSnackbar(
                                    context.getString(CoreUiR.string.toast_clear_success)
                                )
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = stringResource(id = R.string.title_history_delete)
                            )
                        }
                    }
                )
                val selectedColor = tabSelectedColor()
                val unselectedColor = tabUnselectedColor()
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
                    contentColor = selectedColor,
                    modifier = Modifier
                        .width(100.dp * 2)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Tab(
                        text = {
                            Text(
                                text = stringResource(id = R.string.title_history_thread),
                                fontSize = 13.sp
                            )
                        },
                        selected = pagerState.currentPage == 0,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        },
                        selectedContentColor = selectedColor,
                        unselectedContentColor = unselectedColor
                    )
                    Tab(
                        text = {
                            Text(
                                text = stringResource(id = CoreUiR.string.title_history_forum),
                                fontSize = 13.sp
                            )
                        },
                        selected = pagerState.currentPage == 1,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        },
                        selectedContentColor = selectedColor,
                        unselectedContentColor = unselectedColor
                    )
                }
            }
        }
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { it },
            verticalAlignment = Alignment.Top,
            userScrollEnabled = true,
        ) {
            if (it == 0) {
                HistoryListPage(
                    type = HistoryRepository.TYPE_THREAD,
                    onOpenForum = { forumName ->
                        homeNavigation.openForum(forumName)
                    },
                    onOpenThread = { threadId, postId, seeLz ->
                        homeNavigation.openThread(
                            threadId = threadId,
                            postId = postId,
                            seeLz = seeLz,
                            from = ThreadPageFrom.FROM_HISTORY
                        )
                    }
                )
            } else {
                HistoryListPage(
                    type = HistoryRepository.TYPE_FORUM,
                    onOpenForum = { forumName ->
                        homeNavigation.openForum(forumName)
                    },
                    onOpenThread = { threadId, postId, seeLz ->
                        homeNavigation.openThread(
                            threadId = threadId,
                            postId = postId,
                            seeLz = seeLz,
                            from = ThreadPageFrom.FROM_HISTORY
                        )
                    }
                )
            }
        }
    }
}
