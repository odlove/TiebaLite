package com.huanchengfly.tieba.post.ui.page.settings.block

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.HideSource
import androidx.compose.material.icons.outlined.VideocamOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.feature.settings.R
import com.huanchengfly.tieba.post.dataStore
import com.huanchengfly.tieba.post.ui.common.prefs.PrefsScreen
import com.huanchengfly.tieba.core.ui.compose.settings.SettingsSwitch
import com.huanchengfly.tieba.post.ui.page.settings.destinations.BlockListPageDestination
import com.huanchengfly.tieba.post.ui.page.settings.LeadingIcon
import com.huanchengfly.tieba.core.ui.compose.widgets.AvatarIcon
import com.huanchengfly.tieba.post.ui.common.DefaultBackIcon
import com.huanchengfly.tieba.core.ui.compose.base.SnackbarScaffold
import com.huanchengfly.tieba.core.ui.compose.base.rememberSnackbarState
import com.huanchengfly.tieba.core.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.compose.settings.SettingsItem
import com.huanchengfly.tieba.core.theme.compose.scenes.ThemeTopAppBar
import com.huanchengfly.tieba.core.ui.compose.widgets.Sizes
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.huanchengfly.tieba.post.utils.compose.calcStatusBarColor

@OptIn(ExperimentalMaterialApi::class)
@Destination
@Composable
fun BlockSettingsPage(
    navigator: DestinationsNavigator
) {
    val context = LocalContext.current
    val snackbarState = rememberSnackbarState()
    SnackbarScaffold(
        snackbarState = snackbarState,
        backgroundColor = ExtendedTheme.colors.background,
        topBar = {
            val topBarColor = ExtendedTheme.colors.topBar
            val statusBarColor = topBarColor.calcStatusBarColor()
            ThemeTopAppBar(
                backgroundColor = topBarColor,
                statusBarColor = statusBarColor,
                centerTitle = true,
                title = {
                    Text(
                        text = stringResource(id = R.string.title_block_settings),
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.h6
                    )
                },
                navigationIcon = {
                    DefaultBackIcon(onBack = { navigator.navigateUp()  })
                }
            )
        },
    ) { paddingValues ->
        PrefsScreen(
            dataStore = context.dataStore,
            dividerThickness = 0.dp,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            prefsItem {
                SettingsItem(
                    onClick = { navigator.navigate(BlockListPageDestination) },
                    leadingContent = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.Block,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    title = { Text(text = stringResource(id = R.string.title_block_list)) }
                )
            }
            prefsItem {
                SettingsSwitch(
                    key = "hideBlockedContent",
                    title = stringResource(id = R.string.settings_hide_blocked_content),
                    defaultChecked = false,
                    leadingContent = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.HideSource,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    }
                )
            }
            prefsItem {
                SettingsSwitch(
                    key = "blockVideo",
                    title = stringResource(id = R.string.settings_block_video),
                    defaultChecked = false,
                    summary = { stringResource(id = R.string.settings_block_video_summary) },
                    leadingContent = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.VideocamOff,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    }
                )
            }
        }
    }
}
