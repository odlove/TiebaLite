package com.huanchengfly.tieba.core.common.preferences

import kotlinx.collections.immutable.persistentListOf

object LauncherIcons {
    const val NEW_ICON = "com.huanchengfly.tieba.post.MainActivityV2"
    const val NEW_ICON_THEMED = "com.huanchengfly.tieba.post.MainActivityIconThemed"
    const val NEW_ICON_INVERT = "com.huanchengfly.tieba.post.MainActivityIconInvert"
    const val OLD_ICON = "com.huanchengfly.tieba.post.MainActivityIconOld"

    const val DEFAULT_ICON = NEW_ICON

    val ICONS = persistentListOf(NEW_ICON, NEW_ICON_THEMED, NEW_ICON_INVERT, OLD_ICON)

    val SUPPORT_THEMED_ICON = persistentListOf(NEW_ICON)
    val THEMED_ICON_MAPPING = mapOf(
        NEW_ICON to NEW_ICON_THEMED,
    )

    const val OLD_LAUNCHER_ICON = "com.huanchengfly.tieba.post.activities.MainActivity"
}
