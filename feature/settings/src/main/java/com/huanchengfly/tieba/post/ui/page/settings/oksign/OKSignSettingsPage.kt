package com.huanchengfly.tieba.post.ui.page.settings.oksign

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material.icons.outlined.BrowseGallery
import androidx.compose.material.icons.outlined.OfflinePin
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.WatchLater
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.feature.settings.R
import com.huanchengfly.tieba.post.dataStore
import com.huanchengfly.tieba.core.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.compose.settings.SettingsItem
import com.huanchengfly.tieba.core.ui.compose.settings.SettingsSwitch
import com.huanchengfly.tieba.core.ui.compose.settings.SettingsTimePicker
import com.huanchengfly.tieba.post.ui.common.prefs.PrefsScreen
import com.huanchengfly.tieba.post.ui.common.prefs.depend
import com.huanchengfly.tieba.post.ui.page.settings.LeadingIcon
import com.huanchengfly.tieba.core.ui.compose.widgets.AvatarIcon
import com.huanchengfly.tieba.post.ui.common.DefaultBackIcon
import com.huanchengfly.tieba.core.ui.compose.base.SnackbarScaffold
import com.huanchengfly.tieba.core.ui.compose.base.rememberSnackbarState
import com.huanchengfly.tieba.core.ui.compose.widgets.Sizes
import com.huanchengfly.tieba.core.ui.compose.widgets.ThemeTopAppBar
import com.huanchengfly.tieba.post.utils.isIgnoringBatteryOptimizations
import com.huanchengfly.tieba.post.utils.powerManager
import com.huanchengfly.tieba.post.utils.requestIgnoreBatteryOptimizations
import com.huanchengfly.tieba.post.utils.compose.calcStatusBarColor
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Destination
@Composable
fun OKSignSettingsPage(
    navigator: DestinationsNavigator,
) {
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
                    Text(text = stringResource(id = R.string.title_oksign), fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    DefaultBackIcon(onBack = { navigator.navigateUp()  })
                }
            )
        },
    ) { paddingValues ->
        val context = LocalContext.current
        val dataStore = context.dataStore
        PrefsScreen(
            dataStore = dataStore,
            dividerThickness = 0.dp,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            prefsItem {
                SettingsSwitch(
                    leadingContent = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.BrowseGallery,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    key = "oksign_slow_mode",
                    title = stringResource(id = R.string.title_oksign_slow_mode),
                    defaultChecked = true,
                    summary = { isOn ->
                        if (isOn) {
                            stringResource(id = R.string.summary_oksign_slow_mode_on)
                        } else {
                            stringResource(id = R.string.summary_oksign_slow_mode)
                        }
                    },
                )
            }
            prefsItem {
                SettingsSwitch(
                    leadingContent = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.Speed,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    key = "oksign_use_official_oksign",
                    title = stringResource(id = R.string.title_oksign_use_official_oksign),
                    defaultChecked = true,
                    summary = { stringResource(id = R.string.summary_oksign_use_official_oksign) },
                )
            }
            prefsItem {
                SettingsSwitch(
                    leadingContent = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.OfflinePin,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    key = "auto_sign",
                    title = stringResource(id = R.string.title_auto_sign),
                    defaultChecked = false,
                    summary = { isOn ->
                        if (isOn) {
                            stringResource(id = R.string.summary_auto_sign_on)
                        } else {
                            stringResource(id = R.string.summary_auto_sign)
                        }
                    },
                )
            }
            prefsItem {
                SettingsTimePicker(
                    key = "auto_sign_time",
                    title = stringResource(id = R.string.title_auto_sign_time),
                    defaultValue = "09:00",
                    summary = { time -> context.getString(R.string.summary_auto_sign_time, time.format()) },
                    enabled = depend(key = "auto_sign"),
                    leadingContent = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.WatchLater,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    }
                )
            }
            prefsItem {
                SettingsItem(
                    leadingContent = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.BatteryAlert,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!context.powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
                                context.requestIgnoreBatteryOptimizations()
                            } else {
                                snackbarState.showSnackbar(context.getString(R.string.toast_ignore_battery_optimization_already))
                            }
                        }
                    },
                    title = { Text(text = stringResource(id = R.string.title_ignore_battery_optimization)) },
                    subtitle = {
                        Text(
                            text = when {
                                Build.VERSION.SDK_INT < Build.VERSION_CODES.M ->
                                    stringResource(id = R.string.summary_battery_optimization_old_android_version)

                                context.isIgnoringBatteryOptimizations() ->
                                    stringResource(id = R.string.summary_battery_optimization_ignored)

                                else ->
                                    stringResource(id = R.string.summary_ignore_battery_optimization)
                            }
                        )
                    },
                    enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !context.isIgnoringBatteryOptimizations()
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
                        append(stringResource(id = R.string.tip_auto_sign))
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
        }
    }
}
