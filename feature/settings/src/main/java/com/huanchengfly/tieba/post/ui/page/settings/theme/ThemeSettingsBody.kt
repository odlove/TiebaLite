package com.huanchengfly.tieba.post.ui.page.settings.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.core.common.theme.ThemeChannel
import com.huanchengfly.tieba.core.common.theme.ThemeChannelConfig
import com.huanchengfly.tieba.core.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.feature.settings.R
import com.huanchengfly.tieba.core.ui.compose.widgets.DialogState
import com.huanchengfly.tieba.post.ui.common.prefs.widgets.TextPref
import com.huanchengfly.tieba.core.ui.compose.widgets.Switch as AppSwitch
import com.huanchengfly.tieba.core.ui.compose.widgets.SwitchDefaults as AppSwitchDefaults

@Composable
internal fun ThemeSettingsBody(
    modifier: Modifier = Modifier,
    followSystemNight: Boolean,
    isNightChannelActive: Boolean,
    onFollowSystemNightChange: (Boolean) -> Unit,
    onManualNightToggle: () -> Unit,
    editingChannel: ThemeChannel,
    onEditingChannelChange: (ThemeChannel) -> Unit,
    themeValues: Array<String>,
    editingConfig: ThemeChannelConfig,
    customPrimaryColor: Color,
    customPrimaryColorDialogState: DialogState,
    customToolbarPrimaryColor: Boolean,
    onToolbarPrimaryChange: (Boolean) -> Unit,
    customStatusBarFontDark: Boolean,
    onStatusBarFontDarkChange: (Boolean) -> Unit,
    onPresetSelected: (String) -> Unit,
    onManageTranslucent: (ThemeChannel) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FollowSystemNightRow(
            checked = followSystemNight,
            isNight = isNightChannelActive,
            onCheckedChange = onFollowSystemNightChange,
            onManualToggle = onManualNightToggle
        )
        ChannelFramedContainer(
            selected = editingChannel,
            onSelect = onEditingChannelChange
        ) {
            ThemePresetSection(
                themeValues = themeValues,
                editingChannel = editingChannel,
                editingConfig = editingConfig,
                onThemeSelected = onPresetSelected
            )
            CustomColorCard(
                isDynamicEnabled = editingConfig.useDynamicColorWanted,
                customPrimaryColor = customPrimaryColor,
                customPrimaryColorDialogState = customPrimaryColorDialogState,
                customToolbarPrimaryColor = customToolbarPrimaryColor,
                onToolbarPrimaryChange = onToolbarPrimaryChange,
                customStatusBarFontDark = customStatusBarFontDark,
                onStatusBarFontDarkChange = onStatusBarFontDarkChange
            )
            TransparentThemeCard(
                editingChannel = editingChannel,
                backgroundPath = editingConfig.translucent?.backgroundPath,
                blur = editingConfig.translucent?.blur ?: 0,
                alpha = editingConfig.translucent?.alpha ?: 255,
                onManage = onManageTranslucent
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun FollowSystemNightRow(
    checked: Boolean,
    isNight: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onManualToggle: () -> Unit
) {
    val switchColors = AppSwitchDefaults.colors(
        checkedThumbColor = ExtendedTheme.colors.onPrimary,
        checkedTrackColor = ExtendedTheme.colors.primary,
        uncheckedThumbColor = ExtendedTheme.colors.onChip,
        uncheckedTrackColor = ExtendedTheme.colors.chip
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextPref(
            title = stringResource(id = R.string.title_settings_follow_system_night),
            summary = stringResource(
                id = if (isNight) R.string.theme_summary_follow_system_night else R.string.theme_summary_follow_system_day
            ),
            onClick = { onCheckedChange(!checked) }
        ) {
            AppSwitch(checked = checked, onCheckedChange = onCheckedChange, colors = switchColors)
        }
        TextPref(
            title = stringResource(id = R.string.theme_night_mode),
            summary = null,
            enabled = !checked,
            onClick = { if (!checked) onManualToggle() }
        ) {
            AppSwitch(
                checked = isNight,
                enabled = !checked,
                onCheckedChange = { if (!checked) onManualToggle() },
                colors = switchColors
            )
        }
    }
}
