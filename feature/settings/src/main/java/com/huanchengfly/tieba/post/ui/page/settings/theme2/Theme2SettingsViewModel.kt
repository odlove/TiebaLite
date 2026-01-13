package com.huanchengfly.tieba.post.ui.page.settings.theme2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huanchengfly.tieba.core.theme2.model.ThemeChannel
import com.huanchengfly.tieba.core.theme2.runtime.ThemeRuntime
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class Theme2SettingsUiState(
    val followSystemNight: Boolean,
    val manualChannel: ThemeChannel,
    val surfacePrimary: Int
)

@HiltViewModel
class Theme2SettingsViewModel @Inject constructor(
    private val runtime: ThemeRuntime
) : ViewModel() {
    val uiState: StateFlow<Theme2SettingsUiState> =
        combine(runtime.profileStateFlow, runtime.snapshotFlow) { profile, snapshot ->
            Theme2SettingsUiState(
                followSystemNight = profile.followSystemNight,
                manualChannel = profile.manualChannel,
                surfacePrimary = snapshot.semantic.surfacePrimary
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Theme2SettingsUiState(
                followSystemNight = runtime.currentProfile.followSystemNight,
                manualChannel = runtime.currentProfile.manualChannel,
                surfacePrimary = runtime.snapshotFlow.value.semantic.surfacePrimary
            )
        )

    fun setFollowSystemNight(enable: Boolean) {
        runtime.updateProfile { it.copy(followSystemNight = enable) }
    }

    fun setManualChannel(channel: ThemeChannel) {
        runtime.updateProfile { it.copy(manualChannel = channel) }
    }
}
