package com.huanchengfly.tieba.post.ui.page.settings.more

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.OfflineBolt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.feature.settings.R
import com.huanchengfly.tieba.post.dataStore
import com.huanchengfly.tieba.post.ui.page.settings.destinations.AboutPageDestination
import com.huanchengfly.tieba.post.ui.page.settings.LeadingIcon
import com.huanchengfly.tieba.core.ui.compose.widgets.AvatarIcon
import com.huanchengfly.tieba.post.ui.common.DefaultBackIcon
import com.huanchengfly.tieba.core.ui.compose.base.SnackbarScaffold
import com.huanchengfly.tieba.core.ui.compose.base.rememberSnackbarState
import com.huanchengfly.tieba.core.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.compose.settings.SettingsItem
import com.huanchengfly.tieba.core.ui.compose.settings.SettingsSwitch
import com.huanchengfly.tieba.core.theme.compose.scenes.ThemeTopAppBar
import com.huanchengfly.tieba.post.ui.common.prefs.PrefsScreen
import com.huanchengfly.tieba.post.ui.common.prefs.dependNot
import com.huanchengfly.tieba.core.ui.compose.widgets.Sizes
import com.huanchengfly.tieba.post.utils.ImageCacheUtil
import com.huanchengfly.tieba.post.preferences.appPreferences
import com.huanchengfly.tieba.post.utils.compose.calcStatusBarColor
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

@OptIn(ExperimentalMaterialApi::class)
@Destination
@Composable
fun MoreSettingsPage(
    navigator: DestinationsNavigator,
) {
    val coroutineScope = rememberCoroutineScope()
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
                        text = stringResource(id = R.string.title_settings_more),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    DefaultBackIcon(onBack = { navigator.navigateUp()  })
                }
            )
        },
    ) { paddingValues ->
        val context = LocalContext.current
        val versionName = remember(context) {
            runCatching {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            }.getOrNull().orEmpty()
        }
        var cacheSize by remember { mutableStateOf("0.0B") }
        LaunchedEffect(Unit) {
            thread {
                cacheSize = ImageCacheUtil.getCacheSize(context)
            }
        }
        PrefsScreen(
            dataStore = LocalContext.current.dataStore,
            dividerThickness = 0.dp,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            if (context.appPreferences.showExperimentalFeatures) {
                prefsItem {
                    SettingsSwitch(
                        leadingContent = {
                            LeadingIcon {
                                AvatarIcon(
                                    icon = Icons.Outlined.BugReport,
                                    size = Sizes.Small,
                                    contentDescription = null,
                                )
                            }
                        },
                        key = "checkCIUpdate",
                        title = stringResource(id = R.string.title_check_ci_update),
                        defaultChecked = false,
                        summary = { stringResource(id = R.string.tip_check_ci_update) }
                    )
                }
            }
            prefsItem {
                SettingsSwitch(
                    leadingContent = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = ImageVector.vectorResource(id = R.drawable.ic_chrome),
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    key = "use_webview",
                    title = stringResource(id = R.string.title_use_webview),
                    defaultChecked = true,
                    summary = { isOn ->
                        if (isOn) stringResource(id = R.string.tip_use_webview_on)
                        else stringResource(id = R.string.tip_use_webview)
                    },
                )
            }
            prefsItem {
                SettingsSwitch(
                    leadingContent = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = ImageVector.vectorResource(id = R.drawable.ic_today),
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    enabled = dependNot(key = "use_webview"),
                    key = "use_custom_tabs",
                    title = stringResource(id = R.string.title_use_custom_tabs),
                    defaultChecked = true,
                    summary = { isOn ->
                        if (isOn) stringResource(id = R.string.tip_use_custom_tab_on)
                        else stringResource(id = R.string.tip_use_custom_tab)
                    },
                )
            }
            prefsItem {
                SettingsItem(
                    leadingContent = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.OfflineBolt,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    onClick = {
                        coroutineScope.launch {
                            ImageCacheUtil.clearImageAllCache(context)
                            cacheSize = "0.0B"
                            snackbarState.showSnackbar(context.getString(R.string.toast_clear_picture_cache_success))
                        }
                    },
                    title = { Text(text = stringResource(id = R.string.title_clear_picture_cache)) },
                    subtitle = { Text(text = stringResource(id = R.string.tip_cache, cacheSize)) }
                )
            }
            prefsItem {
                SettingsItem(
                    leadingContent = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = ImageVector.vectorResource(id = R.drawable.ic_link),
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS, Uri.parse("package:${context.packageName}"))
                                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                        )
                    },
                    title = { Text(text = stringResource(id = R.string.title_open_by_default)) },
                    subtitle = { Text(text = stringResource(id = R.string.tip_open_by_default)) }
                )
            }
            prefsItem {
                SettingsItem(
                    leadingContent = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.Info,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    onClick = {
                        navigator.navigate(AboutPageDestination)
                    },
                    title = { Text(text = stringResource(id = R.string.title_about)) },
                    subtitle = { Text(text = stringResource(id = R.string.tip_about, versionName)) }
                )
            }
        }
    }
}
