package com.huanchengfly.tieba.post.ui.page.settings.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.SupervisedUserCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.feature.settings.R
import com.huanchengfly.tieba.post.dataStore
import com.huanchengfly.tieba.post.ui.common.prefs.PrefsScreen
import com.huanchengfly.tieba.core.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.compose.settings.SettingsItem
import com.huanchengfly.tieba.core.ui.compose.settings.SettingsListPicker
import com.huanchengfly.tieba.core.ui.compose.settings.SettingsTextField
import com.huanchengfly.tieba.core.ui.compose.settings.ThemeScaffold
import com.huanchengfly.tieba.core.theme.compose.scenes.ThemeTopAppBar
import com.huanchengfly.tieba.core.ui.navigation.LocalHomeNavigation
import com.huanchengfly.tieba.post.ui.page.settings.LeadingIcon
import com.huanchengfly.tieba.core.ui.compose.widgets.AvatarIcon
import com.huanchengfly.tieba.post.ui.common.DefaultBackIcon
import com.huanchengfly.tieba.core.ui.compose.widgets.Sizes
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.AccountUtil.AllAccounts
import com.huanchengfly.tieba.post.utils.AccountUtil.LocalAccount
import com.huanchengfly.tieba.post.preferences.appPreferences
import com.huanchengfly.tieba.post.utils.compose.calcStatusBarColor
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Destination
@Composable
fun AccountManagePage(
    navigator: DestinationsNavigator,
) {
    ThemeScaffold(
        containerColor = ExtendedTheme.colors.background,
        topBar = {
            val topBarColor = ExtendedTheme.colors.topBar
            val statusBarColor = topBarColor.calcStatusBarColor()
            ThemeTopAppBar(
                backgroundColor = topBarColor,
                statusBarColor = statusBarColor,
                centerTitle = true,
                title = {
                    Text(
                        text = stringResource(id = R.string.title_account_manage),
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.h6
                    )
                },
                navigationIcon = {
                    DefaultBackIcon(onBack = { navigator.navigateUp()  })
                }
            )
        },
    ) { paddingValues ->
        val account = LocalAccount.current
        val context = LocalContext.current
        val homeNavigation = runCatching { LocalHomeNavigation.current }.getOrNull()
        val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
        val littleTailSummary = remember { context.appPreferences.littleTail }
        PrefsScreen(
            dataStore = LocalContext.current.dataStore,
            dividerThickness = 0.dp,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            prefsItem {
                if (account != null) {
                    val accountsMap = AllAccounts.current.associate {
                        it.id.toString() to (it.nameShow ?: it.name)
                    }
                    SettingsListPicker(
                        key = "switch_account",
                        title = stringResource(id = R.string.title_switch_account),
                        defaultValue = account.id.toString(),
                        enabled = true,
                        leadingContent = {
                            LeadingIcon {
                                AvatarIcon(
                                    icon = Icons.Outlined.AccountCircle,
                                    size = Sizes.Small,
                                    contentDescription = null,
                                )
                            }
                        },
                        summary = { selectedKey ->
                            val name = accountsMap[selectedKey]
                                ?: account.nameShow ?: account.name
                            stringResource(
                                id = R.string.summary_now_account,
                                name
                            )
                        },
                        entries = accountsMap,
                        onValueChange = { selected ->
                            coroutineScope.launch {
                                AccountUtil.switchAccount(context, selected.toInt())
                            }
                        }
                    )
                } else {
                    SettingsItem(
                        leadingContent = {
                            LeadingIcon {
                                AvatarIcon(
                                    icon = Icons.Outlined.AccountCircle,
                                    size = Sizes.Small,
                                    contentDescription = null,
                                )
                            }
                        },
                        title = { Text(text = stringResource(id = R.string.title_switch_account)) },
                        subtitle = { Text(text = stringResource(id = R.string.summary_not_logged_in)) },
                        enabled = false
                    )
                }
            }
            prefsItem {
                SettingsItem(
                    leadingContent = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.AddCircleOutline,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    onClick = { homeNavigation?.openLogin() },
                    title = { Text(text = stringResource(id = R.string.title_new_account)) }
                )
            }
            prefsItem {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(stringResource(id = R.string.tip_start))
                        }
                        append(stringResource(id = R.string.tip_account_error))
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .padding(start = 8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(color = ExtendedTheme.colors.chip)
                        .padding(12.dp),
                    color = ExtendedTheme.colors.onChip,
                    fontSize = 12.sp
                )
            }
            prefsItem {
                SettingsItem(
                    leadingContent = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = ImageVector.vectorResource(id = R.drawable.ic_outlined_close_circle_24),
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    onClick = {
                        coroutineScope.launch {
                            AccountUtil.exit(context)
                        }
                    },
                    title = { Text(text = stringResource(id = R.string.title_exit_account)) }
                )
            }
            prefsItem {
                SettingsItem(
                    leadingContent = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.SupervisedUserCircle,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    onClick = {
                        homeNavigation?.openWeb(
                            "https://wappass.baidu.com/static/manage-chunk/change-username.html#/showUsername"
                        )
                    },
                    enabled = account != null,
                    title = { Text(text = stringResource(id = R.string.title_modify_username)) }
                )
            }
            prefsItem {
                SettingsItem(
                    leadingContent = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.ContentCopy,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    onClick = { homeNavigation?.copyText(account?.bduss.orEmpty(), isSensitive = true) },
                    enabled = account != null,
                    title = { Text(text = stringResource(id = R.string.title_copy_bduss)) },
                    subtitle = { Text(text = stringResource(id = R.string.summary_copy_bduss)) }
                )
            }
            prefsItem {
                SettingsTextField(
                    key = "little_tail",
                    title = stringResource(id = R.string.title_my_tail),
                    defaultValue = littleTailSummary ?: "",
                    dialogTitle = stringResource(id = R.string.title_dialog_modify_little_tail),
                    summary = { value ->
                        if (value.isEmpty()) {
                            context.getString(R.string.tip_no_little_tail)
                        } else {
                            value
                        }
                    },
                    leadingContent = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.Edit,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    onValueSaved = { context.appPreferences.littleTail = it }
                )
            }
        }
    }
}
