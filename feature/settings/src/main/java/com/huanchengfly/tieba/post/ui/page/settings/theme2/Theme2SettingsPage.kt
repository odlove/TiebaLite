package com.huanchengfly.tieba.post.ui.page.settings.theme2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.core.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.compose.settings.SettingsItem
import com.huanchengfly.tieba.core.ui.compose.settings.SettingsSectionContainer
import com.huanchengfly.tieba.core.ui.compose.settings.ThemeScaffold
import com.huanchengfly.tieba.core.ui.compose.widgets.Switch
import com.huanchengfly.tieba.core.ui.compose.widgets.ThemeTopAppBar
import com.huanchengfly.tieba.core.theme2.model.ThemeChannel
import com.huanchengfly.tieba.feature.settings.R
import com.huanchengfly.tieba.post.ui.common.DefaultBackIcon
import com.huanchengfly.tieba.post.utils.compose.calcStatusBarColor
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.huanchengfly.tieba.core.ui.hiltViewModel

@Destination
@Composable
fun Theme2SettingsPage(
    navigator: DestinationsNavigator
) {
    val viewModel: Theme2SettingsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
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
                        text = stringResource(id = R.string.title_theme2_settings),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.h6
                    )
                },
                navigationIcon = {
                    DefaultBackIcon(onBack = { navigator.navigateUp() })
                }
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionContainer {
                SettingsItem(
                    title = { Text(text = stringResource(id = R.string.title_theme2_follow_system)) },
                    trailingContent = {
                        Switch(
                            checked = uiState.followSystemNight,
                            onCheckedChange = { viewModel.setFollowSystemNight(it) }
                        )
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                SettingsItem(
                    enabled = !uiState.followSystemNight,
                    title = { Text(text = stringResource(id = R.string.title_theme2_manual_channel)) },
                    trailingContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ChannelButton(
                                text = stringResource(id = R.string.label_theme2_channel_day),
                                selected = uiState.manualChannel == ThemeChannel.DAY,
                                enabled = !uiState.followSystemNight,
                                onClick = { viewModel.setManualChannel(ThemeChannel.DAY) }
                            )
                            ChannelButton(
                                text = stringResource(id = R.string.label_theme2_channel_night),
                                selected = uiState.manualChannel == ThemeChannel.NIGHT,
                                enabled = !uiState.followSystemNight,
                                onClick = { viewModel.setManualChannel(ThemeChannel.NIGHT) }
                            )
                        }
                    }
                )
            }
            SettingsSectionContainer {
                SettingsItem(
                    title = { Text(text = stringResource(id = R.string.title_theme2_surface_primary)) },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(uiState.surfacePrimary))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = String.format("#%08X", uiState.surfacePrimary))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ChannelButton(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) ExtendedTheme.colors.primary else ExtendedTheme.colors.textSecondary
    TextButton(
        enabled = enabled,
        onClick = onClick
    ) {
        Text(text = text, color = color)
    }
}
