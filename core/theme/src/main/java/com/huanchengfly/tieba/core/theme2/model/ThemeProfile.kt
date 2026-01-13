package com.huanchengfly.tieba.core.theme2.model

data class ThemeProfile(
    val day: ThemeSettings,
    val night: ThemeSettings,
    val manualChannel: ThemeChannel,
    val followSystemNight: Boolean
) {
    fun effectiveChannel(systemNight: Boolean): ThemeChannel {
        return if (followSystemNight) {
            if (systemNight) ThemeChannel.NIGHT else ThemeChannel.DAY
        } else {
            manualChannel
        }
    }

    fun settingsFor(channel: ThemeChannel): ThemeSettings =
        if (channel == ThemeChannel.DAY) day else night
}
