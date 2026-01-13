package com.huanchengfly.tieba.core.theme2.runtime

import com.huanchengfly.tieba.core.theme2.model.ThemeChannel
import com.huanchengfly.tieba.core.theme2.model.ThemeProfile
import com.huanchengfly.tieba.core.theme2.model.ThemeSemanticColors
import com.huanchengfly.tieba.core.theme2.model.ThemeSettings
import com.huanchengfly.tieba.core.theme2.model.ThemeSnapshot

fun interface ThemeSemanticResolver {
    fun resolve(
        settings: ThemeSettings,
        channel: ThemeChannel,
        systemNight: Boolean
    ): ThemeSemanticColors
}

class ThemeResolver(
    private val semanticResolver: ThemeSemanticResolver
) {
    fun resolve(profile: ThemeProfile, systemNight: Boolean): ThemeSnapshot {
        val channel = profile.effectiveChannel(systemNight)
        val settings = profile.settingsFor(channel)
        val semantic = semanticResolver.resolve(settings, channel, systemNight)
        return ThemeSnapshot(semantic)
    }
}
