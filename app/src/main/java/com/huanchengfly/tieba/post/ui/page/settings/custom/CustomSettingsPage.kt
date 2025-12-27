package com.huanchengfly.tieba.post.ui.page.settings.custom

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Brightness2
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FontDownload
import androidx.compose.material.icons.outlined.FormatColorFill
import androidx.compose.material.icons.outlined.Upcoming
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.AppFontSizeActivity
import com.huanchengfly.tieba.post.dataStore
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.preferences.appPreferences
import com.huanchengfly.tieba.core.ui.preferences.rememberPreferenceAsState
import com.huanchengfly.tieba.post.ui.common.prefs.PrefsScreen
import com.huanchengfly.tieba.post.ui.page.settings.LeadingIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.AvatarIcon
import com.huanchengfly.tieba.post.ui.common.DefaultBackIcon
import com.huanchengfly.tieba.core.ui.compose.SnackbarScaffold
import com.huanchengfly.tieba.core.ui.compose.rememberSnackbarState
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.SettingsItem
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.SettingsListPicker
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.SettingsSwitch
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.scenes.ThemeTopAppBar
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.utils.AppIconUtil
import com.huanchengfly.tieba.core.common.preferences.LauncherIcons
import com.huanchengfly.tieba.post.utils.compose.calcStatusBarColor
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Destination
@Composable
fun CustomSettingsPage(
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
                        text = stringResource(id = R.string.title_settings_custom),
                        fontWeight = FontWeight.Bold
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
                    leadingContent = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.FontDownload,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    onClick = { context.goToActivity<AppFontSizeActivity>() },
                    title = { Text(text = stringResource(id = R.string.title_custom_font_size)) }
                )
            }
            prefsItem {
                val iconEntries = mapOf(
                    LauncherIcons.NEW_ICON to "新图标",
                    LauncherIcons.NEW_ICON_INVERT to "新图标（反色）",
                    LauncherIcons.OLD_ICON to "旧图标",
                )
                val iconIcons: Map<String, @Composable () -> Unit> = mapOf(
                    LauncherIcons.NEW_ICON to {
                        Image(
                            painter = rememberDrawablePainter(
                                drawable = LocalContext.current.getDrawable(
                                    R.drawable.ic_launcher_new_round
                                )
                            ),
                            contentDescription = "新图标",
                            modifier = Modifier.size(40.dp)
                        )
                    },
                    LauncherIcons.NEW_ICON_INVERT to {
                        Image(
                            painter = rememberDrawablePainter(
                                drawable = LocalContext.current.getDrawable(
                                    R.drawable.ic_launcher_new_invert_round
                                )
                            ),
                            contentDescription = "新图标（反色）",
                            modifier = Modifier.size(40.dp)
                        )
                    },
                    LauncherIcons.OLD_ICON to {
                        Image(
                            painter = rememberDrawablePainter(
                                drawable = LocalContext.current.getDrawable(
                                    R.drawable.ic_launcher_round
                                )
                            ),
                            contentDescription = "旧图标",
                            modifier = Modifier.size(40.dp)
                        )
                    },
                )
                SettingsListPicker(
                    key = "app_icon",
                    title = stringResource(id = R.string.settings_app_icon),
                    defaultValue = context.appPreferences.appIcon ?: LauncherIcons.DEFAULT_ICON,
                    leadingContent = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.Apps,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    entries = iconEntries,
                    itemIcons = iconIcons,
                    useSelectedAsSummary = true,
                    summary = { selected -> selected?.let { iconEntries[it] } ?: context.getString(R.string.tip_no_little_tail) },
                    onValueChange = { AppIconUtil.applyIconSelection(context, icon = it) }
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                prefsItem {
                    val supportThemedIcons = remember {
                        persistentListOf(
                            LauncherIcons.NEW_ICON,
//                            LauncherIcons.NEW_ICON_INVERT,
                        )
                    }
                    val currentLauncherIcon by rememberPreferenceAsState(
                        key = stringPreferencesKey("app_icon"),
                        defaultValue = LauncherIcons.NEW_ICON
                    )
                    val isCurrentSupportThemedIcon by remember {
                        derivedStateOf {
                            supportThemedIcons.contains(currentLauncherIcon)
                        }
                    }
                    SettingsSwitch(
                        key = "useThemedIcon",
                        title = stringResource(id = R.string.title_settings_use_themed_icon),
                        defaultChecked = false,
                        enabled = isCurrentSupportThemedIcon,
                        leadingContent = {
                            LeadingIcon {
                                AvatarIcon(
                                    icon = Icons.Outlined.ColorLens,
                                    size = Sizes.Small,
                                    contentDescription = null,
                                )
                            }
                        },
                        onCheckedChange = { checked ->
                            AppIconUtil.applyIconSelection(context, isThemed = checked)
                        },
                        summary = {
                            if (!isCurrentSupportThemedIcon) {
                                context.getString(R.string.tip_settings_use_themed_icon_summary_not_supported)
                            } else {
                                null
                            }
                        }
                    )
                }
            }
            prefsItem {
                SettingsSwitch(
                    key = "follow_system_night",
                    title = stringResource(id = R.string.title_settings_follow_system_night),
                    defaultChecked = true,
                    leadingContent = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.BrightnessAuto,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    }
                )
            }
            prefsItem {
                SettingsSwitch(
                    key = "status_bar_darker",
                    title = stringResource(id = R.string.title_settings_status_bar_darker),
                    defaultChecked = true,
                    summary = { stringResource(id = R.string.summary_settings_status_bar_darker) },
                    leadingContent = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = ImageVector.vectorResource(id = R.drawable.ic_beaker),
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    }
                )
            }
            prefsItem {
                SettingsSwitch(
                    key = "custom_toolbar_primary_color",
                    title = stringResource(id = R.string.tip_toolbar_primary_color),
                    defaultChecked = false,
                    summary = { stringResource(id = R.string.tip_toolbar_primary_color_summary) },
                    leadingContent = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.FormatColorFill,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    }
                )
            }
            prefsItem {
                SettingsSwitch(
                    key = "listSingle",
                    title = stringResource(id = R.string.settings_forum_single),
                    defaultChecked = false,
                    leadingContent = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.ViewAgenda,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    }
                )
            }
            prefsItem {
                SettingsSwitch(
                    key = "hideExplore",
                    title = stringResource(id = R.string.title_hide_explore),
                    defaultChecked = false,
                    leadingContent = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.Explore,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    }
                )
            }
            prefsItem {
                SettingsSwitch(
                    key = "liftUpBottomBar",
                    title = stringResource(id = R.string.title_lift_up_bottom_bar),
                    defaultChecked = true,
                    summary = { stringResource(id = R.string.summary_lift_up_bottom_bar) },
                    leadingContent = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.Upcoming,
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
