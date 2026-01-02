package com.huanchengfly.tieba.post.ui.page.forum.rule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import com.huanchengfly.tieba.feature.forum.R
import com.huanchengfly.tieba.core.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.core.mvi.collectPartialAsState
import com.huanchengfly.tieba.core.mvi.getOrNull
import com.huanchengfly.tieba.core.ui.pageViewModel
import com.huanchengfly.tieba.core.ui.navigation.ProvideNavigator
import com.huanchengfly.tieba.core.ui.compose.widgets.Avatar
import com.huanchengfly.tieba.post.ui.common.DefaultBackIcon
import com.huanchengfly.tieba.core.ui.compose.widgets.ErrorScreen
import com.huanchengfly.tieba.core.ui.compose.base.LazyLoad
import com.huanchengfly.tieba.core.ui.compose.base.SnackbarScaffold
import com.huanchengfly.tieba.core.ui.compose.base.rememberSnackbarState
import com.huanchengfly.tieba.core.ui.compose.widgets.Sizes
import com.huanchengfly.tieba.core.ui.text.StringFormatUtils
import com.huanchengfly.tieba.core.theme.compose.scenes.ThemeTopAppBar
import com.huanchengfly.tieba.core.ui.compose.widgets.UserHeader
import com.huanchengfly.tieba.core.ui.compose.widgets.states.StateScreen
import com.huanchengfly.tieba.post.preferences.appPreferences
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.collections.immutable.persistentListOf


@Destination
@Composable
fun ForumRuleDetailPage(
    forumId: Long,
    navigator: DestinationsNavigator,
    viewModel: ForumRuleDetailViewModel = pageViewModel(),
) {
    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(ForumRuleDetailUiIntent.Load(forumId))
        viewModel.initialized = true
    }

    val isLoading by viewModel.uiState.collectPartialAsState(
        prop1 = ForumRuleDetailUiState::isLoading,
        initial = true
    )
    val error by viewModel.uiState.collectPartialAsState(
        prop1 = ForumRuleDetailUiState::error,
        initial = null
    )
    val title by viewModel.uiState.collectPartialAsState(
        prop1 = ForumRuleDetailUiState::title,
        initial = ""
    )
    val publishTime by viewModel.uiState.collectPartialAsState(
        prop1 = ForumRuleDetailUiState::publishTime,
        initial = ""
    )
    val preface by viewModel.uiState.collectPartialAsState(
        prop1 = ForumRuleDetailUiState::preface,
        initial = ""
    )
    val data by viewModel.uiState.collectPartialAsState(
        prop1 = ForumRuleDetailUiState::data,
        initial = persistentListOf()
    )
    val author by viewModel.uiState.collectPartialAsState(
        prop1 = ForumRuleDetailUiState::author,
        initial = null
    )

    ProvideNavigator(navigator = navigator) {
        StateScreen(
            modifier = Modifier.fillMaxSize(),
            isEmpty = data.isEmpty(),
            isError = error != null,
            isLoading = isLoading,
            onReload = {
                viewModel.send(ForumRuleDetailUiIntent.Load(forumId))
            },
            errorScreen = { ErrorScreen(error = error.getOrNull()) }
        ) {
        val snackbarState = rememberSnackbarState()
        SnackbarScaffold(
            snackbarState = snackbarState,
            topBar = {
                ThemeTopAppBar(
                    backgroundColor = ExtendedTheme.colors.topBar,
                    contentColor = ExtendedTheme.colors.onTopBar,
                    statusBarColor = ExtendedTheme.colors.topBar,
                    centerTitle = false,
                    title = {
                        Text(
                            text = stringResource(id = R.string.title_forum_rule),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        DefaultBackIcon(onBack = { navigator.navigateUp() })
                    }
                )
            }
        ) {
            Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = title, style = MaterialTheme.typography.h5)
                    author?.let { authorInfo ->
                        val context = LocalContext.current
                        val showBoth = remember(context) {
                            context.appPreferences.showBothUsernameAndNickname
                        }
                        UserHeader(
                            avatar = {
                                Avatar(
                                    data = StringFormatUtils.getAvatarUrl(authorInfo.get { portrait }),
                                    size = Sizes.Small,
                                    contentDescription = null
                                )
                            },
                            name = {
                                Text(
                                    text = StringFormatUtils.formatUsernameAnnotated(
                                        showBoth = showBoth,
                                        username = authorInfo.get { userName },
                                        nickname = authorInfo.get { nameShow },
                                        color = LocalContentColor.current
                                    )
                                )
                            },
                            desc = (@Composable {
                                Text(text = publishTime)
                            }).takeIf { publishTime.isNotEmpty() }
                        )
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProvideTextStyle(value = MaterialTheme.typography.body1) {
                            Text(text = preface)
                            data.forEach {
                                if (it.title.isNotEmpty()) {
                                    Text(
                                        text = it.title,
                                        style = MaterialTheme.typography.subtitle1
                                    )
                                }
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (it.contentText.isNotEmpty()) {
                                        Text(text = it.contentText)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
