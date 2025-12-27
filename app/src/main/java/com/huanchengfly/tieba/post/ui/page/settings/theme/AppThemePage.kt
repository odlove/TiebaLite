package com.huanchengfly.tieba.post.ui.page.settings.theme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.core.common.theme.ThemeChannel
import com.huanchengfly.tieba.core.common.theme.ThemeChannelConfig
import com.huanchengfly.tieba.core.common.theme.ThemeSettingsSnapshot
import com.huanchengfly.tieba.core.ui.compose.SnackbarScaffold
import com.huanchengfly.tieba.core.ui.compose.rememberSnackbarState
import com.huanchengfly.tieba.core.ui.hiltViewModel
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.LocalThemeController
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.LocalThemeState
import com.huanchengfly.tieba.post.activities.TranslucentThemeActivity
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.scenes.ThemeTopAppBar
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.ui.common.DefaultBackIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.DialogState
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberDialogState
import com.huanchengfly.tieba.post.utils.compose.calcStatusBarColor
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@Destination
@Composable
fun AppThemePage(
    navigator: DestinationsNavigator,
) {
    val themeSettingsViewModel: ThemeSettingsViewModel = hiltViewModel()
    val uiState by themeSettingsViewModel.uiState.collectAsState()
    ThemeSettingsScreen(
        uiState = uiState,
        onAction = themeSettingsViewModel::onAction,
        onBack = { navigator.navigateUp() }
    )
}

private data class EditingChannelState(
    val channel: ThemeChannel,
    val config: ThemeChannelConfig,
    val isActive: Boolean,
    val onChannelChange: (ThemeChannel) -> Unit
)

private data class CustomThemeEditingState(
    val primaryColor: Color,
    val onPrimaryColorChange: (Color) -> Unit,
    val toolbarPrimary: Boolean,
    val onToolbarPrimaryChange: (Boolean) -> Unit,
    val statusBarFontDark: Boolean,
    val onStatusBarFontDarkChange: (Boolean) -> Unit,
    val dialogState: DialogState,
    val resetToConfig: () -> Unit
)

@Composable
private fun rememberEditingChannelState(
    themeSettings: ThemeSettingsSnapshot,
    channelValues: Array<ThemeChannel>
): EditingChannelState {
    var editingChannelOrdinal by rememberSaveable {
        mutableStateOf(themeSettings.activeChannel.ordinal)
    }
    LaunchedEffect(themeSettings.activeChannel) {
        editingChannelOrdinal = themeSettings.activeChannel.ordinal
    }
    val editingChannel = channelValues.getOrNull(editingChannelOrdinal) ?: themeSettings.activeChannel
    val editingConfig = remember(themeSettings, editingChannel) {
        if (editingChannel == ThemeChannel.DAY) themeSettings.light else themeSettings.dark
    }
    val editingIsActive = editingChannel == themeSettings.activeChannel
    return EditingChannelState(
        channel = editingChannel,
        config = editingConfig,
        isActive = editingIsActive,
        onChannelChange = { channel -> editingChannelOrdinal = channel.ordinal }
    )
}

@Composable
private fun rememberCustomThemeEditingState(
    editingChannel: ThemeChannel,
    editingConfig: ThemeChannelConfig,
    palettePrimary: Int
): CustomThemeEditingState {
    var customPrimaryColor by remember(editingChannel, editingConfig.custom?.primaryColor) {
        mutableStateOf(Color(editingConfig.custom?.primaryColor ?: palettePrimary))
    }
    var customToolbarPrimaryColor by remember(editingChannel, editingConfig.toolbarPrimary) {
        mutableStateOf(editingConfig.toolbarPrimary)
    }
    var customStatusBarFontDark by remember(editingChannel, editingConfig.custom?.statusBarDark) {
        mutableStateOf(editingConfig.custom?.statusBarDark ?: false)
    }
    val customPrimaryColorDialogState = rememberDialogState()
    val resetToConfig = {
        customPrimaryColor = Color(editingConfig.custom?.primaryColor ?: palettePrimary)
        customToolbarPrimaryColor = editingConfig.toolbarPrimary
        customStatusBarFontDark = editingConfig.custom?.statusBarDark ?: false
    }
    return CustomThemeEditingState(
        primaryColor = customPrimaryColor,
        onPrimaryColorChange = { customPrimaryColor = it },
        toolbarPrimary = customToolbarPrimaryColor,
        onToolbarPrimaryChange = { customToolbarPrimaryColor = it },
        statusBarFontDark = customStatusBarFontDark,
        onStatusBarFontDarkChange = { customStatusBarFontDark = it },
        dialogState = customPrimaryColorDialogState,
        resetToConfig = resetToConfig
    )
}

@Composable
private fun ThemeSettingsScreen(
    uiState: ThemeSettingsUiState,
    onAction: (ThemeSettingsAction) -> Unit,
    onBack: () -> Unit
) {
    // 基础依赖
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val themeController = LocalThemeController.current
    val themeState = LocalThemeState.current
    val snackbarState = rememberSnackbarState()
    // 主题快照及派生状态
    val themeSettings = uiState.snapshot
    val themeValues = stringArrayResource(id = R.array.theme_values)
    val isNightChannelActive = themeSettings.activeChannel == ThemeChannel.NIGHT
    val channelValues = remember { ThemeChannel.values() }
    // 编辑态封装
    val editingChannelState = rememberEditingChannelState(themeSettings, channelValues)
    val customThemeState = rememberCustomThemeEditingState(
        editingChannel = editingChannelState.channel,
        editingConfig = editingChannelState.config,
        palettePrimary = themeState.palette.primary
    )

    CustomColorDialog(
        dialogState = customThemeState.dialogState,
        customPrimaryColor = customThemeState.primaryColor,
        onPrimaryColorChange = customThemeState.onPrimaryColorChange,
        customToolbarPrimaryColor = customThemeState.toolbarPrimary,
        onToolbarPrimaryChange = customThemeState.onToolbarPrimaryChange,
        customStatusBarFontDark = customThemeState.statusBarFontDark,
        onStatusBarFontDarkChange = customThemeState.onStatusBarFontDarkChange,
        onCancel = customThemeState.resetToConfig,
        onApply = {
            val appliedColor = customThemeState.primaryColor.toArgb()
            val toolbarPrimary = customThemeState.toolbarPrimary
            val statusBarDark = customThemeState.statusBarFontDark || !toolbarPrimary
            onAction(
                ThemeSettingsAction.ApplyCustomTheme(
                    channel = editingChannelState.channel,
                    primaryColor = appliedColor,
                    toolbarPrimary = toolbarPrimary,
                    statusBarDark = statusBarDark
                )
            )
            if (editingChannelState.isActive) {
                themeController.setUseDynamicTheme(false)
            } else {
                coroutineScope.launch {
                    val msgRes = if (editingChannelState.channel == ThemeChannel.DAY) {
                        R.string.theme_custom_saved_day
                    } else {
                        R.string.theme_custom_saved_night
                    }
                    snackbarState.showSnackbar(message = context.getString(msgRes))
                }
            }
        }
    )

    SnackbarScaffold(
        snackbarState = snackbarState,
        topBar = {
            val topBarColor = ExtendedTheme.colors.topBar
            val statusBarColor = topBarColor.calcStatusBarColor()
            ThemeTopAppBar(
                backgroundColor = topBarColor,
                statusBarColor = statusBarColor,
                centerTitle = true,
                title = { Text(text = stringResource(id = R.string.title_theme)) },
                navigationIcon = { DefaultBackIcon(onBack = onBack) }
            )
        }
    ) { paddingValues ->
        ThemeSettingsBody(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            followSystemNight = themeSettings.followSystemNight,
            isNightChannelActive = isNightChannelActive,
            onFollowSystemNightChange = { enabled ->
                onAction(ThemeSettingsAction.SetFollowSystemNight(enabled))
            },
            onManualNightToggle = {
                if (!themeSettings.followSystemNight) {
                    themeController.toggleNightMode()
                }
            },
            editingChannel = editingChannelState.channel,
            onEditingChannelChange = editingChannelState.onChannelChange,
            themeValues = themeValues,
            editingConfig = editingChannelState.config,
            customPrimaryColor = customThemeState.primaryColor,
            customPrimaryColorDialogState = customThemeState.dialogState,
            customToolbarPrimaryColor = customThemeState.toolbarPrimary,
            onToolbarPrimaryChange = customThemeState.onToolbarPrimaryChange,
            customStatusBarFontDark = customThemeState.statusBarFontDark,
            onStatusBarFontDarkChange = customThemeState.onStatusBarFontDarkChange,
            onPresetSelected = { themeKey ->
                onAction(
                    ThemeSettingsAction.SetRawTheme(
                        channel = editingChannelState.channel,
                        themeKey = themeKey,
                        useDynamicColorWanted = false
                    )
                )
                if (editingChannelState.isActive) {
                    themeController.setUseDynamicTheme(false)
                }
            },
            onManageTranslucent = { channel ->
                context.goToActivity<TranslucentThemeActivity> {
                    putExtra(TranslucentThemeActivity.EXTRA_THEME_CHANNEL, channel.name)
                }
            }
        )
    }
}
