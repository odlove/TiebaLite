package com.huanchengfly.tieba.post.ui.page.main.tabs.user.ui

import android.graphics.Typeface
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.eygraber.compose.placeholder.material.placeholder
import com.huanchengfly.tieba.core.ui.R as CoreUiR
import com.huanchengfly.tieba.core.common.utils.AvatarUtils
import com.huanchengfly.tieba.core.ui.hiltViewModel
import com.huanchengfly.tieba.core.ui.navigation.LocalHomeNavigation
import com.huanchengfly.tieba.core.ui.preferences.LocalAppPreferences
import com.huanchengfly.tieba.core.theme.compose.CardSurface
import com.huanchengfly.tieba.core.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.core.theme.compose.LocalThemeController
import com.huanchengfly.tieba.core.theme.compose.LocalThemeState
import com.huanchengfly.tieba.core.theme.compose.pullRefreshIndicator
import com.huanchengfly.tieba.core.ui.compose.widgets.Avatar
import com.huanchengfly.tieba.core.ui.compose.widgets.ConfirmDialog
import com.huanchengfly.tieba.core.ui.compose.widgets.HorizontalDivider
import com.huanchengfly.tieba.core.ui.compose.widgets.ListMenuItem
import com.huanchengfly.tieba.core.ui.compose.widgets.Sizes
import com.huanchengfly.tieba.core.ui.compose.widgets.Switch
import com.huanchengfly.tieba.core.ui.compose.widgets.VerticalDivider
import com.huanchengfly.tieba.core.ui.compose.widgets.rememberDialogState
import com.huanchengfly.tieba.core.common.account.AccountInfo
import com.huanchengfly.tieba.post.ui.page.main.tabs.user.viewmodel.UserViewModel
import com.huanchengfly.tieba.post.utils.CuidUtils

@Composable
private fun StatCardPlaceholder(modifier: Modifier = Modifier) {
    CardSurface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        backgroundColor = ExtendedTheme.colors.chip
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 16.dp)
                .placeholder(visible = true, color = ExtendedTheme.colors.chip),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatCardItem(
                statNum = 0,
                statText = stringResource(id = CoreUiR.string.text_stat_follow)
            )
            HorizontalDivider(color = ExtendedTheme.colors.divider)
            StatCardItem(
                statNum = 0,
                statText = stringResource(id = CoreUiR.string.text_stat_fans)
            )
            HorizontalDivider(color = ExtendedTheme.colors.divider)
            StatCardItem(
                statNum = 0,
                statText = stringResource(id = CoreUiR.string.title_stat_posts_num)
            )
        }
    }
}

@Composable
private fun StatCard(
    account: AccountInfo,
    modifier: Modifier = Modifier
) {
    val postNum by animateIntAsState(targetValue = account.postNum?.toIntOrNull() ?: 0)
    val fansNum by animateIntAsState(targetValue = account.fansNum?.toIntOrNull() ?: 0)
    val concernNum by animateIntAsState(targetValue = account.concernNum?.toIntOrNull() ?: 0)
    CardSurface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        backgroundColor = ExtendedTheme.colors.chip
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatCardItem(
                statNum = concernNum,
                statText = stringResource(id = CoreUiR.string.text_stat_follow)
            )
            HorizontalDivider(color = ExtendedTheme.colors.divider)
            StatCardItem(
                statNum = fansNum,
                statText = stringResource(id = CoreUiR.string.text_stat_fans)
            )
            HorizontalDivider(color = ExtendedTheme.colors.divider)
            StatCardItem(
                statNum = postNum,
                statText = stringResource(id = CoreUiR.string.title_stat_posts_num)
            )
        }
    }
}

@Composable
private fun InfoCard(
    modifier: Modifier = Modifier,
    userName: String = "",
    userIntro: String = "",
    avatar: String? = null,
    isPlaceholder: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.Bottom)
        ) {
            Text(
                text = userName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ExtendedTheme.colors.text,
                modifier = Modifier
                    .fillMaxWidth()
                    .placeholder(visible = isPlaceholder, color = ExtendedTheme.colors.chip),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = userIntro,
                fontSize = 12.sp,
                color = ExtendedTheme.colors.textSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .placeholder(visible = isPlaceholder, color = ExtendedTheme.colors.chip),
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        if (avatar != null) {
            Avatar(
                data = avatar,
                size = Sizes.Large,
                contentDescription = stringResource(id = CoreUiR.string.desc_user_avatar),
                modifier = Modifier
                    .align(Alignment.Bottom)
                    .placeholder(
                        visible = isPlaceholder,
                        color = ExtendedTheme.colors.chip
                    ),
            )
        }
    }
}

@Composable
private fun RowScope.StatCardItem(
    statNum: Int,
    statText: String
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "$statNum",
            fontSize = 20.sp,
            fontFamily = FontFamily(Typeface.createFromAsset(LocalContext.current.assets, "bebas.ttf")),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = statText,
            fontSize = 12.sp,
            color = ExtendedTheme.colors.textSecondary
        )
    }
}

@Composable
private fun LoginTipCard(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.Bottom)
        ) {
            Text(
                text = stringResource(id = CoreUiR.string.tip_login),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ExtendedTheme.colors.text,
                modifier = Modifier
                    .fillMaxWidth(),
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Icon(
            imageVector = Icons.Rounded.AccountCircle,
            contentDescription = null,
            tint = ExtendedTheme.colors.onChip,
            modifier = Modifier
                .clip(CircleShape)
                .size(Sizes.Large)
                .background(color = ExtendedTheme.colors.chip)
                .padding(16.dp),
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UserPage(
    viewModel: UserViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val homeNavigation = LocalHomeNavigation.current
    val uiState by viewModel.uiState.collectAsState()
    val isLoading = uiState.isLoading
    val account = uiState.account
    val errorMessage = uiState.errorMessage
    val themeController = LocalThemeController.current
    val themeState = LocalThemeState.current
    val appPreferences = LocalAppPreferences.current

    LaunchedEffect(viewModel) {
        viewModel.refresh()
    }

    LaunchedEffect(errorMessage) {
        if (!errorMessage.isNullOrEmpty()) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    val switchToNightDialogState = rememberDialogState()
    ConfirmDialog(
        dialogState = switchToNightDialogState,
        onConfirm = {},
        onCancel = {
            appPreferences.followSystemNight = false
            themeController.toggleNightMode()
        },
        confirmText = stringResource(id = CoreUiR.string.btn_keep_following),
        cancelText = stringResource(id = CoreUiR.string.btn_close_following)
    ) {
        Text(text = stringResource(id = CoreUiR.string.message_dialog_follow_system_night))
    }

    Scaffold(
        backgroundColor = Color.Transparent,
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxSize()
    ) { contentPaddings ->
        val pullRefreshState = rememberPullRefreshState(
            refreshing = isLoading,
            onRefresh = { viewModel.refresh() })
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPaddings)
                .pullRefresh(pullRefreshState),
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(state = rememberScrollState())
                    .fillMaxSize()
            ) {
                val currentAccount = account
                if (currentAccount != null) {
                    InfoCard(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .clickable {
                                homeNavigation.openUserProfile(currentAccount.uid.toLong())
                            }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        userName = currentAccount.nameShow ?: currentAccount.name,
                        userIntro = currentAccount.intro ?: stringResource(id = CoreUiR.string.tip_no_intro),
                        avatar = AvatarUtils.getAvatarUrl(currentAccount.portrait),
                    )
                    StatCard(
                        account = currentAccount,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                } else if (isLoading) {
                    InfoCard(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                            .padding(top = 8.dp),
                        isPlaceholder = true,
                    )
                    StatCardPlaceholder(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                } else {
                    LoginTipCard(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                            .padding(top = 8.dp),
                    )
                }
                if (account != null) {
                    ListMenuItem(
                        icon = ImageVector.vectorResource(id = CoreUiR.drawable.ic_collect),
                        text = stringResource(id = CoreUiR.string.title_my_collect),
                        onClick = { homeNavigation.openThreadCollect() }
                    )
                }
                ListMenuItem(
                    icon = ImageVector.vectorResource(id = CoreUiR.drawable.ic_outline_watch_later_24),
                    text = stringResource(id = CoreUiR.string.title_history),
                    onClick = { homeNavigation.openHistory() }
                )
                ListMenuItem(
                    icon = ImageVector.vectorResource(id = CoreUiR.drawable.ic_brush_24),
                    text = stringResource(id = CoreUiR.string.title_theme),
                    onClick = { homeNavigation.openAppTheme() }
                ) {
                    Text(
                        text = stringResource(id = CoreUiR.string.my_info_night),
                        color = ExtendedTheme.colors.textSecondary,
                        fontSize = 12.sp,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Switch(
                        checked = themeState.isNightMode,
                        onCheckedChange = {
                            if (appPreferences.followSystemNight) {
                                switchToNightDialogState.show()
                            } else {
                                themeController.toggleNightMode()
                            }
                        }
                    )
                }
                if (account != null) {
                    ListMenuItem(
                        icon = ImageVector.vectorResource(id = CoreUiR.drawable.ic_help_outline_black_24),
                        text = stringResource(id = CoreUiR.string.my_info_service_center),
                        onClick = {
                            homeNavigation.openWeb(
                                "https://tieba.baidu.com/mo/q/hybrid-main-service/uegServiceCenter?cuid=${CuidUtils.getNewCuid()}&cuid_galaxy2=${CuidUtils.getNewCuid()}&cuid_gid=&timestamp=${System.currentTimeMillis()}&_client_version=12.52.1.0&nohead=1"
                            )
                        },
                    )
                }
                VerticalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))
                ListMenuItem(
                    icon = ImageVector.vectorResource(id = CoreUiR.drawable.ic_settings_24),
                    text = stringResource(id = CoreUiR.string.my_info_settings),
                    onClick = { homeNavigation.openSettings() },
                )
                ListMenuItem(
                    icon = ImageVector.vectorResource(id = CoreUiR.drawable.ic_info_black_24),
                    text = stringResource(id = CoreUiR.string.my_info_about),
                    onClick = { homeNavigation.openAbout() },
                )
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
