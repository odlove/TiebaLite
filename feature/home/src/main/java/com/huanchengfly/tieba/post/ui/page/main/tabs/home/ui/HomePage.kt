package com.huanchengfly.tieba.post.ui.page.main.tabs.home.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import com.huanchengfly.tieba.core.mvi.CommonUiEvent
import com.huanchengfly.tieba.core.mvi.collectPartialAsState
import com.huanchengfly.tieba.core.mvi.onGlobalEvent
import com.huanchengfly.tieba.core.ui.R as CoreUiR
import com.huanchengfly.tieba.core.ui.compose.base.SnackbarScaffold
import com.huanchengfly.tieba.core.ui.compose.base.rememberSnackbarState
import com.huanchengfly.tieba.core.ui.navigation.LocalHomeNavigation
import com.huanchengfly.tieba.core.ui.pageViewModel
import com.huanchengfly.tieba.core.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.core.theme.compose.pullRefreshIndicator
import com.huanchengfly.tieba.core.ui.compose.widgets.ThemeTopAppBar
import com.huanchengfly.tieba.core.ui.compose.widgets.ActionItem
import com.huanchengfly.tieba.core.ui.compose.widgets.ConfirmDialog
import com.huanchengfly.tieba.core.ui.compose.widgets.ErrorScreen
import com.huanchengfly.tieba.core.ui.compose.widgets.accountNavIconIfCompact
import com.huanchengfly.tieba.core.ui.compose.widgets.rememberDialogState
import com.huanchengfly.tieba.core.ui.compose.widgets.states.StateScreen
import com.huanchengfly.tieba.post.preferences.appPreferences
import com.huanchengfly.tieba.post.ui.page.main.tabs.home.contract.HomeUiIntent
import com.huanchengfly.tieba.post.ui.page.main.tabs.home.contract.HomeUiState
import com.huanchengfly.tieba.post.ui.page.main.tabs.home.ui.components.EmptyScreen
import com.huanchengfly.tieba.post.ui.page.main.tabs.home.ui.components.HomePageSkeletonScreen
import com.huanchengfly.tieba.post.ui.page.main.tabs.home.ui.components.getGridCells
import com.huanchengfly.tieba.post.ui.page.main.tabs.home.ui.sections.HomeForumGridSection
import com.huanchengfly.tieba.post.ui.page.main.tabs.home.ui.sections.HomeSearchSection
import com.huanchengfly.tieba.post.ui.page.main.tabs.home.viewmodel.HomeViewModel
import com.huanchengfly.tieba.post.utils.AccountUtil.LocalAccount
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomePage(
    viewModel: HomeViewModel = pageViewModel<HomeUiIntent, HomeViewModel>(listOf(HomeUiIntent.Refresh)),
    canOpenExplore: Boolean = false,
    onOpenExplore: () -> Unit = {},
) {
    val account = LocalAccount.current
    val context = LocalContext.current
    val navigator = LocalHomeNavigation.current
    val isLoading by viewModel.uiState.collectPartialAsState(
        prop1 = HomeUiState::isLoading,
        initial = true
    )
    val forums by viewModel.uiState.collectPartialAsState(
        prop1 = HomeUiState::forums,
        initial = persistentListOf()
    )
    val topForums by viewModel.uiState.collectPartialAsState(
        prop1 = HomeUiState::topForums,
        initial = persistentListOf()
    )
    val historyForums by viewModel.uiState.collectPartialAsState(
        prop1 = HomeUiState::historyForums,
        initial = persistentListOf()
    )
    val expandHistoryForum by viewModel.uiState.collectPartialAsState(
        prop1 = HomeUiState::expandHistoryForum,
        initial = true
    )
    val error by viewModel.uiState.collectPartialAsState(
        prop1 = HomeUiState::error,
        initial = null
    )
    val isLoggedIn = remember(account) { account != null }
    val isEmpty by remember { derivedStateOf { forums.isEmpty() } }
    val hasTopForum by remember { derivedStateOf { topForums.isNotEmpty() } }
    val showHistoryForum by remember { derivedStateOf { context.appPreferences.homePageShowHistoryForum && historyForums.isNotEmpty() } }
    var listSingle by remember { mutableStateOf(context.appPreferences.listSingle) }
    val isError by remember { derivedStateOf { error != null } }
    val gridCells by remember { derivedStateOf { getGridCells(context, listSingle) } }

    onGlobalEvent<CommonUiEvent.Refresh>(
        filter = { it.key == "home" }
    ) {
        viewModel.send(HomeUiIntent.Refresh)
    }

    var unfollowForum by remember { mutableStateOf<HomeUiState.Forum?>(null) }
    val confirmUnfollowDialog = rememberDialogState()
    ConfirmDialog(
        dialogState = confirmUnfollowDialog,
        onConfirm = {
            unfollowForum?.let {
                viewModel.send(HomeUiIntent.Unfollow(it.forumId, it.forumName))
            }
        },
    ) {
        Text(
            text = stringResource(
                id = CoreUiR.string.title_dialog_unfollow_forum,
                unfollowForum?.forumName.orEmpty()
            )
        )
    }

    LaunchedEffect(Unit) {
        if (viewModel.initialized) viewModel.send(HomeUiIntent.RefreshHistory)
    }

    val snackbarState = rememberSnackbarState()
    SnackbarScaffold(
        snackbarState = snackbarState,
        backgroundColor = Color.Transparent,
        topBar = {
            ThemeTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = CoreUiR.string.title_main),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = accountNavIconIfCompact(),
                actions = {
                    ActionItem(
                        icon = ImageVector.vectorResource(id = CoreUiR.drawable.ic_oksign),
                        contentDescription = stringResource(id = CoreUiR.string.title_oksign),
                        onClick = { /* TODO host triggers sign */ }
                    )
                    ActionItem(
                        icon = Icons.Outlined.ViewAgenda,
                        contentDescription = stringResource(id = CoreUiR.string.title_switch_list_single),
                        onClick = {
                            context.appPreferences.listSingle = !listSingle
                            listSingle = !listSingle
                        }
                    )
                }
            )
        },
        modifier = Modifier.fillMaxSize(),
    ) { contentPaddings ->
        val pullRefreshState = rememberPullRefreshState(
            refreshing = isLoading,
            onRefresh = { viewModel.send(HomeUiIntent.Refresh) }
        )
        Box(
            modifier = Modifier
                .pullRefresh(pullRefreshState)
                .padding(contentPaddings)
        ) {
            Column {
                HomeSearchSection(
                    onSearchClick = { navigator.openSearch() }
                )
                StateScreen(
                    isEmpty = isEmpty,
                    isError = isError,
                    isLoading = isLoading,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    onReload = {
                        viewModel.send(HomeUiIntent.Refresh)
                    },
                    emptyScreen = {
                        EmptyScreen(
                            loggedIn = isLoggedIn,
                            canOpenExplore = canOpenExplore,
                            onOpenExplore = onOpenExplore
                        )
                    },
                    loadingScreen = {
                        HomePageSkeletonScreen(listSingle = listSingle, gridCells = gridCells)
                    },
                    errorScreen = {
                        error?.let { ErrorScreen(error = it) }
                    }
                ) {
                    HomeForumGridSection(
                        gridCells = gridCells,
                        listSingle = listSingle,
                        showHistoryForum = showHistoryForum,
                        expandHistoryForum = expandHistoryForum,
                        historyForums = historyForums,
                        hasTopForum = hasTopForum,
                        topForums = topForums,
                        forums = forums,
                        onToggleHistory = {
                            viewModel.send(HomeUiIntent.ToggleHistory(it))
                        },
                        onOpenForum = { navigator.openForum(it) },
                        onUnfollow = {
                            unfollowForum = it
                            confirmUnfollowDialog.show()
                        },
                        onAddTopForum = { viewModel.send(HomeUiIntent.TopForums.Add(it)) },
                        onDeleteTopForum = { viewModel.send(HomeUiIntent.TopForums.Delete(it.forumId)) }
                    )
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
}
