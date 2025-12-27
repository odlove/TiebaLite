package com.huanchengfly.tieba.post.ui.page.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.dataStore
import com.huanchengfly.tieba.core.common.account.AccountInfo
import com.huanchengfly.tieba.post.ui.common.prefs.PrefsScreen
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.SettingsItem
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ThemeScaffold
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.scenes.ThemeTopAppBar
import com.huanchengfly.tieba.core.ui.navigation.LocalNavigator
import com.huanchengfly.tieba.core.ui.navigation.ProvideNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.AccountManagePageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.BlockSettingsPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.CustomSettingsPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.HabitSettingsPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.LoginPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.MoreSettingsPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.OKSignSettingsPageDestination
import com.huanchengfly.tieba.core.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.core.ui.widgets.compose.AvatarIcon
import com.huanchengfly.tieba.post.ui.common.DefaultBackIcon
import com.huanchengfly.tieba.core.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.utils.AccountUtil.LocalAccount
import com.huanchengfly.tieba.post.utils.StringUtil
import com.huanchengfly.tieba.post.utils.compose.calcStatusBarColor
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Composable
internal fun LeadingIcon(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalContentColor provides ExtendedTheme.colors.primary) {
        content()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NowAccountItem(
    account: AccountInfo?,
    modifier: Modifier = Modifier
) {
    val navigator = LocalNavigator.current
    if (account != null) {
        SettingsItem(
            modifier = modifier,
            onClick = { navigator.navigate(AccountManagePageDestination) },
            leadingContent = {
                LeadingIcon {
                    Avatar(
                        data = StringUtil.getAvatarUrl(account.portrait),
                        size = Sizes.Small,
                        contentDescription = null
                    )
                }
            },
            title = { Text(text = stringResource(id = R.string.title_account_manage)) },
            subtitle = {
                Text(
                    text = stringResource(
                        id = R.string.summary_now_account,
                        account.nameShow ?: account.name
                    )
                )
            }
        )
    } else {
        SettingsItem(
            modifier = modifier,
            onClick = { navigator.navigate(LoginPageDestination) },
            leadingContent = {
                LeadingIcon {
                    AvatarIcon(
                        icon = Icons.Rounded.AccountCircle,
                        size = Sizes.Small,
                        contentDescription = stringResource(id = R.string.title_new_account),
                        color = ExtendedTheme.colors.onChip,
                        backgroundColor = ExtendedTheme.colors.chip,
                    )
                }
            },
            title = { Text(text = stringResource(id = R.string.title_account_manage)) },
            subtitle = { Text(text = stringResource(id = R.string.summary_not_logged_in)) }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Destination
@Composable
fun SettingsPage(
    navigator: DestinationsNavigator,
) {
    ProvideNavigator(navigator = navigator) {
        ThemeScaffold(
            topBar = {
                val topBarColor = ExtendedTheme.colors.topBar
                val statusBarColor = topBarColor.calcStatusBarColor()
                ThemeTopAppBar(
                    backgroundColor = topBarColor,
                    statusBarColor = statusBarColor,
                    centerTitle = true,
                    title = {
                        Text(
                            text = stringResource(id = R.string.title_settings),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        DefaultBackIcon(onBack = { navigator.navigateUp()  })
                    }
                )
            },
        ) { padding ->
            PrefsScreen(
                dataStore = LocalContext.current.dataStore,
                dividerThickness = 0.dp,
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
            ) {
                prefsItem {
                    NowAccountItem(account = LocalAccount.current)
                }
                prefsItem {
                    SettingsItem(
                        onClick = { navigator.navigate(BlockSettingsPageDestination) },
                        leadingContent = {
                            LeadingIcon {
                                AvatarIcon(
                                    icon = ImageVector.vectorResource(id = R.drawable.ic_settings_block),
                                    size = Sizes.Small,
                                    contentDescription = null,
                                )
                            }
                        },
                        title = { Text(text = stringResource(id = R.string.title_block_settings)) },
                        subtitle = { Text(text = stringResource(id = R.string.summary_block_settings)) }
                    )
                }
                prefsItem {
                    SettingsItem(
                        onClick = { navigator.navigate(CustomSettingsPageDestination) },
                        leadingContent = {
                            LeadingIcon {
                                AvatarIcon(
                                    icon = ImageVector.vectorResource(id = R.drawable.ic_brush_black_24dp),
                                    size = Sizes.Small,
                                    contentDescription = null,
                                )
                            }
                        },
                        title = { Text(text = stringResource(id = R.string.title_settings_custom)) },
                        subtitle = { Text(text = stringResource(id = R.string.summary_settings_custom)) }
                    )
                }
                prefsItem {
                    SettingsItem(
                        onClick = { navigator.navigate(HabitSettingsPageDestination) },
                        leadingContent = {
                            LeadingIcon {
                                AvatarIcon(
                                    icon = ImageVector.vectorResource(id = R.drawable.ic_dashboard_customize_black_24),
                                    size = Sizes.Small,
                                    contentDescription = null,
                                )
                            }
                        },
                        title = { Text(text = stringResource(id = R.string.title_settings_read_habit)) },
                        subtitle = { Text(text = stringResource(id = R.string.summary_settings_habit)) }
                    )
                }
                prefsItem {
                    SettingsItem(
                        onClick = {
                            navigator.navigate(OKSignSettingsPageDestination)
                        },
                        leadingContent = {
                            LeadingIcon {
                                AvatarIcon(
                                    icon = ImageVector.vectorResource(id = R.drawable.ic_rocket_launch_black_24),
                                    size = Sizes.Small,
                                    contentDescription = null,
                                )
                            }
                        },
                        title = { Text(text = stringResource(id = R.string.title_oksign)) },
                        subtitle = { Text(text = stringResource(id = R.string.summary_settings_oksign)) }
                    )
                }
                prefsItem {
                    SettingsItem(
                        onClick = {
                            navigator.navigate(MoreSettingsPageDestination)
                        },
                        leadingContent = {
                            LeadingIcon {
                                AvatarIcon(
                                    icon = ImageVector.vectorResource(id = R.drawable.ic_more_horiz_black_24),
                                    size = Sizes.Small,
                                    contentDescription = null,
                                )
                            }
                        },
                        title = { Text(text = stringResource(id = R.string.title_settings_more)) },
                        subtitle = { Text(text = stringResource(id = R.string.summary_settings_more)) }
                    )
                }
            }
        }
    }
}
