package com.huanchengfly.tieba.core.theme2.runtime

import com.huanchengfly.tieba.core.theme2.model.ThemeChannel
import com.huanchengfly.tieba.core.theme2.model.ThemeMode
import com.huanchengfly.tieba.core.theme2.model.ThemeProfile
import com.huanchengfly.tieba.core.theme2.model.ThemeSemanticColors
import com.huanchengfly.tieba.core.theme2.model.ThemeSettings
import com.huanchengfly.tieba.core.theme2.model.ThemeSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

object DefaultThemeSemanticResolver : ThemeSemanticResolver {
    override fun resolve(
        settings: ThemeSettings,
        channel: ThemeChannel,
        systemNight: Boolean
    ): ThemeSemanticColors {
        val surfacePrimary = when (settings.mode) {
            ThemeMode.TRANSLUCENT -> 0x00000000
            else -> if (channel == ThemeChannel.NIGHT) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
        }
        return ThemeSemanticColors(surfacePrimary = surfacePrimary)
    }
}

class ThemeRuntime(
    scope: CoroutineScope,
    initialProfile: ThemeProfile = ThemeDefaults.defaultProfile,
    initialSystemNight: Boolean = false,
    semanticResolver: ThemeSemanticResolver = DefaultThemeSemanticResolver
) {
    private val resolver = ThemeResolver(semanticResolver)
    private val profileFlow = MutableStateFlow(initialProfile)
    private val systemNightFlow = MutableStateFlow(initialSystemNight)

    val currentProfile: ThemeProfile
        get() = profileFlow.value

    val profileStateFlow: StateFlow<ThemeProfile> = profileFlow
    val systemNightStateFlow: StateFlow<Boolean> = systemNightFlow

    val snapshotFlow: StateFlow<ThemeSnapshot> =
        combine(profileFlow, systemNightFlow) { profile, systemNight ->
            resolver.resolve(profile, systemNight)
        }.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = resolver.resolve(initialProfile, initialSystemNight)
        )

    fun setSystemNight(isNight: Boolean) {
        systemNightFlow.value = isNight
    }

    fun updateProfile(transform: (ThemeProfile) -> ThemeProfile) {
        profileFlow.update(transform)
    }
}
